package net.codejava.analyse;

import net.codejava.start.Constantes;

public class Son extends Note {
	
	protected final double intensity;
	protected double frequency;
	
	/**
     * Fr�quence du La de r�f�rence
     */
    public static int aFreqRef=440; 
	
    public Son() {
		intensity=0;
		
	}
	
    public Son(double freq) {
    	super(freq);
    	intensity=0;
    	frequency=freq;
    	if (Constantes.DEBUG==1) {System.out.println("x- Init Son");
    	}
    }
    
    
	public Son(double freq, double i) {
		super(freq);
		intensity=i;
		frequency=freq;
		if (Constantes.DEBUG==1) {System.out.println("x- Init Son");
		}
    }

	 /**
     * Retourne l'intensit� de la note
     * @return intensit� sous forme de double positif
     */
    public double getIntensity() {
        return intensity;
    }
    
    /**
     * Retourne la fr�quence de la Note
     * @return fr�quence sous forme de double
     */
    public double getFrequency() {
        return frequency;
    }
	
    /**
    * Retourne la fr�quence de la Note
    * @return fr�quence sous forme de double
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
