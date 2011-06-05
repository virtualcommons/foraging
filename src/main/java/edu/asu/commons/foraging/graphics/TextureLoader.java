package edu.asu.commons.foraging.graphics;

import java.awt.Graphics;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Hashtable;

import javax.imageio.ImageIO;
import javax.media.opengl.GL;

import edu.asu.commons.foraging.fileplugin.FileLoader;

/**
 * A utility class to load textures for JOGL.
 * @author <a href='deepali.bhagvat@asu.edu'>Deepali Bhagvat</a>
 */
public class TextureLoader {

	/**
	 * <code>true</code> if an error occured during the last call to
	 * <code>getTexture()</code> or <code>false</code> otherwise
	 */
	private boolean error = false;

	/**
	 * ColorModel allowing alpha color component
	 */
	private ColorModel glAlphaColorModel;

	/**
	 * ColorModel disallowing alpha color component
	 */
	private ColorModel glColorModel;

	/**
	 * This constructor creates the two default ColorModels.
	 */
	public TextureLoader() {
		glAlphaColorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB),
						new int[] {8,8,8,8},
						true,
						false,
						ComponentColorModel.TRANSLUCENT,
						DataBuffer.TYPE_BYTE);

		glColorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB),
						new int[] {8,8,8,0},
						false,
						false,
						ComponentColorModel.OPAQUE,
						DataBuffer.TYPE_BYTE);
	} // end constructor

//-----------------------------------------------------------------------------
// METHODS

	/**
	 * This method loads a Texture from the specified filename and
	 * returns it. If an error occured while loading the texture, null is
	 * returned and <code>isOK()</code> returns false. Otherwise, <code>isOK()</code>
	 * returns true, indicating that texture loading succeeded.
	 *
	 * @param resourceName		the filename of the texture to load
	 * @param forcePowersOfTwo	<code>true</code> to force the height and width
	 *									of the texture to be powers of two or
	 *									<code>false</code> otherwise
	 * @return	Texture - the loaded Texture object
	 */
	public Texture getTexture(String resourceName, boolean forcePowersOfTwo)
	{		
		//System.out.println("Loading texture "+resourceName);
		Texture texture = new Texture(resourceName);
		BufferedImage bufferedImage = null;
		try {
			bufferedImage = loadImage(resourceName);
		}
		catch( Exception e ) {
            e.printStackTrace();
			System.out.println("Unable to load texture "+ e );
			error = true;
			return null;
		}
		error = false;
		if(texture.getImageType() == Texture.JPG) {
			// Flip Image
			AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
			tx.translate(0, -bufferedImage.getHeight(null));
			AffineTransformOp op = new AffineTransformOp(tx,
					 AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
			bufferedImage = op.filter(bufferedImage, null);
		}

		// Getting the real Width/Height of the Texture in the Memory
		int realWidth, realHeight;
		if(forcePowersOfTwo) {
			// force the width and height of the created texture to be powers of two
			realWidth = get2Fold(bufferedImage.getWidth());
			realHeight = get2Fold(bufferedImage.getHeight());
		} // end if
		else {
			// DO NOT force the width and height of the created texture to be powers of two
			realWidth = bufferedImage.getWidth();
			realHeight = bufferedImage.getHeight();
		} // end else

		// set the height and width of image loaded from disk
		texture.setImageWidth(realWidth);
		texture.setImageHeight(realHeight);

		if (bufferedImage.getColorModel().hasAlpha()) {
			texture.setGLFormat(GL.GL_RGBA);
		}
		else {
			texture.setGLFormat(GL.GL_RGB);
		}

		// convert that image into a byte buffer of texture data
		ByteBuffer textureBuffer = convertImageData(bufferedImage,texture,forcePowersOfTwo);		
		texture.setTextureBuffer(textureBuffer);
		texture.rewind();
		return texture;

	} // end getTexture

	/**
	 * This method should be called after each call to <code>getTexture()</code>.
	 * It returns <code>false</code> if an error occured during texture loading or
	 * <code>true</code> if texture loading succeeded.
	 *
	 * @return	<code>true</code> if texture loading succeeded or <code>false</code> otherwise
	 */
	public boolean isOK()
	{
		return !error;

	} // end isOK

	/**
	 * This method returns the nearest power of two greater than or equal
	 * to the specified value.
	 *
	 * @param fold	the value to find power of two above
	 * @return	int - the nearest power of two greater than or equal to the specified value
	 */
	private int get2Fold(int fold)
	{
		int ret = 2;
		while (ret < fold) {
			ret *= 2;
		}
		return ret;
	} // end get2Fold

	/**
	 * This method returns a ByteBuffer representation of the specified
	 * BufferedImage and Texture data.
	 * <p>
	 * If <code>forcePowersOfTwo</code> is <code>true</code> then this method performs
	 * steps necessary to force the width and height of the returned texture
	 * ByteBuffer to be powers of two. It does this by creating a new
	 * BufferedImage with a width and height that are the nearest power
	 * of two greater than or equal to the parameter's. The parameter's pixel
	 * data is then copied into the new BufferedImage. Thus, no scaling is
	 * done but any area outside the original texture size will be filled with
	 * a solid background color. The background color depends on what is used
	 * by default when creating a new BufferedImage.
	 * <p>
	 * If <code>forcePowersOfTwo</code> is <code>false</code> then this method uses the
	 * width and height of the BufferedImage parameter without modifying them.
	 *
	 * @param bufferedImage		the BufferedImage to use for conversion
	 * @param texture				the Texture to use for conversion
	 * @param forcePowersOfTwo	<code>true</code> to force the height and width
	 *									of the texture to be powers of two or
	 *									<code>false</code> otherwise
	 * @return	ByteBuffer - the ByteBuffer containing the texture data
	 */
	private ByteBuffer convertImageData(BufferedImage bufferedImage, Texture texture, boolean forcePowersOfTwo)
	{
		ByteBuffer imageBuffer = null;
		WritableRaster raster;
		BufferedImage texImage;
		int nChannels = 3;

		// Determine the texture height and width.
		int actualTexWidth, actualTexHeight;
		int newTexWidth, newTexHeight;
		actualTexWidth = bufferedImage.getWidth();
		actualTexHeight = bufferedImage.getHeight();
		
		if(forcePowersOfTwo) {
			// We require that the texture height and width be powers of two
			// by using get2fold()
			newTexWidth = get2Fold(bufferedImage.getWidth());
			newTexHeight = get2Fold(bufferedImage.getHeight());
		} // end if
		else {
			// DO NOT require that the texture height and width be powers of two
			newTexWidth = actualTexWidth;
			newTexHeight = actualTexHeight;
		} // end else
		
		//Set texture width and height to new width and height
		texture.setTextureHeight(newTexHeight);
		texture.setTextureWidth(newTexWidth);

		//create a Raster
		// create a BufferedImage using the raster
		if (bufferedImage.getColorModel().hasAlpha()) {
			nChannels = 4;
			raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE,actualTexWidth,actualTexHeight,nChannels,null);
			texImage = new BufferedImage(glAlphaColorModel,raster,false,new Hashtable());
		}
		else {
			raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE,actualTexWidth,actualTexHeight,nChannels,null);
			texImage = new BufferedImage(glColorModel,raster,false,new Hashtable());
		}
						
		Graphics g = texImage.getGraphics();
		g.drawImage(bufferedImage,0,0,null);

		// get the byte data
		byte[] data = ((DataBufferByte) texImage.getRaster().getDataBuffer()).getData();
		
		// allocate space for and store the pixel data
		imageBuffer = ByteBuffer.allocateDirect(newTexWidth*newTexHeight*nChannels);
		imageBuffer.order(ByteOrder.nativeOrder());
		
		//Copies actualTexWidth*actualTexHeight data into newTexWidth*newTexHeight buffer
		//which creates black borders if actual is less than new
		//imageBuffer.put(data, 0, data.length);		
				
		//Tiling to get data = newTexWidth*newTexHeight
		int quotient = newTexWidth / actualTexWidth;
		int remainder = newTexWidth % actualTexWidth;
		for (int rowIndex = 0; rowIndex < actualTexHeight; rowIndex++) {
			int offset = rowIndex*actualTexWidth*nChannels;
			for (int iteration = 0; iteration < quotient; iteration++) {
				imageBuffer.put(data, offset, actualTexWidth*nChannels);
			}
			imageBuffer.put(data, offset, remainder*nChannels);
		}
		for (int rowIndex = 0; rowIndex < (newTexHeight-actualTexHeight); rowIndex++) {
			int offset = rowIndex*actualTexWidth*nChannels;
			for (int iteration = 0; iteration < quotient; iteration++) {
				imageBuffer.put(data, offset, actualTexWidth*nChannels);
			}
			imageBuffer.put(data, offset, remainder*nChannels);
		}		
		
		// return the pixel data
		return imageBuffer;
	}
		
	private ByteBuffer convertImageData1(BufferedImage bufferedImage, Texture texture, boolean forcePowersOfTwo)
	{
		ByteBuffer imageBuffer = null;
		WritableRaster raster;
		BufferedImage texImage;

		// Determine the texture height and width.
		int texWidth, texHeight;
		if(forcePowersOfTwo) {
			// We require that the texture height and width be powers of two
			// by using get2fold()
			texWidth = get2Fold(bufferedImage.getWidth());
			texHeight = get2Fold(bufferedImage.getHeight());
		} // end if
		else {
			// DO NOT require that the texture height and width be powers of two
			texWidth = bufferedImage.getWidth();
			texHeight = bufferedImage.getHeight();
		} // end else

		// Set the texture width and height.
		// This also updates the width ratio and height ratio where:
		// height ratio = ((float) image height) / (texture height)
		// width ratio = ((float) image width) / (texture width)
		texture.setTextureHeight(texHeight);
		texture.setTextureWidth(texWidth);

		String [] propNames = bufferedImage.getPropertyNames();
		//System.out.println(bufferedImage);

		// create a Raster
		// create a BufferedImage using the raster
		if (bufferedImage.getColorModel().hasAlpha()) {
			raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE,texWidth,texHeight,4,null);
			texImage = new BufferedImage(glAlphaColorModel,raster,false,new Hashtable());
		} // end if
		else {
			raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE,texWidth,texHeight,3,null);
			texImage = new BufferedImage(glColorModel,raster,false,new Hashtable());
		} // end else

		// if (forcePowersOfTwo == true) then:
		// - this basically copies the BufferedImage parameter into a new BufferedImage
		//   that is guaranteed to have a width and height that are a power of two.
		// - if the BufferedImage parameter already had a width and height that were
		//   powers of two, then there isn't any difference between it and the new BufferedImage.
		// - up to this point, we basically just forced the width and height to be powers
		//   of two if they were not already.
		Graphics g = texImage.getGraphics();
		g.drawImage(bufferedImage,0,0,null);

		// get the byte data
		byte[] data = ((DataBufferByte) texImage.getRaster().getDataBuffer()).getData();
		System.out.println("Image width = " + texture.getImageWidth() + " height = " + texture.getImageHeight());
		System.out.println("Data Length = " + data.length);

		// allocate space for and store the pixel data
		imageBuffer = ByteBuffer.allocateDirect(data.length);
		imageBuffer.order(ByteOrder.nativeOrder());
		imageBuffer.put(data, 0, data.length);

		// return the pixel data
		return imageBuffer;

	} // end convertImageData

	/**
	 * This method loads a BufferedImage from the specified resource (on the classpath) using ImageIO.
	 *
	 * @param ref	the filename of the image to load
	 * @return	BufferedImage - the loaded BufferedImage
	 * @exception	IOException - thrown if the indicated file cannot be found
	 * @exception	Exception - thrown if the BufferedImage is null after loading
	 */
	private BufferedImage loadImage(String ref) throws IOException
	{
        InputStream stream = FileLoader.getInputStream(ref);
		ImageIO.setUseCache(false);
		BufferedImage bufferedImage =
				ImageIO.read(new BufferedInputStream(stream));

		if (bufferedImage == null) {
			throw new IOException("null buffered image from loadImage()");
        }
		return bufferedImage;
	} // end loadImage

} // end class TextureLoader
