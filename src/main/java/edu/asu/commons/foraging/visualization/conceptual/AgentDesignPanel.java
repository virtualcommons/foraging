package edu.asu.commons.foraging.visualization.conceptual;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.asu.commons.foraging.client.ForagingClient;
import edu.asu.commons.foraging.graphics.RGBA;

public class AgentDesignPanel extends JPanel {

	private static final long serialVersionUID = -4499096953900771727L;
	private Color color = new Color(0.83f, 0.98f, 0.74f, 1.0f);
	private AgentPreviewView agentPreviewView = null;
	private ForagingClient client;
	
	public AgentDesignPanel(ForagingClient client) {
		super();
		this.client = client;
		
		int labelSpacing = 10;
		int verticalSpacing = 15;
		Dimension buttonSize = new Dimension(100, 20);
		Dimension previewSize = new Dimension(200, 200);
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		JLabel label = new JLabel("Design Your Agent");
		label.setFont(new Font(Font.DIALOG, Font.BOLD, 16));
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(label);
		add(Box.createRigidArea(new Dimension(0, labelSpacing)));
				
		JPanel designPanel = new JPanel();
		designPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		designPanel.setLayout(new BoxLayout(designPanel, BoxLayout.Y_AXIS));
		designPanel.setBorder(BorderFactory.createRaisedBevelBorder());
		add(designPanel);
		
		////////////////////////////////////////////////////////////////
		//Panel displaying controls to select color 
		JPanel controlPanel = new JPanel();
		controlPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.X_AXIS));
		designPanel.add(controlPanel);
		
		controlPanel.add(Box.createRigidArea(new Dimension(0, verticalSpacing)));
		
		//Label
		label = new JLabel("Color ");
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		controlPanel.add(label);
		controlPanel.add(Box.createRigidArea(new Dimension(0, labelSpacing)));
		
		//Color box
		final JButton colorButton = new JButton();
		colorButton.setMinimumSize(buttonSize);
		colorButton.setPreferredSize(buttonSize);
		colorButton.setMaximumSize(buttonSize);
		colorButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		colorButton.setBackground(color);
		colorButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
            	color = showColorDialog(color);
            	colorButton.setBackground(color);
            	agentPreviewView.setAgentColor(new RGBA(color));
            	agentPreviewView.update();
            }
        });
		controlPanel.add(colorButton);
		controlPanel.add(Box.createRigidArea(new Dimension(0, verticalSpacing)));
		
		////////////////////////////////////////////////////////////
		//Panel displaying preview of the agent
		JPanel previewPanel = new JPanel();
		previewPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		previewPanel.setLayout(new BoxLayout(previewPanel, BoxLayout.Y_AXIS));
		designPanel.add(previewPanel);
		
		previewPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Preview") );
		
		agentPreviewView = new AgentPreviewView(this);
		agentPreviewView.setMinimumSize(previewSize);
		agentPreviewView.setPreferredSize(previewSize);
		agentPreviewView.setMaximumSize(previewSize);
		agentPreviewView.setAlignmentX(Component.LEFT_ALIGNMENT);
		agentPreviewView.initialize();
		//agentPreviewView.setAgentColor(new RGBA(color));
		previewPanel.add(agentPreviewView);
		
		////////////////////////////////////////////////////////////////
		JPanel okPanel = new JPanel();
		okPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		okPanel.setLayout(new BoxLayout(okPanel, BoxLayout.X_AXIS));
		designPanel.add(okPanel);
		
		okPanel.add(Box.createRigidArea(new Dimension(70, verticalSpacing)));
		
		//OK button
		final JButton okButton = new JButton("OK");
		okButton.setMinimumSize(buttonSize);
		okButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
            	AgentDesignPanel.this.client.sendAgentInfo(color);
            }
        });
		okPanel.add(okButton);
		okPanel.add(Box.createRigidArea(new Dimension(0, verticalSpacing)));				
	}
	
	public Color showColorDialog(Color defaultColor) {
		Color selectedColor = JColorChooser.showDialog(this, "Select Color", defaultColor);
		if (selectedColor == null)	//User pressed Cancel
			selectedColor = defaultColor;
		
		return selectedColor;
	}

	public Color getColor() {
		return color;
	}
}
