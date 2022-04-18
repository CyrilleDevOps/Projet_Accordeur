//package net.codejava.analyse;

//import net.codejava.actions.*;
//import net.codejava.start.Constantes;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;

public class CaptureSon implements Runnable{


	//Début Déclaration

    private int captureFrequency;
    private int sampleSize; //taille maximale des données récupérables par le micro dans l'ordinateur
    private int dataSize; //nombre de sampleSize présents dans le buffer
    private int zeroPaddedDataSize;
    private TargetDataLine line;
    private ByteArrayOutputStream out;
    private double[][] lastFFTs;
    private int lastFFTindex;
    private int averagingFactor;
    private Mixer.Info mixerInfo;
    private Son waitingMaxNote;
    private int freqStabilization;
    private double[] FenetreGaussienne;
    private EcouteMusique AlEcouteDuSon;
    private boolean isRec;
    private byte[] EchantillonSignalData;
    private double[] signalAanalyser;
    private double max; // Amplitude maximale de la FFT
    private double[] fft;
    private Son NoteFondamentale = null;
    private Son[] peaks;
	    
    public static double freqMin=60; //Seuil en fréquence de détection
    public static double snr=1.41; //Rapport signal sur bruit
	public static int nNotesWaited = 5; //Nombre de notes consécutives entendues avant que la note soit considérée comme stable

    /**
     * Crée une capture de Son avec ses paramètres
     * @param cf Fréquence d'échantillonnage
     * @param s Taille du sample en bits
     * @param b Taille du buffer
     * @param mi Mixer à utiliser
     */
    
	public CaptureSon(int cf, int s, int b, Mixer.Info mi) {
        init(cf, s, b, mi);
    }
	    
    /**
     * Crée une CaptureSon avec ses paramètres et une EcouteMusique
     * @param cf Fréquence d'échantillonnage
     * @param s Taille du sample en bits
     * @param b Taille du buffer
     * @param mi Mixer à utiliser
     * @param fl Interface EcouteMusique
     */
    public CaptureSon(int cf, int s, int b, Mixer.Info mi, EcouteMusique fl) {
    	init(cf, s, b, mi);
        addFrequencyListener(fl);
    }
	
    private void init(int cf, int s, int b, Mixer.Info mi) {

    	if (Constantes.DEBUG==1) {System.out.println("1.1 Initialize les données pour la capture");
    	}
    	// Copie des attributs
	    captureFrequency=cf;
	    sampleSize=s;
	    mixerInfo=mi;
	    
	    // Moyennage de la FFT paramétrable
	    averagingFactor = 8;
	    
	    // Taille du buffer (signal d'entrée) et taille du tableau avec "Zero Padding" pour pouvoir calculer la FFT
	    dataSize=b; 
	    zeroPaddedDataSize=(int)Math.pow(2,Math.ceil(Math.log(dataSize)/Math.log(2))); //puissance de 2 supérieure la plus proche
	    
	    // Initialisation des tableaux du signal d'entrée (EchantillonSignalData), de la FFT, et de l'historique de FFT pour moyennage
	    EchantillonSignalData = new byte[dataSize];
	    fft=new double[dataSize/2];
	    lastFFTs= new double[averagingFactor][zeroPaddedDataSize];
	    
	    // Création de la fenêtre Gaussienne
	    FenetreGaussienne = new double[dataSize];
	    buildGaussianWindow(FenetreGaussienne);
	    
	    isRec=false;
    }
	
    private void beginAudioCapture() {
        isRec=true;
    	if (Constantes.DEBUG==1) {System.out.println("2.2 Init CaptureSon");
    	}
        // On définit un format audio
        AudioFormat format=new AudioFormat(captureFrequency,sampleSize,1,true,true);
        
        // On récupère les informations d'une ligne compatible avec le format
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        
        // Si la ligne n'est pas supportée, on renvoie une erreur
        if (!AudioSystem.isLineSupported(info)) {
        	System.out.println("System is not supported...");
        	}
        
        // Obtain and open the line.
        try {
            // On récupère la ligne du mixer passé en paramètre
            line =(TargetDataLine) AudioSystem.getTargetDataLine(format,mixerInfo);
            
            // On l'ouvre avec une taille de buffer
            line.open(format,dataSize);
        } catch (LineUnavailableException ex) {
            line=null;
            System.err.println("LineUnavailable! Err:"+ex);
        }

        out  = new ByteArrayOutputStream();
        if(line!=null) {
            // Begin audio capture.
            line.start();
        }
    }
    private void getDataFromCapture() {
        // Read the next chunk of EchantillonSignalData from the TargetDataLine.
    	if (Constantes.DEBUG==1) {System.out.println("2.3-capturing ->EchantillonSignalData..");
    	}
        if(line!=null) {
            int numBytesRead = line.read(EchantillonSignalData, 0, dataSize);
            // Save this chunk of EchantillonSignalData.
            out.write(EchantillonSignalData, 0, numBytesRead);
        } else {
            System.err.println("no line!");
        }
    }
	    

    /**
     * Calcule la FFT 
     */
    private void doFFT(double[] Signal) {
        //Complex[]complexData =toComplex(Signal,zeroPaddedDataSize,dataSize);
    	// Transforme un tableau de doubles en tableau de complexes correspondant avec des parties imaginaires nulles
        Complex[] complexData=new Complex[zeroPaddedDataSize];
        for(int i=0;i<complexData.length;i++) {
            if(i<dataSize)
                complexData[i]=new Complex(Signal[i],0);
            else 
                complexData[i]=new Complex(0,0);
        }
    
        Complex[] rawfft=FFT.fft(complexData);
    	if (Constantes.DEBUG==1) {System.out.println("2.5-Calcul fft");
    	}
        
        // On ne fait que la moitié car l'autre moitié n'est pas interprétable (~ Théorème de Shannon)
        for(int i=0;i<fft.length/2;i++) {
            fft[i]=rawfft[i].abs()/zeroPaddedDataSize;
        }
		
    }
	    
    /**
     * Parcourt le tableau des pics et met à jour l'amplitude max (max), 
     * la fréquence correspondante (maxFreq) et l'indice du tableau de la FFT correspondant
     */
    private void searchMaxInPeaks() {
    	Son newMaxNote=new Son(0);
        Son updatedOldNote=null;
        
        for(int i=0;i<peaks.length/2;i++) {
            if(peaks[i]!= null && peaks[i].getIntensity()>newMaxNote.getIntensity()) {
                newMaxNote=peaks[i];
                
                if(NoteFondamentale != null && NoteFondamentale.equals(peaks[i]))
                    updatedOldNote=peaks[i];
            }
        }      
        
        if((NoteFondamentale !=null && !NoteFondamentale.equals(newMaxNote)) || NoteFondamentale==null) {
            waitingMaxNote=newMaxNote;
            freqStabilization=0;
        }
        
       
        if(waitingMaxNote != null && newMaxNote != null && waitingMaxNote.equals(newMaxNote)) {
            if(freqStabilization==nNotesWaited || updatedOldNote == null) {
                if(NoteFondamentale!=null && !NoteFondamentale.equals(newMaxNote))
                    fireNoteChanged();
                NoteFondamentale=newMaxNote;
            } else {
            	newMaxNote=updatedOldNote;
                freqStabilization++;
            }
            
        }
    	if (Constantes.DEBUG==1) {System.out.println("2.10-Note Max ou fondamental.."+NoteFondamentale.GetNomNote());
        }
    }
    /**
     * la méthode permettant de séparer des notes utilisée pour pouvoir ensuite afficher les notes dans le scorePanel
     **/
    private void fireNoteChanged() {
    	if (Constantes.DEBUG==1) {System.out.println("2.11-Affiche Note..");
    	}
        if(AlEcouteDuSon!=null) {
        	AlEcouteDuSon.onNewNote(NoteFondamentale);
        }
    }
  
    
    /**
     * Recherche le maximum de la FFT
    */
    private void searchMax() {
    	if (Constantes.DEBUG==1) {System.out.println("2.7 searchMax");
    	}
    	double newMax=0;
        for(int i=0;i<fft.length/2;i++) {
            if(fft[i]>newMax) {
                newMax=fft[i];
            }
        }
        max=newMax;
    }

    /**
     * Ajoute la FFT actuelle à l'historique et fait la moyenne temporelle des FFT
     */
    private void averageFFT() {
    	if (Constantes.DEBUG==1) {System.out.println("2.6 averageFFT");
    	}
    	lastFFTs[lastFFTindex]=fft;
        double[] averageFFT=new double[lastFFTs[0].length];
        for(int i=0;i<lastFFTs[0].length;i++) {
            for (double[] lastFFT : lastFFTs) {
                averageFFT[i] += lastFFT[i];
            }
            averageFFT[i]=averageFFT[i]/lastFFTs.length;
        }
        fft=averageFFT;
        if(++lastFFTindex>=averagingFactor) lastFFTindex=0;
    }
	    
    /**
     * Liste les pics dont la fréquence est supérieure au seuil de fréquence dans le tableau peaks
     * @param fft tableau de la FFT
     * @param parabolic Si vrai, interpolation parabolique, sinon renvoie l'indice du pic
     * @param threshold Valeur de seuil pour qu'un pic soit pris en compte
     */
    private void peaksIndexation(double[] fft, boolean parabolic, double threshold) {
    	if (Constantes.DEBUG==1) {System.out.println("2.8 peaksIndexation");
    	}
    	peaks=new Son[fft.length];
        for(int i=1;i<fft.length-1;i++) {
            if(fft[i]>threshold) {
                if(fft[i-1]<fft[i] && fft[i+1]<fft[i]) {
                    if(parabolic) {
                        
                        /* Interpolation parabolique
                         *  /!\ avec fenêtrage gaussien /!\ */

                        double alpha=Math.log(fft[i-1]);
                        double beta=Math.log(fft[i]);
                        double gamma=Math.log(fft[i+1]);
                        double p = 0.5*(alpha-gamma)/(alpha-2*beta+gamma);
                        
                        double freq=indexToFreq(i+p);
                        
                        // On n'ajoute aux peaks que si la fréquence détectée est supérieure au seuil
                        if(freq > freqMin)
                            peaks[i]=new Son(freq,Math.exp(beta-0.25*(alpha-gamma)*p));
                        
                    } else {
                        peaks[i]=new Son(indexToFreq(i),fft[i]);
                    }
                } else {
                    peaks[i]=null;
                }
            }
        }
		
    }
	    
    /**
     * Crée une fenêtre gaussienne dans le tableau fourni en paramètre
     * @param window Le tableau où sera écrit la fenêtre
     */
    private void buildGaussianWindow(double[] Spectre) {
    	if (Constantes.DEBUG==1) {System.out.println("1.2 Initialize fenêtre gaussienne FenetreGaussienne");
    	}
    	int n=Spectre.length;
        double sigma=0.01*n;
        for(int i = 0; i<Spectre.length;i++)
        	Spectre[i] = Math.exp(-0.5*Math.pow((i-(n-1)/2)/(sigma*(n-1)/2),2)); // Ceci est l'équation d'une courbe de Gauss
    }

    /**
     * Convolution entre les deux spectres
     * @param Fenetre La fenêtre dans un tableau de même dimension que data
     * @param signa Le signal à fenêtrer
     */
    private double[] Fenetrage( double[] Fenetre, byte[] Spectre ) {
        double [] signalTransforme=new double[dataSize];
    	if (Constantes.DEBUG==1) {System.out.println("2.4 Fenetrage");
    	}
        for(int i = 0; i<Spectre.length;i++)
        	signalTransforme[i] = Spectre[i]*Fenetre[i] ;
        return signalTransforme;
    }

    /**
     * Annule les harmoniques des pics trouvés...
     * Fatal en cas de bruit dans les faibles fréquences et inefficace contre les inharmonies
     */
    private void CLeanPeak() {
    	if (Constantes.DEBUG==1) {System.out.println("2.8-Nettoyage Pic..");
    	}
    	for(int i=0;i<peaks.length;i++) {
            if(peaks[i]!=null) {
                for(int j=i+1;j<peaks.length;j++) {
                    if(peaks[j]!=null && peaks[j].getNoteID()==peaks[i].getNoteID()) {
                        peaks[j]=null;
                    }
                }
            }
        }
    }

    /**
     * Trouve la fréquence à partir de l'indice du tableau FFT
     * @param i indice du tableau de la FFT
     * @return 
     */
    private double indexToFreq(double i) {
        return (double)i*(double)captureFrequency/(double)(zeroPaddedDataSize);
    }
    
    /**
     * Méthode lancée lors du lancement du thread
     * Exemple :
     * FrequencyDetector fc = new FrequencyDetector(...);
     * Thread th = new Thread(fc);
     * th.start();
     */
    @Override
    public void run() {
    	if (Constantes.DEBUG==1) {System.out.println("2.1-Lancement capturing...");
    	}
    	beginAudioCapture();
        while(isRec) {
            getDataFromCapture();
            //movingAverage(data);
                 
            signalAanalyser=Fenetrage(FenetreGaussienne,EchantillonSignalData);
            doFFT(signalAanalyser); // remplissage fft[]
            averageFFT();
            searchMax(); // max dans fft[]
            peaksIndexation(fft,true,(double)max/snr);
            CLeanPeak();
            searchMaxInPeaks();
            
        }
        
        line.stop();
        line.close();
    }
    
    /**
     * Ajoute un EcouteMusique après instanciation
     * @param fl le EcouteMusique
     */
    public final void addFrequencyListener(EcouteMusique fl) {
    	AlEcouteDuSon= fl;
    }

    /**
     * Enclenche l'arrêt de l'enregistrement (donc du thread)
     */
    public void stopRecording() {
    	if (Constantes.DEBUG==1) {System.out.println("Stop capturing...");
    	}
        isRec=false;
    }
    
    /**
     * Retourne le signal fenêtré
     * @return le signal fenêtré
     */
    public double[] getsignalAanalyser() {
        return signalAanalyser;
        
    }
    
    /**
     * Retourne la FFT
     * @return le signal fenêtré
     */
    public double[] getFFT() {
    	return fft;
    }
    
    /**
     * Retourne la note convenue comme fondamentale (d'amplitude maximale)
     * @return la note fondamentale
     */
    public Son GetNoteFondamentale() {
        return NoteFondamentale;
    }
    
    /**
     * Retourne l'ensemble des pics de la FFT
     * @return le tableau des pics
     */
    public Son[] getPeaks() {
        return peaks;
    }
}


