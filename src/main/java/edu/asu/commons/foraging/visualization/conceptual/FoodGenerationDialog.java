package edu.asu.commons.foraging.visualization.conceptual;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;

import edu.asu.commons.foraging.graphics.RGBA;
import edu.asu.commons.foraging.graphics.TextureLoader;
import edu.asu.commons.foraging.util.BasicDialog;
import edu.asu.commons.foraging.util.BasicFileFilter;



public class FoodGenerationDialog extends BasicDialog {

	private static final long serialVersionUID = -6282845580678517329L;
	AbstractView parentView;
	String dataFilePath = System.getProperty("user.dir") + File.separator + "data" + File.separator + "abstract";
	
	/** Creates new form TreeGenerationDialog */
    public FoodGenerationDialog(AbstractView parentView) {
    	this.parentView = parentView;
        initComponents();
        setSize(480, 280);
        parentView.centerChildDialog(this);
        setVisible(true);
        bindUsableComponents(getContentPane());
    }
    
    private void initComponents() {
    	getContentPane().setLayout(null);        
        setTitle("Generate Pillars");
        
        //Trunk Base Radius
        JLabel jLabel1 = new JLabel("Radius");        
        getContentPane().add(jLabel1);
        jLabel1.setBounds(10, 10, 110, 20);                
        getContentPane().add(getRadius());        
                
        //Trunk Top Radius
        JLabel jLabel2 = new JLabel("Initial Height");        
        getContentPane().add(jLabel2);
        jLabel2.setBounds(10, 40, 110, 20);              
        getContentPane().add(getInitialHeight());
        
        //Trunk Length
        JLabel jLabel3 = new JLabel("Height Increment");        
        getContentPane().add(jLabel3);
        jLabel3.setBounds(10, 70, 110, 20);              
        getContentPane().add(getHeightIncrement());
        
        //Iterations
        JLabel jLabel4 = new JLabel("Iterations");        
        getContentPane().add(jLabel4);
        jLabel4.setBounds(10, 100, 110, 20);              
        getContentPane().add(getIterations());
        
        //Texture File
        JLabel jLabel5 = new JLabel("Texture File");        
        getContentPane().add(jLabel5);
        jLabel5.setBounds(10, 130, 110, 20);        
        getContentPane().add(getTextureFile());
        getContentPane().add(getBrowseButton());
        
        //Save File location
        JLabel jLabel6 = new JLabel("Save File");        
        getContentPane().add(jLabel6);
        jLabel6.setBounds(10, 160, 110, 20);        
        getContentPane().add(getSaveFile());
        getContentPane().add(getSaveBrowseButton());
                
        //Generate button
        getContentPane().add(getGenerate());
        
        pack();
    }
    
    private JTextField getRadius() {
    	if (radius == null){
    		radius = new JTextField("8.0");
    		radius.setBounds(130, 10, 70, 20);    		
    	}
    	return radius;
    }
    
    private JTextField getInitialHeight() {
    	if (initialHeight == null){
    		initialHeight = new JTextField("0");
    		initialHeight.setBounds(130, 40, 70, 20);   		
    	}
    	return initialHeight;
    }
    
    private JTextField getHeightIncrement() {
    	if (heightIncrement == null){
    		heightIncrement = new JTextField("6");
    		heightIncrement.setBounds(130, 70, 70, 20);    		
    	}
    	return heightIncrement;
    }
    
    private JTextField getIterations() {
    	if (iterations == null){
    		iterations = new JTextField("6");
    		iterations.setBounds(130, 100, 70, 20);    		
    	}
    	return iterations;
    }
    
    private JTextField getTextureFile() {
    	if (textureFile == null){
    		textureFile = new JTextField();
    		textureFile.setBounds(130, 130, 240, 20);    		
    	}
    	return textureFile;
    }
    
    private JButton getBrowseButton() {
    	if (browse == null) {  
    		browse = new JButton("Browse");    		
    		browse.setBounds(380, 130, 79, 23);
    		
    		browse.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					BasicFileFilter filter = new BasicFileFilter();
				    filter.addExtension("jpg");
				    filter.addExtension("gif");
				    filter.setDescription("Image Files");
				    				    
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setFileFilter(filter);
					fileChooser.setCurrentDirectory(new File(dataFilePath));
	                int returnVal = fileChooser.showOpenDialog(FoodGenerationDialog.this);
	                if (returnVal == JFileChooser.APPROVE_OPTION) {
	                	getTextureFile().setText(fileChooser.getSelectedFile().getPath());
	                }
				}
			});
    	}
    	return browse;
    }
    
    private JTextField getSaveFile() {
    	if (saveFile == null){
    		saveFile = new JTextField(dataFilePath + File.separator + "pillars1.pillar");
    		saveFile.setBounds(130, 160, 240, 20);    		
    	}
    	return saveFile;
    }
    
    private JButton getSaveBrowseButton() {
    	if (saveFileBrowse == null) {  
    		saveFileBrowse = new JButton("Browse");    		
    		saveFileBrowse.setBounds(380, 160, 79, 23);
    		
    		saveFileBrowse.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					BasicFileFilter filter = new BasicFileFilter();
				    filter.addExtension("tree");				    
				    filter.setDescription("Tree Files");
				    				    
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setFileFilter(filter);
					fileChooser.setCurrentDirectory(new File(dataFilePath));
	                int returnVal = fileChooser.showOpenDialog(FoodGenerationDialog.this);
	                if (returnVal == JFileChooser.APPROVE_OPTION) {
	                	getTextureFile().setText(fileChooser.getSelectedFile().getPath());
	                }
				}
			});
    	}
    	return saveFileBrowse;
    }
       
    private JButton getGenerate() {
    	if (generate == null) {
    		generate = new JButton("Generate");
    		generate.setBounds(130, 190, 110, 23);
    		
    		generate.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Food.radius = Float.valueOf(getRadius().getText());
					Food.initialHeight = Float.valueOf(getInitialHeight().getText());
					Food.heightIncrement = Float.valueOf(getHeightIncrement().getText());
					Food.maxAge = Integer.valueOf(getIterations().getText());
					//TODO: Add fields for these values
					Food.oldAge = 5;
					Food.color = new RGBA(0.5f, 0, 1, 1);
					Food.oldAgeColor = new RGBA(1, 1, 0, 1);
					Food.selectedColor = new RGBA(0, 1, 0, 1);
					Food.specular = new RGBA(1, 1, 1, 1);
					TextureLoader texLoader = new TextureLoader();
					
					String textureFile = getTextureFile().getText();					
					if (!textureFile.equals("")) {					
						Food.texture = texLoader.getTexture(textureFile, true);
					}
					parentView.createFood();
					
					//Also load coin texture				
					//String coinTextureFile = dataFilePath + File.separator + "coins.jpg";
					//Food.coinTexture = texLoader.getTexture(coinTextureFile, true);
					
					//Save the tree object in a file
					String saveFile = getSaveFile().getText();
					if (!saveFile.equals("")) parentView.saveFood(saveFile); 
						
					dispose();
					
				}
			});
    	}
    	return generate;
    }
   
    private JTextField radius;
    private JTextField initialHeight;
    private JTextField heightIncrement;
    private JTextField iterations;
    private JTextField textureFile;
    private JButton browse;
    private JTextField saveFile;
    private JButton saveFileBrowse;
    private JButton generate;
}
