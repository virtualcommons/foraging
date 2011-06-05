package edu.asu.commons.foraging.visualization.forestry;

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
import edu.asu.commons.foraging.visualization.forestry.va.AvatarPreviewView;

public class AvatarDesignPanel extends JPanel {

	private static final long serialVersionUID = -4397984607162159818L;
	private boolean male = true;
	private Color skinColor = new Color(1.0f, 0.77f, 0.75f, 1.0f);
	private Color hairColor = new Color(0.0f, 0.0f, 0.0f, 1.0f);
	private Color shirtColor = new Color(1.0f, 0.0f, 0.0f, 1.0f);
	private Color trouserColor = new Color(0.0f, 0.0f, 1.0f, 1.0f);
	private Color shoesColor = new Color(0.5f, 0.0f, 0.0f, 1.0f);
	private AvatarPreviewView avatarPreviewView = null;
	private ForagingClient client;
	
	public AvatarDesignPanel(ForagingClient client) {
		super();
		this.client = client;
		
		int labelSpacing = 10;
		int verticalSpacing = 6;
		Dimension buttonSize = new Dimension(100, 20);
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		JLabel label = new JLabel("Design Your Avatar");
		label.setFont(new Font(Font.DIALOG, Font.BOLD, 16));
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(label);
		add(Box.createRigidArea(new Dimension(0, labelSpacing)));
				
		JPanel designPanel = new JPanel();
		designPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		designPanel.setLayout(new BoxLayout(designPanel, BoxLayout.X_AXIS));
		designPanel.setBorder(BorderFactory.createRaisedBevelBorder());
		add(designPanel);
		
		JPanel labelPanel = new JPanel();
		designPanel.add(labelPanel);
		labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.Y_AXIS));
		labelPanel.setAlignmentY(Component.TOP_ALIGNMENT);
		
		JPanel optionPanel = new JPanel();
		designPanel.add(optionPanel);
		optionPanel.setLayout(new BoxLayout(optionPanel, BoxLayout.Y_AXIS));
		optionPanel.setAlignmentY(Component.TOP_ALIGNMENT);
		
		//Panel displaying preview of the agent
		JPanel previewPanel = new JPanel();
		designPanel.add(previewPanel);
		previewPanel.setAlignmentY(Component.TOP_ALIGNMENT);		
		previewPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Preview") );
		
		labelPanel.add(Box.createRigidArea(new Dimension(0, 100)));
		
/*		label = new JLabel("Gender ");
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		labelPanel.add(label);
		labelPanel.add(Box.createRigidArea(new Dimension(0, labelSpacing)));
*/				
		label = new JLabel("Skin Color ");
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		labelPanel.add(label);
		labelPanel.add(Box.createRigidArea(new Dimension(0, labelSpacing)));
		
/*		label = new JLabel("Hair Color ");
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		labelPanel.add(label);
		labelPanel.add(Box.createRigidArea(new Dimension(0, labelSpacing)));
*/	
		label = new JLabel("Shirt Color ");
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		labelPanel.add(label);
		labelPanel.add(Box.createRigidArea(new Dimension(0, labelSpacing)));
		
		label = new JLabel("Trouser Color ");
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		labelPanel.add(label);
		labelPanel.add(Box.createRigidArea(new Dimension(0, labelSpacing)));
		
		label = new JLabel("Shoes Color ");
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		labelPanel.add(label);
		labelPanel.add(Box.createRigidArea(new Dimension(0, labelSpacing)));
		
		optionPanel.add(Box.createRigidArea(new Dimension(0, 100)));
		
/*		//Gender
		JPanel radioButtonPanel = new JPanel();
		radioButtonPanel.setLayout(new BoxLayout(radioButtonPanel, BoxLayout.X_AXIS));
		radioButtonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		final JRadioButton maleRadioButton = new JRadioButton("Male");
		maleRadioButton.setSelected(male);
		JRadioButton femaleRadioButton = new JRadioButton("Female");
		ButtonGroup genderButtonsGroup = new ButtonGroup();
		genderButtonsGroup.add(maleRadioButton);
		genderButtonsGroup.add(femaleRadioButton);
		radioButtonPanel.add(maleRadioButton);
		radioButtonPanel.add(femaleRadioButton);
		optionPanel.add(radioButtonPanel);
		optionPanel.add(Box.createRigidArea(new Dimension(0, verticalSpacing)));
*/		
		//Skin color
		final JButton skinColorButton = new JButton();
		skinColorButton.setMinimumSize(buttonSize);
		skinColorButton.setPreferredSize(buttonSize);
		skinColorButton.setMaximumSize(buttonSize);
		skinColorButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		skinColorButton.setBackground(skinColor);
		skinColorButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
            	skinColor = showColorDialog(skinColor);
            	skinColorButton.setBackground(skinColor);
            	avatarPreviewView.setSkinColor(skinColor);
            	avatarPreviewView.update();
            }
        });
		optionPanel.add(skinColorButton);
		optionPanel.add(Box.createRigidArea(new Dimension(0, verticalSpacing)));
		
/*		//Hair color
		final JButton hairColorButton = new JButton();
		hairColorButton.setMinimumSize(buttonSize);
		hairColorButton.setPreferredSize(buttonSize);
		hairColorButton.setMaximumSize(buttonSize);
		hairColorButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		hairColorButton.setBackground(hairColor);
		hairColorButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
            	hairColor = showColorDialog(hairColor);
            	hairColorButton.setBackground(hairColor);
//            	avatarPreviewView.setHairColor(hairColor);
//            	avatarPreviewView.update();
            }
        });
		optionPanel.add(hairColorButton);
		optionPanel.add(Box.createRigidArea(new Dimension(0, verticalSpacing)));
*/		
		//Shirt color
		final JButton shirtColorButton = new JButton();
		shirtColorButton.setMinimumSize(buttonSize);
		shirtColorButton.setPreferredSize(buttonSize);
		shirtColorButton.setMaximumSize(buttonSize);
		shirtColorButton.setBackground(shirtColor);
		shirtColorButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
            	shirtColor = showColorDialog(shirtColor);
            	shirtColorButton.setBackground(shirtColor);
            	avatarPreviewView.setShirtColor(shirtColor);
            	avatarPreviewView.update();
            }
        });
		optionPanel.add(shirtColorButton);
		optionPanel.add(Box.createRigidArea(new Dimension(0, verticalSpacing)));
		
		//Trouser color
		final JButton trouserColorButton = new JButton();
		trouserColorButton.setMinimumSize(buttonSize);
		trouserColorButton.setPreferredSize(buttonSize);
		trouserColorButton.setMaximumSize(buttonSize);
		trouserColorButton.setBackground(trouserColor);
		trouserColorButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
            	trouserColor = showColorDialog(trouserColor);
            	trouserColorButton.setBackground(trouserColor);
            	avatarPreviewView.setTrouserColor(trouserColor);
            	avatarPreviewView.update();
            }
        });
		optionPanel.add(trouserColorButton);
		optionPanel.add(Box.createRigidArea(new Dimension(0, verticalSpacing)));
		
		//Shoes color
		final JButton shoesColorButton = new JButton();
		shoesColorButton.setMinimumSize(buttonSize);
		shoesColorButton.setPreferredSize(buttonSize);
		shoesColorButton.setMaximumSize(buttonSize);
		shoesColorButton.setBackground(shoesColor);
		shoesColorButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
            	shoesColorButton.setBackground(shoesColor);
            	shoesColor = showColorDialog(shoesColor);
            	shoesColorButton.setBackground(shoesColor);
            	avatarPreviewView.setShoesColor(shoesColor);
            	avatarPreviewView.update();
            }
        });
		optionPanel.add(shoesColorButton);
		optionPanel.add(Box.createRigidArea(new Dimension(0, verticalSpacing)));
		
		//OK button
		final JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
//            	male = maleRadioButton.isSelected() ? true : false;
            	AvatarDesignPanel.this.client.sendAvatarInfo(male, hairColor, skinColor, shirtColor, trouserColor, shoesColor);
            }
        });
		optionPanel.add(okButton);
		
		//Preview
		Dimension previewSize = new Dimension(400, 400);
		avatarPreviewView = new AvatarPreviewView(this);
		avatarPreviewView.setMinimumSize(previewSize);
		avatarPreviewView.setPreferredSize(previewSize);
		avatarPreviewView.setMaximumSize(previewSize);
		avatarPreviewView.setAlignmentX(Component.LEFT_ALIGNMENT);
		avatarPreviewView.initialize();
		previewPanel.add(avatarPreviewView);		
	}
	
	public Color showColorDialog(Color defaultColor) {
		Color selectedColor = JColorChooser.showDialog(this, "Select Color", defaultColor);
		if (selectedColor == null)	//User pressed Cancel
			selectedColor = defaultColor;
		
		return selectedColor;
	}

	public boolean isMale() {
		return male;
	}

	public Color getSkinColor() {
		return skinColor;
	}

	public Color getHairColor() {
		return hairColor;
	}

	public Color getShirtColor() {
		return shirtColor;
	}

	public Color getTrouserColor() {
		return trouserColor;
	}

	public Color getShoesColor() {
		return shoesColor;
	}
	
	
}
