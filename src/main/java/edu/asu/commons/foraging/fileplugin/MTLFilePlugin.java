package edu.asu.commons.foraging.fileplugin;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

import edu.asu.commons.foraging.graphics.RGBA;
import edu.asu.commons.foraging.graphics.TextureLoader;
import edu.asu.commons.foraging.graphics.Triangulation;

public class MTLFilePlugin {
	public static void readMaterial(String mtlFile, String material, Triangulation object) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(mtlFile));
			String line;
			int index;			
			float a = 1.0f;
						
			while ( (line = reader.readLine()) != null && !line.contains(material)) {
				//Check each line in the file till we find the desired material
			}
			
			//TODO: Create constants for each string, trim the line and make sure that the line starts with this string
			//Also make the reader independent of the order of if-else
			do {
				if (line.indexOf("#") != -1) {
					//Comment. Ignore. Do nothing
				}
				else if ( (index = line.indexOf("map_Kd")) != -1) {
					System.out.println("Loading texture...");						
					String textureFile = mtlFile.substring(0, mtlFile.lastIndexOf('/')); 
					textureFile = textureFile + "/" + line.substring(index + "map_Kd ".length());						
//					PPMFilePlugin.readFile(textureFile, object);
					TextureLoader texLoader = new TextureLoader();
					object.setTexture(texLoader.getTexture(textureFile, true));
					System.out.println("Done!");
				}	
				else if ( (index = line.indexOf("Ka")) != -1) {
					StringTokenizer colorComponent = new StringTokenizer(line.substring(index+2));
					object.setAmbient(new RGBA(Float.valueOf(colorComponent.nextToken()), Float.valueOf(colorComponent.nextToken()), Float.valueOf(colorComponent.nextToken()), a));										
				}
				else if ( (index = line.indexOf("Kd")) != -1) {
					StringTokenizer colorComponent = new StringTokenizer(line.substring(index+2));
					object.setDiffuse(new RGBA(Float.valueOf(colorComponent.nextToken()), Float.valueOf(colorComponent.nextToken()), Float.valueOf(colorComponent.nextToken()), a));										
				}
				else if ( (index = line.indexOf("Ks")) != -1) {
					StringTokenizer colorComponent = new StringTokenizer(line.substring(index+2));
					object.setSpecular(new RGBA(Float.valueOf(colorComponent.nextToken()), Float.valueOf(colorComponent.nextToken()), Float.valueOf(colorComponent.nextToken()), a));										
				}
				else if ( (index = line.indexOf("Ns")) != -1) {
					object.setShininess(Float.valueOf(line.substring(index+2)));
				}				
			}while( (line = reader.readLine()) != null && !line.contains("newmtl") );
			reader.close();
		}
		catch (FileNotFoundException fnf) {
			fnf.printStackTrace();
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}
