package edu.asu.commons.foraging.visualization.forestry;

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
import edu.asu.commons.foraging.visualization.forestry.vbo.ForestryView;


public class TerrainGenerationDialog extends BasicDialog {	
	private static final long serialVersionUID = -9008524313227763109L;
	
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
	private JTextField peakHeight;
	private JTextField roughnessConstant;
	private JTextField iterations;
	private JTextField textureFilePath;
	private JButton browseButton;
	private JTextField saveFile;
	private JButton saveFileBrowse;
	private JButton generateButton;
	private ForestryView parentView;
	
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
	
	String dataFilePath = System.getProperty("user.dir") + File.separator + "data" + File.separator + "forestry";


	public TerrainGenerationDialog(ForestryView parentView) {
		this.parentView = parentView;
		initDialog();
		parentView.centerChildDialog(this);
		setVisible(true);
		bindUsableComponents(getContentPane());
	}
	
	private void initDialog() {	
        setLayout(new GridLayout(3, 1));        
        
        add(getGroundPlanePanel());
        add(getMaterialPanel());
        add(getParamPanel());           
        
        setTitle("Generate Terrain");
        setSize(550, 350);        
    }

	private JPanel getGroundPlanePanel() {
		JPanel groundPlanePlanel = new JPanel();
		groundPlanePlanel.setBorder(BorderFactory.createEtchedBorder() );
		groundPlanePlanel.setLayout(new GridLayout(5, 7));		
		
		//Panel Label
		groundPlanePlanel.add(new JLabel(""));
		groundPlanePlanel.add(new JLabel(""));
		groundPlanePlanel.add(new JLabel(""));
		groundPlanePlanel.add(new JLabel("Ground Plane"));
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

	private JTextField getTopLeftX() {
		if (topLeftX == null) {
			topLeftX = new JTextField("-256");
		}
		return topLeftX;
	}
	
	private JTextField getTopLeftY() {
		if (topLeftY == null) {
			topLeftY = new JTextField("0");
		}
		return topLeftY;
	}
	
	private JTextField getTopLeftZ() {
		if (topLeftZ == null) {
			topLeftZ = new JTextField("-256");
		}
		return topLeftZ;
	}
	
	private JTextField getBottomLeftX() {
		if (bottomLeftX == null) {
			bottomLeftX = new JTextField("-256");
		}
		return bottomLeftX;		
	}

	private JTextField getBottomLeftY() {
		if (bottomLeftY == null) {
			bottomLeftY = new JTextField("0");
		}
		return bottomLeftY;
	}

	private JTextField getBottomLeftZ() {
		if (bottomLeftZ == null) {
			bottomLeftZ = new JTextField("256");
		}
		return bottomLeftZ;
	}
	
	private JTextField getTopRightX() {
		if (topRightX == null) {
			topRightX = new JTextField("256");
		}
		return topRightX;	
	}

	private JTextField getTopRightY() {
		if (topRightY == null) {
			topRightY = new JTextField("0");
		}
		return topRightY;
	}

	private JTextField getTopRightZ() {
		if (topRightZ == null) {
			topRightZ = new JTextField("-256");
		}
		return topRightZ;
	}
	
	private JTextField getBottomRightX() {
		if (bottomRightX == null) {
			bottomRightX = new JTextField("256");
		}
		return bottomRightX;	
	}

	private JTextField getBottomRightY() {
		if (bottomRightY == null) {
			bottomRightY = new JTextField("0");
		}
		return bottomRightY;	
	}

	private JTextField getBottomRightZ() {
		if (bottomRightZ == null) {
			bottomRightZ = new JTextField("256");
		}
		return bottomRightZ;	
	}

	//Parameters Panel
	private JPanel getParamPanel() {
		JPanel paramPanel = new JPanel();
		paramPanel.setLayout(new GridLayout(6, 3));
				
		paramPanel.add(new JLabel("Texture"));
		paramPanel.add(getTextureFile());
		paramPanel.add(getBrowseButton());
		
		paramPanel.add(new JLabel("Peak Height"));
		paramPanel.add(getPeakHeight());
		paramPanel.add(new JLabel(""));
		
		paramPanel.add(new JLabel("Roughtness Constant"));
		paramPanel.add(getRoughnessConstant());
		paramPanel.add(new JLabel(""));
		
		paramPanel.add(new JLabel("# Iterations"));
		paramPanel.add(getIterations());
		paramPanel.add(new JLabel(""));
		
		paramPanel.add(new JLabel("Save File"));
		paramPanel.add(getSaveFile());
		paramPanel.add(getSaveFileBrowse());
		
		paramPanel.add(new JLabel(""));
		paramPanel.add(getGenerateButton());
		paramPanel.add(new JLabel(""));
				
		return paramPanel;
	}

	private JTextField getTextureFile() {
		if (textureFilePath == null) {			
			textureFilePath = new JTextField(dataFilePath + File.separator + "grass-texture-1.jpg");			
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
	                int returnVal = fileChooser.showOpenDialog(TerrainGenerationDialog.this);
	                if (returnVal == JFileChooser.APPROVE_OPTION) {
	                	getTextureFile().setText(fileChooser.getSelectedFile().getPath());
	                }
				}
			});
		}
		return browseButton;
	}

	private JTextField getIterations() {
		if (iterations == null) {
			iterations = new JTextField("4");			
		}
		return iterations;
	}

	private JTextField getRoughnessConstant() {
		if (roughnessConstant == null) {
			roughnessConstant = new JTextField("1.25");
		}
		return roughnessConstant;
	}

	private JTextField getPeakHeight() {
		if (peakHeight == null) {
			peakHeight = new JTextField("64");	
		}
		return peakHeight;
	}
	
	private JTextField getSaveFile() {
		if (saveFile == null) {
			saveFile = new JTextField(dataFilePath + File.separator + "terrain1.terrain");			
		}
		return saveFile;
	}
	
	private JButton getSaveFileBrowse() {
		if (saveFileBrowse == null) {
			saveFileBrowse = new JButton("Browse...");
			saveFileBrowse.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					BasicFileFilter filter = new BasicFileFilter();
				    filter.addExtension("terrain");				    
				    filter.setDescription("Terrain Files");
				    				    
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setFileFilter(filter);
					fileChooser.setCurrentDirectory(new File(dataFilePath));
	                int returnVal = fileChooser.showOpenDialog(TerrainGenerationDialog.this);
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
					//Create terrain
					parentView.createTerrain(
						new Point3D(Float.valueOf(getTopLeftX().getText()), Float.valueOf(getTopLeftY().getText()), Float.valueOf(getTopLeftZ().getText())),
						new Point3D(Float.valueOf(getBottomLeftX().getText()), Float.valueOf(getBottomLeftY().getText()), Float.valueOf(getBottomLeftZ().getText())),	
						new Point3D(Float.valueOf(getTopRightX().getText()), Float.valueOf(getTopRightY().getText()), Float.valueOf(getTopRightZ().getText())),
						new Point3D(Float.valueOf(getBottomRightX().getText()), Float.valueOf(getBottomRightY().getText()), Float.valueOf(getBottomRightZ().getText())),
						Float.valueOf(getPeakHeight().getText()),
						Float.valueOf(getRoughnessConstant().getText()),
						Integer.valueOf(getIterations().getText()),
						getTextureFile().getText(),
						new RGBA(Float.valueOf(getAmbientR().getText()), Float.valueOf(getAmbientG().getText()), Float.valueOf(getAmbientB().getText()), Float.valueOf(getAmbientAlpha().getText())),
						new RGBA(Float.valueOf(getAmbientR().getText()), Float.valueOf(getAmbientG().getText()), Float.valueOf(getAmbientB().getText()), Float.valueOf(getAmbientAlpha().getText())),
						new RGBA(Float.valueOf(getAmbientR().getText()), Float.valueOf(getAmbientG().getText()), Float.valueOf(getAmbientB().getText()), Float.valueOf(getAmbientAlpha().getText())),
						Float.valueOf(getShininess().getText()));
					
					//Save in the file specified
					String saveFilePath = getSaveFile().getText();
					//if (!saveFilePath.equals("")) parentView.saveTerrain(saveFilePath);
					
					dispose();
				}
			});
		}
		return generateButton;
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
	
	//Ambient
	private JTextField getAmbientR() {
		if (ambientR == null) {
			ambientR = new JTextField("1.0");
		}
		return ambientR;
	}

	private JTextField getAmbientG() {
		if (ambientG == null) {
			ambientG = new JTextField("1.0");
		}
		return ambientG;
	}

	private JTextField getAmbientB() {
		if (ambientB == null) {
			ambientB = new JTextField("1.0");
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
			diffuseG = new JTextField("1.0");
		}
		return diffuseG;
	}

	private JTextField getDiffuseB() {
		if (diffuseB == null) {
			diffuseB = new JTextField("1.0");
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
			specularG = new JTextField("1.0");
		}
		return specularG;
	}

	private JTextField getSpecularB() {
		if (specularB == null) {
			specularB = new JTextField("1.0");
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
