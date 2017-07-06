package edu.asu.commons.foraging.ui;

import javax.swing.JPanel;

import edu.asu.commons.foraging.event.EndRoundEvent;

/**
 *
 * Marker interface for 2d and 3d game windows.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 */
public interface GameWindow {

    public void startRound();
    public void endRound(EndRoundEvent event);
    public void init();
    public void dispose();
    public void update(long millisecondsLeft);
    public void showInstructions(boolean summarized);
    public void showTrustGame();
    public JPanel getPanel();
    public void requestFocusInWindow();

}
