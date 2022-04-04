package net.codejava.analyse;
import java.util.LinkedList;

/**
 *
 * @author ScoriusTeam
 */
public class HeardNote { //extends Note {
    private double cents;
    private final String[] noteTable={"a","a#","b","c","c#","d","d#","e","f","f#","g","g#"};
    private final double intensity;
    private double frequency;
    private int noteID;
    private LinkedList lastCentValues;
    
    /**
     * Octave de la note (norme anglaise)
     */
    protected int octave; // 1 ou 2 

    /**
     * Nom de la note en minuscule (a, b, c...)
     */
    protected char name; // A B C D E F G

    /**
     * Altération : -1=bémol; 0=rien; 1=dièse
     */
    protected int accidental;
    
    /**
     * Fréquence du La de référence
     */
    public static int aFreqRef=440; //on rend la variable static pour pouvoir la modifier à loisir dans la classe TunerOptionPanel

    /**
     * Facteur de moyennage de chaque note
     */
    public static int AVERAGING_FACTOR=20; //idem
    
    /**
     * Crée une note à partir de la fréquence
     * @param freq fréquence d'entrée
     */
    public HeardNote(double freq) {
        intensity=0;
        initWithFreq(freq);
    }
    
    /**
     * Crée une note à partir de la fréquence et de son intensité
     * @param freq fréquence d'entrée
     * @param i intensité
     */
    public HeardNote(double freq, double i) {
        intensity=i;
        initWithFreq(freq);
    }
    
    
    /**
     * Retourne le numéro de la note (0 : La, 1 : Si, 2 : Do, etc)
     * @return le numéro de la note
     */
    public int getNoteID() {
        
        return noteID;
    }
    
    /**
     * Retourne la précision de la note entre -50 et 50 centièmes (0 étant le plus précis)
     * @return la précision
     */
    public double getCents() {
        return cents;
    }
    
    /**
     * Retourne l'intensité de la note
     * @return intensité sous forme de double positif
     */
    public double getIntensity() {
        return intensity;
    }
    
    /**
     * Retourne la fréquence de la Note
     * @return fréquence sous forme de double
     */
    public double getFrequency() {
        return frequency;
    }
    
    /**
     * Fait la moyenne de la précision de plusieurs HeardNotes en fonction du facteur de moyennage
     * @param other une autre HeardNote à ajouter pour moyenner
     */
    public void meanWith(HeardNote other) {
        if(lastCentValues==null)
            lastCentValues=new LinkedList();
        lastCentValues.add(other.getCents());
        if(lastCentValues.size()>AVERAGING_FACTOR)
            lastCentValues.remove(0);
            
        cents=calculateAverage(lastCentValues);
    }
    
    private double calculateAverage(LinkedList <Double> list) {
        Double sum = 0.;
        if(!list.isEmpty()) {
           // sum = list.stream().map((mark) -> mark).reduce(sum, Double::sum);
            return sum / list.size();
        }
        return sum;
    }
    
    private void initWithFreq(double f) {
        frequency=f;
        
        // On calcule le nombre de demis tons entre la fréquence étudiée f et la fréquence de référence
        float nabs=(float)(12*Math.log(f/aFreqRef) / Math.log(2));
                
        // "%" en java est l'opérateur reste, et non un modulo, la différence étant que le premier renvoie un nombre positif
        // C'est pourquoi on utilise l'opération mod = (n % div + div) % div pour faire un modulo
        // n est la valeur de la note en demis tons (0 -> LA, 1 -> LA #, etc)
        float n=(nabs%12+12)%12; 
        
        // 4 est l'octave de la fréquence de référence
        octave=(int)Math.floor((double)nabs/12)+4;
        
        // (int)n est la partie entière de n. Ici on récupère donc la partie décimale et on la multiplie par 100.
        cents=(n-(int)n)*100;
        
        // On arrondi à la note la plus proche à partir de la valeur en centièmes
        if(cents>50) {
            cents-=100;
            n++;
            if(n>=12) {
                n=0;
                octave++;
            }
        }
        
        // L'origine d'une octave se trouve au do, par exemple on passe du Si2 au Do3... On arrange ceci de cette manière :
        if(n>=3) octave++;
        
        noteID=(int)n;
        name=noteTable[(int)n].charAt(0);
        if(noteTable[(int)n].length()>1) accidental=1;
    }
}
