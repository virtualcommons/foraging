package edu.asu.commons.foraging.client;

import javax.swing.JPanel;

import edu.asu.commons.foraging.event.EndRoundEvent;

/**
 * $Id: GameWindow.java 416 2009-12-25 05:17:14Z alllee $
 * 
 * Marker interface for both 2d and 3d game windows.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision: 416 $
 */
public interface GameWindow {
    
    public void startRound();
    public void endRound(EndRoundEvent event);
    public void init();
    public void update(long millisecondsLeft);
    public void showInstructions();
    public void showTrustGame();
    public JPanel getPanel();
    public void requestFocusInWindow();

}
