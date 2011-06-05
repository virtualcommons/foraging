package edu.asu.commons.foraging.visualization.forestry.shader;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Vector;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import edu.asu.commons.foraging.fileplugin.FileLoader;
import edu.asu.commons.foraging.graphics.GraphicsObject;
import edu.asu.commons.foraging.graphics.Matrix4;
import edu.asu.commons.foraging.graphics.MovingObject;
import edu.asu.commons.foraging.graphics.Point3D;
import edu.asu.commons.foraging.graphics.RGBA;
import edu.asu.commons.foraging.graphics.Ray;
import edu.asu.commons.foraging.graphics.TextureLoader;
import edu.asu.commons.foraging.graphics.Vector3D;
import edu.asu.commons.foraging.jcal3d.core.CoreMaterial;
import edu.asu.commons.foraging.jcal3d.core.CoreMesh;
import edu.asu.commons.foraging.jcal3d.core.CoreModel;
import edu.asu.commons.foraging.jcal3d.core.CoreSubmesh;
import edu.asu.commons.foraging.jcal3d.instance.Model;
import edu.asu.commons.foraging.jcal3d.instance.Renderer;
import edu.asu.commons.foraging.jcal3d.instance.Submesh;
import edu.asu.commons.foraging.jcal3d.instance.Submesh.Face;


public class Woodcutter extends GraphicsObject implements MovingObject {

	private static final long serialVersionUID = 3225396389123396264L;
	
	//File related variables
	protected static final String PATH = "path";
	protected static final String SCALE = "scale";
	protected static final String SKELETON = "skeleton";
	protected static final String ANIMATION = "animation";
	protected static final String MESH = "mesh";
	protected static final String MATERIAL = "material";
		
	protected String dataPath = new String();	//File path
	
	protected Point3D position;
	protected float heading; //In radians
	protected float speed;		
	protected static float ANGLE_STEP = 0.03f; //0.034906585f; //In radians
	protected static float RADIANS_TO_DEGREES = 57.3f;
	protected static float COLLISION_DISTANCE = 1.5f;
		
	//Model realted variables
	protected CoreModel coreModel;				//Core Model containing everything mentioned in the .cfg file
	protected Model model;						//Instance Model
//	protected int meshIds[] = new int[32];		//All meshes
//	protected int meshCount;					//Total # of meshes present
//	protected int textureIds[] = new int[32];	//All textures
//	protected int textureCount;					//Total # of textures present
	protected float lodLevel;					
	
	//Rendering related variables
	protected float renderScale;				//scale specified in .cfg file

	//Animation related variables
	public static final int STATE_IDLE = 0;	
	public static final int STATE_WALKING = 1;
	public static final int STATE_AXING = 2;		
	protected int animationState;				//Current animation state
	protected int animationIds[] = new int[16]; //Possible animations
	protected int animationCount;				//Total # of animations present
	protected float motionBlend[] = new float[3];//Weights used while blending animations		  
	protected float duration;
	protected long lastTick;
	
	//Collision related variables	
	private Ray frontRay = new Ray(new Point3D(), new Vector3D());
	private Ray leftRay = new Ray(new Point3D(), new Vector3D());
	private Ray rightRay = new Ray(new Point3D(), new Vector3D());
	private Ray backRay = new Ray(new Point3D(), new Vector3D());
	private Ray frontLeftRay = new Ray(new Point3D(), new Vector3D());
	private Ray frontRightRay = new Ray(new Point3D(), new Vector3D());
	private Ray backLeftRay = new Ray(new Point3D(), new Vector3D());
	private Ray backRightRay = new Ray(new Point3D(), new Vector3D());
	protected static Point3D lowerRayOffset = new Point3D(0, 3, 0);
	protected static Point3D upperRayOffset = new Point3D(0, 14, 0);
	protected boolean axeHitInAFrame = false;
	private ForestryView parentView;
	private Matrix4 transformation = null;
	private Point3D center_WS = null;

	private int assignedNo;			//Represents the no. of this woodcutter in the group
	
	public Woodcutter(Point3D position, int angle, float speed, ForestryView parentView) {
		this();	
		this.position = position;
		this.heading = angle;
		this.speed = speed;		
		this.parentView = parentView;
	}
	
	public Woodcutter() {
	  coreModel = new CoreModel("dummy");

	  renderScale = 1.0f;
	  animationState = STATE_IDLE;
	  motionBlend[STATE_IDLE] = 1.0f;
	  motionBlend[STATE_WALKING] = 0.0f;
	  motionBlend[STATE_AXING] = 0.0f;	  
	  animationCount = 0; 
	  lodLevel = 0.1f;	  
	  lastTick = System.currentTimeMillis();
	}

	public boolean init(String filePath, Color hairColor, Color skinColor, Color shirtColor, Color trouserColor, Color shoesColor) {
		//Read .cfg file
		if (!readCFGfile(filePath)) return false;
		
		coreModel.createCoreMaterial(skinColor);
		coreModel.createCoreMaterial(shoesColor);
		coreModel.createCoreMaterial(trouserColor);
		coreModel.createCoreMaterial(shirtColor);
		coreModel.createCoreMaterial(new Color(0.79f, 0.7f, 0.21f, 1.0f)); //caneColor
		coreModel.createCoreMaterial(new Color(0.5f, 0.5f, 0.5f, 1.0f));	//axeColor;
		
		//Load textures using material maps
		loadTextures();
		
		createMaterialThreads();
		
		//Calculate Bounding Boxes of the core model
		coreModel.getCoreSkeleton().calculateBoundingBoxes(coreModel);
				
		 //Create model instance from core model
		createModel();
		
		//Calculate bounding boxes of the model
		//Note :
		// You have to call coreSkeleton.calculateBoundingBoxes(calCoreModel)
		// during the initialisation (before calModel.create(calCoreModel))
		// if you want to use bounding boxes.
		model.getSkeleton().calculateBoundingBoxes();
		
		calculateExtents();
		calculateCenter();
		calculateTransformation();
		calculateCenter_WS();
		
		return true;
	}

	public void display(GLAutoDrawable drawable) {
		GL gl = drawable.getGL();
		
		gl.glDisable(GL.GL_TEXTURE_2D);
		gl.glPushMatrix();
			//Translate to displace the object
			gl.glTranslated(position.x, position.y, position.z);

			gl.glPushMatrix();
				//Rotate the object around it's center
//				gl.glTranslated(center.x, center.y, center.z);
				gl.glRotated((heading * RADIANS_TO_DEGREES) + 90, 0, 1, 0);  
//				gl.glTranslated(-center.x, -center.y, -center.z);
				
				gl.glRotatef(-90, 1, 0, 0);		
				gl.glScalef(renderScale, renderScale, renderScale);			
				
				//render the skeleton
				//renderSkeleton(drawable);
//				renderBoundingBox(drawable);
				boolean wireFrame = false;
				renderMesh(wireFrame, drawable);
											
			gl.glPopMatrix();			
		gl.glPopMatrix();
		
//		renderCollisionRays(drawable);
	}
	
	public void calculateExtents() {
		//Iterate over all the submeshes of the meshes and calculate extends
		for(int meshIndex = 0; meshIndex < coreModel.getCoreMeshCount(); meshIndex++) {
			CoreMesh coreMesh = coreModel.getCoreMesh(meshIndex);
			for (int submeshIndex = 0; submeshIndex < coreMesh.getCoreSubmeshCount(); submeshIndex++) {
				CoreSubmesh coreSubmesh = coreMesh.getCoreSubmesh(submeshIndex);
				for (int vertexIndex = 0; vertexIndex < coreSubmesh.getVertexCount(); vertexIndex++) {
					CoreSubmesh.Vertex vertex = coreSubmesh.getVertices().get(vertexIndex);
					updateExtents(vertex.position);
				}
			}
		}
	}
	
	public boolean isNewHit() {
		if (!axeHitInAFrame) {
			axeHitInAFrame = true;
			return true;
		}
		return false;
	}
		
	//TODO: Make this method part of the Loader class
	private boolean readCFGfile(String filePath) {
		try {
			BufferedReader reader = FileLoader.getBufferedReader(filePath);
			String line = reader.readLine();		
			int index;			
			String file;
			
			if (dataPath.equals("")) 
                dataPath = filePath.substring(0, filePath.lastIndexOf('/')+1);
			
			while (line != null) {
				//Blank line
				if (line.equals("") || line.contains("\t") || line.contains(" ")) {
					//Do nothing					
				}
				else if (line.startsWith("#")) {
					//Comment. Do nothing					
				}
				else if (line.startsWith(PATH)) {
					index = line.indexOf(PATH) + PATH.length();
					if (line.charAt(index) != '=') {
						System.err.println(filePath + ": " + line + " :Invalid syntax");
						return false;
					}
					//if (dataPath.equals("")) {						
						dataPath = dataPath + line.substring(index + 1);					
					//}
				}
				else if (line.startsWith(SCALE)) {
					index = line.indexOf(SCALE) + SCALE.length();
					if (line.charAt(index) != '=') {
						System.err.println(filePath + ": " + line + " :Invalid syntax");
						return false;
					}
					renderScale = Float.valueOf(line.substring(index + 1));
				}
				else if (line.startsWith(SKELETON)) {
					index = line.indexOf(SKELETON) + SKELETON.length();
					if (line.charAt(index) != '=') {
						System.err.println(filePath + ": " + line + " :Invalid syntax");
						return false;
					}
					file = line.substring(index + 1);
					
					System.out.println("Loading skeleton '" + file + "'...");
					if (! coreModel.loadCoreSkeleton(dataPath + file)) {
						edu.asu.commons.foraging.jcal3d.misc.Error.printLastError();
						return false;
					}					
				}
				else if (line.startsWith(ANIMATION)) {
					index = line.indexOf(ANIMATION) + ANIMATION.length();
					if (line.charAt(index) != '=') {
						System.err.println(filePath + ": " + line + " :Invalid syntax");
						return false;
					}
					file = line.substring(index + 1);
					
					System.out.println("Loading animation '" + file + "'...");
					animationIds[animationCount] = coreModel.loadCoreAnimation(dataPath + file);
					if (animationIds[animationCount] == -1) {
						edu.asu.commons.foraging.jcal3d.misc.Error.printLastError();
						return false;	
					}
					animationCount++;
				}
				else if (line.startsWith(MESH)) {
					index = line.indexOf(MESH) + MESH.length();
					if (line.charAt(index) != '=') {
						System.err.println(filePath + ": " + line + " :Invalid syntax");
						return false;
					}
					file = line.substring(index + 1);
					
					System.out.println("Loading mesh '" + file + "'...");
					if (coreModel.loadCoreMesh(dataPath + file) == -1) {
						edu.asu.commons.foraging.jcal3d.misc.Error.printLastError();
						return false;
					}					
				}
				
				else if (line.startsWith(MATERIAL)) {
					index = line.indexOf(MATERIAL) + MATERIAL.length();
					if (line.charAt(index) != '=') {
						System.err.println(filePath + ": " + line + " :Invalid syntax");
						return false;
					}
					file = line.substring(index + 1);
					
					/*System.out.println("Loading material '" + file + "'...");
					if (coreModel.loadCoreMaterial(dataPath + file) == -1) {
						edu.asu.commons.csan3d.charanim.misc.Error.printLastError();
						return false;
					}*/
				}
				else {
					System.err.println(filePath + ": " + line + " :Invalid syntax");
					return false;
				}
				
				line = reader.readLine();
			}//end while
			
			reader.close();
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return true;
	}
	
	private void loadTextures() {
		String file;
		
		//Load all textures and store them in the corresponding map in the material			
		for(int materialIndex = 0; materialIndex < coreModel.getCoreMaterialCount(); materialIndex++) {
			//get the core material
		    CoreMaterial coreMaterial = coreModel.getCoreMaterial(materialIndex);

		    // loop through all maps of the core material			    
		    for(int mapIndex = 0; mapIndex < coreMaterial.getMapCount(); mapIndex++)
		    {
		    	// get the filename of the texture			    	
		    	file = coreMaterial.getMapFilename(mapIndex);

		    	// load the texture from the file and store it in the material
		    	TextureLoader texLoader = new TextureLoader();
		    	coreMaterial.setMapUserData(mapIndex, texLoader.getTexture(dataPath + file, true));
		    }
		}
	}
	
	private void createMaterialThreads() {
		//make one material thread for each material
		// NOTE: this is not the right way to do it, but this viewer can't do the right
		// mapping without further information on the model etc.
		for(int materialIndex = 0; materialIndex < coreModel.getCoreMaterialCount(); materialIndex++) {
			// create the a material thread
		    coreModel.createCoreMaterialThread(materialIndex);

		    // initialize the material thread
		    coreModel.setCoreMaterialId(materialIndex, 0, materialIndex);
		 }
	}
	
	private void createModel() {
		model = new Model(coreModel);
		
		model.getSkeleton().calculateState();
		 
		//attach all meshes to the model			 
		for(int meshIndex = 0; meshIndex < coreModel.getCoreMeshCount(); meshIndex++) {
			 model.attachMesh(meshIndex);
		}

		// set the material set of the whole model
		model.setMaterialSet(0);

		// set initial animation state
		model.getMixer().blendCycle(animationIds[STATE_IDLE], 1.0f, 0);
	    model.getMixer().clearCycle(animationIds[STATE_WALKING], 0);
	    model.getMixer().clearCycle(animationIds[STATE_AXING], 0);
	    animationState = STATE_IDLE;	
	}
	
	public void setDataPath(String dataPath) {
		this.dataPath = dataPath;
	}
	
//	private void renderSkeleton(GLAutoDrawable drawable) {
//		GL gl = drawable.getGL();
////		GLU glu = drawable.getGLU();
//		
//	  // draw the bone lines	  
//	  Vector<Point3D> boneLines = model.getSkeleton().getBoneLines();
////	  nrLines = m_calModel->getSkeleton()->getBoneLinesStatic(&lines[0][0][0]);
//
//	  gl.glEnable(GL.GL_COLOR_MATERIAL);
//	  
//	  gl.glLineWidth(3.0f);	  
//	  gl.glColor3f(1.0f, 1.0f, 1.0f);
//	  gl.glBegin(GL.GL_LINES);
//	    int currLine;
//	    for(currLine = 0; currLine < boneLines.size(); currLine++)
//	    {
//	    	Point3D point = boneLines.get(currLine);
//	    	gl.glVertex3f(point.x, point.y, point.z);
//	    	point = boneLines.get(++currLine);
//	    	gl.glVertex3f(point.x, point.y, point.z);
//	    }
//	  gl.glEnd();	  
//	  gl.glLineWidth(1.0f);
//
//	  // draw the bone points
//	  Vector<Point3D> bonePoints =  model.getSkeleton().getBonePoints();
////	  nrPoints = m_calModel->getSkeleton()->getBonePointsStatic(&points[0][0]);
//
//	  gl.glPointSize(4.0f);
//	  gl.glBegin(GL.GL_POINTS);
//	    gl.glColor3f(0.0f, 0.0f, 1.0f);
//	    int currPoint;
//	    for(currPoint = 0; currPoint < bonePoints.size(); currPoint++)
//	    {
//	    	Point3D point = bonePoints.get(currPoint);
//	    	gl.glVertex3f(point.x, point.y, point.z);
//	    }
//	  gl.glEnd();
//	  gl.glPointSize(1.0f);
//	  gl.glDisable(GL.GL_COLOR_MATERIAL);
//	}

//	public Vector<BoundingBox> getBoundingBoxes() {
//		Vector<BoundingBox> boundingBoxes = new Vector<BoundingBox>();
//		
//		//Recalculate bounding boxes, if required
//		model.getSkeleton().calculateBoundingBoxes();
//				
//		Vector<Bone> coreBones = model.getSkeleton().getBones();
//		for(int boneId = 0; boneId < coreBones.size(); ++boneId)
//		{
//			BoundingBox boundingBox = coreBones.get(boneId).getBoundingBox();
//			boundingBoxes.add(boundingBox);
//		}
//		return boundingBoxes;
//	}
	
//	----------------------------------------------------------------------------//
//	 Render the bounding boxes of a model                                       //
//	----------------------------------------------------------------------------//

//	private void renderBoundingBox(GLAutoDrawable drawable)
//	{  
//		GL gl = drawable.getGL();
//		
//	   Skeleton skeleton = model.getSkeleton();
//
//	   Vector<Bone> coreBones = skeleton.getBones();
//
//	   gl.glEnable(GL.GL_COLOR_MATERIAL);
//	   gl.glColor3f(0.0f, 0.0f, 0.0f);
//	   gl.glBegin(GL.GL_LINES);      
//
//	   for(int boneId = 0; boneId < coreBones.size(); ++boneId)
//	   {
//	      BoundingBox boundingBox = coreBones.get(boneId).getBoundingBox();
//
//		  Vector<Point3D> points = boundingBox.computePoints();
//	
//		  Point3D point = points.get(0);
//		  gl.glVertex3f(point.x,point.y,point.z);
//		  point = points.get(1);
//		  gl.glVertex3f(point.x,point.y,point.z);
//
//		  point = points.get(0);
//		  gl.glVertex3f(point.x,point.y,point.z);
//		  point = points.get(2);
//		  gl.glVertex3f(point.x,point.y,point.z);
//
//		  point = points.get(1);
//		  gl.glVertex3f(point.x,point.y,point.z);
//		  point = points.get(3);
//		  gl.glVertex3f(point.x,point.y,point.z);
//
//		  point = points.get(2);		  
//		  gl.glVertex3f(point.x,point.y,point.z);
//		  point = points.get(3);
//		  gl.glVertex3f(point.x,point.y,point.z);
//
//		  point = points.get(4);
//		  gl.glVertex3f(point.x,point.y,point.z);
//		  point = points.get(5);
//		  gl.glVertex3f(point.x,point.y,point.z);
//
//		  point = points.get(4);
//		  gl.glVertex3f(point.x,point.y,point.z);
//		  point = points.get(6);
//		  gl.glVertex3f(point.x,point.y,point.z);
//
//		  point = points.get(5);
//		  gl.glVertex3f(point.x,point.y,point.z);
//		  point = points.get(7);
//		  gl.glVertex3f(point.x,point.y,point.z);
//
//		  point = points.get(6);
//		  gl.glVertex3f(point.x,point.y,point.z);
//		  point = points.get(7);
//		  gl.glVertex3f(point.x,point.y,point.z);
//
//		  point = points.get(0);
//		  gl.glVertex3f(point.x,point.y,point.z);
//		  point = points.get(4);
//		  gl.glVertex3f(point.x,point.y,point.z);
//
//		  point = points.get(1);
//		  gl.glVertex3f(point.x,point.y,point.z);
//		  point = points.get(5);
//		  gl.glVertex3f(point.x,point.y,point.z);
//
//		  point = points.get(2);
//		  gl.glVertex3f(point.x,point.y,point.z);
//		  point = points.get(6);
//		  gl.glVertex3f(point.x,point.y,point.z);
//
//		  point = points.get(3);
//		  gl.glVertex3f(point.x,point.y,point.z);
//		  point = points.get(7);
//		  gl.glVertex3f(point.x,point.y,point.z);  
//
//	   }
//
//	   gl.glEnd();
//	   gl.glDisable(GL.GL_COLOR_MATERIAL);
//
//	}


//	----------------------------------------------------------------------------//
//	 Render the mesh of the model                                               //
//	----------------------------------------------------------------------------//

	private void renderMesh(boolean bWireframe, GLAutoDrawable drawable)
	{
		GL gl = drawable.getGL();
		
	  // get the renderer of the model
	  Renderer renderer = model.getRenderer();

	  // begin the rendering loop
	  if(!renderer.beginRendering()) return;

	  // set wireframe mode if necessary
	  if(bWireframe) gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_LINE);
//	  else 			 gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_FILL);
	  
	  // we will use vertex arrays, so enable them
//	  gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
//	  gl.glEnableClientState(GL.GL_NORMAL_ARRAY);

	  // get the number of meshes
	  int meshCount = renderer.getMeshCount();

	  // render all meshes of the model	  
	  for(int meshId = 0; meshId < meshCount; meshId++)
	  {
	    // get the number of submeshes
	    int submeshCount = renderer.getSubmeshCount(meshId);

	    // render all submeshes of the mesh	    
	    for(int submeshId = 0; submeshId < submeshCount; submeshId++)
	    {
	      // select mesh and submesh for further data access
	      if(renderer.selectMeshSubmesh(meshId, submeshId))
	      {
	        // set the material ambient color
	        RGBA rgba = renderer.getAmbientColor();
	        float[] ambientColor = rgba.getfv();
	        gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT, ambientColor, 0);

	        // set the material diffuse color
	        rgba = renderer.getDiffuseColor();	        
	        float[] diffuseColor = rgba.getfv();
	        gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_DIFFUSE, diffuseColor, 0);

	        // set the material specular color
	        rgba = renderer.getSpecularColor();
	        float[] specularColor = rgba.getfv();
	        gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_SPECULAR, specularColor, 0);

	        // set the material shininess factor
	        float shininess = renderer.getShininess();
	        gl.glMaterialf(GL.GL_FRONT_AND_BACK, GL.GL_SHININESS, shininess);

	        // get the transformed vertices of the submesh	        
	        Vector<Point3D> vertices = renderer.getVertices();
	        //FloatBuffer vertexBuffer = Buffer.getPointBuffer(vertices);

	        // get the transformed normals of the submesh
	        Vector<Vector3D> normals = renderer.getNormals();
	        //FloatBuffer normalBuffer = Buffer.getNomalBuffer(normals);

	        // get the texture coordinates of the submesh	        
	        //Vector<CoreSubmesh.TextureCoordinate> textureCoordinates = renderer.getTextureCoordinates(0);

	        // get the faces of the submesh	        
	        Vector<Submesh.Face> faces = renderer.getFaces();
	        //IntBuffer faceBuffer = Buffer.getFaceBuffer(faces);	
	        
	        //set the vertex and normal buffers
//	        gl.glVertexPointer(3, GL.GL_FLOAT, 0, vertexBuffer);
//	        gl.glNormalPointer(GL.GL_FLOAT, 0, normalBuffer);

	        // set the texture coordinate buffer and state if necessary
//	        if((renderer.getMapCount() > 0) && (textureCoordinates.size() > 0))
//	        {
//	          gl.glEnable(GL.GL_TEXTURE_2D);
//	          gl.glEnableClientState(GL.GL_TEXTURE_COORD_ARRAY);
//	          gl.glEnable(GL.GL_COLOR_MATERIAL);
//
//	          // set the texture id we stored in the map user data
//	          //gl.glBindTexture(GL.GL_TEXTURE_2D, renderer.getMapUserData(0));
//
//	          // set the texture coordinate buffer
//	          //gl.glTexCoordPointer(2, GL.GL_FLOAT, 0, textureCoordinates);
//	          gl.glColor3f(1.0f, 1.0f, 1.0f);
//	        }

	        //draw the submesh
	        gl.glEnable(GL.GL_CULL_FACE);
	        gl.glCullFace(GL.GL_BACK);
	        gl.glBegin(GL.GL_TRIANGLES);	        
	        for (int faceIndex = 0; faceIndex < faces.size(); faceIndex++) {
	        	Face face = faces.get(faceIndex);
	        	
	        	int vertexIndex = face.vertexId[0];	        	
	        	Vector3D normal = normals.get(vertexIndex);
	        	gl.glNormal3f(normal.x, normal.y, normal.z);
	        	Point3D vertex = vertices.get(vertexIndex);
	        	gl.glVertex3f(vertex.x, vertex.y, vertex.z);
	        	
	        	vertexIndex = face.vertexId[1];	        	
	        	normal = normals.get(vertexIndex);
	        	gl.glNormal3f(normal.x, normal.y, normal.z);
	        	vertex = vertices.get(vertexIndex);
	        	gl.glVertex3f(vertex.x, vertex.y, vertex.z);
	        	
	        	vertexIndex = face.vertexId[2];	        	
	        	normal = normals.get(vertexIndex);
	        	gl.glNormal3f(normal.x, normal.y, normal.z);
	        	vertex = vertices.get(vertexIndex);
	        	gl.glVertex3f(vertex.x, vertex.y, vertex.z);
	        }
	        gl.glEnd();
	        gl.glDisable(GL.GL_CULL_FACE);
	        
//	        gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
//	        gl.glDrawElements(GL.GL_TRIANGLES, faces.size() * 3, GL.GL_INT, faceBuffer);	        
//	        gl.glDrawArrays(GL.GL_TRIANGLES, 0, faceBuffer.capacity());
	        

//	        // disable the texture coordinate state if necessary
//	        if((pCalRenderer->getMapCount() > 0) && (textureCoordinateCount > 0))
//	        {
//	          glDisable(GL_COLOR_MATERIAL);
//	          glDisableClientState(GL_TEXTURE_COORD_ARRAY);
//	          glDisable(GL_TEXTURE_2D);
//	        }

///////////////////// DEBUG-CODE //////////////////////////////////////////////////////////////////	
//			gl.glBegin(GL.GL_LINES);
//			System.out.println("MeshId = " + meshId + " SubmeshId = " + submeshId + " # of vertices = " + vertices.size());
//			//float[] color = {0.0f, 0.0f, 1.0f, 0.0f};
//			//gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT_AND_DIFFUSE, color);
//			int vertexId;
//			for(vertexId = 0; vertexId < vertices.size(); vertexId++)
//			{
//				float scale = 0.3f;
//				Point3D vertex = vertices.get(vertexId);
//				gl.glVertex3f(vertex.x, vertex.y, vertex.z);
//				gl.glVertex3f(vertex.x + vertex.x * scale, vertex.y + vertex.y * scale, vertex.z + vertex.z * scale);
//			}
//			gl.glEnd();	
//////////////////////////////////////////////////////////////////////////////	//
	      }//end if(renderer.selectMeshSubmesh(meshId, submeshId))
	    }//end for submesh
	  }//end for mesh

	  // clear vertex array state
//	  gl.glDisableClientState(GL.GL_NORMAL_ARRAY);
//	  gl.glDisableClientState(GL.GL_VERTEX_ARRAY);

	  // reset wireframe mode if necessary
	  if(bWireframe)
	  {
	    gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_FILL);
	  }

	  // end the rendering
	  renderer.endRendering();
	}
	
	//Code used to display animations of other avatars
	public void animate(int state) {		
//		if (this.elapsedSeconds != elapsedSeconds) {
//			this.elapsedSeconds = elapsedSeconds;
			
			setAnimationState(state);
			
			//get the current tick value		  
		    long tick = System.currentTimeMillis();

		    // calculate the amount of elapsed seconds
		    float elapsedSeconds = (float)(tick - lastTick) / 1000.0f;
		    
			// update the current model
		    if (model != null)
		    {	
		        model.update(elapsedSeconds);
		        model.getSkeleton().calculateBoundingBoxes();
		    }
		    
		    //current tick will be last tick next round
		    lastTick = tick;
		}
//	}
	
	public void axe() {
	    if (animationState != STATE_AXING) {
	        setAnimationState(Woodcutter.STATE_AXING, 0.3f);
	    }

	    //get the current tick value		  
	    long tick = System.currentTimeMillis();

	    // calculate the amount of elapsed seconds
	    float elapsedSeconds = (float)(tick - lastTick) / 1000.0f;

	    // adjust fps counter
	    duration += elapsedSeconds;
	    if(duration >= 1.0f) { //One animation cycle is complete 
	        duration = 0.0f;
	        axeHitInAFrame = false;
	    }

	    // update the current model
	    if (model != null)
	    {	
	        model.update(elapsedSeconds);
	        model.getSkeleton().calculateBoundingBoxes();
//	        calculateAxeVector();
	    }

	    // current tick will be last tick next round
	    lastTick = tick;
	}
	
	
	public void walk() {
		if (animationState != STATE_WALKING) {
			setAnimationState(STATE_WALKING, 0.3f);
		}
		
		//get the current tick value		  
		  long tick = System.currentTimeMillis();

		  // calculate the amount of elapsed seconds
		  float elapsedSeconds = (float)(tick - lastTick) / 1000.0f;
          //frameCounter++;
		  // adjust fps counter
		  duration += elapsedSeconds;
		  if(duration >= 1.0f) { //One animation cycle is complete 
		    duration = 0.0f; 
		  }

		  // update the current model
		  if (model != null)
		  {	
			  model.update(elapsedSeconds);
			  model.getSkeleton().calculateBoundingBoxes();
		  }

		  // current tick will be last tick next round
		  lastTick = tick;
	}
	
//	public void standSlowly() {
//		if (animationState != STATE_IDLE) {
//			setAnimationState(STATE_IDLE, 0.3f);
//		}
//		
//		while (true) {
//		//get the current tick value		  
//		  long tick = System.currentTimeMillis();
//
//		  // calculate the amount of elapsed seconds
//		  float elapsedSeconds = (float)(tick - lastTick) / 1000.0f;
//
//		  // adjust fps counter
//		  duration += elapsedSeconds;
//		  if(duration >= 1.0f)
//		  {
//		    duration = 0.0f;
//			  // update the current model
//			  if (model != null)
//			  {
//					model.update(elapsedSeconds);
//					model.getSkeleton().calculateBoundingBoxes();
//			  }
//
//			  // current tick will be last tick next round
//			  lastTick = tick;
//			  return;
//		  }		  
//		  // update the current model
//		  if (model != null)
//		  {
//			  model.update(elapsedSeconds);
//			  model.getSkeleton().calculateBoundingBoxes();
//		  }
//
//		  // current tick will be last tick next round
//		  lastTick = tick;	  
//		}
//	}
	
	public int getAnimationState() {
		return animationState;
	}
	
	private void setAnimationState(int state) {
		if(state != animationState) {
			switch(state) {
			case STATE_IDLE:
                setAnimationState(STATE_IDLE, 0.3f);
				break;
			case STATE_WALKING:
                setAnimationState(STATE_WALKING, 0.3f);
				break;
			case STATE_AXING:
                setAnimationState(STATE_AXING, 0.3f);
				break;
			}
		}
	}
	
//	----------------------------------------------------------------------------//
//	 Set a new animation state within a given delay                             //
//	----------------------------------------------------------------------------//
	public void setAnimationState(int state, float delay) {
	  // check if this is really a new state
	  if(state != animationState) {
	    if(state == STATE_IDLE) {	      
	      model.getMixer().blendCycle(animationIds[STATE_IDLE], 1.0f, delay);
	      model.getMixer().clearCycle(animationIds[STATE_WALKING], delay);
	      model.getMixer().clearCycle(animationIds[STATE_AXING], delay);
	      animationState = STATE_IDLE;
	    }
	    else if(state == STATE_WALKING)
	    {
	    	model.getMixer().clearCycle(animationIds[STATE_IDLE], delay);
	    	model.getMixer().blendCycle(animationIds[STATE_WALKING], 1.0f, delay);
	    	model.getMixer().clearCycle(animationIds[STATE_AXING], delay);
	    	animationState = STATE_WALKING;
	    }
	    else if(state == STATE_AXING)
	    {
	    	model.getMixer().clearCycle(animationIds[STATE_IDLE], delay);	    	
	    	model.getMixer().clearCycle(animationIds[STATE_WALKING], delay);
	    	model.getMixer().blendCycle(animationIds[STATE_AXING], 1.0f, delay);
	    	animationState = STATE_AXING;
	    }	    
	  }
	}
	
//	----------------------------------------------------------------------------//
//	 Set the motion blend factors state of the model                            //
//	----------------------------------------------------------------------------//
	public void setMotionBlend(float[] motionBlend, float delay)
	{
	  this.motionBlend[0] = motionBlend[0];
	  this.motionBlend[1] = motionBlend[1];
	  this.motionBlend[2] = motionBlend[2];

	  model.getMixer().clearCycle(animationIds[STATE_IDLE], delay);
	  model.getMixer().clearCycle(animationIds[STATE_AXING], delay);
	  model.getMixer().blendCycle(animationIds[STATE_WALKING], this.motionBlend[0], delay);	  

	  animationState = STATE_WALKING;
	}
		
	///////////////// Functions related to movements //////////////////////////////
	//	This class assumes that the object's center is at the origin and 
	//is facing in the positive X direction
	public void forward() {		
		position.x = position.x + speed * (float)Math.cos(heading); 
		//e. g. if angle with X axis is 0, go forward in the X direction
		
		position.z = position.z - speed * (float)Math.sin(heading); 
		//e. g. if angle with X axis is 90, go forward in negative Z direction
	}
	
	public void reverse() {
		position.x = position.x - speed * (float)Math.cos(heading); 
		position.z = position.z + speed * (float)Math.sin(heading); 
	}
	
	public void moveLeft() {
		heading = heading + ANGLE_STEP;
	}
	
	public void moveRight() {
		heading = heading - ANGLE_STEP; 
	}
	
	/////////////// Getter-Setter methods ///////////////////////////////////
	public Point3D getForwardPosition() {
		return new Point3D(	position.x + speed * (float)Math.cos(heading), 
							position.y, 
							position.z - speed * (float)Math.sin(heading));
	}
	
	public Point3D getBackwardPosition() {
		return new Point3D(	position.x - speed * (float)Math.cos(heading), 
							position.y, 
							position.z + speed * (float)Math.sin(heading));
	}
	
	public Point3D getPosition() {
		return position;
	}
	
	public void setPosition(Point3D position) {
		this.position = position;
	}
	
	public float getHeading() {
		return heading;
	}
	
	public void setHeading(float heading) {
		this.heading = heading;
	}
	
	public void setElevation(float y) {
		this.position.y = y;
	}

	public float getSpeed() {
		return speed;
	}
	
	//################ Functions related to collision ###############
	private Point3D getLowerRayOrigin() {
		return position.add(lowerRayOffset);
	}
	
	private Point3D getUpperRayOrigin() {
		return position.add(upperRayOffset);
	}
	
	public Ray getFrontRay(boolean lower) {
		if (lower)
			frontRay.setOrigin(getLowerRayOrigin());
		else
			frontRay.setOrigin(getUpperRayOrigin());
		frontRay.setDirection((float)Math.cos(heading), 0, -(float)Math.sin(heading));
		return frontRay;
	}
	
	public Ray getBackRay(boolean lower) {
		if (lower)
			backRay.setOrigin(getLowerRayOrigin());
		else
			backRay.setOrigin(getUpperRayOrigin());
		backRay.setDirection(-(float)Math.cos(heading), 0, (float)Math.sin(heading));
		return backRay;
	}
	
	public Ray getLeftRay(boolean lower) {
		if (lower)
			leftRay.setOrigin(getLowerRayOrigin());
		else
			leftRay.setOrigin(getUpperRayOrigin());
		leftRay.setDirection((float)Math.cos(heading+1.570796325), 0, -(float)Math.sin(heading+1.570796325));
		return leftRay;
	}	
	
	public Ray getRightRay(boolean lower) {
		if (lower)
			rightRay.setOrigin(getLowerRayOrigin());
		else
			rightRay.setOrigin(getUpperRayOrigin());
		rightRay.setDirection((float)Math.cos(heading-1.570796325), 0, -(float)Math.sin(heading-1.570796325));
		return rightRay;
	}
	
	public Ray getFrontLeftRay(boolean lower) {
		if (lower)
			frontLeftRay.setOrigin(getLowerRayOrigin());
		else
			frontLeftRay.setOrigin(getUpperRayOrigin());
		frontLeftRay.setDirection((float)Math.cos(heading+0.7853981625), 0, -(float)Math.sin(heading+0.7853981625));
		return frontLeftRay;
	}
	
	public Ray getFrontRightRay(boolean lower) {
		if (lower)
			frontRightRay.setOrigin(getLowerRayOrigin());
		else
			frontRightRay.setOrigin(getUpperRayOrigin());
		frontRightRay.setDirection((float)Math.cos(heading-0.7853981625), 0, -(float)Math.sin(heading-0.7853981625));
		return frontRightRay;
	}
	
	public Ray getBackLeftRay(boolean lower) {
		if (lower)
			backLeftRay.setOrigin(getLowerRayOrigin());
		else
			backLeftRay.setOrigin(getUpperRayOrigin());
		backLeftRay.setDirection((float)Math.cos(heading+2.3561944875), 0, -(float)Math.sin(heading+2.3561944875));
		return backLeftRay;
	}
	
	public Ray getBackRightRay(boolean lower) {
		if (lower)
			backRightRay.setOrigin(getLowerRayOrigin());
		else
			backRightRay.setOrigin(getUpperRayOrigin());
		backRightRay.setDirection((float)Math.cos(heading-2.3561944875), 0, -(float)Math.sin(heading-2.3561944875));
		return backRightRay;
	}
	
	private void renderCollisionRays(GLAutoDrawable drawable) {				
		GL gl = drawable.getGL();
		
		gl.glEnable(GL.GL_COLOR_MATERIAL);
		gl.glColor3f(1, 0, 1);
		gl.glBegin(GL.GL_LINES);

		Ray ray = getFrontRay(false);
		//Front center ray
		Point3D startPt = ray.getOrigin();
		Point3D endPt = ray.pointAtParameter(COLLISION_DISTANCE);				
		gl.glVertex3f(startPt.x, startPt.y, startPt.z);		
		gl.glVertex3f(endPt.x, endPt.y, endPt.z);
		
		//Left ray
		ray = getLeftRay(false);
		startPt = ray.getOrigin();
		endPt = ray.pointAtParameter(COLLISION_DISTANCE);				
		gl.glVertex3f(startPt.x, startPt.y, startPt.z);		
		gl.glVertex3f(endPt.x, endPt.y, endPt.z);
		
		//Right ray
		ray = getRightRay(false);
		startPt = ray.getOrigin();
		endPt = ray.pointAtParameter(COLLISION_DISTANCE);				
		gl.glVertex3f(startPt.x, startPt.y, startPt.z);		
		gl.glVertex3f(endPt.x, endPt.y, endPt.z);
		
		//front left
		ray = getFrontLeftRay(false);
		startPt = ray.getOrigin();
		endPt = ray.pointAtParameter(1.5f);				
		gl.glVertex3f(startPt.x, startPt.y, startPt.z);		
		gl.glVertex3f(endPt.x, endPt.y, endPt.z);
		
		//front right 
		ray = getFrontRightRay(false);
		startPt = ray.getOrigin();
		endPt = ray.pointAtParameter(1.5f);				
		gl.glVertex3f(startPt.x, startPt.y, startPt.z);		
		gl.glVertex3f(endPt.x, endPt.y, endPt.z);
		
		//back left
		ray = getBackLeftRay(false);
		startPt = ray.getOrigin();
		endPt = ray.pointAtParameter(1.5f);				
		gl.glVertex3f(startPt.x, startPt.y, startPt.z);		
		gl.glVertex3f(endPt.x, endPt.y, endPt.z);
		
		//back right
		ray = getBackRightRay(false);
		startPt = ray.getOrigin();
		endPt = ray.pointAtParameter(1.5f);				
		gl.glVertex3f(startPt.x, startPt.y, startPt.z);		
		gl.glVertex3f(endPt.x, endPt.y, endPt.z);
		
		//Back center ray
		ray = getBackRay(false);
		startPt = ray.getOrigin();
		endPt = ray.pointAtParameter(COLLISION_DISTANCE);				
		gl.glVertex3f(startPt.x, startPt.y, startPt.z);		
		gl.glVertex3f(endPt.x, endPt.y, endPt.z);

		gl.glEnd();
		gl.glDisable(GL.GL_COLOR_MATERIAL);
	}

	//################ Functions related to picking woodcutter ######################
	public void setAssignedNumber(int assignedNo) {
		this.assignedNo = assignedNo;		
	}
  
	public int getAssignedNumber() {
		return assignedNo;		
	}
	
	//################ Functions related to woodcutter transformation #############################
	public void calculateTransformation() {
		transformation = new Matrix4(-90, new Vector3D(1, 0, 0)).multiply(new Matrix4(new Point3D(renderScale, renderScale, renderScale), false)); 
	}
	
	public Matrix4 getTransformation() {
		return transformation;
	}
	
	public void calculateCenter_WS() {
		center_WS = transformation.multiply(center);
	}
	
	public Point3D getCenter_WS() {
		return center_WS;
	}
	
}
