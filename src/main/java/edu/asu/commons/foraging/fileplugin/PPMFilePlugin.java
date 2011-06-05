package edu.asu.commons.foraging.fileplugin;

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

import edu.asu.commons.foraging.graphics.Triangulation;

public class PPMFilePlugin {
	protected static int NO_OF_COLORS = 4;
	
	public static void readFile(String fileName, Triangulation object) {
		try {
			int dataIndex = 0;
						
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			
			//Check if the file has ppm header
			String line = reader.readLine();
			line = line.trim();
			if (!line.equals("P3") && !line.equals("P6")) {
				System.err.println("Not a valid ppm file. Aborting loading image data.");
			}
			
			//Read image size
			line = reader.readLine();
			StringTokenizer lineTokenizer = new StringTokenizer(line);
			Dimension imageSize = new Dimension(Integer.valueOf(lineTokenizer.nextToken()), Integer.valueOf(lineTokenizer.nextToken()));
//			object.setTextureSize(imageSize, NO_OF_COLORS);
			
			//Read and ignore 255
			reader.readLine();
			
			//Read the first line of image data
			line = reader.readLine();
			lineTokenizer = new StringTokenizer(line);
			
			for (int rowIndex = imageSize.height - 1; rowIndex >= 0; rowIndex--)	{				
				for (int columnIndex = 0; columnIndex < imageSize.width; columnIndex++) {					
					dataIndex = (rowIndex * imageSize.width * NO_OF_COLORS) + (columnIndex * NO_OF_COLORS);
//					object.setColorAt(new RGBA(Float.valueOf(lineTokenizer.nextToken()), Float.valueOf(lineTokenizer.nextToken()), Float.valueOf(lineTokenizer.nextToken()), 1.0f), dataIndex);					
					//If no more colors in this line, read the next line 
					if (!lineTokenizer.hasMoreTokens()) {
						line = reader.readLine();
						if (line != null) lineTokenizer = new StringTokenizer(line);
						else return;
					}
				}		
			}
			reader.close();
		}
		catch (FileNotFoundException fnf) {
			fnf.printStackTrace();
		}
		catch(IOException ioe) {
			ioe.printStackTrace();
		}		
	}
}
