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
    private HeardNote waitingMaxNote;
    private int freqStabilization;
    private double[] window;
    private EcouteMusique frequencyListener;
    private boolean isRec;
    private byte[] data;
    private double[] windowedData;
    private double max; // Amplitude maximale de la FFT
    private double[] fft;
    private HeardNote maxNote;
    private HeardNote[] peaks;
	    
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
     * Cr�e un FrequencyDetector avec ses param�tres et un FrequencyListener
     * @param cf Fr�quence d'�chantillonnage
     * @param s Taille du sample en bits
     * @param b Taille du buffer
     * @param mi Mixer � utiliser
     * @param fl Interface FrequencyListener
     */
    public CaptureSon(int cf, int s, int b, Mixer.Info mi, EcouteMusique fl) {
        init(cf, s, b, mi);
        addFrequencyListener(fl);
    }
	
    private void init(int cf, int s, int b, Mixer.Info mi) {
	    // Copie des attributs
	    captureFrequency=cf;
	    sampleSize=s;
	    mixerInfo=mi;
	    
	    // Moyennage de la FFT param�trable
	    averagingFactor = 8;
	    
	    // Taille du buffer (signal d'entr�e) et taille du tableau avec "Zero Padding" pour pouvoir calculer la FFT
	    dataSize=b; 
	    zeroPaddedDataSize=(int)Math.pow(2,Math.ceil(Math.log(dataSize)/Math.log(2))); //puissance de 2 sup�rieure la plus proche
	    
	    // Initialisation des tableaux du signal d'entr�e (data), de la FFT, et de l'historique de FFT pour moyennage
	    data = new byte[dataSize];
	    fft=new double[dataSize/2];
	    lastFFTs= new double[averagingFactor][zeroPaddedDataSize];
	    
	    // Cr�ation de la fen�tre Gaussienne
	    window = new double[dataSize];
	    buildGaussianWindow(window);
	    
	    isRec=false;
    }
	
    private void beginAudioCapture() {
        isRec=true;
        
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
        // Read the next chunk of data from the TargetDataLine.
        if(line!=null) {
            int numBytesRead = line.read(data, 0, dataSize);
            // Save this chunk of data.
            out.write(data, 0, numBytesRead);
        } else {
            System.err.println("no line!");
        }
    }
	    
    /**
     * Transforme un tableau de doubles en tableau de complexes correspondant avec des parties imaginaires nulles
     * @param doubleData
     * @return le tableau de complexe
     */
    private Complex[] toComplex(double[] doubleData) {
        Complex[] complexData=new Complex[zeroPaddedDataSize];
        for(int i=0;i<complexData.length;i++) {
            if(i<dataSize)
                complexData[i]=new Complex(doubleData[i],0);
            else 
                complexData[i]=new Complex(0,0);
        }
        return complexData;
    }
	    
    /**
     * Calcule la FFT 
     */
    private void doFFT(double[] data) {
        Complex[]complexData =toComplex(data);
        Complex[] rawfft=FFT.fft(complexData);
        
        // On ne fait que la moiti� car l'autre moiti� n'est pas interpr�table (~ Th�or�me de Shannon)
        for(int i=0;i<fft.length/2;i++) {
            fft[i]=rawfft[i].abs()/zeroPaddedDataSize;
        }
		
    }
	    
    /**
     * Parcourt le tableau des pics et met � jour l'amplitude max (max), la fr�quence correspondante (maxFreq) et l'indice du tableau de la FFT correspondant
     */
    private void searchMaxInPeaks() {
        HeardNote newMaxNote=new HeardNote(0,0);
        HeardNote updatedOldNote=null;
        for(int i=0;i<peaks.length/2;i++) {
            if(peaks[i]!= null && peaks[i].getIntensity()>newMaxNote.getIntensity()) {
                newMaxNote=peaks[i];
                
                if(maxNote != null && maxNote.equals(peaks[i]))
                    updatedOldNote=peaks[i];
            }
        }
	        
	        
        if((maxNote !=null && !maxNote.equals(newMaxNote)) || maxNote==null) {
            waitingMaxNote=newMaxNote;
            freqStabilization=0;
        }
	        
	       
        if(waitingMaxNote != null && newMaxNote != null && waitingMaxNote.equals(newMaxNote)) {
            if(freqStabilization==nNotesWaited || updatedOldNote == null) {
                if(maxNote!=null && !maxNote.equals(newMaxNote))
                    fireNoteChanged();
                maxNote=newMaxNote;
            } else {
                maxNote.meanWith(updatedOldNote);
                freqStabilization++;
            }
            
        }
    }

    /**
     * Recherche le maximum de la FFT
     */
    private void searchMax() {
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
        peaks=new HeardNote[fft.length];
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
                            peaks[i]=new HeardNote(freq,Math.exp(beta-0.25*(alpha-gamma)*p));
                        
                    } else {
                        peaks[i]=new HeardNote(indexToFreq(i),fft[i]);
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
    private void buildGaussianWindow(double[] window) {
        int n=window.length;
        double sigma=0.01*n;
        for(int i = 0; i<window.length;i++)
            window[i] = Math.exp(-0.5*Math.pow((i-(n-1)/2)/(sigma*(n-1)/2),2)); // Ceci est l'�quation d'une courbe de Gauss
    }

    /**
     * Applique la fen�tre � un signal
     * @param window La fen�tre dans un tableau de m�me dimension que data
     * @param data Le signal � fen�trer
     */
    private double[] applyWindow( double[] window, byte[] data ) {
        double [] newWindowedData=new double[dataSize];
        for(int i = 0; i<data.length;i++)
            newWindowedData[i] = data[i]*window[i] ;
        return newWindowedData;
    }

    /**
     * Calcule la moyenne glissante d'un signal
     * @param data le signal � moyenner
     */
    private void movingAverage(byte[] data) {
        for(int i=1;i<data.length;i++)
            data[i]=(byte)((data[i]+data[i-1])/2);
    }

    /**
     * Annule les harmoniques des pics trouv�s...
     * Fatal en cas de bruit dans les faibles fr�quences et inefficace contre les inharmonies
     */
    private void getFundamental() {
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
        beginAudioCapture();
        
        while(isRec) {
            getDataFromCapture();
            movingAverage(data);
                
            windowedData=applyWindow(window,data);
            doFFT(windowedData);
            averageFFT();
            searchMax();
            peaksIndexation(fft,true,(double)max/snr);
            getFundamental();
            searchMaxInPeaks();
            
        }
        
        line.stop();
        line.close();
    }
    
    /**
     * la m�thode permettant de s�parer des notes utilis�e pour pouvoir ensuite afficher les notes dans le scorePanel
     **/
    private void fireNoteChanged() {
        if(frequencyListener!=null) {
            frequencyListener.onNewNote(maxNote);
        }
    }
    
    /**
     * Ajoute un FrequencyListener apr�s instanciation
     * @param fl le FrequencyListener
     */
    public final void addFrequencyListener(EcouteMusique fl) {
        frequencyListener= fl;
    }

    /**
     * Enclenche l'arr�t de l'enregistrement (donc du thread)
     */
    public void stopRecording() {
        isRec=false;
    }
    
    /**
     * Retourne le signal fen�tr�
     * @return le signal fen�tr�
     */
    public double[] getWindowedData() {
        return windowedData;
        
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
    public HeardNote getMaxNote() {
        return maxNote;
    }
    
    /**
     * Retourne l'ensemble des pics de la FFT
     * @return le tableau des pics
     */
    public HeardNote[] getPeaks() {
        return peaks;
    }
}


