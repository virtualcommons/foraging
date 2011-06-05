package edu.asu.commons.foraging.fileplugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Vector;

import edu.asu.commons.foraging.graphics.Point3D;
import edu.asu.commons.foraging.graphics.Triangulation;
import edu.asu.commons.foraging.graphics.Vector3D;
import edu.asu.commons.foraging.util.Tuple2f;
import edu.asu.commons.foraging.util.Tuple3i;


public class OBJFilePlugin {
	
	//Loads a single object from obj file
	public static void readFile(String file, Triangulation object) {		
		int index = -1;
		String mtlFilePath = "";			
		Float x, y, z;
		int a, b, c;
		int t1 = 0, t2 = 0, t3 = 0;
		int vertexNormalIndex = 1;						
        BufferedReader reader = FileLoader.getBufferedReader(file);
        try {
            String line = reader.readLine();

            while (line != null) {
                //Blank line
                if (line.equals("")) {
                    //Do nothing
                }
                //Comments
                else if (line.startsWith("#")) {
                    //Do nothing
                }				
                //Material file
                else if ( (index = line.indexOf("mtllib")) != -1) {
                    //Initialize mtl file path with the package same as the obj file 
                    index = file.lastIndexOf('/');
                    if (index == -1) {
                        index = file.lastIndexOf('\\');
                        mtlFilePath = file.substring(0, index);
                        //Store the material file name
                        mtlFilePath = mtlFilePath + "\\" + line.substring("mtllib ".length());
                    }
                    else {						
                        mtlFilePath = file.substring(0, index);
                        //Store the material file name
                        mtlFilePath = mtlFilePath + "/" + line.substring("mtllib ".length());
                    }					
                }
                else if ( (index = line.indexOf("usemtl")) != -1) {
                    String material = line.substring("usemtl ".length());
                    MTLFilePlugin.readMaterial(mtlFilePath, material, object);
                }
                //Vertex normals
                else if ( (index = line.indexOf("vn")) != -1) {
                    StringTokenizer coordinates = new StringTokenizer(line.substring(index + 2));
                    object.addVertexNormal(new Vector3D(Float.valueOf(coordinates.nextToken()), Float.valueOf(coordinates.nextToken()), Float.valueOf(coordinates.nextToken())), vertexNormalIndex++);			
                }
                //Texture co-ordinates
                else if ( (index = line.indexOf("vt")) != -1) {
                    StringTokenizer coordinates = new StringTokenizer(line.substring(index + 2));
                    object.addTextureCoordinate(new Tuple2f(Float.valueOf(coordinates.nextToken()), Float.valueOf(coordinates.nextToken())));			
                }
                //Vertex
                else if ( (index = line.indexOf("v")) != -1) {
                    StringTokenizer coordinates = new StringTokenizer(line.substring(index + 1));
                    x = Float.valueOf(coordinates.nextToken());
                    y = Float.valueOf(coordinates.nextToken());
                    z = Float.valueOf(coordinates.nextToken());
                    object.addVertex(new Point3D(x, y, z));					
                }				
                //Face
                else if ( (index = line.indexOf("f")) != -1) {
                    StringTokenizer face = new StringTokenizer(line.substring(index + 1));
                    StringTokenizer vertexTokenizer = new StringTokenizer(face.nextToken(), "/");
                    //Vertex 1
                    //vertex index
                    a = Integer.valueOf(vertexTokenizer.nextToken());
                    //texture coordinate index, if any
                    if (vertexTokenizer.hasMoreTokens()) t1 = Integer.valueOf(vertexTokenizer.nextToken());

                    //Vertex 2
                    vertexTokenizer = new StringTokenizer(face.nextToken(), "/");
                    b = Integer.valueOf(vertexTokenizer.nextToken());
                    //texture coordinate index, if any
                    if (vertexTokenizer.hasMoreTokens()) t2 = Integer.valueOf(vertexTokenizer.nextToken());

                    //Vertex 3
                    vertexTokenizer = new StringTokenizer(face.nextToken(), "/");
                    c = Integer.valueOf(vertexTokenizer.nextToken());
                    //texture coordinate index, if any
                    if (vertexTokenizer.hasMoreTokens()) t3 = Integer.valueOf(vertexTokenizer.nextToken());

                    object.addFace(new Tuple3i(a, b, c));
                    //if (t1 != 0) object.addFaceTexture(new Tuple3i(t1, t2, t3));
                }
                //Face normal
                else if ( (index = line.indexOf("fn")) != -1) {
                    StringTokenizer coordinates = new StringTokenizer(line.substring(index + 2));					
                    object.addFaceNormal(new Vector3D(Float.valueOf(coordinates.nextToken()), Float.valueOf(coordinates.nextToken()), Float.valueOf(coordinates.nextToken())));
                }
                //Read next line
                line = reader.readLine();
            }//end while
            reader.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	//Loads multiple objects from obj file 
	public static void readFile(String filePath, Vector<Triangulation> objects) {
		try {
			int index = -1;
			String mtlFilePath = "";
			Triangulation object = null;
			BufferedReader reader = FileLoader.getBufferedReader(filePath);
			String line = reader.readLine();
			Float x, y, z;
			int a, b, c, d;
			int t1 = 0, t2 = 0, t3 = 0, t4 = 0;
			int vertexIndex = 0;
			int vertexCount = 0;
			int vertexNormalIndex = 1;
						
			//TODO: Create constants for each string, trim the line and make sure that the line starts with this string
			//Also make the reader independent of the order of if-else
			while (line != null) {
				//Blank line
				if (line.equals("")) {
					//Do nothing
				}
				//Comments
				else if (line.startsWith("#")) {
					//Do nothing
				}				
				//Material file
				else if ( (index = line.indexOf("mtllib")) != -1) {
					//Initialize mtl file path with the package same as the obj file 
					mtlFilePath = filePath.substring(0, filePath.lastIndexOf('/'));
					//Store the material file name
					mtlFilePath = mtlFilePath + "/" + line.substring("mtllib ".length());
				}
				//Group - next object
				else if ( (index = line.indexOf("g")) != -1) {
					if (object != null) {										
						//Update total vertices count 
						vertexCount += vertexIndex;
						//Reset vertexIndex
						vertexIndex = 0;
						vertexNormalIndex = 1;
					}

					//Create new object
					object = new Triangulation();
					objects.add(object);					
				}
				else if ( (index = line.indexOf("usemtl")) != -1) {
					String material = line.substring("usemtl ".length());
					MTLFilePlugin.readMaterial(mtlFilePath, material, object);
				}
				//Vertex normals
				else if ( (index = line.indexOf("vn")) != -1) {
					StringTokenizer coordinates = new StringTokenizer(line.substring(index + 2));
					object.addVertexNormal(new Vector3D(Float.valueOf(coordinates.nextToken()), Float.valueOf(coordinates.nextToken()), Float.valueOf(coordinates.nextToken())), vertexNormalIndex++);			
				}
				//Texture co-ordinates
				else if ( (index = line.indexOf("vt")) != -1) {
					StringTokenizer coordinates = new StringTokenizer(line.substring(index + 2));
					object.addTextureCoordinate(new Tuple2f(Float.valueOf(coordinates.nextToken()), Float.valueOf(coordinates.nextToken())));			
				}
				//Vertex
				else if ( (index = line.indexOf("v")) != -1) {
					StringTokenizer coordinates = new StringTokenizer(line.substring(index + 1));
					x = Float.valueOf(coordinates.nextToken());
					y = Float.valueOf(coordinates.nextToken());
					z = Float.valueOf(coordinates.nextToken());
					object.addVertex(new Point3D(x, y, z));
					vertexIndex++;
				}
				//Face normal
				else if ( (index = line.indexOf("fn")) != -1) {
					StringTokenizer coordinates = new StringTokenizer(line.substring(index + 2));					
					object.addFaceNormal(new Vector3D(Float.valueOf(coordinates.nextToken()), Float.valueOf(coordinates.nextToken()), Float.valueOf(coordinates.nextToken())));
				}
				//Face
				else if ( (index = line.indexOf("f")) != -1) {
					StringTokenizer face = new StringTokenizer(line.substring(index + 1));
					StringTokenizer vertexTokenizer = new StringTokenizer(face.nextToken(), "/");
					//Vertex 1
					//vertex index
					a = Integer.valueOf(vertexTokenizer.nextToken());
					a -= vertexCount;
					//texture coordinate index, if any
					if (vertexTokenizer.hasMoreTokens()) t1 = Integer.valueOf(vertexTokenizer.nextToken()) - vertexCount;
					
					//Vertex 2
					vertexTokenizer = new StringTokenizer(face.nextToken(), "/");
					b = Integer.valueOf(vertexTokenizer.nextToken());
					b -= vertexCount;
					//texture coordinate index, if any
					if (vertexTokenizer.hasMoreTokens()) t2 = Integer.valueOf(vertexTokenizer.nextToken()) - vertexCount;
										
					//Vertex 3
					vertexTokenizer = new StringTokenizer(face.nextToken(), "/");
					c = Integer.valueOf(vertexTokenizer.nextToken());
					c -= vertexCount;
					//texture coordinate index, if any
					if (vertexTokenizer.hasMoreTokens()) t3 = Integer.valueOf(vertexTokenizer.nextToken()) - vertexCount;					
										
//					//Vertex 4
//					if (face.hasMoreTokens()) {
//						vertexTokenizer = new StringTokenizer(face.nextToken(), "/");
//						d = Integer.valueOf(vertexTokenizer.nextToken()) - vertexCount;
//						//texture coordinate index, if any
//						if (vertexTokenizer.hasMoreTokens()) t4 = Integer.valueOf(vertexTokenizer.nextToken());						
//												
//						object.addSquareFace(new Tuple4i(a, b, c, d));
//						if (t1 != 0) object.addSquareFaceTexture(new Tuple4i(t1, t2, t3, t3));
//					}
//					else {											
						object.addFace(new Tuple3i(a, b, c));
						//if (t1 != 0) object.addFaceTexture(new Tuple3i(t1, t2, t3));
//					}
					
				}				
				//Read next line
				line = reader.readLine();
			}//end while
			reader.close();
		}
		catch(IOException ioe) {
			ioe.printStackTrace();
		}
				
	}//end method
	
}//end class
		