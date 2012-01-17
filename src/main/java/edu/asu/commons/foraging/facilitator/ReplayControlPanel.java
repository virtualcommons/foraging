package edu.asu.commons.foraging.facilitator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;

import edu.asu.commons.event.EventChannelFactory;
import edu.asu.commons.event.PersistableEvent;
import edu.asu.commons.experiment.IPersister;
import edu.asu.commons.foraging.conf.RoundConfiguration;
import edu.asu.commons.foraging.conf.ServerConfiguration;
import edu.asu.commons.foraging.event.AddClientEvent;
import edu.asu.commons.foraging.event.FacilitatorEndRoundEvent;
import edu.asu.commons.foraging.event.ResourceAddedEvent;
import edu.asu.commons.foraging.event.ResourcesAddedEvent;
import edu.asu.commons.foraging.model.ClientData;
import edu.asu.commons.foraging.model.GroupDataModel;
import edu.asu.commons.foraging.model.ServerDataModel;


/**
 * $ Id: Exp $
 *  
 *  Basic replay control panel for csan experiment.  Only works for 2D version (and probably broken right now)...
 *
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */

public class ReplayControlPanel extends JPanel {

    private static final long serialVersionUID = -7627740532357289283L;
    
    private ReplayRoundThread replayRoundThread;
    private FacilitatorWindow facilitatorWindow;            
    private JButton playButton;
    private JButton stopButton;
    private JButton nextRoundButton;
    private JButton previousRoundButton;
    private JLabel replayExperimentFilePath = new JLabel();
    private JSlider slider = new JSlider(); 
    private File replayFile;

    private JButton previousFrameButton;

    private JButton nextFrameButton;
    
    public ReplayControlPanel(FacilitatorWindow facilitatorWindow) {
        this.facilitatorWindow = facilitatorWindow;
                
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(getPlayButton());
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(getStopButton());
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(getPreviousRoundButton());
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(getNextRoundButton());        
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(getPreviousFrameButton());
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(getNextFrameButton());        
        buttonPanel.add(Box.createHorizontalGlue());
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(replayExperimentFilePath);
//        add(slider);
        add(buttonPanel);
    }
    

    /**
     * Initializes the replay control panel with a directory containing a persisted experiment run.
     * @param replayFilePath
     */
    public void init(File replayFilePath, IPersister<ServerConfiguration, RoundConfiguration> persister) {
        this.replayFile = replayFilePath;
        replayExperimentFilePath.setText(replayFile.getAbsolutePath()); 
        replayRoundThread = new ReplayRoundThread(persister);
        slider.setValue(0);
        getPreviousRoundButton().setEnabled(! isFirstRound());
        getNextRoundButton().setEnabled( ! isLastRound() );
        getNextFrameButton().setEnabled(true);
        getPreviousFrameButton().setEnabled(false);
    }

    private JButton getNextFrameButton() {
        if (nextFrameButton == null) {
            nextFrameButton = new JButton("Next frame");
            nextFrameButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (replayRoundThread.playing) {
                        replayRoundThread.nextFrame();
                    }
                    else {
                        playNextRound();
                        replayRoundThread.nextFrame();
                    }
                }
            });
        }
        return nextFrameButton;
    }

    private JButton getPreviousFrameButton() {
        if (previousFrameButton == null) {
            previousFrameButton = new JButton("Previous frame");
            previousFrameButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    replayRoundThread.previousFrame();
                }
            });
        }
        return previousFrameButton;
    }

    private JButton getNextRoundButton() {
        if (nextRoundButton == null) {
            nextRoundButton = new JButton("Next round");
            nextRoundButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    replayRoundThread.haltThread();
                    playNextRound();
                }
            });
        }
        return nextRoundButton;
    }

    private JButton getPreviousRoundButton() {
        if (previousRoundButton == null) {
            previousRoundButton = new JButton("Previous round");
            previousRoundButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    replayRoundThread.haltThread();
                    playPreviousRound();
                }
                
            });
        }
        return previousRoundButton;
    }


    private JButton getPlayButton() {
        if (playButton == null) {
            playButton = new JButton("Play");
            ActionListener playButtonListener = new ActionListener() {
                public void actionPerformed(ActionEvent event) {                    
                    try {                        
                        if (replayFile == null || replayFile.equals("")) {
                            JOptionPane.showMessageDialog(null,
                                    "Please select a valid save file.");
                            return;
                        }
                        
                        if ("Pause".equals(playButton.getText())) {
                            // replay
                            replayRoundThread.pause();
                            return;
                        } else if ("Play".equals(playButton.getText())) {                            
                            replayRoundThread.play();
                            playButton.setText("Pause");
                            return;
                        }
                        
                    } catch (Exception e) {
                        e.printStackTrace();
                        // oh well
                        JOptionPane.showMessageDialog(null, e.getMessage());
                    }
                }
            };
            playButton.addActionListener(playButtonListener);
        }
        return playButton;
    }
    
    private JButton getStopButton() {
        if (stopButton == null) {
            stopButton = new JButton("Stop");
            ActionListener stopButtonListener = new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    replayRoundThread.stop();
                    playButton.setText("Play");
                }
            };
            stopButton.addActionListener(stopButtonListener);
        }
        return stopButton;
    }
    
    private void playNextRound() {
        facilitatorWindow.getFacilitator().getServerConfiguration().nextRound();
        playRound();
    }
    
    private void playPreviousRound() {
        // FIXME: implement previousRound
//        facilitatorWindow.getFacilitator().getRoundConfiguration().previousRound();
        playRound();
    }
    private void playRound() {
        facilitatorWindow.initializeReplay();
        replayRoundThread.play();
    }
    
    private boolean isLastRound() {
        ServerConfiguration configuration = facilitatorWindow.getFacilitator().getServerConfiguration();
        return configuration.isLastRound();
    }

    private boolean isFirstRound() {
        ServerConfiguration configuration = facilitatorWindow.getFacilitator().getServerConfiguration();
        return configuration.getCurrentRoundNumber() == 0;
    }
    
    private class ReplayRoundThread {

        private static final int DEFAULT_STEP_SIZE = 100;
        private final List<PersistableEvent> actions;
        private final ServerDataModel serverDataModel;
        private Thread saveGameViewerThread;
        private ListIterator<PersistableEvent> actionsIterator;
        private volatile boolean playing;
        private volatile boolean shouldBlock;
        private volatile long offset;
        private volatile long pauseTime;
        private volatile boolean abortedRound;
        
        private volatile PersistableEvent currentPersistableEvent;

        public ReplayRoundThread(IPersister<ServerConfiguration, RoundConfiguration> persister) {
            this.actions = new ArrayList<PersistableEvent>(persister.getActions());
            // FIXME: provide data model restore functionality in persister
//            this.serverDataModel = persister.restoreInitialGameState();
            this.serverDataModel = new ServerDataModel(EventChannelFactory.create());
            facilitatorWindow.getFacilitator().setServerDataModel(serverDataModel);
        }
        
        public void nextFrame() {
            pause();
            step(DEFAULT_STEP_SIZE);
        }

        public void previousFrame() {
            pause();
            reverseStep(DEFAULT_STEP_SIZE);
        }



        public void stop() {
            haltThread();
//            facilitatorWindow.getFacilitator().getServerConfiguration().resetExperimentRoundConfiguration();
            serverDataModel.clear();
            actions.clear();
            facilitatorWindow.initializeReplay();
        }
        
        public void haltThread() {
            if (playing) {
                playing = false;
                abortedRound = true;
            }
        }

        public void pause() {
            getPlayButton().setText("Play");
            if (playing && !shouldBlock) {
                shouldBlock = true;
                // take into account the amount of time we've been paused so
                // that when we resume we won't jump into the future all of a sudden.
                pauseTime = System.currentTimeMillis();
                saveGameViewerThread.interrupt();
            }
        }

        public void play() {
            if (playing) {
                shouldBlock = false;
                synchronized (saveGameViewerThread) {
                    saveGameViewerThread.notifyAll();
                }
                // add the pause time elapsed to the difference between now and then 
                // we have to take into consideration this elapsed time so that future
                // events remain in the future.
                offset += (System.currentTimeMillis() - pauseTime);
                return;
            }
            startSaveGameViewerThread();
        }

        private void step(int duration) {
            long timeLeft = 0;
            if (currentPersistableEvent == null) {
                currentPersistableEvent = actionsIterator.next();
            }
            long initialTime = currentPersistableEvent.getCreationTime();
            while (timeLeft < duration) {
                serverDataModel.apply(currentPersistableEvent);
                if (!actionsIterator.hasNext()) {
                    getNextFrameButton().setEnabled(false);
                    saveGameViewerThread.interrupt();
                    break;
                }
                currentPersistableEvent = actionsIterator.next();
                timeLeft = currentPersistableEvent.getCreationTime() - initialTime; 
            }
            getPreviousFrameButton().setEnabled(true);
            facilitatorWindow.repaint();
        }
        
        // FIXME: oh goodness
        // FIXME: deepali must clean this method up.
        private void reverseStep(int duration) {
            long timeLeft = 0;
            if (actionsIterator.hasPrevious()) {
                PersistableEvent previous = actionsIterator.previous();
                if (previous.equals(currentPersistableEvent)) {
                    if (actionsIterator.hasPrevious()) {
                        currentPersistableEvent = actionsIterator.previous();
                    }
                    else {
                        getPreviousFrameButton().setEnabled(false);
                        return;
                    }
                }
                else {
                    currentPersistableEvent = previous;
                }
            }
            else {
                getPreviousFrameButton().setEnabled(false);
                return;
            }
            long initialTime = currentPersistableEvent.getCreationTime();
            while (timeLeft < duration) {
                serverDataModel.unapply(currentPersistableEvent);
                if (actionsIterator.hasPrevious()) {
                    currentPersistableEvent = actionsIterator.previous();
                    timeLeft = initialTime - currentPersistableEvent.getCreationTime();
                }
                else {
                    getPreviousFrameButton().setEnabled(false);
                    facilitatorWindow.repaint();
                    currentPersistableEvent = actionsIterator.next();
                    return;
                }
            }
            // we looked too far back, advance the current persistable event pointer again to
            // maintain the invariant that currentPersistableEvent is always the next-event-to-be-applied
            actionsIterator.next();
            currentPersistableEvent = actionsIterator.next();

            facilitatorWindow.repaint();
        }

        private void startSaveGameViewerThread() {
            // ensure that we aren't already running a save game viewer thread.
            if (playing) {
                return;
            }
            getNextFrameButton().setEnabled(true);
            getPreviousFrameButton().setEnabled(true);
            playing = true;
            for (Iterator<PersistableEvent> iter = actions.iterator(); iter.hasNext(); ) {
                PersistableEvent event = iter.next();
                if (event instanceof AddClientEvent) {
                    System.err.println("XXX: adding client: " + event);
                    AddClientEvent ace = (AddClientEvent) event;
                    ClientData clientData = ace.getClientData();
                    GroupDataModel group = ace.getGroup();
                    group.setServerDataModel(serverDataModel);
                    serverDataModel.addClientToGroup(clientData, group);
                    // XXX: this must occur after we add the client to the group because group.addClient sets
                    // the position as well according to the spacing algorithm.
                    clientData.setPosition(ace.getPosition());
                }
            }
            for (GroupDataModel group: serverDataModel.getGroups()) {
                for (ClientData data: group.getClientDataMap().values()) {
//                    data.setConfiguration(serverDataModel.getRoundConfiguration());
                    data.initializePosition();

                }
            }
            actionsIterator = actions.listIterator();
            while (actionsIterator.hasNext()) {
                PersistableEvent event = actionsIterator.next();
//                System.err.println("skipping past spurious explicit collection mode requests: " + event);
//                if (event instanceof FoodAddedEvent) {
//                    break;
//                }
                if (event instanceof ResourceAddedEvent || event instanceof ResourcesAddedEvent) {
                    break;
                }
            }
            actionsIterator.previous();
            facilitatorWindow.initializeReplayRound();
            saveGameViewerThread = new Thread() {
                public void run() {
                    // check to see if this is the first time through (null
                    // actionsIterator) or if we've already completed one run and
                    // are replaying (i.e., actionsIterator is not null but is
                    // empty)
                    currentPersistableEvent = actionsIterator.next();
                    offset = getTimeOffset(currentPersistableEvent);
                    final long endTime = System.currentTimeMillis() + 240000L;
                    long timeLeft = 0;
                    // the basic algorithm here is:
                    // 1. start at time offset t0, the creation time of the first
                    // PersistableEvent
                    // 2. calculate the time left before the next PersistableEvent
                    // would have happened
                    // 3. If the time left is under some threshold T', keep
                    // iterating through the persistable events and applying them
                    // to the server game state.
                    // 4. If the time left is above the threshold, we sleep for
                    // that amount of time, when the thread wakes up we should be
                    // 'ready' to process the next persistable event.
                    synchronized (this) {
                        while (playing) {
                            if (timeLeft > 100) {
                                // repaint the viewer and then sleep until there are
                                // events that require our immediate attention.
                                try {
                                    Thread.sleep(timeLeft);
                                } catch (InterruptedException e) {
                                }
                            }
                            if (shouldBlock) {
                                try {
                                    saveGameViewerThread.wait();
                                } catch (InterruptedException e) {}
                            }
                            serverDataModel.apply(currentPersistableEvent);
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    facilitatorWindow.updateWindow(endTime - System.currentTimeMillis());
                                    facilitatorWindow.repaint();
                                }
                            });
                            if (!actionsIterator.hasNext()) {
                                playing = false;
                                break;
                            }
                            currentPersistableEvent = actionsIterator.next();
                            timeLeft = (currentPersistableEvent.getCreationTime() + offset) - System.currentTimeMillis();
                        }
                    }
                    getPlayButton().setText("Play");
                    getNextFrameButton().setEnabled(false);
                    getPreviousFrameButton().setEnabled(false);
                    // inform the facilitator window that we're done.
                    if (! abortedRound) {
                        FacilitatorEndRoundEvent endRoundEvent = new FacilitatorEndRoundEvent(facilitatorWindow.getFacilitator().getId(), serverDataModel);
                        facilitatorWindow.endRound(endRoundEvent);
                    }
                }


            };
            saveGameViewerThread.start();
        }
        
//        private Map<Identifier, ClientData> getClientDataMap() {
//            Map<Identifier, ClientData> clientDataMap = new HashMap<Identifier, ClientData>();
//            for (GroupDataModel group: serverDataModel.getGroups()) {
//                clientDataMap.putAll(group.getClientDataMap());
//            }
//            return clientDataMap;
//        }

        private long getTimeOffset(PersistableEvent event) {
            // grab the creation time of the very first persistable event to
            // calculate the offset that lets us know how long we should wait
            // before processing the next action.
            if (event == null)
                throw new NullPointerException("null event in action log");
            return System.currentTimeMillis() - event.getCreationTime();
        }
    }
}
