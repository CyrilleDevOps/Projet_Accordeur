//package net.codejava.fenetre;

//import net.codejava.actions.*;
//import net.codejava.analyse.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.text.DecimalFormat;
import javax.swing.*;
import javax.sound.sampled.*;



/**
 *
 * @author ScoriusTeam
 */
public class Menu extends JFrame implements ActionListener {
	
    // Choix & actions possibles

    /**
     * Tableau des noms des menus
     */

    protected static final String[] OPTIONS = {"Choix1", "Choix2"};

    
    // Menu Fenetre
    
    protected JMenuItem 
    	Choix2, // Menu Fenetre Choix 1

    	Choix1; // Menu Fenetre Choix 2

    /**
     * Les boutons
     */

    /**
     * Boutons - Init
     */
    
    public int selectedButton=-1;


    //Thread th;
    //FrequencyDetector fd;
    protected JButton 
    
    bouton1, //Bouton 1

    boutonEnregistrement; // Bouton Enregistrer : Start/Stop

    //Etat de l'enregistrement
    private boolean stateRecording=false;

    
    @Override
    public void actionPerformed(ActionEvent evtSource) {
        Object source = evtSource.getSource(); // L'objet qui a appelé l'événement
        
        if(source == Choix1) {
        	//choix1();
        } else if(source == Choix2) {
        	//Choix2();
        } else if(source == bouton1) {
        	 new PanelCaptureSon();
        } else if(source == boutonEnregistrement) {
            if(!stateRecording) {
                // On intialise le Thread du FrequencyDetector
                //fd=new FrequencyDetector(TunerWindow.fe,8,TunerWindow.fe/8,TunerWindow.mi,this); //indispensable de le mettre ici car comme ça fd sera instancié à chaque fois avec les nouveaux paramètres
                //th=new Thread(fd);
                //th.start();
            	boutonEnregistrement.setText("Stop");
                stateRecording=true;
            } else {
                //fd.stopRecording();
                //th.interrupt();
            	boutonEnregistrement.setText("Enregistrement");
                stateRecording=false;
            }
        }
 
        /*
        for(int i=0;i<symbolsButtons.length;i++) {
            if(source==symbolsButtons[i]) {
                selectedButton=i;
            }
        }
       */
    } 
	  
 }

