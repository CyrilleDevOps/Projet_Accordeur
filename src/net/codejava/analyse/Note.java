package net.codejava.analyse;

import javax.sound.sampled.*;

import net.codejava.start.Constantes;

import java.io.*;

public class Note {
	
	protected double [] notes = {110 , 116.55, 123.47 , 130.81, 138.59, 146.83, 155.56, 164.81, 174.61, 185.00, 196.00, 207.65};
	protected String [] nom= {"La" , "Sib", "Si" , "Do", "Do#", "Ré", "Ré#", "Mi", "Fa", "Fa#", "Sol","Sol#"};
	protected int indexNote=0; 
	protected int octave=0; //
	protected double ecart=5;
        
        
        public Note() {}

        public Note(double frequence) {
        	double calculOctave=0; 
        	if (Constantes.DEBUG==1) {System.out.println("x- Init Note"+frequence);
        	}
        	for (int i=0; i<notes.length; i++) {
                for (int j=1; j<5 ; j++){
                	if (Constantes.DEBUG>2 && Constantes.DEBUG!=0) {System.out.println("x- Init Note"+(notes[i]*j-frequence));
                	}
                		calculOctave=Math.pow(2,j);
                        if ( (Math.abs(notes[i]-frequence/calculOctave)<ecart ) ){
                        	if (Constantes.DEBUG==1) {System.out.println("*****Note :" + nom[i]+" Octave : "+j);
                           	}
                        	indexNote=i;
                        	octave=j;
                        	ecart=notes[i]-frequence/calculOctave ;                           
                        }  

                }
        	} 
        }
        
        public String getAccordType(Note[] Accords ) {
        	int[][] RefAccord =  {
        				{ 4, 7 },
        				{ 3, 7 },
        				{ 4, 8 },
        				{ 3, 6 },
        				{ 5, 7 },
        				{ 4, 6 }
        						  };
        	String[] RefAccordNom = {
        				"MAJOR",
        				"MINOR",
        				"AUGMENTED",
        				"DIMINISHED",
        				"SUSPENDED_FOURTH",
        				"FLATTED_FIFTH"};     	
        	
            String getAccordType ="Bof";
            int[] AccordType= { 0, 0 };
            
            if (Accords[0].getNoteID()<=Accords[1].getNoteID()) {
            	AccordType[0] = Math.abs(Accords[1].getNoteID()-Accords[0].getNoteID());
			} else {
				AccordType[0] = Math.abs(Accords[1].getNoteID()-Accords[0].getNoteID()+12);
			  }
            
            if (Accords[0].getNoteID()<=Accords[2].getNoteID()) {
                AccordType[1] = Math.abs(Accords[2].getNoteID()-Accords[0].getNoteID());
			} else {
	            AccordType[1] = Math.abs(Accords[2].getNoteID()-Accords[0].getNoteID()+12);
			  }

            
            if (Constantes.DEBUG==0) {System.out.println("x- Cherche Accord : {"+AccordType[0]+","+AccordType[1]+"}");
            }
            boolean AccordTrouve =false;
            int IndiceAccord =0;
            int MaxIndiceAccord =5;
            
            while (AccordTrouve == false && IndiceAccord<=MaxIndiceAccord) {
            	if (AccordType[0] == RefAccord[IndiceAccord][0] && 
            			AccordType[1] == RefAccord[IndiceAccord][1]) {
            		AccordTrouve=true;
            	} 	else {
            		IndiceAccord++;
            		}	
            }                   	
            if (AccordTrouve == true) {
            	getAccordType = RefAccordNom[IndiceAccord];
            }
            
            return getAccordType;
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

        //renvoi la note la plus proche, l'octave à laquelle elle est jouée et l'écart en Hz
        public double  GetEcartNote () {  
        	return ecart;
        }
        
        //renvoi la note la plus proche, l'octave à laquelle elle est jouée et l'écart en Hz
        public double  GetOctaveNote () {  
        	return octave;
        }
	
}
