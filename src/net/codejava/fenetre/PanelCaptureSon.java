package net.codejava.fenetre;

import net.codejava.actions.*;
import net.codejava.analyse.*;
import net.codejava.start.Constantes;

import java.io.*;
import java.text.DecimalFormat;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.*;

import javax.sound.sampled.Mixer;
import javax.swing.*;


public class PanelCaptureSon extends JFrame implements ActionListener,WindowListener {
	
	// Début Declaration
	
	private Son note; //la son capté par le micro
    private CaptureSon FluxEcouter; //la fréquence détectée par le micro
    private Thread ThreadSon; // le flux Audio
	
	public static int FPS=30; // le nombre d'images par seconde
	public static int FrequenceEchantillonage=44100; //Fréquence d'échantillonnage
	public static int frequence; 
	public static Mixer.Info mi; //Audio Device
	
	private double[] fft;
	
    private Box textPanel;
    private Box textPanel2;
    private Box textPanel3;
    private Box textPanel4;
    
    private JLabel noteNameLabel, frequencyLabel, octaveLabel, ecartLabel;

    
    //Fin Déclaration
	
  
        // Ouvre une fenetre pour l'accordeur
    public PanelCaptureSon() {
    	super("Accordeur");
    	init();
    }
    
    
    @Override
    public void actionPerformed(ActionEvent Evenement) {
    	//fft=FrequenceEcouter.getFFT();
    	note=FluxEcouter.GetNoteFondamentale();
    	
    	if (Constantes.DEBUG==1) {System.out.println("ecoute Note  ");
    	}
    	if(note!=null){
    		if (Constantes.DEBUG==1) {System.out.println("ecoute Note  "+note.getNoteID());
    		}
    		if(note.getNoteID()>=0){
	    		
    			if (Constantes.DEBUG==1) {System.out.println("x- Affichage Frequence Fondamentale "+note.getFrequency()+"-"+note.GetEcartNote());
    			}
	           	           	
	           	noteNameLabel.setText((note.getFrequency()!=0) ? note.GetNomNote() : "Joue une note !");
	           	
	           	DecimalFormat FormatAffichageFrequence= new DecimalFormat("#.##");
	           	frequencyLabel.setText(FormatAffichageFrequence.format(note.getFrequency())+" Hz");
	
	           	octaveLabel.setText(" "+ note.GetOctaveNote() );
	           	//ecartLabel.setText(" "+ note.GetEcartNote() );
	           	ecartLabel.setText(FormatAffichageFrequence.format(note.GetEcartNote()));
	           	
	            noteNameLabel.setForeground(new Color(0, 202, 0));
	            frequencyLabel.setForeground(new Color(0, 202, 0));
	            octaveLabel.setForeground(new Color(0, 202, 0));
	            ecartLabel.setForeground(new Color(0, 202, 0));
	            
	            repaint();
    		}
        }

    }
    
    
    private void init() { //pour Ã©viter de mettre des fonctions surchargeables telles setSize() dans le constructeur
          
    	if (Constantes.DEBUG==1) {System.out.println("1-Mise en ecoute");
    	}
    	
    	// On initialise la fequence
    	FluxEcouter=new CaptureSon(FrequenceEchantillonage,8,FrequenceEchantillonage/8,mi);
    	    	
    	// On set la taille de la fenÃªtre et on affiche les bordures
       setSize(400,600);
       setUndecorated(false);
        
        // Un timer pour rafraÃ®chir l'affichage
        Timer timer = new Timer(1000/FPS, this);
        timer.start();
        
        Box verticalBox=Box.createVerticalBox();
        verticalBox.add(Box.createRigidArea(new Dimension(0,20)));
        
        /* ********************************************************
        NOTE
		******************************************************** */
		
        addCenteredPanel(new JLabel("Note"),verticalBox);
        textPanel=Box.createVerticalBox();
        textPanel.setBackground(Color.white);
        textPanel.setOpaque(true);
        
        // Le JLabel qui contiendra le nom de la note
        noteNameLabel=new JLabel("Joue une note ! ",SwingConstants.CENTER);
        noteNameLabel.setFont(new Font("Trebuchet MS",Font.PLAIN,40)); // Grosse police
        addCenteredPanel(noteNameLabel,textPanel);
        
        addCenteredPanel(textPanel,verticalBox);
        
        /* ********************************************************
        FREQUENCE
		******************************************************** */
        addCenteredPanel(new JLabel("Fréquence"),verticalBox);
        textPanel2=Box.createVerticalBox();
        textPanel2.setBackground(Color.white);
        textPanel2.setOpaque(true);
        
        // Le petit JLabel juste en dessous qui contient la fréquence détectée
        frequencyLabel=new JLabel("0 Hz",SwingConstants.CENTER);
        frequencyLabel.setFont(new Font("Trebuchet MS",Font.PLAIN,40)); // Grosse police
        addCenteredPanel(frequencyLabel,textPanel2);
		
		addCenteredPanel(textPanel2,verticalBox);
		
		/* ********************************************************
        Octave
		******************************************************** */
        addCenteredPanel(new JLabel("Octave"),verticalBox);
        textPanel3=Box.createVerticalBox();
        textPanel3.setBackground(Color.white);
        textPanel3.setOpaque(true);
        
        // Le petit JLabel juste en dessous qui contient la fréquence détectée
        octaveLabel=new JLabel("0 ",SwingConstants.CENTER);
        octaveLabel.setFont(new Font("Trebuchet MS",Font.PLAIN,40)); // Grosse police
        addCenteredPanel(octaveLabel,textPanel3);
		
		addCenteredPanel(textPanel3,verticalBox);
		
		/* ********************************************************
        Ecart
		******************************************************** */
        addCenteredPanel(new JLabel("Ecart"),verticalBox);
        textPanel4=Box.createVerticalBox();
        textPanel4.setBackground(Color.white);
        textPanel4.setOpaque(true);
        
        // Le petit JLabel juste en dessous qui contient la fréquence détectée
        ecartLabel=new JLabel("0 ",SwingConstants.CENTER);
        ecartLabel.setFont(new Font("Trebuchet MS",Font.PLAIN,40)); // Grosse police
        addCenteredPanel(ecartLabel,textPanel4);
		
		addCenteredPanel(textPanel4,verticalBox);
		   
	    		
		Box horizontalBox=Box.createHorizontalBox();
		horizontalBox.add(Box.createRigidArea(new Dimension(20,0)));
		horizontalBox.add(verticalBox);
		horizontalBox.add(Box.createRigidArea(new Dimension(20,0)));
				
		add(horizontalBox);
		
				
		//activation lisener
		addWindowListener(this);
		
		// Affichage de la fenÃªtre
        setVisible(true);
    }
    
    private void addCenteredPanel(JComponent panel, Box box) {
        
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        if(panel == textPanel ||panel instanceof JLabel) {
            panel.setMaximumSize(new Dimension(Short.MAX_VALUE,50));
        }
        
        if(panel instanceof JPanel || panel instanceof Box) {
            panel.setBorder(BorderFactory.createLoweredBevelBorder());
        }
        
        box.add(panel);
    }
    @Override
    public void windowDeactivated(WindowEvent w) {
    	FluxEcouter.stopRecording();
    }
	
    @Override
    public void windowActivated(WindowEvent w) {
    	if (Constantes.DEBUG==1) {
    		System.out.println("2-Active Window");
    		System.out.println("2-Début ecoute Lancement du Flux");
    	}
    	ThreadSon=new Thread(FluxEcouter);
    	ThreadSon.start();
    }
    
    @Override
    public void windowClosing(WindowEvent w) {
    	ThreadSon.interrupt();
    }
	
    @Override
    public void windowDeiconified(WindowEvent w) {}
    
    @Override
    public void windowIconified(WindowEvent w) {}
    
    @Override
    public void windowClosed(WindowEvent w) {}
    
    @Override
    public void windowOpened(WindowEvent w) {}
}	
	