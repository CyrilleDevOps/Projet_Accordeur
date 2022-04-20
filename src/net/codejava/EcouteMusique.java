//package net.codejava.actions;
//import net.codejava.analyse.*;

public interface EcouteMusique {
	
    /**
     * Se lance lorsqu'une nouvelle note a été détectée par le FrequencyDetector
     * @param n la nouvelle note
     */
    public void onNewNote(Son n);

}
