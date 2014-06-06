package nl.tudelft.bw4t.scenariogui.botstore.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import nl.tudelft.bw4t.scenariogui.botstore.gui.BotEditor;

/**
 * Handles actions of the cancelbutton
 */
class CancelButton implements ActionListener {
    /**
     * The GUI to be disposed.
     */
    private BotEditor view;

    /**
     * Constructor.
     * @param pview The GUI to be disposed.
     */
    public CancelButton(BotEditor pview) {
        this.view = pview;
    }
    /**
     * Dispose the BotEditor GUI.
     * @param ae the action event causing the method to be invoked.
     */
    public void actionPerformed(ActionEvent ae) {
        view.dispose();
    }
}