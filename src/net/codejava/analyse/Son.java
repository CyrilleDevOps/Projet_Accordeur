package net.codejava.analyse;

public class Son {
	
	protected final double intensity;
	protected double frequency;
	
	public Son() {
		intensity=0;
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
    * Retourne la fréquence de la Note
    * @return fréquence sous forme de double
    */
   public String getSilence() {
	   String s;
       if(frequency<=10)
           s="Silence";
       else 
           s="#";  
       return s;
   }
   
   
   
   
	
}
