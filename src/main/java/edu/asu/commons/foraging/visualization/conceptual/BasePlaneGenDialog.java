package edu.asu.commons.foraging.visualization.conceptual;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import edu.asu.commons.foraging.graphics.Point3D;
import edu.asu.commons.foraging.graphics.RGBA;
import edu.asu.commons.foraging.util.BasicDialog;
import edu.asu.commons.foraging.util.BasicFileFilter;


public class BasePlaneGenDialog extends BasicDialog {

	private static final long serialVersionUID = 2817247106197911950L;

	//Dimension related fields
	private JTextField topLeftX;
	private JTextField topLeftY;
	private JTextField topLeftZ;
	private JTextField bottomLeftX;
	private JTextField bottomLeftY;
	private JTextField bottomLeftZ;
	private JTextField topRightX;
	private JTextField topRightY;
	private JTextField topRightZ;
	private JTextField bottomRightX;
	private JTextField bottomRightY;
	private JTextField bottomRightZ;
	
	//Material related fields
	private JPanel materialPanel;
	private JTextField ambientR;
	private JTextField ambientG;
	private JTextField ambientB;
	private JTextField ambientAlpha;
	private JTextField diffuseR;
	private JTextField diffuseG;
	private JTextField diffuseB;
	private JTextField diffuseAlpha;
	private JTextField specularR;
	private JTextField specularG;
	private JTextField specularB;
	private JTextField specularAlpha;
	private JTextField shininess;
	
	private JTextField textureFilePath;
	private JButton browseButton;
	private JTextField saveFile;
	private JButton saveFileBrowse;
	private JButton generateButton;
	private AbstractView parentView;
	
	String dataFilePath = System.getProperty("user.dir") + File.separator + "data" + File.separator + "abstract";
	
	public BasePlaneGenDialog(AbstractView parentView) {
		this.parentView = parentView;
		initDialog();
		parentView.centerChildDialog(this);
		setVisible(true);
		bindUsableComponents(getContentPane());
	}
	
	private void initDialog() {	
        setLayout(new GridLayout(3, 1));        
        
        add(getBasePlanePanel());
        add(getMaterialPanel());
        add(getParamPanel());
                        
        setTitle("Generate Base Plane");
        setSize(550, 350);        
    }
	
	private JPanel getBasePlanePanel() {
		JPanel groundPlanePlanel = new JPanel();
		groundPlanePlanel.setBorder(BorderFactory.createEtchedBorder() );
		groundPlanePlanel.setLayout(new GridLayout(5, 7));		
		
		//Panel Label
		groundPlanePlanel.add(new JLabel(""));
		groundPlanePlanel.add(new JLabel(""));
		groundPlanePlanel.add(new JLabel(""));
		groundPlanePlanel.add(new JLabel("Dimensions"));
		groundPlanePlanel.add(new JLabel(""));
		groundPlanePlanel.add(new JLabel(""));
		groundPlanePlanel.add(new JLabel(""));
		
		//Top left
		groundPlanePlanel.add(new JLabel("Top Left"));
		groundPlanePlanel.add(new JLabel("x", JLabel.RIGHT));		
		groundPlanePlanel.add(getTopLeftX());
				
		groundPlanePlanel.add(new JLabel("y", JLabel.RIGHT));		
		groundPlanePlanel.add(getTopLeftY());
		
		groundPlanePlanel.add(new JLabel("z", JLabel.RIGHT));		
		groundPlanePlanel.add(getTopLeftZ());
		
		//Bottom Left
		groundPlanePlanel.add(new JLabel("Bottom Left"));
		groundPlanePlanel.add(new JLabel("x", JLabel.RIGHT));		
		groundPlanePlanel.add(getBottomLeftX());
				
		groundPlanePlanel.add(new JLabel("y", JLabel.RIGHT));		
		groundPlanePlanel.add(getBottomLeftY());
		
		groundPlanePlanel.add(new JLabel("z", JLabel.RIGHT));		
		groundPlanePlanel.add(getBottomLeftZ());
		
		//Top Right
		groundPlanePlanel.add(new JLabel("Top Right"));
		groundPlanePlanel.add(new JLabel("x", JLabel.RIGHT));		
		groundPlanePlanel.add(getTopRightX());
				
		groundPlanePlanel.add(new JLabel("y", JLabel.RIGHT));		
		groundPlanePlanel.add(getTopRightY());
		
		groundPlanePlanel.add(new JLabel("z", JLabel.RIGHT));		
		groundPlanePlanel.add(getTopRightZ());
		
		//Bottom Right
		groundPlanePlanel.add(new JLabel("Bottom Right"));
		groundPlanePlanel.add(new JLabel("x", JLabel.RIGHT));		
		groundPlanePlanel.add(getBottomRightX());
				
		groundPlanePlanel.add(new JLabel("y", JLabel.RIGHT));		
		groundPlanePlanel.add(getBottomRightY());
		
		groundPlanePlanel.add(new JLabel("z", JLabel.RIGHT));		
		groundPlanePlanel.add(getBottomRightZ());
		
		return groundPlanePlanel;
	}
	
	private JPanel getMaterialPanel() {
		if (materialPanel == null) {
			materialPanel = new JPanel();
			materialPanel.setBorder(BorderFactory.createEtchedBorder() );
			
			materialPanel.setLayout(new GridLayout(5, 9));
			
			//Label
			materialPanel.add(new JLabel(""));
			materialPanel.add(new JLabel(""));
			materialPanel.add(new JLabel(""));
			materialPanel.add(new JLabel(""));
			materialPanel.add(new JLabel("Material"));
			materialPanel.add(new JLabel(""));
			materialPanel.add(new JLabel(""));
			materialPanel.add(new JLabel(""));
			materialPanel.add(new JLabel(""));
			
			//Ambient
			materialPanel.add(new JLabel("Ambient"));
			materialPanel.add(new JLabel("R", JLabel.RIGHT));			
			materialPanel.add(getAmbientR());
			materialPanel.add(new JLabel("G", JLabel.RIGHT));
			materialPanel.add(getAmbientG());
			materialPanel.add(new JLabel("B", JLabel.RIGHT));
			materialPanel.add(getAmbientB());
			materialPanel.add(new JLabel("Alpha", JLabel.RIGHT));
			materialPanel.add(getAmbientAlpha());
			
			//Diffuse
			materialPanel.add(new JLabel("Diffuse"));
			materialPanel.add(new JLabel("R", JLabel.RIGHT));			
			materialPanel.add(getDiffuseR());
			materialPanel.add(new JLabel("G", JLabel.RIGHT));
			materialPanel.add(getDiffuseG());
			materialPanel.add(new JLabel("B", JLabel.RIGHT));
			materialPanel.add(getDiffuseB());
			materialPanel.add(new JLabel("Alpha", JLabel.RIGHT));
			materialPanel.add(getDiffuseAlpha());
			
			//Specular
			materialPanel.add(new JLabel("Specular"));
			materialPanel.add(new JLabel("R", JLabel.RIGHT));			
			materialPanel.add(getSpecularR());
			materialPanel.add(new JLabel("G", JLabel.RIGHT));
			materialPanel.add(getSpecularG());
			materialPanel.add(new JLabel("B", JLabel.RIGHT));
			materialPanel.add(getSpecularB());
			materialPanel.add(new JLabel("Alpha", JLabel.RIGHT));
			materialPanel.add(getSpecularAlpha());
			
			//Shininess
			materialPanel.add(new JLabel("Shininess"));
			materialPanel.add(new JLabel(""));			
			materialPanel.add(getShininess());
			materialPanel.add(new JLabel(""));
			materialPanel.add(new JLabel(""));
			materialPanel.add(new JLabel(""));
			materialPanel.add(new JLabel(""));
			materialPanel.add(new JLabel(""));
			materialPanel.add(new JLabel(""));
		}
		
		return materialPanel;
	}
	
//	Parameters Panel
	private JPanel getParamPanel() {
		JPanel paramPanel = new JPanel();
		paramPanel.setLayout(new GridLayout(5, 3));
		
		//filler
		paramPanel.add(new JLabel(""));
		paramPanel.add(new JLabel(""));
		paramPanel.add(new JLabel(""));
		
		paramPanel.add(new JLabel("Texture"));
		paramPanel.add(getTextureFile());
		paramPanel.add(getBrowseButton());
				
		paramPanel.add(new JLabel("Save File"));
		paramPanel.add(getSaveFile());
		paramPanel.add(getSaveFileBrowse());
		
		//filler
		paramPanel.add(new JLabel(""));
		paramPanel.add(new JLabel(""));
		paramPanel.add(new JLabel(""));
		
		paramPanel.add(new JLabel(""));
		paramPanel.add(getGenerateButton());
		paramPanel.add(new JLabel(""));
						
		return paramPanel;
	}

	private JTextField getTopLeftX() {
		if (topLeftX == null) {
			topLeftX = new JTextField("-256.0");
		}
		return topLeftX;
	}
	
	private JTextField getTopLeftY() {
		if (topLeftY == null) {
			topLeftY = new JTextField("0.0");
		}
		return topLeftY;
	}
	
	private JTextField getTopLeftZ() {
		if (topLeftZ == null) {
			topLeftZ = new JTextField("-256.0");
		}
		return topLeftZ;
	}
	
	private JTextField getBottomLeftX() {
		if (bottomLeftX == null) {
			bottomLeftX = new JTextField("-256.0");
		}
		return bottomLeftX;		
	}

	private JTextField getBottomLeftY() {
		if (bottomLeftY == null) {
			bottomLeftY = new JTextField("0.0");
		}
		return bottomLeftY;
	}

	private JTextField getBottomLeftZ() {
		if (bottomLeftZ == null) {
			bottomLeftZ = new JTextField("256.0");
		}
		return bottomLeftZ;
	}
	
	private JTextField getTopRightX() {
		if (topRightX == null) {
			topRightX = new JTextField("256.0");
		}
		return topRightX;	
	}

	private JTextField getTopRightY() {
		if (topRightY == null) {
			topRightY = new JTextField("0.0");
		}
		return topRightY;
	}

	private JTextField getTopRightZ() {
		if (topRightZ == null) {
			topRightZ = new JTextField("-256.0");
		}
		return topRightZ;
	}
	
	private JTextField getBottomRightX() {
		if (bottomRightX == null) {
			bottomRightX = new JTextField("256.0");
		}
		return bottomRightX;	
	}

	private JTextField getBottomRightY() {
		if (bottomRightY == null) {
			bottomRightY = new JTextField("0.0");
		}
		return bottomRightY;	
	}

	private JTextField getBottomRightZ() {
		if (bottomRightZ == null) {
			bottomRightZ = new JTextField("256.0");
		}
		return bottomRightZ;	
	}
	
	private JTextField getTextureFile() {
		if (textureFilePath == null) {			
			textureFilePath = new JTextField();			
		}
		return textureFilePath;
	}
	
	private JButton getBrowseButton() {
		if (browseButton == null) {
			browseButton = new JButton("Browse...");
			browseButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					BasicFileFilter filter = new BasicFileFilter();
				    filter.addExtension("jpg");
				    filter.addExtension("gif");
				    filter.setDescription("JPG & GIF Images");
				    				    
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setFileFilter(filter);
					fileChooser.setCurrentDirectory(new File(dataFilePath));
	                int returnVal = fileChooser.showOpenDialog(BasePlaneGenDialog.this);
	                if (returnVal == JFileChooser.APPROVE_OPTION) {
	                	getTextureFile().setText(fileChooser.getSelectedFile().getPath());
	                }
				}
			});
		}
		return browseButton;
	}
	
	private JTextField getSaveFile() {
		if (saveFile == null) {
			saveFile = new JTextField(dataFilePath + File.separator + "BasePlane1.plane");			
		}
		return saveFile;
	}
	
	private JButton getSaveFileBrowse() {
		if (saveFileBrowse == null) {
			saveFileBrowse = new JButton("Browse...");
			saveFileBrowse.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					BasicFileFilter filter = new BasicFileFilter();
				    filter.addExtension("plane");				    
				    filter.setDescription("Plane Files");
				    				    
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setFileFilter(filter);
					fileChooser.setCurrentDirectory(new File(dataFilePath));
	                int returnVal = fileChooser.showOpenDialog(BasePlaneGenDialog.this);
	                if (returnVal == JFileChooser.APPROVE_OPTION) {
	                	getTextureFile().setText(fileChooser.getSelectedFile().getPath());
	                }
				}
			});
		}
		return saveFileBrowse;
	}

	private JButton getGenerateButton() {
		if (generateButton == null) {
			generateButton = new JButton("Generate");
			generateButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//Create Base Plane
					parentView.createBasePlane(
						new Point3D(Float.valueOf(getTopLeftX().getText()), Float.valueOf(getTopLeftY().getText()), Float.valueOf(getTopLeftZ().getText())),
						new Point3D(Float.valueOf(getBottomLeftX().getText()), Float.valueOf(getBottomLeftY().getText()), Float.valueOf(getBottomLeftZ().getText())),	
						new Point3D(Float.valueOf(getTopRightX().getText()), Float.valueOf(getTopRightY().getText()), Float.valueOf(getTopRightZ().getText())),
						new Point3D(Float.valueOf(getBottomRightX().getText()), Float.valueOf(getBottomRightY().getText()), Float.valueOf(getBottomRightZ().getText())),
						//getTextureFile().getText(),
						new RGBA(Float.valueOf(getAmbientR().getText()), Float.valueOf(getAmbientG().getText()), Float.valueOf(getAmbientB().getText()), Float.valueOf(getAmbientAlpha().getText())),
						new RGBA(Float.valueOf(getAmbientR().getText()), Float.valueOf(getAmbientG().getText()), Float.valueOf(getAmbientB().getText()), Float.valueOf(getAmbientAlpha().getText())),
						new RGBA(Float.valueOf(getAmbientR().getText()), Float.valueOf(getAmbientG().getText()), Float.valueOf(getAmbientB().getText()), Float.valueOf(getAmbientAlpha().getText())),
						Float.valueOf(getShininess().getText()));
					
					//Save in the file specified
					String saveFilePath = getSaveFile().getText();
					if (!saveFilePath.equals("")) parentView.saveBasePlane(saveFilePath);
					
					dispose();
				}
			});
		}
		return generateButton;
	}

//	Ambient
	private JTextField getAmbientR() {
		if (ambientR == null) {
			ambientR = new JTextField("1.0");
		}
		return ambientR;
	}

	private JTextField getAmbientG() {
		if (ambientG == null) {
			ambientG = new JTextField("0.75");
		}
		return ambientG;
	}

	private JTextField getAmbientB() {
		if (ambientB == null) {
			ambientB = new JTextField("0.75");
		}
		return ambientB;
	}

	private JTextField getAmbientAlpha() {
		if (ambientAlpha == null) {
			ambientAlpha = new JTextField("1.0");
		}
		return ambientAlpha;
	}
	
	//Diffuse
	private JTextField getDiffuseR() {
		if (diffuseR == null) {
			diffuseR = new JTextField("1.0");
		}
		return diffuseR;
	}

	private JTextField getDiffuseG() {
		if (diffuseG == null) {
			diffuseG = new JTextField("0.75");
		}
		return diffuseG;
	}

	private JTextField getDiffuseB() {
		if (diffuseB == null) {
			diffuseB = new JTextField("0.75");
		}
		return diffuseB;
	}

	private JTextField getDiffuseAlpha() {
		if (diffuseAlpha == null) {
			diffuseAlpha = new JTextField("1.0");
		}
		return diffuseAlpha;
	}
	
	//Specular
	private JTextField getSpecularR() {
		if (specularR == null) {
			specularR = new JTextField("1.0");
		}
		return specularR;
	}

	private JTextField getSpecularG() {
		if (specularG == null) {
			specularG = new JTextField("0.75");
		}
		return specularG;
	}

	private JTextField getSpecularB() {
		if (specularB == null) {
			specularB = new JTextField("0.75");
		}
		return specularB;
	}

	private JTextField getSpecularAlpha() {
		if (specularAlpha == null) {
			specularAlpha = new JTextField("1.0");
		}
		return specularAlpha;
	}
	
	//Shininess
	private JTextField getShininess() {
		if (shininess == null) {
			shininess = new JTextField("12.8");
		}
		return shininess;
	}
}
