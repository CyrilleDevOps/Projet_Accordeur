package net.codejava.analyse;

import net.codejava.actions.*;

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
     * Crée un FrequencyDetector avec ses paramètres et un FrequencyListener
     * @param cf Fréquence d'échantillonnage
     * @param s Taille du sample en bits
     * @param b Taille du buffer
     * @param mi Mixer à utiliser
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
	    
	    // Moyennage de la FFT paramétrable
	    averagingFactor = 8;
	    
	    // Taille du buffer (signal d'entrée) et taille du tableau avec "Zero Padding" pour pouvoir calculer la FFT
	    dataSize=b; 
	    zeroPaddedDataSize=(int)Math.pow(2,Math.ceil(Math.log(dataSize)/Math.log(2))); //puissance de 2 supérieure la plus proche
	    
	    // Initialisation des tableaux du signal d'entrée (data), de la FFT, et de l'historique de FFT pour moyennage
	    data = new byte[dataSize];
	    fft=new double[dataSize/2];
	    lastFFTs= new double[averagingFactor][zeroPaddedDataSize];
	    
	    // Création de la fenêtre Gaussienne
	    window = new double[dataSize];
	    buildGaussianWindow(window);
	    
	    isRec=false;
    }
	
    private void beginAudioCapture() {
        isRec=true;
        
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
        
        // On ne fait que la moitié car l'autre moitié n'est pas interprétable (~ Théorème de Shannon)
        for(int i=0;i<fft.length/2;i++) {
            fft[i]=rawfft[i].abs()/zeroPaddedDataSize;
        }
		
    }
	    
    /**
     * Parcourt le tableau des pics et met à jour l'amplitude max (max), la fréquence correspondante (maxFreq) et l'indice du tableau de la FFT correspondant
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
     * Ajoute la FFT actuelle à l'historique et fait la moyenne temporelle des FFT
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
     * Liste les pics dont la fréquence est supérieure au seuil de fréquence dans le tableau peaks
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
                         *  /!\ avec fenêtrage gaussien /!\ */

                        double alpha=Math.log(fft[i-1]);
                        double beta=Math.log(fft[i]);
                        double gamma=Math.log(fft[i+1]);
                        double p = 0.5*(alpha-gamma)/(alpha-2*beta+gamma);
                        
                        double freq=indexToFreq(i+p);
                        
                        // On n'ajoute aux peaks que si la fréquence détectée est supérieure au seuil
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
     * Crée une fenêtre gaussienne dans le tableau fourni en paramètre
     * @param window Le tableau où sera écrit la fenêtre
     */
    private void buildGaussianWindow(double[] window) {
        int n=window.length;
        double sigma=0.01*n;
        for(int i = 0; i<window.length;i++)
            window[i] = Math.exp(-0.5*Math.pow((i-(n-1)/2)/(sigma*(n-1)/2),2)); // Ceci est l'équation d'une courbe de Gauss
    }

    /**
     * Applique la fenêtre à un signal
     * @param window La fenêtre dans un tableau de même dimension que data
     * @param data Le signal à fenêtrer
     */
    private double[] applyWindow( double[] window, byte[] data ) {
        double [] newWindowedData=new double[dataSize];
        for(int i = 0; i<data.length;i++)
            newWindowedData[i] = data[i]*window[i] ;
        return newWindowedData;
    }

    /**
     * Calcule la moyenne glissante d'un signal
     * @param data le signal à moyenner
     */
    private void movingAverage(byte[] data) {
        for(int i=1;i<data.length;i++)
            data[i]=(byte)((data[i]+data[i-1])/2);
    }

    /**
     * Annule les harmoniques des pics trouvés...
     * Fatal en cas de bruit dans les faibles fréquences et inefficace contre les inharmonies
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
     * la méthode permettant de séparer des notes utilisée pour pouvoir ensuite afficher les notes dans le scorePanel
     **/
    private void fireNoteChanged() {
        if(frequencyListener!=null) {
            frequencyListener.onNewNote(maxNote);
        }
    }
    
    /**
     * Ajoute un FrequencyListener après instanciation
     * @param fl le FrequencyListener
     */
    public final void addFrequencyListener(EcouteMusique fl) {
        frequencyListener= fl;
    }

    /**
     * Enclenche l'arrêt de l'enregistrement (donc du thread)
     */
    public void stopRecording() {
        isRec=false;
    }
    
    /**
     * Retourne le signal fenêtré
     * @return le signal fenêtré
     */
    public double[] getWindowedData() {
        return windowedData;
        
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


