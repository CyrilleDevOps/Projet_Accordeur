//package net.codejava.actions;
//import net.codejava.analyse.*;

public interface EcouteMusique {
	
    /**
     * Se lance lorsqu'une nouvelle note a �t� d�tect�e par le FrequencyDetector
     * @param n la nouvelle note
     */
    public void onNewNote(Son n);

}
