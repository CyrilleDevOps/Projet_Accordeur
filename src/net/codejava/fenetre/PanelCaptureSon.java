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
	
	// DÈbut Declaration
	private HeardNote Note; //la note captÈe par le micro
    private CaptureSon FrequenceEcouter; //la frÈquence dÈtectÈe par le micro
    private Thread th;
	
	public static int FPS=30; // le nombre d'images par seconde
	public static int fe=44100; //FrÈquence d'Èchantillonnage
	public static Mixer.Info mi;
	
    private Box textPanel;
    private JLabel noteNameLabel, frequencyLabel;
    //private TunerGraduationsPanel tunerPanel;
    //private FFTPanel fftPanel;
    //private SignalViewerPanel signalPanel;

    //Fin DÈclaration
	
    // Ouvre une fenetre pour l'accordeur
    public PanelCaptureSon() {
        super("Accordeur");
    	init();
    }
    
    
    @Override
    public void actionPerformed(ActionEvent Evenement) {
    	Note=FrequenceEcouter.getMaxNote();
        if(Note!=null) {
            //tunerPanel.Note=Note;
            //noteNameLabel.setText((Note.getFrequency()!=0) ? Note.getName() : "Joue une note !");
        }
            DecimalFormat FormatAffichage= new DecimalFormat("#.##");
            frequencyLabel.setText(FormatAffichage.format(Note.getFrequency())+" Hz");
            //if(FrequenceEcouter.getWindowedData()!=null) signalPanel.data=FrequenceEcouter.getWindowedData();
            
            //repaint();
    }
    
    
    private void init() { //pour √©viter de mettre des fonctions surchargeables telles setSize() dans le constructeur
                
    	Note=new HeardNote(0);
    	FrequenceEcouter=new CaptureSon(fe,8,fe/8,mi);
    	
    	
    	// On set la taille de la fen√™tre et on affiche les bordures
       setSize(500,750);
       setUndecorated(false);
        
        // Un timer pour rafra√Æchir l'affichage
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
		
		// Le JLabel qui contiendra le nom de la note
		noteNameLabel=new JLabel("Joue une note ! ",SwingConstants.CENTER);
		noteNameLabel.setFont(new Font("Trebuchet MS",Font.PLAIN,40)); // Grosse police
		addCenteredPanel(noteNameLabel,textPanel);
		
		// Le petit JLabel juste en dessous qui contient la fr√©quence d√©tect√©e
		
		frequencyLabel=new JLabel("0 Hz",SwingConstants.CENTER);
		frequencyLabel.setFont(new Font("Trebuchet MS",Font.PLAIN,10)); // Petite police
		addCenteredPanel(frequencyLabel,textPanel);
		
		addCenteredPanel(textPanel,verticalBox);
		
		verticalBox.add(Box.createRigidArea(new Dimension(0,20)));
		
		
		
		/* ********************************************************
		         GRADUATIONS
		******************************************************** */
		
		addCenteredPanel(new JLabel("Accordage"),verticalBox);
		// On d√©clare le JPanel Tuner
		
		//tunerPanel=new TunerGraduationsPanel();
		//addCenteredPanel(tunerPanel,verticalBox);
		
		verticalBox.add(Box.createRigidArea(new Dimension(0,20)));
		
		
		
		/* ********************************************************
		         SPECTRE
		******************************************************** */
		addCenteredPanel(new JLabel("Spectre (FFT)"),verticalBox);
		// On d√©clare le JPanel FFTPanel
		//fftPanel=new FFTPanel(fd);
		//addCenteredPanel(fftPanel,verticalBox);
		
		verticalBox.add(Box.createRigidArea(new Dimension(0,20)));
		
		
		/* ********************************************************
        SIGNAL
		******************************************************** */
		addCenteredPanel(new JLabel("Signal d'entr√©e"),verticalBox);
		// On d√©clare le JPanel Signal
		//signalPanel=new SignalViewerPanel();
		//addCenteredPanel(signalPanel,verticalBox);
		
		verticalBox.add(Box.createRigidArea(new Dimension(0,20)));
		
		
		Box horizontalBox=Box.createHorizontalBox();
		horizontalBox.add(Box.createRigidArea(new Dimension(20,0)));
		horizontalBox.add(verticalBox);
		horizontalBox.add(Box.createRigidArea(new Dimension(20,0)));
		
		
		add(horizontalBox);
		
		//addWindowListener(this);
		
		// Affichage de la fen√™tre
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
        th=new Thread(FrequenceEcouter);
        th.start();
    }
    
    @Override
    public void windowClosing(WindowEvent w) {
        th.interrupt();
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
	