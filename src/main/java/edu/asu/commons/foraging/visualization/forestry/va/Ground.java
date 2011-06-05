package edu.asu.commons.foraging.visualization.forestry.va;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import edu.asu.commons.foraging.graphics.GraphicsObject;
import edu.asu.commons.foraging.graphics.Point3D;
import edu.asu.commons.foraging.graphics.RGBA;
import edu.asu.commons.foraging.graphics.TextureLoader;

/**
 * The Ground class represents the forest ground in the foresty experiment visualization. 
 * @author <a href='deepali.bhagvat@asu.edu'>Deepali Bhagvat</a>
 * @version $Revision: 4 $
 *
 */
public class Ground extends GraphicsObject {
	
	/**
	 * Three dimensional coordinates of the right front vertex of the ground 
	 */
	private Point3D rfVertex = new Point3D();
	
	/**
	 * Three dimensional coordinates of the right back vertex of the ground
	 */
	private Point3D rbVertex = new Point3D();
	
	/**
	 * Three dimensional coordinates of the left back vertex of the ground
	 */
	private Point3D lbVertex = new Point3D();
	
	/**
	 * Three dimensional coordinates of the left front vertex of the ground
	 */
	private Point3D lfVertex = new Point3D();
	
	/**
	 * Display list id used to render the ground
	 */
	private int displayListId = -1;
	
	/**
	 * Constructs a new ground with the specified four corner points and materials.
	 * @param rightFrontCorner 3d coordinates of the right front corner of the ground
	 * @param rightBackCorner 3d coordinates of the right back corner of the ground
	 * @param leftBackCorner 3d coordinates of the left back corner of the ground
	 * @param leftFrontCorner 3d coordinates of the left front corner of the ground
	 * @param ambient ambient material used to render the ground
	 * @param diffuse diffuse material used to render the ground
	 * @param specular specular material used to render the ground
	 * @param shininess shininess used to render the ground
	 * @param textureFile image file used to texture the ground
	 */
	public Ground(Point3D rightFrontCorner, Point3D rightBackCorner, Point3D leftBackCorner, Point3D leftFrontCorner,
			RGBA ambient, RGBA diffuse, RGBA specular, float shininess, String textureFile) {
		this.rfVertex = rightFrontCorner;
		this.rbVertex = rightBackCorner;
		this.lbVertex = leftBackCorner;
		this.lfVertex = leftFrontCorner;
		
		setMaterials(ambient, diffuse, specular, shininess);
		
		setMinExtent(lbVertex);
		setMaxExtent(rfVertex);
		
		if (!textureFile.isEmpty())
		{
			TextureLoader texLoader = new TextureLoader();
			setTexture(texLoader.getTexture(textureFile, true));
		}
	}
	
	/**
	 * Renders this ground using a display list
	 * @param drawable current rendering context
	 */
	public void display(GLAutoDrawable drawable) {
		
		GL gl = drawable.getGL();
		
		if (displayListId == -1) {
			//Create texture
			//texture.create(drawable);
			
			//Create new display list
			displayListId = gl.glGenLists(1);
			gl.glNewList(displayListId, GL.GL_COMPILE_AND_EXECUTE);
			
			//texture.load(gl);
			//gl.glEnable(GL.GL_TEXTURE_2D);	
			//gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_MODULATE);
			
			//Apply material
			gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT, ambient, 0);
			gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_DIFFUSE, diffuse, 0);
			gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_SPECULAR, specular, 0);
			gl.glMaterialf(GL.GL_FRONT_AND_BACK, GL.GL_SHININESS, shininess);
			
			//gl.glEnable(GL.GL_CULL_FACE);
			//gl.glCullFace(GL.GL_BACK);
			gl.glBegin(GL.GL_TRIANGLE_STRIP);
			//gl.glTexCoord2f(rfTexCoord.a, rfTexCoord.b);
			gl.glNormal3f(0, 1, 0);
			gl.glVertex3f(rfVertex.x, rfVertex.y, rfVertex.z);
			//gl.glTexCoord2f(rbTexCoord.a, rbTexCoord.b);
			gl.glNormal3f(0, 1, 0);
			gl.glVertex3f(rbVertex.x, rbVertex.y, rbVertex.z);
			//gl.glTexCoord2f(lfTexCoord.a, lfTexCoord.b);
			gl.glNormal3f(0, 1, 0);
			gl.glVertex3f(lfVertex.x, lfVertex.y, lfVertex.z);
			//gl.glTexCoord2f(lbTexCoord.a, lbTexCoord.b);
			gl.glNormal3f(0, 1, 0);
			gl.glVertex3f(lbVertex.x, lbVertex.y, lbVertex.z);
			gl.glEnd();
			
			gl.glEndList();
		}
		else {
			gl.glCallList(displayListId);
		}
	}
	
	/**
	 * Returns the 3d coordinate of the left back corner of thie ground 
	 * @return 3d coordinate
	 */
	public Point3D getLB() {
		return lbVertex;
	}
}
