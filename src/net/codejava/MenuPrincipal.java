package net.codejava.fenetre;

	/**
	 * Mise en place du menu principal
	 */

	// Chargement des librairies  Swing et AWT
	import java.awt.*;
	import javax.swing.*;

import java.awt.Color;
import java.awt.Dimension;

	public class MenuPrincipal extends Menu {


	    /**
	     * Création et ouverture du Menu principal
	     */
	    public MenuPrincipal() {
	        init();
	    }
	   
   
	    private void init() {
	        setTitle("Accordeur");
	        setMinimumSize(new Dimension(1100, 700));
	        
	        // La barre de menus
	        JMenuBar BarreMenu = new JMenuBar();
	        setJMenuBar(BarreMenu);
	        
	        // L'objet contenant chacun des menus de la barre de menus
	        JMenu[] menuButton = new JMenu[OPTIONS.length];
	        for (int i = 0; i < OPTIONS.length; i++) {
	            menuButton[i] = new JMenu(OPTIONS[i]);
	            BarreMenu.add(menuButton[i]);
	        }
	        
	        Choix1 = new JMenuItem("Choix1");
	        //newItem.setIcon(new ImageIcon("nouveau.png"));
	        Choix1.addActionListener(this);
	        menuButton[0].add(Choix1);

	        Choix2 = new JMenuItem("Choix2");
	        //settings.setIcon(new ImageIcon("idee.png"));
	        Choix2.addActionListener(this);
	        menuButton[1].add(Choix2);
	        
	        // PanneauLateralDroit : Le JPanel qui contient les boutons Enregistrement et analyse
	        JPanel PanneauLateralDroit = new JPanel();
	        
	        PanneauLateralDroit.setBackground(Color.gray);
	        PanneauLateralDroit.setLayout(new BoxLayout(PanneauLateralDroit, BoxLayout.PAGE_AXIS));
	        PanneauLateralDroit.setPreferredSize(new Dimension(190, 350));
	         
	        
	        //PanelBoutons : Le JPanel qui contient les boutons symboles
	        JPanel PanelBoutons = new JPanel();
	        PanelBoutons.setMaximumSize(new Dimension(Integer.MAX_VALUE,200));
	        
	        //Ajout des boutons
	        // buttonTuner : Le bouton pour accéder à l'accordeur
	        bouton1 = new JButton();
	        bouton1.setText("Accordeur");
	        bouton1.setBackground(new Color(10, 144, 10));
	        bouton1.setForeground(Color.black);
	        bouton1.addActionListener(this);
	        PanelBoutons.add(bouton1);
	        
	        // buttonRecord : Le bouton pour enregistrer
	        boutonEnregistrement = new JButton();
	        boutonEnregistrement.setText("Accords");
	        boutonEnregistrement.setBackground(new Color(10, 144, 10));
	        boutonEnregistrement.setForeground(Color.black);
	        boutonEnregistrement.addActionListener(this);
	        PanelBoutons.add(boutonEnregistrement);
	        
	        PanneauLateralDroit.add(PanelBoutons);
	        add(PanneauLateralDroit, BorderLayout.LINE_END);
	        
	                      
	       setVisible(true);
	        
	    }			
	}
