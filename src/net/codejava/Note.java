package net.codejava.analyse;

import javax.sound.sampled.*;
import java.io.*;

public class Note {
	
	protected double [] notes = {110 , 116.55, 123.47 , 130.81, 138.59, 146.83, 155.56, 164.81, 174.61, 185.00, 196.00, 207.65};
	protected String [] nom= {"La" , "Sib", "Si" , "Do", "Do#", "Ré", "Ré#", "Mi", "Fa", "Fa#", "Sol","Sol#"};
	protected int indexNote=0; 
	protected int octave=0; // 1 ou 2 
	protected double ecart=10;
        
        
        public Note() {}

        public Note(double frequence) {
        	System.out.println("x- Init Note"+frequence);
        	for (int i=0; i<notes.length; i++) {
                for (int j=1; j<7 ; j++){
                		//System.out.println("x- Init Note"+(notes[i]*j-frequence));
                        if ( (Math.abs(notes[i]*j-frequence)<ecart ) ){
                        	System.out.println("*****Note :" + nom[i]);
                        	indexNote=i;
                        	octave=j;
                        	ecart=notes[i]*j-frequence ;                           
                        }  

                }
        	} 
        }

        /**
         * Retourne le numéro de la note (0 : La, 1 : Si, 2 : Do, etc)
         * @return le numéro de la note
         */
        public int getNoteID() { 
            return indexNote;
        }
        	
        //renvoi la note la plus proche, l'octave à laquelle elle est jouée et l'écart en Hz
        public double  GetNote () {  
        	return notes[indexNote];
        }
        
        //renvoi la note la plus proche, l'octave à laquelle elle est jouée et l'écart en Hz
        public String  GetNomNote () {  
        	return nom[indexNote];
        }

	
	
}
