//package net.codejava.actions;


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


public class BoutonsActions extends JButton {
	
    /**
     * Crée un PalletButton
     * @param s Texte du bouton
     * @param a ActionListener du bouton
     */
    public BoutonsActions(String TexteBouton, ActionListener ActionBouton) {
        super(TexteBouton);
        init(ActionBouton);
    }
    
    private void init(ActionListener ActionBouton) {
        this.setFont(new Font("Bravura",Font.PLAIN, 20));
        this.setPreferredSize(new Dimension(40,40));
        this.addActionListener(ActionBouton);
    }
    
}

