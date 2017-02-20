package nl.tudelft.bw4t.client.gui.listeners;

import java.awt.event.ActionEvent;

import org.apache.log4j.Logger;

import eis.iilang.Percept;
import nl.tudelft.bw4t.client.controller.ClientController;
import nl.tudelft.bw4t.client.gui.BW4TClientGUI;

public class DropEPartnerActionListener extends AbstractClientActionListener {
    private static final Logger LOGGER = Logger.getLogger(DropEPartnerActionListener.class);
    private final BW4TClientGUI gui;

    public DropEPartnerActionListener(ClientController controller, BW4TClientGUI gui) {
        super(controller);
        this.gui = gui;
    }

    @Override
    protected void actionWithHumanAgent(ActionEvent arg0) {
        try {
            getController().getHumanAgent().putDownEPartner();
            gui.getEpartnerMessageButton().setEnabled(false);
        } catch (Exception e1) {
            LOGGER.warn("failed to put down the e-partner", e1);
        }
    }

    @Override
    protected void actionWithGoalAgent(ActionEvent arg0) {
            Percept percept = new Percept("putDownEPartner");
            getController().addToBePerformedAction(percept);
    }

}
