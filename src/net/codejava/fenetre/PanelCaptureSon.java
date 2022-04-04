package net.codejava.fenetre;

import net.codejava.actions.*;
import net.codejava.analyse.*;

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
	
	private Son note; //la son captée par le micro
    private CaptureSon FrequenceEcouter; //la fréquence détectée par le micro
    private Thread ThreadSon;
	
	public static int FPS=30; // le nombre d'images par seconde
	public static int fe=44100; //Fréquence d'échantillonnage
	public static Mixer.Info mi;
	
	private double[] fft;
	
    private Box textPanel;
    
    private JLabel noteNameLabel, frequencyLabel;

    
    //Fin Déclaration
	
    // Ouvre une fenetre pour l'accordeur
    public PanelCaptureSon() {
        super("Accordeur");
    	init();
    }
    
    
    @Override
    public void actionPerformed(ActionEvent Evenement) {
    	fft=FrequenceEcouter.getFFT();
    	//fft=FrequenceEcouter.averageFFT();
    	note=FrequenceEcouter.getMaxNote();
    	//System.out.println("ecoute");
    	//if(note!=null) {
    	if(fft!=null) {
           	//System.out.println(note.getFrequency());
           	           	
           	noteNameLabel.setText(fft+" Hz");
           	//noteNameLabel.setText((note.getFrequency()!=0) ? note.getSilence() : "Joue une note !");
           	DecimalFormat FormatAffichageFrequence= new DecimalFormat("#.##");
            //frequencyLabel.setText(FormatAffichageFrequence.format(note.getFrequency())+" Hz");
           	frequencyLabel.setText(fft+" Hz");
            noteNameLabel.setForeground(new Color(0, 202, 0));
            frequencyLabel.setForeground(new Color(0, 202, 0));
            
            repaint();
            
        }

    }
    
    
    private void init() { //pour Ã©viter de mettre des fonctions surchargeables telles setSize() dans le constructeur
          
    	System.out.println("Init Accordeur");
    	
    	// On initialise la fequence
    	FrequenceEcouter=new CaptureSon(fe,8,fe/8,mi);
    	    	
    	// On set la taille de la fenÃªtre et on affiche les bordures
       setSize(400,300);
       setUndecorated(false);
        
        // Un timer pour rafraÃ®chir l'affichage
        Timer timer = new Timer(1000/FPS, this);
        timer.start();
        
        Box verticalBox=Box.createVerticalBox();
        verticalBox.add(Box.createRigidArea(new Dimension(0,20)));
        
        /* ********************************************************
        NOTE ET FREQUENCE
		******************************************************** */
		
		addCenteredPanel(new JLabel("Note"),verticalBox);
		textPanel=Box.createVerticalBox();
		textPanel.setBackground(Color.white);
		textPanel.setOpaque(true);
		
		// Le JLabel qui indiquera si il y a capture de on ou un silence
		noteNameLabel=new JLabel("Joue une note ! ",SwingConstants.CENTER);
		noteNameLabel.setFont(new Font("Trebuchet MS",Font.PLAIN,40)); // Grosse police
		addCenteredPanel(noteNameLabel,textPanel);
		
		// Le petit JLabel juste en dessous qui contient la frÃ©quence dÃ©tectÃ©e
		frequencyLabel=new JLabel("0 Hz",SwingConstants.CENTER);
		frequencyLabel.setFont(new Font("Trebuchet MS",Font.PLAIN,10)); // Petite police
		addCenteredPanel(frequencyLabel,textPanel);
		
		addCenteredPanel(textPanel,verticalBox);
		   
	    		
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
    	FrequenceEcouter.stopRecording();
    }
	
    @Override
    public void windowActivated(WindowEvent w) {
    	System.out.println("Active Window");
    	ThreadSon=new Thread(FrequenceEcouter);
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
	