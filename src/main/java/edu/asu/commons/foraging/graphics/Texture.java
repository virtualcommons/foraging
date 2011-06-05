package edu.asu.commons.foraging.graphics;

import java.io.Serializable;
import java.nio.ByteBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.glu.GLU;

/**
 * The Texture class encapsulates a texture used in graphics applications.
 * @author <a href='deepali.bhagvat@asu.edu'>Deepali Bhagvat</a>
 *
 */
public class Texture implements Serializable{

	private static final long serialVersionUID = -8664289842434114486L;

	/**
	 * Image file from which the texture is loaded
	 */
	private String resourceName;
	
	/**
	 * Texture id
	 */
	protected transient int[] id = new int[1];

	/**
	 * Height of the image loaded from disk
	 */
	private transient int imageHeight;

	/**
	 * Width of the image loaded from disk
	 */
	private transient int imageWidth;

	/**
	 * Height of the texture. This may differ from the image height
	 * if the image height is not a power of two.
	 */
	private transient int texHeight; 

	/**
	 * Width of the texture. This may differ from the image width
	 * if the image width is not a power of two.
	 */
	private transient int texWidth;

	/**
	 * The ratio of the image width to the texture width
	 */
	private transient float widthRatio;

	/**
	 * The ratio of the image height to the texture height
	 */
	private transient float heightRatio;

	/**
	 * The type of image this texture was loaded from. Either JPG or TGA.
	 */
	private transient int imageType;

	/**
	 * Used as srcPixelFormat when building mipmaps
	 */
	private transient int glFormat;

	/**
	 * Byte buffer containing pixel data
	 */
	private transient ByteBuffer buffer;

	/**
	 * constant defined to represent an image type of .jpg
	 */
	public transient final static int JPG=0;

	/**
	 * constant defined to represent an image type of .tga
	 */
	public transient final static int TGA=1;

	/**
	 * This constructor sets the texture type (JPG or TGA) and stores the
	 * resource path.
	 *
	 * @param resourceName	filename of texture file
	 */
	public Texture(String resourceName) {
		if(resourceName.endsWith(".tga")) {
			//System.out.println("Texture is TGA");
			imageType = TGA;
		}
		else if(resourceName.endsWith(".jpg")){
			//System.out.println("Texture is JPG");
			imageType = JPG;
		}
		this.resourceName = resourceName;
		id[0] = -1;
	} 

	/**
	 * This method sets the texture's byte buffer.
	 *
	 * @param buffer	the byte buffer to set for the texture
	 */
	public void setTextureBuffer(ByteBuffer  buffer) {
		this.buffer = buffer;
	} // end setTextureBuffer

	/**
	 * This method returns the texture's byte buffer.
	 *
	 * @return	ByteBuffer - the texture's byte buffer
	 */
	public ByteBuffer getTextureBuffer() {
		return buffer;
	} // end getTextureBuffer

	/**
	 * This method returns the texture's GL Format.
	 *
	 * @return	int - the texture's GL Format
	 */
	public int getGLFormat(){
		return glFormat;
	} // end getGLFormat

	/**
	 * This method sets the texture's GL Format.
	 *
	 * @param newFormat	the GL Format to set for the texture
	 */
	public void setGLFormat(int newFormat){
		glFormat = newFormat;
	} // end setGLFormat

	/**
	 * This method sets the texture's image height.
	 *
	 * @param imgHeight	the image height to set for the texture
	 */
	public void setImageHeight(int imgHeight) {
		this.imageHeight = imgHeight;
	} // end setImageHeight

	/**
	 * This method sets the texture's image width.
	 *
	 * @param imgWidth	the image width to set for the texture
	 */
	public void setImageWidth(int imgWidth) {
		this.imageWidth = imgWidth;
	} // end setImageWidth

	/**
	 * This method returns the texture's image height.
	 *
	 * @return	int - the texture's image height
	 */
	public int getImageHeight() {
		return imageHeight;
	} // end getImageHeight

	/**
	 * This method returns the texture's image width.
	 *
	 * @return	int - the texture's image width
	 */
	public int getImageWidth() {
		return imageWidth;
	} // end getImageWidth

	/**
	 * This method returns the texture's height ratio.
	 * That is, the ratio of the images's height to the
	 * texture's height.
	 *
	 * @return	float - the texture's height ratio
	 */
	public float getHeightRatio() {
		return heightRatio;
	} // end getHeightRatio

	/**
	 * This method returns the texture's width ratio.
	 * That is, the ratio of the image's width to the
	 * texture's width.
	 *
	 * @return	float - the texture's width ratio
	 */
	public float getWidthRatio() {
		return widthRatio;
	} // end getWidthRatio

	/**
	 * This method returns the texture's image type.
	 *
	 * @return	int - the texture's image type, either Texture.JPG or Texture.TGA
	 */
	public int getImageType() {
		return imageType;
	} // end getImageType

	/**
	 * This method sets the texture's height and updates the
	 * heightRatio.
	 *
	 * @param texHeight	the height to set for the texture
	 */
	public void setTextureHeight(int texHeight) {
		this.texHeight = texHeight;
		setHeightRatio();
	} // end setTextureHeight

	/**
	 * This method sets the texture's width and updates the
	 * widthRatio.
	 *
	 * @param texWidth	the width to set for the texture
	 */
	public void setTextureWidth(int texWidth) {
		this.texWidth = texWidth;
		setWidthRatio();
	} // end setTextureWidth

	/**
	 * This method sets the texture's height ratio to the ratio of
	 * the image's height to the texture's height.
	 */
	private void setHeightRatio() {
		if (texHeight != 0) {
			heightRatio = ((float) imageHeight)/texHeight;
		} // end if
	} // end setHeightRatio

	/**
	 * This method sets the texture's width ratio to the ratio of
	 * the image's width to the texture's width.
	 */
	private void setWidthRatio() {
		if (texWidth != 0) {
			widthRatio = ((float) imageWidth)/texWidth;
		} // end if
	} // end setWidthRatio

	/**
	 * This method method returns the file suffix (extension) of the provided filename.
	 *
	 * @param filePath	the filename to determine the file suzffix for
	 * @return	String - the file's suffix (extension)
	 */
	private String fileSuffix(String filePath) {
		int dotPos = filePath.lastIndexOf(".");
		return filePath.substring(dotPos+1);
	}//end fileFromPath method
	
	public void create(GLAutoDrawable drawable) {
		GL gl = drawable.getGL();
		GLU glu = new GLU();
		
		if (id[0] == -1) {
			gl.glGenTextures(1, id, 0);
			gl.glBindTexture(GL.GL_TEXTURE_2D, id[0]);
			
			//Repeat the texture on s axis
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
			//Repeat the texture on t axis
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
								
			//Create mip maps for minification filter
			glu.gluBuild2DMipmaps(GL.GL_TEXTURE_2D, glFormat, texWidth, texHeight, glFormat, GL.GL_UNSIGNED_BYTE, buffer);		
			gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR_MIPMAP_NEAREST);		
			
			//Use linear smoothing for magnification filter				
			gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
		}
	}
	
	public void load(GL gl) {
		gl.glBindTexture(GL.GL_TEXTURE_2D, id[0]);		
	}

	public void rewind() {
		buffer.rewind();		
	}

	public String getResourceName() {
		return resourceName;
	}

} 
