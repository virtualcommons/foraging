package edu.asu.commons.foraging.visualization.conceptual;

import java.awt.Color;
import java.awt.Point;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

import edu.asu.commons.foraging.graphics.MovingObject;
import edu.asu.commons.foraging.graphics.Point3D;
import edu.asu.commons.foraging.graphics.RGBA;
import edu.asu.commons.foraging.graphics.Texture;
import edu.asu.commons.foraging.graphics.TextureLoader;
import edu.asu.commons.foraging.visualization.GameView3d;


public class Agent implements MovingObject {
	public static float radius;
	public static float speed;
	public static RGBA specular;
	//FIXME: Make above 3 variables class variables and set using constructor
	public static int PITCH_STEP = 10;
	public static int SLIDE_STEP = 2;
		
	protected GameView3d parentView;
	protected Point3D position;
	protected Point lablePosition;
	protected float roll = 0;		//angle around X-axis
	protected float heading = 0;	//angle around Y-axis
	protected float pitch = 0;		//angle around Z-axis	
	protected RGBA ambientColor;
	protected RGBA diffuseColor;
	public static String textureFile = "data/abstract/grid.jpg";
	protected Texture texture = null;
	protected int displayListID = -1;
	protected boolean displayListDirty = true;
	protected GLUquadric quadric = null;
//	private Ray frontRay = new Ray(new Point3D(), new Vector3D());
//	private Ray leftRay = new Ray(new Point3D(), new Vector3D());
//	private Ray rightRay = new Ray(new Point3D(), new Vector3D());
//	private Ray backRay = new Ray(new Point3D(), new Vector3D());
//	private Ray frontLeftRay = new Ray(new Point3D(), new Vector3D());
//	private Ray frontRightRay = new Ray(new Point3D(), new Vector3D());
//	private Ray backLeftRay = new Ray(new Point3D(), new Vector3D());
//	private Ray backRightRay = new Ray(new Point3D(), new Vector3D());
	protected static float RADIANS_TO_DEGREES = 57.3f;
	protected static final float ANGLE_STEP = 0.03f; //0.034906585f; //In radians
	private static final int slices = 24;	//The number of subdivisions around the z-axis.
	private static final int stacks = 24;	//The number of subdivisions along the z-axis.
	private GLU glu = new GLU();
	
	private int assignedNo;			//Represents the no. of this woodcutter in the group
	
    public Agent(Point3D position, float angle, RGBA color, String textureFile, int assignedNo, GameView3d parentView) {
		this.position = position;
		this.heading = angle;		
		this.diffuseColor = color;
		setAmbientColor();
		this.assignedNo = assignedNo;
		this.parentView = parentView;
		
		if (textureFile != null) {		//(!textureFile.equals("")) {
			TextureLoader texLoader = new TextureLoader();
			texture = texLoader.getTexture(textureFile, true);
		}
		//headingRay = new Ray(position, new Vector3D(1, 0, 0));
	}
	
	public void display(GLAutoDrawable drawable) {
		GL gl = drawable.getGL();
		GLU glu = new GLU();
		
		gl.glPushMatrix();
		gl.glTranslatef(position.x, position.y+radius, position.z);
		//gl.glRotatef(roll, 1, 0, 0);
		gl.glRotated((heading * RADIANS_TO_DEGREES), 0, 1, 0); 	
		gl.glRotatef(pitch, 0, 0, 1);
		//gl.glRotatef(-90, 1, 0, 0);
			
			if (displayListDirty) {
				displayListDirty = false;
						    
				if (quadric == null) {
					quadric = glu.gluNewQuadric();
					glu.gluQuadricNormals(quadric, GLU.GLU_SMOOTH);
					if (texture != null) {
						texture.create(drawable);
					}
				}				
			
				if (displayListID != -1) {
					gl.glDeleteLists(displayListID, 1);					
				}
		        displayListID = gl.glGenLists(1);
				gl.glNewList(displayListID, GL.GL_COMPILE_AND_EXECUTE);
								
					//Apply material
					gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT, ambientColor.getfv(), 0);
					gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_DIFFUSE, diffuseColor.getfv(), 0);		
					gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_SPECULAR, Agent.specular.getfv(), 0);
					
					//Apply texture, if any
					if (texture != null) {
						texture.load(gl);
						gl.glEnable(GL.GL_TEXTURE_2D);
						glu.gluQuadricTexture(quadric, true);
					}
					else {
						gl.glDisable(GL.GL_TEXTURE_2D);
						glu.gluQuadricTexture(quadric, false);
					}
					//glu.gluCylinder(quadric, Agent.radius, 0, Agent.radius*2.0, slices, stacks);
					glu.gluSphere(quadric, Agent.radius, slices, stacks);
					gl.glDisable(GL.GL_TEXTURE_2D);
				gl.glEndList();
			}
			else {
				gl.glCallList(displayListID);
			}
		gl.glPopMatrix();
		
		updateLablePosition(drawable);
		
		//Display heading ray
		//headingRay.display(drawable, 10);
	}
	
	public void forward() {
		position.x = position.x + speed * (float)Math.cos(heading);
		position.z = position.z - speed * (float)Math.sin(heading);
		//headingRay.setOrigin(position);
		pitch = pitch - PITCH_STEP;
		if (pitch < 0) pitch = 360 + pitch;		
	}
	
	public void reverse() {
		position.x = position.x - speed * (float)Math.cos(heading);
		position.z = position.z + speed * (float)Math.sin(heading);	
		pitch = (pitch + PITCH_STEP) % 360;
		//headingRay.setOrigin(position);
	}
	
	public void moveLeft() {
		//position.z = position.z - SLIDE_STEP;
		//headingRay.setOrigin(position);
		
		heading = heading + ANGLE_STEP;
		//headingRay.setDirection((float)Math.cos(Math.toRadians(heading)), 0, -(float)Math.sin(Math.toRadians(heading)));
		
		//roll = roll - PITCH_STEP;
		//if (roll < 0) roll = 360 + roll;
	}
	
	public void moveRight() {
		//position.z = position.z + SLIDE_STEP;
		//headingRay.setOrigin(position);
		
		heading = heading - ANGLE_STEP;
		//headingRay.setDirection((float)Math.cos(Math.toRadians(heading)), 0, -(float)Math.sin(Math.toRadians(heading)));
		
		//roll = (roll + PITCH_STEP) % 360;
	}
	
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

	/*public Ray getFrontRay() {
		frontRay.setOrigin(position);
		frontRay.setDirection((float)Math.cos(heading), 0, -(float)Math.sin(heading));
		return frontRay;
	}
	
	public Ray getBackRay() {
		backRay.setOrigin(position);
		backRay.setDirection(-(float)Math.cos(heading), 0, (float)Math.sin(heading));
		return backRay;
	}
	
	public Ray getLeftRay() {
		leftRay.setOrigin(position);
		leftRay.setDirection((float)Math.cos(heading+1.570796325), 0, -(float)Math.sin(heading+1.570796325));
		return leftRay;
	}	
	
	public Ray getRightRay() {
		rightRay.setOrigin(position);
		rightRay.setDirection((float)Math.cos(heading-1.570796325), 0, -(float)Math.sin(heading-1.570796325));
		return rightRay;
	}
	
	public Ray getFrontLeftRay() {
		frontLeftRay.setOrigin(position);
		frontLeftRay.setDirection((float)Math.cos(heading+0.7853981625), 0, -(float)Math.sin(heading+0.7853981625));
		return frontLeftRay;
	}
	
	public Ray getFrontRightRay() {
		frontRightRay.setOrigin(position);
		frontRightRay.setDirection((float)Math.cos(heading-0.7853981625), 0, -(float)Math.sin(heading-0.7853981625));
		return frontRightRay;
	}
	
	public Ray getBackLeftRay() {
		backLeftRay.setOrigin(position);
		backLeftRay.setDirection((float)Math.cos(heading+2.3561944875), 0, -(float)Math.sin(heading+2.3561944875));
		return backLeftRay;
	}
	
	public Ray getBackRightRay() {
		backRightRay.setOrigin(position);
		backRightRay.setDirection((float)Math.cos(heading-2.3561944875), 0, -(float)Math.sin(heading-2.3561944875));
		return backRightRay;
	}
	*/
//	public boolean isIntersecting(BoundingBox boundingBox) {
//		return boundingBox.intersects(headingRay, Agent.radius);		
//	}

	public Point3D getPosition() {
		return position;
	}
	
	public Point getLabelPosition() {
		return this.lablePosition;
	}
	
	public void updateLablePosition(GLAutoDrawable drawable) {
		double[] lablePos = new double[3];		
		double[] cameraModelViewMatrix = new double[16];
		
		GL gl = drawable.getGL();
		gl.glGetDoublev(GL.GL_MODELVIEW_MATRIX, cameraModelViewMatrix, 0);
		
		boolean success = glu.gluProject(position.x, position.y, position.z, 
				cameraModelViewMatrix, 0, parentView.getCameraProjectionMatrix(), 0, parentView.getViewportMatrix(), 0,
				lablePos, 0); 
		
		if (!success) {
			//System.err.println("Could not project the avatar position");
			this.lablePosition = new Point(-1000, -1000);
		}
		else {
			this.lablePosition = new Point((int)lablePos[0]-20, (int)lablePos[1]-20);
			//System.out.println("Avatar position " + position.x + ", " + position.y + ", " + position.z);
			//System.out.println("Lable position " + lablePos[0] + ", " + lablePos[1] + ", " + lablePos[2]);
		}
	}
	
	public void setPosition(Point3D position) {
		this.position = position;
	}
	
	public Point3D getCenter() {
		return new Point3D(0, radius, 0);
	}
	
	public float getHeading() {
		return heading;
	}

	public void setHeading(float heading) {
		this.heading = heading;
	}
	
//	################ Functions related to picking woodcutter ######################
	public void setAssignedNumber(int assignedNo) {
		this.assignedNo = assignedNo;		
	}
  
	public int getAssignedNumber() {
		return assignedNo;		
	}

	public void setColor(RGBA diffuseColor) {
		this.diffuseColor = diffuseColor;
		setAmbientColor();
		displayListDirty = true;
	}
	
	private void setAmbientColor() {
		Color ambientColor = new Color(diffuseColor.r, diffuseColor.g, diffuseColor.b, diffuseColor.a); // * 255
		for (int index = 0; index < 10; index++)
			ambientColor.darker();
		this.ambientColor = new RGBA(ambientColor);
		
//		this.ambientColor = new RGBA(diffuseColor);
	}
}
