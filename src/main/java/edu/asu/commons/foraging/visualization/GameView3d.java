package edu.asu.commons.foraging.visualization;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

import edu.asu.commons.foraging.client.ClientDataModel;
import edu.asu.commons.foraging.client.ForagingClient;
import edu.asu.commons.foraging.graphics.Camera;
import edu.asu.commons.foraging.graphics.Point3D;
import edu.asu.commons.foraging.graphics.PointLight;
import edu.asu.commons.foraging.graphics.ViewSettings;
import edu.asu.commons.foraging.model.Resource;
import edu.asu.commons.foraging.util.Tuple3f;
import edu.asu.commons.foraging.visualization.forestry.shader.ForestryView;
import edu.asu.commons.net.Identifier;

/*
 * Implements GLEventListener interface to incorporate basic functionality 
 * of all 3D views
 */
public abstract class GameView3d extends JPanel implements GLEventListener, KeyListener, MouseListener{

	private static final long serialVersionUID = 5560970217741196437L;
    
    protected ClientDataModel dataModel;
    protected ForagingClient client;
    
	protected GLCapabilities openGLCapabilities = new GLCapabilities();
	protected GraphicsEnvironment graphicsEnvironment = null;
    protected GraphicsDevice graphicsDevice = null;
    protected GraphicsConfiguration graphicsConfiguration = null;
    
	//protected GLJPanel glPanel = null;
    protected GLCanvas glCanvas = null;
    
    protected Dimension size = new Dimension();
	protected Tuple3f worldScaleFactor = new Tuple3f();	
	protected float worldExtent = 1;	
	
	protected float fovy = 60;
	protected float aspectRatio = 1.33f;
	protected float zNear = 0.01f;
	protected float zFar = 1000;
	
	protected Camera camera = new Camera(new Point3D(50, 75, 100));
	protected Vector<PointLight> lights = new Vector<PointLight>();
	protected boolean cameraSelected = true;
	private static float TWO_PI = (float)Math.PI * 2;
	
	protected boolean mouseClickedFlag = false;
	protected Point3D mouseClickedLocation;
	protected int rasterizationMode = GL.GL_RENDER; //Used for picking up objects at the cursor location
	
	private double[] cameraProjectionMatrix = new double[16];
	private double[] cameraModelViewMatrix = new double[16];
	private int[]  viewportMatrix = new int[4];
	
//	private enum VideoCardSupport{SHADER_SUPPORT, VBO_SUPPORT, VERTEX_ARRAY_SUPPORT};
//	private VideoCardSupport featureSupported;
//	
	protected GameView3d() {		
		graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		graphicsDevice = graphicsEnvironment.getDefaultScreenDevice();
		graphicsConfiguration = graphicsDevice.getDefaultConfiguration();
		
		//Create GLJPanel
		//glPanel = GLAutoDrawableFactory.getFactory().createGLJPanel(openGLCapabilities);		
		//glPanel.addGLEventListener(this);
		glCanvas = new GLCanvas(openGLCapabilities);
		glCanvas.addGLEventListener(this);
				
		//Add it the view
		setLayout(new BorderLayout());
		//add(glPanel, BorderLayout.CENTER);
		add(glCanvas, BorderLayout.CENTER);
				
		glCanvas.addKeyListener(this);
		glCanvas.addMouseListener(this);
	}
    
    public static GameView3d createView(ForagingClient client) {
        try {
            //String viewClass = client.getConfiguration().getClientViewClass();
            String viewClass = getViewClassName(client);
            GameView3d view = (GameView3d) Class.forName(viewClass).newInstance();            
            view.setClient(client);
            return view;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    
    private static String getViewClassName(ForagingClient client) {
    	String experimentType = client.getDataModel().getRoundConfiguration().getExperimentType().toString();
        String viewClass = "";
        
        //Forestry
        if (experimentType.equals("forestry")) {
        	/*if (GameWindow.featureSupported == GameWindow.VideoCardSupport.SHADER_SUPPORT) {        		
        		System.out.println("Shader is supported");
        		viewClass = "edu.asu.shesc.csan3d.forestry.shader.ForestryView";
        	}	
        	else if (GameWindow.featureSupported == GameWindow.VideoCardSupport.VBO_SUPPORT) {
        		System.out.println("VBO is supported");
        		viewClass = "edu.asu.shesc.csan3d.forestry.vbo.ForestryView";     		
        	}
        	else if (GameWindow.featureSupported == GameWindow.VideoCardSupport.VERTEX_ARRAY_SUPPORT) {
        		System.out.println("Vertex array is supported");*/
            viewClass = edu.asu.commons.foraging.visualization.forestry.va.ForestryView.class.getName();
        	//}
        }
        
        //Test
        else if (experimentType.equals("test")) {
//        	if (GameWindow.featureSupported == GameWindow.VideoCardSupport.SHADER_SUPPORT) {        		
//        		System.out.println("Shader is supported");
//        		viewClass = edu.asu.shesc.csan.visualization.forestry.shader.TestView.class.getName();
//        	}
//        	else if (GameWindow.featureSupported == GameWindow.VideoCardSupport.VERTEX_ARRAY_SUPPORT) {
//        		System.out.println("Vertex array is supported");
        		viewClass = edu.asu.commons.foraging.visualization.forestry.va.TestView.class.getName();
//        	}
        }
        
        //abstract
        else if (experimentType.equals("abstract")) {
        	viewClass = edu.asu.commons.foraging.visualization.conceptual.AbstractView.class.getName();
        }
        
        //2D version
        else if (experimentType.equals("costly sanctioning")) {
        	
        }
        
        return viewClass;
    }

	public void init(GLAutoDrawable drawable) {
		GL gl = drawable.getGL();
		
		//checkExtensionSupport(gl);
						
		//Set the clear color
        gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f ); //white
        
        //Set default drawing color
        gl.glColor3f( 1.0f, 1.0f, 1.0f ); //white
        
        //Enable z buffering
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glDepthFunc(GL.GL_LESS);
            	
        //Enable smooth shading 
        gl.glShadeModel(GL.GL_SMOOTH);
        
    	//Enable normalization
    	gl.glEnable(GL.GL_NORMALIZE);

    	//Enable back face culling
//    	gl.glEnable(GL.GL_CULL_FACE);
//    	gl.glCullFace(GL.GL_BACK);
//    	
    	//Enable texturing just before it is to be applied and disable it at the end
    	//This is because, if we do it here, the loaded texture gets applied to all the objects
    	//gl.glEnable(GL.GL_TEXTURE_2D);
		gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_MODULATE);
    	    	       
        //Enable lighting
    	gl.glEnable(GL.GL_LIGHTING);
        initLightModel(gl);
                
        //Create and initialize one point light source
        PointLight pointLight0 = new PointLight(GL.GL_LIGHT0);
        lights.add(pointLight0);
        pointLight0.setPosition(new Point3D(5, 250, -5), false);
        //pointLight0.setPosition(new Point3D(0, 5, 5), false);
        pointLight0.setAmbient(0.5f, 0.5f, 0.5f, 1.0f);
        pointLight0.setDiffuse(0.8f, 0.8f, 0.8f, 1.0f);
        pointLight0.setSpeculart(1.0f, 1.0f, 1.0f, 1.0f);
        pointLight0.init(gl);
	}
	
	private void initLightModel(GL gl) {
		//gl.glLightModelfv(GL.GL_LIGHT_MODEL_AMBIENT, GlobalAmbient);
		gl.glLightModeli(GL.GL_LIGHT_MODEL_LOCAL_VIEWER, GL.GL_TRUE);
		gl.glLightModeli(GL.GL_LIGHT_MODEL_TWO_SIDE, GL.GL_TRUE);
	}
	
//	private void checkExtensionSupport(GL gl) {
//		String versionStr = gl.glGetString( GL.GL_VERSION );
//		System.out.println("GL version: " + versionStr); 
//		versionStr = versionStr.substring( 0, 4);
//		
//		// Check if extensions are available.
//		if (gl.isExtensionAvailable("GL_ARB_vertex_program") && gl.isExtensionAvailable("GL_ARB_fragment_program")
//			&& gl.isFunctionAvailable("glVertexAttribPointer") ) {	
//			featureSupported = VideoCardSupport.SHADER_SUPPORT;			
//		}
//		else if (gl.isExtensionAvailable("GL_ARB_vertex_buffer_object")) {	//VBO support
////			  gl.isFunctionAvailable("glGenBuffersARB") &&
////            gl.isFunctionAvailable("glBindBufferARB") &&
////            gl.isFunctionAvailable("glBufferDataARB") &&
////            gl.isFunctionAvailable("glDrawArrays") &&
////            gl.isFunctionAvailable("glVertexPointer") &&
////            gl.isFunctionAvailable("glDeleteBuffersARB")
//			featureSupported = VideoCardSupport.VBO_SUPPORT;
//		}
//		else if (gl.isExtensionAvailable("")) {
//			featureSupported = VideoCardSupport.VERTEX_ARRAY_SUPPORT;
//		}
//		else {
//			throw new RuntimeException("Vertex array not supported. No billboard support yet.");
//		}
//	}

	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		//GL gl = GLContext.getCurrent().getGL();
		GL gl = drawable.getGL();
        GLU glu = new GLU();
                
        //Set projection matrix: clipping rectangle
        gl.glMatrixMode( GL.GL_PROJECTION );  
        gl.glLoadIdentity(); 
        //glu.gluOrtho2D( 0.0, 600.0, 0.0, 600.0); //left, right, bottom, top
  
        //The aspect ratio of a two-dimensional shape is the ratio of its longer dimension to its shorter dimension.
        if (width > height) {
        	aspectRatio = (float)width / (float)height;
        	worldScaleFactor.a = (2 * worldExtent * aspectRatio)/width;
        	worldScaleFactor.b = (2 * worldExtent)/height;        	
        }
        else {
        	aspectRatio = (float)height/ (float)width;
        	worldScaleFactor.a = (2 * worldExtent)/width;
        	worldScaleFactor.b = (2 * worldExtent * aspectRatio)/height;        	
        }
        worldScaleFactor.c = 1;
        	
        glu.gluPerspective(fovy, aspectRatio, zNear, zFar);
        //gl.glOrtho(-width/2.0d, width/2.0d, -height/2.0d, height/2.0d, zNear, zFar); //left, right, bottom, top, near, far
        gl.glGetDoublev(GL.GL_PROJECTION_MATRIX, cameraProjectionMatrix, 0);
        
        //Set View matrix: camera, look at point and up vector         
    	camera.loadModelViewMatrix(drawable);
    	gl.glGetDoublev(GL.GL_MODELVIEW_MATRIX, cameraModelViewMatrix, 0);
    	
    	//Set viewport size
        gl.glViewport( 0, 0, width, height );
        gl.glGetIntegerv(GL.GL_VIEWPORT, viewportMatrix, 0);        
        
        //Save the size for further use
        size.width = width;
        size.height = height;
	}
	
	public void display(GLAutoDrawable drawable) {
		GL gl = drawable.getGL();
				
		if (rasterizationMode == GL.GL_RENDER) {            
            // Only clear the buffers when in GL_RENDER mode. Avoids flickering            
			gl.glClear (GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		}
				
		camera.loadModelViewMatrix(drawable);
		
		for (int lightIndex = 0; lightIndex < lights.size(); lightIndex++)
		{
			lights.get(lightIndex).enable(gl);
		}	
		
		gl.glPolygonMode(GL.GL_FRONT_AND_BACK, ViewSettings.fillModel);
		gl.glShadeModel(ViewSettings.shadeModel);
		
		//displayAxes(drawable);
					
//		//Draw something as a test
//		gl.glPushMatrix();			
//			gl.glRotated((angleX % 360), 0, 0, 1); 
//			//gl.glRotated(1, angleY, 0, 0);
//			//gl.glScalef(1.0f/600.0f, 1.0f, 1.0f);
//	        drawTestGeometry(gl);
//        gl.glPopMatrix();
		
	}
	
	public void displayAxes(GLAutoDrawable drawable) {
		GL gl = drawable.getGL();
		
		float xAxisColor[] = {1.0f, 0.0f, 0.0f, 1.0f};
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT_AND_DIFFUSE, xAxisColor, 0);
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_SPECULAR, xAxisColor, 0);
		gl.glBegin(GL.GL_LINES);
			gl.glVertex3f(0, 0, 0);
			gl.glVertex3f(100, 0, 0);
		gl.glEnd();
		
		float yAxisColor[] = {0.0f, 1.0f, 0.0f, 1.0f};
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT_AND_DIFFUSE, yAxisColor, 0);
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_SPECULAR, yAxisColor, 0);
		gl.glBegin(GL.GL_LINES);
			gl.glVertex3f(0, 0, 0);
			gl.glVertex3f(0, 100, 0);
		gl.glEnd();
		
		float zAxisColor[] = {0.0f, 0.0f, 1.0f, 1.0f};
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT_AND_DIFFUSE, zAxisColor, 0);
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_SPECULAR, zAxisColor, 0);
		gl.glBegin(GL.GL_LINES);
			gl.glVertex3f(0, 0, 0);
			gl.glVertex3f(0, 0, 100);
		gl.glEnd();
	}
	
	private void drawTestGeometry(GL gl) {
		gl.glBegin (GL.GL_TRIANGLES);
			gl.glColor3f(1.0f, 0.0f, 1.0f);
			gl.glVertex3f(-300.0f, 0, 8.0f);
			gl.glVertex3f(300.0f, -0.3f, 8.0f);
			gl.glVertex3f(300.0f, 0.3f, 8.0f);
			
			gl.glColor3f(0.0f, 1.0f, 0.0f);			
			gl.glVertex3f(300.0f, -0.3f, 8.0f);
			gl.glVertex3f(300.0f, 0.3f, 8.0f);
			gl.glVertex3f(400.0f, 0, 4.0f);
							
        gl.glEnd();
        
//		gl.glLineWidth(4.0f);
//		gl.glBegin (GL.GL_LINES);
//			gl.glVertex3f(-1.0f, 0.0f, 8.0f);
//			gl.glColor3f(0.0f, 1.0f, 0.0f);
//			gl.glVertex3f(1.0f, 0.0f, 8.0f);
//		gl.glEnd();
	}

	public void displayChanged(GLAutoDrawable drawable,
            boolean modeChanged,
            boolean deviceChanged) {
		
	}
	
	//KeyListener functions
	public void keyPressed(KeyEvent e) {		
		int keyCode = e.getKeyCode();
		PointLight light = lights.get(0);
		
		switch(keyCode) {
			//Moves camera left
			case KeyEvent.VK_LEFT:
				if (cameraSelected)
					camera.setAngle( (camera.getAngle()+Camera.ANGLE_STEP) % TWO_PI);
				else
					light.rotateLeft();
				break;
			//Moves camera right
			case KeyEvent.VK_RIGHT:
				if (cameraSelected)
					camera.setAngle( (camera.getAngle()-Camera.ANGLE_STEP) % TWO_PI);
				else
					light.rotateRight();
				break;
			//Moves camera nearer the look at point 
			case KeyEvent.VK_UP:
				if (cameraSelected)
					camera.setLength(camera.getRadius()-Camera.RADIUS_STEP);
				else
					light.moveNear();
				break;
			//Moves camera farther from the look at point
			case KeyEvent.VK_DOWN:
				if (cameraSelected)
					camera.setLength(camera.getRadius()+Camera.RADIUS_STEP);
				else
					light.moveAway();
				break;
			//Moves camera up
			case KeyEvent.VK_PAGE_UP:
				if (cameraSelected)
					camera.setElevation(camera.getElevation()+Camera.ELEVATION_STEP);
				else
					light.moveUp();
				break;
			//Moves camera down
			case KeyEvent.VK_PAGE_DOWN:
				if (cameraSelected)
					camera.setElevation(camera.getElevation()-Camera.ELEVATION_STEP);
				else
					light.moveDown();
				break;		
			case KeyEvent.VK_F:
				if (ViewSettings.fillModel == GL.GL_LINE)
					ViewSettings.fillModel = GL.GL_FILL;
				else
					ViewSettings.fillModel = GL.GL_LINE;
				break;
			case KeyEvent.VK_L:
				cameraSelected = false;
				break;
			case KeyEvent.VK_C:
				cameraSelected = true;
				break;
		}
		update();
	}

	public void keyReleased(KeyEvent e) {
		
	}

	public void keyTyped(KeyEvent e) {
		
	}
	
	//MouseListener functions
	//FIXME: Modify to update view in real time on mouse motion
	public void mousePressed(MouseEvent e) {
		//oldMousePosition = e.getPoint();
	}

	public void mouseReleased(MouseEvent e) {		
//		Point newMousePosition = e.getPoint(); 
//		float delta = newMousePosition.x - oldMousePosition.x;
//		//FIXME: magic no
//		delta /= 80.0f;
//		//Moves camera left or right according to the x coordinate of the mouse
//		camera.setAngle(camera.getAngle()+(delta*Camera.ANGLE_STEP) % TWO_PI);
//		
//		delta = newMousePosition.y - oldMousePosition.y;
//		delta /= 80.0f;
//		//Moves camera nearer or farther from the look at point according to the y coordinate of the mouse
//		camera.setRadius(camera.getRadius()+(delta*Camera.RADIUS_STEP));	
//		
//		oldMousePosition = newMousePosition;
//		update();
	}
	
	public void mouseClicked(MouseEvent e) {
		mouseClickedFlag = true;
		mouseClickedLocation = new Point3D(e.getX(), e.getY(), 0);
//		System.out.println("Mouse clicked location " + mouseClickedLocation);
	}

	public void mouseEntered(MouseEvent e) {

	}

	public void mouseExited(MouseEvent e) {

	}
		
	public void update() {
		glCanvas.repaint();
	}
	
	public void requestFocus() {
		glCanvas.requestFocusInWindow();
	}
    
    abstract public void initialize();
	
    //Test function
    public static void main(String[] args) {    
        try {
            UIManager.setLookAndFeel(
                UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) { }
        
//        final AbstractView view = new AbstractView(); 
      final ForestryView view = new ForestryView();
//      final TestView view = new TestView();
        
        JFrame frame = new JFrame("Forest Commons Game");
        //Get screen width and height
        //Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        //Set window size same as the screen size
        //frame.setPreferredSize(new Dimension(screenSize.width, screenSize.height));
        frame.setPreferredSize(new Dimension(640, 480));
        frame.setLocation(new Point(100, 100));

        frame.add(view);
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //Make glCanvas get the focus whenever frame is activated.
        frame.addWindowListener(new WindowAdapter() {
            public void windowActivated(WindowEvent e) {
                view.update();
            }
        });
    }

    public void setDataModel(ClientDataModel dataModel) {
        this.dataModel = dataModel;        
    }
    
    public ClientDataModel getDataModel() {
        return dataModel;        
    }

    public abstract void updateResources();

    public ForagingClient getClient() {
        return client;
    }

    public void setClient(ForagingClient client) {
        this.client = client;
    }

    //Indicates the resource lock is successful
    public void highlightResource(Resource resource) {
        
    }

    //Indicates the resource lock is unsuccessful
    public void flashResource(Resource resource) {
        
    }

    public void updateAgentPositions() {
        
    }
    
    public boolean shouldChat(Identifier clientId1, Identifier clientId2) {		
		return false;
	}
    
    public double[] getCameraProjectionMatrix() {
    	return cameraProjectionMatrix;
    }
    
    public double[] getCameraModelViewMatrix() {
    	return cameraModelViewMatrix;
    }
    
    public int[] getViewportMatrix() {
    	return viewportMatrix;
    }
}
