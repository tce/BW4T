package nl.tudelft.bw4t.scenariogui.botstore.gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import nl.tudelft.bw4t.scenariogui.botstore.controller.BotController;

/**
 * Handles actions of the sizeslider
 */
class SizeSlider extends MouseAdapter {
    /**
     * The panel containing the slider.
     */
    private BotEditorPanel view;
    /**
     * Constructor.
     * @param pview The panel containing the slider.
     */
    public SizeSlider(BotEditorPanel pview) {
        this.view = pview;
    }
    
    @Override
    public void mouseReleased(MouseEvent arg0) {
    	BotController currentController = view.getBotController();
    	currentController.setNewBatteryValue(view.getBotSpeed(), view.getBotSize(), view);
    }
}
