package edu.asu.commons.foraging.ui;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import edu.asu.commons.foraging.client.ClientDataModel;
import edu.asu.commons.foraging.client.ForagingClient;
import edu.asu.commons.foraging.conf.RoundConfiguration;
import edu.asu.commons.foraging.event.EndRoundEvent;
import edu.asu.commons.foraging.event.LockResourceEvent;
import edu.asu.commons.foraging.visualization.GameView3d;
import edu.asu.commons.net.Identifier;

/**
 * $Id$
 * 
 * TODO: fix chat panel layout, scrolling messages cause the chat panel to overwhelm the game window.
 * 
 * @author <a href='Allen.Lee@asu.edu'>Allen Lee</a>, Deepali Bhagvat
 * @version $Revision$
 */

public class GameWindow3D implements GameWindow {
    
    private ForagingClient client;
    private ClientDataModel dataModel;
    
    private JPanel panel;
    // the current center component within the JPanel, either the instructionsView or the gameView
    private Component currentCenterComponent;

    private InstructionsView instructionsView;
//    private GLCapabilityPanel glCapabilityPanel;
    //private ForestryView forestryView;
    private GameView3d gameView;
    
    private JLabel timeLeftLabel;
    private JLabel incomeLabel;
    private JLabel resourcesHarvestedLabel;
    
    private JPanel gameInformationPanel;
    
    private EmbeddedChatPanel chatPanel;

    @Override
    public void showTrustGame() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public static enum VideoCardSupport{SHADER_SUPPORT, VBO_SUPPORT, VERTEX_ARRAY_SUPPORT};
	public static VideoCardSupport featureSupported;
    
    public GameWindow3D(ForagingClient client) {
        this.client = client;
        this.dataModel = client.getDataModel();
        panel = new JPanel(new BorderLayout());
        instructionsView = new InstructionsView();
        currentCenterComponent = instructionsView.getMainPanel(); //getInstructionsScrollPane();
        panel.add(currentCenterComponent, BorderLayout.CENTER);
        
//        glCapabilityPanel = new GLCapabilityPanel();
//        panel.add(glCapabilityPanel, BorderLayout.NORTH);
//        SwingUtilities.invokeLater(new Runnable() {
//            public void run() {
//            	glCapabilityPanel.update();
//            	glCapabilityPanel.requestFocus();
//            }
//        });
        
//        forestryView = new ForestryView();
//        panel.add(forestryView, BorderLayout.NORTH);
//        SwingUtilities.invokeLater(new Runnable() {
//        	public void run() {
//        		forestryView.update();
//        		forestryView.requestFocus();
//        	}
//        });
        
        // set up information panel
        gameInformationPanel = new JPanel();
        gameInformationPanel.setLayout(new BoxLayout(gameInformationPanel, BoxLayout.X_AXIS));
        timeLeftLabel = new JLabel("Round has not started yet.");
        gameInformationPanel.add(timeLeftLabel);
        gameInformationPanel.add(Box.createHorizontalGlue());
        resourcesHarvestedLabel = new JLabel("Trees harvested: ");
        gameInformationPanel.add(resourcesHarvestedLabel);
        gameInformationPanel.add(Box.createHorizontalGlue());
        incomeLabel = new JLabel("Income: " );
        gameInformationPanel.add(incomeLabel);
    }
    
    public void dispose() {
        getChatPanel().stop();
        gameInformationPanel.setVisible(false);
        panel.setVisible(false);
    }

    public JPanel getPanel() {
        return panel;
    }
    
    public void startRound() {
        initializeChatPanel();
        panel.add(chatPanel, BorderLayout.SOUTH);
        gameView = GameView3d.createView(client);
        gameView.setDataModel(getDataModel());
        gameView.initialize();
        instructionsView.removeAgentDesignPanel();	//Remove if still present

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                addCenterComponent(gameView);
                panel.add(gameInformationPanel, BorderLayout.NORTH);
                gameView.update(); 
                gameView.requestFocus();
            }
        });
        
    }
    
    private void initializeChatPanel() {
        getChatPanel().initialize();
    }
    
    public void switchToInstructionsWindow() {
        // dispose of game view
        panel.remove(gameInformationPanel);
        panel.remove(chatPanel);
        addCenterComponent(instructionsView.getMainPanel()); //getInstructionsScrollPane()
    }
    
    private void addCenterComponent(Component newCenterComponent) {
        if (currentCenterComponent.equals(newCenterComponent)) return;
        if (currentCenterComponent != null) {
            currentCenterComponent.setVisible(false);
            panel.remove(currentCenterComponent);
        }
        currentCenterComponent = newCenterComponent;
        panel.add(newCenterComponent, BorderLayout.CENTER);
        newCenterComponent.setVisible(true);
        panel.validate();
    }
    
    public void setInstructions(final String instructions) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                instructionsView.setInstructions(instructions);        
            }
        });        
    }
    
    public void endRound(final EndRoundEvent event) {
        dataModel.setGroupDataModel(event.getGroupDataModel());
        instructionsView.debrief(dataModel, event.isLastRound());
        switchToInstructionsWindow();
    }
    
    private boolean displayAgentDesigner = true;
    // FIXME: place SetConfigurationEvent initialization here.
    public void init() {
        // FIXME: initialize 3d game window
        RoundConfiguration configuration = dataModel.getRoundConfiguration();
//        updateInstructions(configuration.getInstructions());
        String experimentType = configuration.getExperimentType().toString();
        if (displayAgentDesigner) {
            displayAgentDesigner(experimentType);
            instructionsView.setInstructions(configuration.getWelcomeInstructions());
            displayAgentDesigner = false;
        }
        
    }
    
    public void showInstructions() {
        // FIXME: add quiz listeners (in InstructionsView?)
        instructionsView.setInstructions(dataModel.getRoundConfiguration().getInstructions());
    }
    
    public void updateInstructions(final String instructions) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                instructionsView.appendInstructions(instructions);
            }
        });
    }
    
    public void displayAgentDesigner(String visualizationType) {
    	instructionsView.addAgentDesignPanel(visualizationType, client);
    }
    
    public void removeAgentDesigner() {
    	instructionsView.removeAgentDesignPanel();
    }
    
    public EmbeddedChatPanel getChatPanel() {
        if (chatPanel == null) {
            chatPanel = new EmbeddedChatPanel(client);
        }
        return chatPanel;
    }

    public ClientDataModel getDataModel() {
        return dataModel;
    }

    public void update(final long millisLeft) {
        synchronized (getDataModel()) {
            gameView.updateResources();
            gameView.updateAgentPositions();
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                timeLeftLabel.setText("Time left: " + millisLeft / 1000L);
                resourcesHarvestedLabel.setText("Resources harvested: " + dataModel.getCurrentTokens());
                incomeLabel.setText(String.format("Income: $%3.2f", dataModel.getCurrentIncome()));
                gameInformationPanel.repaint();
                gameView.update();
            }
        });
    }

    public void highlightResource(LockResourceEvent event) {
        if (event.isLockOwner()) {
            //Lock successful - highlight this resource
            gameView.highlightResource(event.getResource());
        }   
        else {
        	//Lock unsuccessful - flash the resource
            gameView.flashResource(event.getResource());
        }
    }
    
    public GameView3d getGameView() {
    	return gameView;
    }
    
    public String getChatHandle(Identifier id) {
    	return chatPanel.getChatHandle(id);
    }

    @Override
    public void requestFocusInWindow() {
        getPanel().requestFocusInWindow();
    }
}
