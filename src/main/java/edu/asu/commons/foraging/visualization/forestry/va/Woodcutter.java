package edu.asu.commons.foraging.visualization.forestry.va;

import java.awt.Color;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Vector;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.glu.GLU;

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
import edu.asu.commons.foraging.visualization.GameView3d;

/**
 * The Woodcutter class represents an avatar in the forestry experiment. The woodcutter is created by loading files in cal3d format. These files
 * include skeleton file, mesh file, animation files and material files. Skeleton, mesh and material files are used to render the woodcutter
 * in the forestry visualization. Animation files are used to animate the woodcutter to perform different actions. The actions supported are
 * standing, walking and axing.  
 * @author <a href='deepali.bhagvat@asu.edu'>Deepali Bhagvat</a>
 * @version $Revision: 4 $
 *
 */
public class Woodcutter extends GraphicsObject implements MovingObject {

	private static final long serialVersionUID = 3225396389123396264L;
	
	//File related variables
	/**
	 * Keyword used for path in the cal3d file 
	 */
	protected static final String PATH = "path";
	
	/**
	 * Keyword used for scale in the cal3d file
	 */
	protected static final String SCALE = "scale";
	
	/**
	 * Keyword used for skeleton in the cal3d file
	 */
	protected static final String SKELETON = "skeleton";
	
	/**
	 * Keyword used for animation in the cal3d file
	 */
	protected static final String ANIMATION = "animation";
	
	/**
	 * Keyword used for mesh in the cal3d file
	 */
	protected static final String MESH = "mesh";
	
	/**
	 * Keyword used for material in the cal3d file
	 */
	protected static final String MATERIAL = "material";
		
	/**
	 * Path of the cal3d files
	 */
	protected String dataPath = new String();
		
	/**
	 * Position of this woodcutter 
	 */
	protected Point3D position;
	
	/**
	 * Angle (in radians) made by this woodcutter with the positive X-axis 
	 */
	protected float heading; 
	
	/**
	 * Walking speed of this woodcutter 
	 */
	protected float speed;		
	
	/**
	 * Position of the woodcutter label
	 */
	protected Point lablePosition;
	
	/**
	 * Angle step (in radians) used while rotating this woodcutter
	 */
	protected static float ANGLE_STEP = 0.03f;
	
	/**
	 * Radians to degrees conversion factor
	 */
	protected static float RADIANS_TO_DEGREES = 57.3f;
	
	/**
	 * Maximum distance between this woodcutter and trees at which collision test is positive  
	 */
	protected static float COLLISION_DISTANCE = 1.5f;
	
	//Model related variables
	/**
	 * Core model containing skeleton, mesh, materials and animations
	 */
	protected CoreModel coreModel;
	
	/**
	 * Instance of the core model
	 */
	protected Model model;
	
	/**
	 * Scale used to render this woodcutter
	 */
	protected float renderScale;

	//Animation related variables
	/**
	 * Standing animation state
	 */
	public static final int STATE_IDLE = 0;
	
	/**
	 * Walking animation state
	 */
	public static final int STATE_WALKING = 1;
	
	/**
	 * Axing animation state
	 */
	public static final int STATE_AXING = 2;
	
	/**
	 * Current animation state
	 */
	protected int animationState;
	
	/**
	 * Array storing animation information according to the animation state
	 */
	protected int animationIds[] = new int[3];
	
	/**
	 * Total no. of animations present
	 */
	protected int animationCount;
	
	/**
	 * Weights used while blending the animations
	 */
	protected float motionBlend[] = new float[3];
	
	/**
	 * Duration of the animation cycle
	 */
	protected float duration;
	
	/**
	 * System time at the start of a animation cycle
	 */
	protected long lastTick;
	
	//Collision related variables
	/**
	 * Ray from the center of this woodcutter pointing in the forward direction
	 */
	private Ray frontRay = new Ray(new Point3D(), new Vector3D());
	
	/**
	 * Ray from the center of this woodcutter pointing in the left direction
	 */
	private Ray leftRay = new Ray(new Point3D(), new Vector3D());
		
	/**
	 * Ray from the center of this woodcutter pointing in the right direction
	 */
	private Ray rightRay = new Ray(new Point3D(), new Vector3D());
	
	/**
	 * Ray from the center of this woodcutter pointing in the backward direction
	 */
	private Ray backRay = new Ray(new Point3D(), new Vector3D());
	
	/**
	 * Ray from the center of this woodcutter pointing in the front left direction
	 */
	private Ray frontLeftRay = new Ray(new Point3D(), new Vector3D());
	
	/**
	 * Ray from the center of this woodcutter pointing in the front right direction
	 */
	private Ray frontRightRay = new Ray(new Point3D(), new Vector3D());
	
	/**
	 * Ray from the center of this woodcutter pointing in the back left direction
	 */
	private Ray backLeftRay = new Ray(new Point3D(), new Vector3D());
	
	/**
	 * Ray from the center of this woodcutter pointing in the back right direction
	 */
	private Ray backRightRay = new Ray(new Point3D(), new Vector3D());
	
	/**
	 * Lower center of this woodcutter
	 */
	protected static Point3D lowerRayOffset = new Point3D(0, 3, 0);
	
	/**
	 * Upper center of this woodcutter
	 */
	protected static Point3D upperRayOffset = new Point3D(0, 14, 0);
	
	/**
	 * Flag specifying if the woodcutter's axe has hit the tree in the current axing animation frame
	 */
	protected boolean axeHitInAFrame = false;
	
	/**
	 * Game view instantiating this woodcutter
	 */
	private GameView3d parentView;
	
	/**
	 * Matrix representing the transformation applied to this woodcutter
	 */
	private Matrix4 transformation = null;
	
	/**
	 * Woodcutter center in world space
	 */
	private Point3D center_WS = null;
	
	/**
	 * Interface to the graphics library utility 
	 */
	private GLU glu = new GLU();

	/**
	 * No. assigned to this woodcutter in his group
	 */
	private int assignedNo;
	
	/**
	 * Creates a new woodcutter with the specified parameters 
	 * @param position location of the woodcutter in the game world 
	 * @param heading direction in which the woodcutter is facing
	 * @param speed walking speed of the woodcutter
	 * @param parentView game view instantiating this woodcutter 
	 */
	public Woodcutter(Point3D position, int heading, float speed, GameView3d parentView) {
		this();	
		this.position = position;
		this.heading = heading;
		this.speed = speed;		
		this.parentView = parentView;
	}

	/**
	 * Creates and initializes the model related variables 
	 */
	private Woodcutter() {
	  coreModel = new CoreModel("dummy");

	  renderScale = 1.0f;
	  animationState = STATE_IDLE;
	  motionBlend[STATE_IDLE] = 1.0f;
	  motionBlend[STATE_WALKING] = 0.0f;
	  motionBlend[STATE_AXING] = 0.0f;	  
	  animationCount = 0; 
	  lastTick = System.currentTimeMillis();
	}

	/**
	 * Initializes woodcutter by loading the model from the specified .cfg file and using the specified material colors
	 * @param filePath path of the .cfg file
	 * @param hairColor material used to render woodcutter hair
	 * @param skinColor material used to render woodcutter skin
	 * @param shirtColor material used to render woodcutter shirt
	 * @param trouserColor material used to render woodcutter trousers
	 * @param shoesColor material used to render woodcutter shoes
	 * @return true if the model is loaded from the files successfully, false otherwise
	 */
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

	/**
	 * Renders this woodcutter
	 * @param drawable current rendering context 
	 */
	public void display(GLAutoDrawable drawable) {
		GL gl = drawable.getGL();
		
		gl.glDisable(GL.GL_TEXTURE_2D);
		gl.glPushMatrix();
			//Translate to displace the object
			gl.glTranslated(position.x, position.y, position.z);

			gl.glPushMatrix();
				gl.glRotated((heading * RADIANS_TO_DEGREES) + 90, 0, 1, 0);  
				
				gl.glRotatef(-90, 1, 0, 0);		
				gl.glScalef(renderScale, renderScale, renderScale);			
				
				boolean wireFrame = false;
				renderMesh(wireFrame, drawable);				
											
			gl.glPopMatrix();			
		gl.glPopMatrix();
		
		updateLablePosition(drawable);
		
//		renderCollisionRays(drawable);
	}
	
	/**
	 * Calculates extents of the woodcutter mesh
	 */
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
	
	/**
	 * Checks if the woodcutter has hit the tree in this animation cycle
	 * @return true if this is the first hit, false otherwise
	 */
	public boolean isNewHit() {
		if (!axeHitInAFrame) {
			axeHitInAFrame = true;
			return true;
		}
		return false;
	}
		
	/**
	 * Reads the cfg file and all other files specified in the cfg file and loads the core model of this woodcutter 
	 * @param filePath path of the cfg file
	 * @return true if the model is loaded successfully, false otherwise
	 */
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
	
	/**
	 * Loads textures specified as the material maps
	 */
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
	
	/**
	 * Creates one material thread for each material
	 */
	private void createMaterialThreads() {
		// NOTE: this is not the right way to do it, but this viewer can't do the right
		// mapping without further information on the model etc.
		for(int materialIndex = 0; materialIndex < coreModel.getCoreMaterialCount(); materialIndex++) {
			// create the a material thread
		    coreModel.createCoreMaterialThread(materialIndex);

		    // initialize the material thread
		    coreModel.setCoreMaterialId(materialIndex, 0, materialIndex);
		 }
	}
	
	/**
	 * Creates an instance of the core model used to render and animate this woodcutter 
	 */
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
	
	/**
	 * Saves the path specified in the cfg file which points to the location of all other cal3d files
	 * @param dataPath path specified in the cfg file
	 */
	public void setDataPath(String dataPath) {
		this.dataPath = dataPath;
	}

	/**
	 * Renders the woodcutter mesh either in wireframe or solid form
	 * @param bWireframe true if the mesh is to be rendered as wireframe, false otherwise
	 * @param drawable current rendering context
	 */
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
	
	/**
	 * Animates other avatars
	 * @param state animation state
	 */
	public void animate(int state) {			
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

	/**
	 * Animates this woodcutter as axing
	 */
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
	
	/**
	 * Animates this woodcutter as walking 
	 */
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


	/**
	 * Returns the current animation state
	 * @return current animation state
	 */
	public int getAnimationState() {
		return animationState;
	}
	
	/**
	 * Sets the animation state to the specified state
	 * @param state new animation state
	 */
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

	/**
	 * Sets the new animation state within the given time interval
	 * @param state new animation state
	 * @param delay time interval for switching from the current animation state to the new state
	 */
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
	
	/**
	 * Sets the motion blend factors state of the model within the given time interval
	 * @param motionBlend motion blend factors state
	 * @param delay time interval for blending 
	 */
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
		
	/**
	 * Moves the woodcutter forward in the direction he is heading. The function assumes that the woodcutter's initial position is origin and
	 * is heading in the positive X direction
	 */
	public void forward() {		
		position.x = position.x + speed * (float)Math.cos(heading); 
		//e. g. if angle with X axis is 0, go forward in the X direction
		
		position.z = position.z - speed * (float)Math.sin(heading); 
		//e. g. if angle with X axis is 90, go forward in negative Z direction
	}

	/**
	 * Moves the woodcutter backward, in the direction he is heading. 
	 */
	public void reverse() {
		position.x = position.x - speed * (float)Math.cos(heading); 
		position.z = position.z + speed * (float)Math.sin(heading);
	}

	/**
	 * Rotates the woodcutter in anti-clockwise direction
	 */
	public void moveLeft() {
		heading = heading + ANGLE_STEP;
	}
	
	/**
	 * Rotates the woodcutter in clockwise direction
	 */
	public void moveRight() {
		heading = heading - ANGLE_STEP; 
	}
	
	/**
	 * Returns a position if this woodcutter takes a step forward.
	 * @return forward position
	 */
	public Point3D getForwardPosition() {
		return new Point3D(	position.x + speed * (float)Math.cos(heading), 
							position.y, 
							position.z - speed * (float)Math.sin(heading));
	}
	
	/**
	 * Returns a position if this woodcutter takes a step backwards.
	 * @return backward position
	 */
	public Point3D getBackwardPosition() {
		return new Point3D(	position.x - speed * (float)Math.cos(heading), 
							position.y, 
							position.z + speed * (float)Math.sin(heading));
	}
	
	/**
	 * Returns the current position of this woodcutter
	 * @return current position
	 */
	public Point3D getPosition() {
		return position;
	}
	
	/**
	 * Returns the label position of this woodcutter
	 * @return label position
	 */
	public Point getLabelPosition() {
		return this.lablePosition;
	}
	
	/**
	 * Updates lable position of this woodcutter according to its new position.
	 * @param drawable current rendering context
	 */
	public void updateLablePosition(GLAutoDrawable drawable) {
		double[] lablePos = new double[3];
		double[] cameraModelViewMatrix = new double[16];
		
		GL gl = drawable.getGL();
		gl.glGetDoublev(GL.GL_MODELVIEW_MATRIX, cameraModelViewMatrix, 0);
		
		boolean success = glu.gluProject(position.x, position.y, position.z, 
				cameraModelViewMatrix, 0, parentView.getCameraProjectionMatrix(), 0, parentView.getViewportMatrix(), 0,
				lablePos, 0); 
		
		if (!success) {
			System.err.println("Could not project the avatar position");
			this.lablePosition = new Point(-100000, -100000);
		}
		else {
			this.lablePosition = new Point((int)lablePos[0]-20, (int)lablePos[1]-20);
		}
	}
	
	/**
	 * Sets the woodcutter at the specified position 
	 * @param position new position
	 */
	public void setPosition(Point3D position) {
		this.position = position;
	}
	
	/**
	 * Returns heading of this woodcutter, the angle made by him with the positive X-axis
	 * @return angle with positive X-axis
	 */
	public float getHeading() {
		return heading;
	}
	
	/**
	 * Sets heading of this woodcutter to the specified angle
	 * @param heading new angle with positive X-axis
	 */
	public void setHeading(float heading) {
		this.heading = heading;
	}
	
	/**
	 * Sets woodcutter elevation to the specified value 
	 * @param y new elevation
	 */
	public void setElevation(float y) {
		this.position.y = y;
	}

	/**
	 * Returns woodcutter speed 
	 * @return speed
	 */
	public float getSpeed() {
		return speed;
	}
	
	/**
	 * Sets skin color of this woodcutter to the specified color
	 * @param color new skin color
	 */
	public void setSkinColor(Color color) {
		int materialIndex = 0;
		coreModel.setCoreMaterial(materialIndex, color);		
	}
	
	/**
	 * Sets shoes color of this woodcutter to the specified color
	 * @param color new shoes color
	 */
	public void setShoesColor(Color color) {
		int materialIndex = 1;
		coreModel.setCoreMaterial(materialIndex, color);
	}
	
	/**
	 * Sets trouser color of this woodcutter to the specified color
	 * @param color new trouser color
	 */
	public void setTrouserColor(Color color) {
		int materialIndex = 2;
		coreModel.setCoreMaterial(materialIndex, color);
	}
	
	/**
	 * Sets shirt color of this woodcutter to the specified color
	 * @param color new shirt color
	 */
	public void setShirtColor(Color color) {
		int materialIndex = 3;
		coreModel.setCoreMaterial(materialIndex, color);
	}
	
	//################ Functions related to collision ###############
	/**
	 * Returns origin of the lower rays used for detecting collision between this woodcutter and trees
	 * @return origin
	 */
	private Point3D getLowerRayOrigin() {
		return position.add(lowerRayOffset);
	}
	
	/**
	 * Returns origin of the upper rays used for detecting collision between this woodcutter and trees
	 * @return origin
	 */
	private Point3D getUpperRayOrigin() {
		return position.add(upperRayOffset);
	}
	
	/**
	 * Returns the upper or lower ray in the front direction of this woodcutter to detect collision with trees
	 * @param lower if true, returns the lower front ray, else the upper front ray
	 * @return ray in the front direction
	 */
	public Ray getFrontRay(boolean lower) {
		if (lower)
			frontRay.setOrigin(getLowerRayOrigin());
		else
			frontRay.setOrigin(getUpperRayOrigin());
		frontRay.setDirection((float)Math.cos(heading), 0, -(float)Math.sin(heading));
		return frontRay;
	}
	
	/**
	 * Returns the lower or upper ray in the back direction of this woodcutter to detect collision with trees
	 * @param lower if true, returns the lower back ray, else the upper back ray
	 * @return ray in the back direction
	 */
	public Ray getBackRay(boolean lower) {
		if (lower)
			backRay.setOrigin(getLowerRayOrigin());
		else
			backRay.setOrigin(getUpperRayOrigin());
		backRay.setDirection(-(float)Math.cos(heading), 0, (float)Math.sin(heading));
		return backRay;
	}
	
	/**
	 * Returns the lower or upper ray in the left direction of this woodcutter to detect collision with trees
	 * @param lower if true, returns the lower left ray, else the upper left ray
	 * @return ray in the left direction
	 */
	public Ray getLeftRay(boolean lower) {
		if (lower)
			leftRay.setOrigin(getLowerRayOrigin());
		else
			leftRay.setOrigin(getUpperRayOrigin());
		leftRay.setDirection((float)Math.cos(heading+1.570796325), 0, -(float)Math.sin(heading+1.570796325));
		return leftRay;
	}	
	
	/**
	 * Returns the lower or upper ray in the right direction of this woodcutter to detect collision with trees
	 * @param lower if true, returns the lower right ray, else the upper right ray
	 * @return ray in the right direction
	 */
	public Ray getRightRay(boolean lower) {
		if (lower)
			rightRay.setOrigin(getLowerRayOrigin());
		else
			rightRay.setOrigin(getUpperRayOrigin());
		rightRay.setDirection((float)Math.cos(heading-1.570796325), 0, -(float)Math.sin(heading-1.570796325));
		return rightRay;
	}
	
	/**
	 * Returns the lower or upper ray in the front-left direction of this woodcutter to detect collision with trees
	 * @param lower if true, returns the lower front-left ray, else the upper front-left ray
	 * @return ray in the front-left direction
	 */
	public Ray getFrontLeftRay(boolean lower) {
		if (lower)
			frontLeftRay.setOrigin(getLowerRayOrigin());
		else
			frontLeftRay.setOrigin(getUpperRayOrigin());
		frontLeftRay.setDirection((float)Math.cos(heading+0.7853981625), 0, -(float)Math.sin(heading+0.7853981625));
		return frontLeftRay;
	}
	
	/**
	 * Returns the lower or upper ray in the front-right direction of this woodcutter to detect collision with trees
	 * @param lower if true, returns the lower front-right ray, else the upper front-right ray
	 * @return ray in the front-right direction
	 */
	public Ray getFrontRightRay(boolean lower) {
		if (lower)
			frontRightRay.setOrigin(getLowerRayOrigin());
		else
			frontRightRay.setOrigin(getUpperRayOrigin());
		frontRightRay.setDirection((float)Math.cos(heading-0.7853981625), 0, -(float)Math.sin(heading-0.7853981625));
		return frontRightRay;
	}
	
	/**
	 * Returns the lower or upper ray in the back-left direction of this woodcutter to detect collision with trees
	 * @param lower if true, returns the lower back-left ray, else the upper back-left ray
	 * @return ray in the back-left direction
	 */
	public Ray getBackLeftRay(boolean lower) {
		if (lower)
			backLeftRay.setOrigin(getLowerRayOrigin());
		else
			backLeftRay.setOrigin(getUpperRayOrigin());
		backLeftRay.setDirection((float)Math.cos(heading+2.3561944875), 0, -(float)Math.sin(heading+2.3561944875));
		return backLeftRay;
	}
	
	/**
	 * Returns the lower or upper ray in the back-right direction of this woodcutter to detect collision with trees
	 * @param lower if true, returns the lower back-right ray, else the upper back-right ray
	 * @return ray in the back-right direction
	 */
	public Ray getBackRightRay(boolean lower) {
		if (lower)
			backRightRay.setOrigin(getLowerRayOrigin());
		else
			backRightRay.setOrigin(getUpperRayOrigin());
		backRightRay.setDirection((float)Math.cos(heading-2.3561944875), 0, -(float)Math.sin(heading-2.3561944875));
		return backRightRay;
	}
	
	/**
	 * Debug function to render the collision rays
	 * @param drawable current rendering context
	 */
	private void renderCollisionRays(GLAutoDrawable drawable) {				
		GL gl = drawable.getGL();
		
		gl.glEnable(GL.GL_COLOR_MATERIAL);
		gl.glColor3f(1, 0, 1);
		gl.glBegin(GL.GL_LINES);

		Ray ray = getFrontRay(true);
		//Front center ray
		Point3D startPt = ray.getOrigin();
		Point3D endPt = ray.pointAtParameter(COLLISION_DISTANCE);				
		gl.glVertex3f(startPt.x, startPt.y, startPt.z);		
		gl.glVertex3f(endPt.x, endPt.y, endPt.z);
		
		//Left ray
		ray = getLeftRay(true);
		startPt = ray.getOrigin();
		endPt = ray.pointAtParameter(COLLISION_DISTANCE);				
		gl.glVertex3f(startPt.x, startPt.y, startPt.z);		
		gl.glVertex3f(endPt.x, endPt.y, endPt.z);
		
		//Right ray
		ray = getRightRay(true);
		startPt = ray.getOrigin();
		endPt = ray.pointAtParameter(COLLISION_DISTANCE);				
		gl.glVertex3f(startPt.x, startPt.y, startPt.z);		
		gl.glVertex3f(endPt.x, endPt.y, endPt.z);
		
		//front left
		ray = getFrontLeftRay(true);
		startPt = ray.getOrigin();
		endPt = ray.pointAtParameter(1.5f);				
		gl.glVertex3f(startPt.x, startPt.y, startPt.z);		
		gl.glVertex3f(endPt.x, endPt.y, endPt.z);
		
		//front right 
		ray = getFrontRightRay(true);
		startPt = ray.getOrigin();
		endPt = ray.pointAtParameter(1.5f);				
		gl.glVertex3f(startPt.x, startPt.y, startPt.z);		
		gl.glVertex3f(endPt.x, endPt.y, endPt.z);
		
		//back left
		ray = getBackLeftRay(true);
		startPt = ray.getOrigin();
		endPt = ray.pointAtParameter(1.5f);				
		gl.glVertex3f(startPt.x, startPt.y, startPt.z);		
		gl.glVertex3f(endPt.x, endPt.y, endPt.z);
		
		//back right
		ray = getBackRightRay(true);
		startPt = ray.getOrigin();
		endPt = ray.pointAtParameter(1.5f);				
		gl.glVertex3f(startPt.x, startPt.y, startPt.z);		
		gl.glVertex3f(endPt.x, endPt.y, endPt.z);
		
		//Back center ray
		ray = getBackRay(true);
		startPt = ray.getOrigin();
		endPt = ray.pointAtParameter(COLLISION_DISTANCE);				
		gl.glVertex3f(startPt.x, startPt.y, startPt.z);		
		gl.glVertex3f(endPt.x, endPt.y, endPt.z);

		gl.glEnd();
		gl.glDisable(GL.GL_COLOR_MATERIAL);
	}

	//################ Functions related to picking woodcutter ######################
	/**
	 * Sets the no. assigned to this woodcutter
	 * @param assignedNo no. assigned to this woodcutter
	 */
	public void setAssignedNumber(int assignedNo) {
		this.assignedNo = assignedNo;		
	}
  
	/**
	 * Returns the no. assigned to this woodcutter  
	 * @return no. assigned to this woodcutter
	 */
	public int getAssignedNumber() {
		return assignedNo;		
	}
	
	//################ Functions related to woodcutter transformation #############################
	/**
	 * Calculates transformation applied to this woodcutter
	 */
	public void calculateTransformation() {
		transformation = new Matrix4(-90, new Vector3D(1, 0, 0)).multiply(new Matrix4(new Point3D(renderScale, renderScale, renderScale), false)); 
	}
	
	/**
	 * Returns transformation applied to this woodcutter
	 * @return transformations applied to this woodcutter
	 */
	public Matrix4 getTransformation() {
		return transformation;
	}
	
	/**
	 * Calculates center of this woodcutter in world space
	 */
	public void calculateCenter_WS() {
		center_WS = transformation.multiply(center);
	}
	
	/**
	 * Returns center of this woodcutter in world space
	 * @return center in world space
	 */
	public Point3D getCenter_WS() {
		return center_WS;
	}
	
}
