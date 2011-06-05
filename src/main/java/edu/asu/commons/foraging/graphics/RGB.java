package edu.asu.commons.foraging.graphics;

/**
 * The RGB class represents color with three components, red, green and blue.
 * @author <a href='deepali.bhagvat@asu.edu'>Deepali Bhagvat</a>
 *
 */
public class RGB {
	/**
	 * Red component of the color
	 */
	public float r;
	
	/**
	 * Green component of the color
	 */
	public float g;
	
	/**
	 * Blue component of the color
	 */
	public float b;
		
	/**
	 * Constructs a new color whose component values are initialized to 0.
	 *
	 */
	public RGB() {
		r = 0.0f;
		g = 0.0f;
		b = 0.0f;		
	}
	
	/**
	 * Constructs a new color with the specified component values
	 * @param r red component
	 * @param g green component
	 * @param b blue component
	 */
	public RGB(float r, float g, float b) {
		this.r = r;
		this.g = g;
		this.b= b;		
	}
}
