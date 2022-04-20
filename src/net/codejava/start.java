//package net.codejava.start;

//import net.codejava.fenetre.*;


import javax.swing.JFrame;

//import net.codejava.fenetre.MenuPrincipal;

import javax.swing.*;
import javax.sound.sampled.*;

	/**
	 *
	 * @author GreatTeam
	 */
	public class start extends JFrame {

	    //Version 
		private static final long serialVersionUID = 01;
		

	    public static void main(String[] args) {
	        //FontUtils.importFont("Bravura.otf");
	        
	        // Définition du Look & Feel : on utilise celui du système qui est plus beau que le metal par défaut
	        try {
	            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
	            System.err.println(e);
	        }
	        
	        MenuPrincipal FenetreDemarrage = new MenuPrincipal();
	        FenetreDemarrage.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Pour permettre la fermeture de la fenêtre lors de l'appui sur la croix rouge
			
	        
	            boolean found=false;
	            Mixer.Info[] mixers = AudioSystem.getMixerInfo();
	            for(int i=0; i<mixers.length; i++) {
	                Mixer mi = AudioSystem.getMixer(mixers[i]);
	                Line.Info[] lines = mi.getTargetLineInfo();
	                for (int j=0;j<lines.length; j++){
	                    if(lines[j].getLineClass()==TargetDataLine.class){
	                    	if (Constantes.DEBUG==1) {System.out.println("00- Cherche Sound Device");
	                    	}
	                        PanelCaptureSon.mi=mixers[i];
	                        PanelCaptureAccord.miAccord=mixers[i];
	                        break;
	                    }
	                }

	                if(found) break;
	            }
	      
	    }
	    
	}

