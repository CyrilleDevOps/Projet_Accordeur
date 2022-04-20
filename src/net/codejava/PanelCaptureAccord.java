//package net.codejava.fenetre;

//import net.codejava.actions.*;
//import net.codejava.analyse.*;
//import net.codejava.start.Constantes;

import java.io.*;
import java.text.DecimalFormat;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.*;

import javax.sound.sampled.Mixer;
import javax.swing.*;


public class PanelCaptureAccord extends JFrame implements ActionListener,WindowListener {
	
	// Début Declaration
	
	public static int FPS=30; // le nombre d'images par seconde
	public static int FrequenceEchantillonage=44100; //Fréquence d'échantillonnage
	public static int frequence; 
	public static Mixer.Info miAccord; //Audio Device
		
	private Note noteAccord; //la son capté par le micro
    private CaptureSon FluxEcouterAccord; //la fréquence détectée par le micro
    private EcouteMusique AlEcouteDuSonAccord;
    private Thread ThreadAccord; // le flux Audio
		
    private Note[] Accords;
	private int accordsNbNotes=3;
	private int indiceAccord =0;
	private int indiceAccordN =1;
	private String accordAffichage = "";
	private String accordAffichageN1 = "";
	private String accordTypeN1= "";
	private String accordAffichageN2 = "";
	private String accordTypeN2= "";
    private Box textPanel;
    private Box textPanel2;
    private Box textPanel3;
    private Box textPanel4;
    
    private JLabel noteNameLabel, accordLabel, AccordNmoins1, AccordNmoins2;
    private JLabel TypeAccordNmoins1,TypeAccordNmoins2;
    
    protected JButton   boutonRaz; //Bouton Raz

    
    //Fin Déclaration
	
  
    // Ouvre une fenetre pour l'accordeur
    public PanelCaptureAccord() {
    	super("Accordeur");
    	init();
    }
    
    
    @Override
    public void actionPerformed(ActionEvent Evenement) {

    	Object source = Evenement.getSource(); // L'objet qui a appelÃ© l'Ã©vÃ©nement
    	if(source == boutonRaz) {
    		accordAffichage="";
    		indiceAccord = 0;
    		noteNameLabel.setText((noteAccord.getNoteID()!=0) ? noteAccord.GetNomNote() : "Joue une note !");
           	accordLabel.setText(accordAffichage);
           	repaint();
    	} else {    	
	    	//fft=FrequenceEcouter.getFFT();
	    	noteAccord=FluxEcouterAccord.GetNoteFondamentale();
	    	//System.out.println("ecoute Accord  ");
	    	if(noteAccord!=null){
	    		if (Constantes.DEBUG==1) {System.out.println("ecoute Accord  "+indiceAccord);
	    		}
	    		if(noteAccord.getNoteID()>0){
	    			if (Constantes.DEBUG==1) {System.out.println("Accord- Affichage Frequence Fondamentale "+noteAccord.GetNomNote()+"-"+noteAccord.GetEcartNote());
	    			}
	    			
		    		if (indiceAccord==0 ) {		    		
			    		Accords[0]=noteAccord;
			    		indiceAccord=1;
			    		accordAffichage = noteAccord.GetNomNote();
			    	} else {
			    		if (noteAccord.GetNomNote()!=Accords[indiceAccord-1].GetNomNote()) {		    		
				    		Accords[indiceAccord]=noteAccord;
				    		indiceAccord=indiceAccord+1;
				    		accordAffichage = accordAffichage+"/"+noteAccord.GetNomNote();
				    		if (indiceAccord == accordsNbNotes)  {
				    			indiceAccord = 0;
				    			
				    			if (indiceAccordN ==2){
				    				accordAffichageN2 =accordAffichage;
				    				accordTypeN2= noteAccord.getAccordType(Accords);
				    				indiceAccordN=1;
				    			}
				    			if (indiceAccordN ==1){
				    				accordAffichageN2 =accordAffichageN1;
				    				accordTypeN2= accordTypeN1;
				    				accordAffichageN1 =accordAffichage;
				    				accordTypeN1= noteAccord.getAccordType(Accords);
				    				indiceAccordN=2;
				    			}
				    			
				    		}
			    		}
			    	}
		           	noteNameLabel.setText((noteAccord.getNoteID()!=0) ? noteAccord.GetNomNote() : "Joue une note !");
		           	accordLabel.setText(accordAffichage);
		
		           	AccordNmoins1.setText(accordAffichageN1);
		           	AccordNmoins2.setText(accordAffichageN2 );
		           	
		           	TypeAccordNmoins1.setText(accordTypeN1);
		           	TypeAccordNmoins2.setText(accordTypeN2);
		           	
		           	
		            noteNameLabel.setForeground(new Color(0, 202, 0));
		            accordLabel.setForeground(new Color(0, 202, 0));
		            
		            //AccordNmoins1.setForeground(new Color(0, 202, 0));
		            //AccordNmoins2.setForeground(new Color(0, 202, 0));
		            
		            repaint();
	    		}
	    	}
    	}
    }
  
       
    private void init() { //pour Ã©viter de mettre des fonctions surchargeables telles setSize() dans le constructeur
          
    	if (Constantes.DEBUG==1) {System.out.println("1-Mise en ecoute Accord");
    	}
    	
    	// On initialise la frequence
    	FluxEcouterAccord=new CaptureSon(FrequenceEchantillonage,8,FrequenceEchantillonage/8,miAccord,AlEcouteDuSonAccord);
    	Accords=new Son[accordsNbNotes];   	
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
        Accord
		******************************************************** */
        addCenteredPanel(new JLabel("Accord"),verticalBox);
        textPanel2=Box.createVerticalBox();
        textPanel2.setBackground(Color.white);
        textPanel2.setOpaque(true);
        
        // Le petit JLabel juste en dessous qui contient la fréquence détectée
        accordLabel=new JLabel("-",SwingConstants.CENTER);
        accordLabel.setFont(new Font("Trebuchet MS",Font.PLAIN,40)); // Grosse police
        addCenteredPanel(accordLabel,textPanel2);
        
        addCenteredPanel(textPanel2,verticalBox);
				 
		/* ********************************************************
        Accord -1
		******************************************************** */
        addCenteredPanel(new JLabel("Accord n-1"),verticalBox);
        textPanel3=Box.createVerticalBox();
        textPanel3.setBackground(Color.white);
        textPanel3.setOpaque(true);
        
        // Le petit JLabel juste en dessous qui contient la fréquence détectée
        AccordNmoins1=new JLabel("---",SwingConstants.CENTER);
        AccordNmoins1.setFont(new Font("Trebuchet MS",Font.PLAIN,40)); // Grosse police
        addCenteredPanel(AccordNmoins1,textPanel3);
        
     // Le petit JLabel juste en dessous qui contient la fréquence détectée
        TypeAccordNmoins1=new JLabel("-",SwingConstants.CENTER);
        TypeAccordNmoins1.setFont(new Font("Trebuchet MS",Font.PLAIN,10)); // Petite police
        addCenteredPanel(TypeAccordNmoins1,textPanel3);
		
		addCenteredPanel(textPanel3,verticalBox);
		
		/* ********************************************************
        Accord -2
		******************************************************** */
        addCenteredPanel(new JLabel("Accord n-2"),verticalBox);
        textPanel4=Box.createVerticalBox();
        textPanel4.setBackground(Color.white);
        textPanel4.setOpaque(true);
        
        // Le petit JLabel juste en dessous qui contient la fréquence détectée
        AccordNmoins2=new JLabel("---",SwingConstants.CENTER);
        AccordNmoins2.setFont(new Font("Trebuchet MS",Font.PLAIN,40)); // Grosse police
        addCenteredPanel(AccordNmoins2,textPanel4);
        
        // Le petit JLabel juste en dessous qui contient la fréquence détectée
        TypeAccordNmoins2=new JLabel("-",SwingConstants.CENTER);
        TypeAccordNmoins2.setFont(new Font("Trebuchet MS",Font.PLAIN,10)); // Petite police
        addCenteredPanel(TypeAccordNmoins2,textPanel4);
		
		addCenteredPanel(textPanel4,verticalBox);
		
		//**********************
		//Ajout des Bouton Raz
		//**********************
				
		boutonRaz = new JButton();
		boutonRaz.setText("RAZ");
		boutonRaz.setBackground(new Color(10, 144, 10));
		boutonRaz.setForeground(Color.black);
		boutonRaz.addActionListener(this);
		
		addCenteredPanel(boutonRaz,verticalBox);
	    
		
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
    	FluxEcouterAccord.stopRecording();
    }
	
    @Override
    public void windowActivated(WindowEvent w) {
    	if (Constantes.DEBUG==1) {
    		System.out.println("2-Active Window Accord");
    		System.out.println("2-Début ecoute Lancement du Flux Accord");
    	}
    	ThreadAccord=new Thread(FluxEcouterAccord);
    	ThreadAccord.start();
    }
    
    @Override
    public void windowClosing(WindowEvent w) {
    	ThreadAccord.interrupt();
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
	
