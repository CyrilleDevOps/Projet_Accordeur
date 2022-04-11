package net.codejava.analyse;

import net.codejava.actions.*;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;

public class CaptureSon implements Runnable{


	//D�but D�claration

    private int captureFrequency;
    private int sampleSize; //taille maximale des donn�es r�cup�rables par le micro dans l'ordinateur
    private int dataSize; //nombre de sampleSize pr�sents dans le buffer
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
    private Son NoteFondamentale;
    private Son[] peaks;
	    
    public static double freqMin=60; //Seuil en fr�quence de d�tection
    public static double snr=1.41; //Rapport signal sur bruit
	public static int nNotesWaited = 5; //Nombre de notes cons�cutives entendues avant que la note soit consid�r�e comme stable

    /**
     * Cr�e une capture de Son avec ses param�tres
     * @param cf Fr�quence d'�chantillonnage
     * @param s Taille du sample en bits
     * @param b Taille du buffer
     * @param mi Mixer � utiliser
     */
    
	public CaptureSon(int cf, int s, int b, Mixer.Info mi) {
        init(cf, s, b, mi);
    }
	    
    /**
     * Cr�e une CaptureSon avec ses param�tres et une EcouteMusique
     * @param cf Fr�quence d'�chantillonnage
     * @param s Taille du sample en bits
     * @param b Taille du buffer
     * @param mi Mixer � utiliser
     * @param fl Interface EcouteMusique
     */
    public CaptureSon(int cf, int s, int b, Mixer.Info mi, EcouteMusique fl) {
    	init(cf, s, b, mi);
        addFrequencyListener(fl);
    }
	
    private void init(int cf, int s, int b, Mixer.Info mi) {

    	System.out.println("1.1 Initialize les donn�es pour la capture");
    	// Copie des attributs
	    captureFrequency=cf;
	    sampleSize=s;
	    mixerInfo=mi;
	    
	    // Moyennage de la FFT param�trable
	    averagingFactor = 8;
	    
	    // Taille du buffer (signal d'entr�e) et taille du tableau avec "Zero Padding" pour pouvoir calculer la FFT
	    dataSize=b; 
	    zeroPaddedDataSize=(int)Math.pow(2,Math.ceil(Math.log(dataSize)/Math.log(2))); //puissance de 2 sup�rieure la plus proche
	    
	    // Initialisation des tableaux du signal d'entr�e (EchantillonSignalData), de la FFT, et de l'historique de FFT pour moyennage
	    EchantillonSignalData = new byte[dataSize];
	    fft=new double[dataSize/2];
	    lastFFTs= new double[averagingFactor][zeroPaddedDataSize];
	    
	    // Cr�ation de la fen�tre Gaussienne
	    FenetreGaussienne = new double[dataSize];
	    buildGaussianWindow(FenetreGaussienne);
	    
	    isRec=false;
    }
	
    private void beginAudioCapture() {
        isRec=true;
        System.out.println("2.2 Init CaptureSon");
        // On d�finit un format audio
        AudioFormat format=new AudioFormat(captureFrequency,sampleSize,1,true,true);
        
        // On r�cup�re les informations d'une ligne compatible avec le format
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        
        // Si la ligne n'est pas support�e, on renvoie une erreur
        if (!AudioSystem.isLineSupported(info)) {
            System.out.println("System is not supported...");
        }
        
        // Obtain and open the line.
        try {
            // On r�cup�re la ligne du mixer pass� en param�tre
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
    	System.out.println("2.3-capturing ->EchantillonSignalData..");
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
        System.out.println("2.5-Calcul fft");
        
        // On ne fait que la moiti� car l'autre moiti� n'est pas interpr�table (~ Th�or�me de Shannon)
        for(int i=0;i<fft.length/2;i++) {
            fft[i]=rawfft[i].abs()/zeroPaddedDataSize;
        }
		
    }
	    
    /**
     * Parcourt le tableau des pics et met � jour l'amplitude max (max), la fr�quence correspondante (maxFreq) et l'indice du tableau de la FFT correspondant
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
     	System.out.println("2.10-Not Max ou fondamental.."+NoteFondamentale.GetNomNote());
    }
    /**
     * la m�thode permettant de s�parer des notes utilis�e pour pouvoir ensuite afficher les notes dans le scorePanel
     **/
    private void fireNoteChanged() {
        if(AlEcouteDuSon!=null) {
        	AlEcouteDuSon.onNewNote(NoteFondamentale);
        }
    }
  
    
    /**
     * Recherche le maximum de la FFT
    */
    private void searchMax() {
    	System.out.println("2.7 searchMax");
    	double newMax=0;
        for(int i=0;i<fft.length/2;i++) {
            if(fft[i]>newMax) {
                newMax=fft[i];
            }
        }
        max=newMax;
    }

    /**
     * Ajoute la FFT actuelle � l'historique et fait la moyenne temporelle des FFT
     */
    private void averageFFT() {
    	System.out.println("2.6 averageFFT");
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
     * Liste les pics dont la fr�quence est sup�rieure au seuil de fr�quence dans le tableau peaks
     * @param fft tableau de la FFT
     * @param parabolic Si vrai, interpolation parabolique, sinon renvoie l'indice du pic
     * @param threshold Valeur de seuil pour qu'un pic soit pris en compte
     */
    private void peaksIndexation(double[] fft, boolean parabolic, double threshold) {
    	System.out.println("2.8 peaksIndexation");
    	peaks=new Son[fft.length];
        for(int i=1;i<fft.length-1;i++) {
            if(fft[i]>threshold) {
                if(fft[i-1]<fft[i] && fft[i+1]<fft[i]) {
                    if(parabolic) {
                        
                        /* Interpolation parabolique
                         *  /!\ avec fen�trage gaussien /!\ */

                        double alpha=Math.log(fft[i-1]);
                        double beta=Math.log(fft[i]);
                        double gamma=Math.log(fft[i+1]);
                        double p = 0.5*(alpha-gamma)/(alpha-2*beta+gamma);
                        
                        double freq=indexToFreq(i+p);
                        
                        // On n'ajoute aux peaks que si la fr�quence d�tect�e est sup�rieure au seuil
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
     * Cr�e une fen�tre gaussienne dans le tableau fourni en param�tre
     * @param window Le tableau o� sera �crit la fen�tre
     */
    private void buildGaussianWindow(double[] Spectre) {
    	System.out.println("1.2 Initialize fen�tre gaussienne FenetreGaussienne");
    	int n=Spectre.length;
        double sigma=0.01*n;
        for(int i = 0; i<Spectre.length;i++)
        	Spectre[i] = Math.exp(-0.5*Math.pow((i-(n-1)/2)/(sigma*(n-1)/2),2)); // Ceci est l'�quation d'une courbe de Gauss
    }

    /**
     * Convolution entre les deux spectres
     * @param Fenetre La fen�tre dans un tableau de m�me dimension que data
     * @param signa Le signal � fen�trer
     */
    private double[] Fenetrage( double[] Fenetre, byte[] Spectre ) {
        double [] signalTransforme=new double[dataSize];
        System.out.println("2.4 Fenetrage");
        for(int i = 0; i<Spectre.length;i++)
        	signalTransforme[i] = Spectre[i]*Fenetre[i] ;
        return signalTransforme;
    }

    /**
     * Annule les harmoniques des pics trouv�s...
     * Fatal en cas de bruit dans les faibles fr�quences et inefficace contre les inharmonies
     */
    private void CLeanPeak() {
    	System.out.println("2.8-Nettoyage Pic..");       
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
     * Trouve la fr�quence � partir de l'indice du tableau FFT
     * @param i indice du tableau de la FFT
     * @return 
     */
    private double indexToFreq(double i) {
        return (double)i*(double)captureFrequency/(double)(zeroPaddedDataSize);
    }
    
    /**
     * M�thode lanc�e lors du lancement du thread
     * Exemple :
     * FrequencyDetector fc = new FrequencyDetector(...);
     * Thread th = new Thread(fc);
     * th.start();
     */
    @Override
    public void run() {
    	System.out.println("2.1-Lancement capturing...");
    	beginAudioCapture();
        while(isRec) {
            getDataFromCapture();
            //movingAverage(data);
                 
            signalAanalyser=Fenetrage(FenetreGaussienne,EchantillonSignalData);
            doFFT(signalAanalyser);
            averageFFT();
            searchMax();
            peaksIndexation(fft,true,(double)max/snr);
            CLeanPeak();
            searchMaxInPeaks();
            
        }
        
        line.stop();
        line.close();
    }
    
    /**
     * Ajoute un EcouteMusique apr�s instanciation
     * @param fl le EcouteMusique
     */
    public final void addFrequencyListener(EcouteMusique fl) {
    	AlEcouteDuSon= fl;
    }

    /**
     * Enclenche l'arr�t de l'enregistrement (donc du thread)
     */
    public void stopRecording() {
        System.out.println("Stop capturing...");
        isRec=false;
    }
    
    /**
     * Retourne le signal fen�tr�
     * @return le signal fen�tr�
     */
    public double[] getsignalAanalyser() {
        return signalAanalyser;
        
    }
    
    /**
     * Retourne la FFT
     * @return le signal fen�tr�
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


