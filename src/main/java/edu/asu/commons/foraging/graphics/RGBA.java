package edu.asu.commons.foraging.graphics;

import java.awt.Color;

/**
 * The RGBA class represents color with four components, red, green, blue and alpha.
 * @author <a href='deepali.bhagvat@asu.edu'>Deepali Bhagvat</a>
 */
public class RGBA {
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
	 * Alpha component of the color
	 */
	public float a;
	
	/**
	 * Constructs a new color whose component values are initialized to 0.
	 *
	 */
	public RGBA() {
		r = 0.0f;
		g = 0.0f;
		b = 0.0f;
		a = 1.0f;
	}
	
	/**
	 * Constructs a new color with the specified component values
	 * @param r red component
	 * @param g green component
	 * @param b blue component
	 * @param a alpha component
	 */
	public RGBA(float r, float g, float b, float a) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}
	
	/**
	 * Constructs a new color using an existing color
	 * @param rgba existing color
	 */
	public RGBA(RGBA rgba) {
		this.r = rgba.r;
		this.g = rgba.g;
		this.b = rgba.b;
		this.a = rgba.a;
	}
	
	/**
	 * Constructs a new color using an existing color
	 * @param color existing color
	 */
	public RGBA(Color color) {
		this.r = color.getRed()   /255.0f;
		this.g = color.getGreen() /255.0f;
		this.b = color.getBlue()  /255.0f;
		this.a = color.getAlpha() /255.0f;
	}
	
	/**
	 * Returns a float array of color components arranged as r, g, b, a
	 * @return float array
	 */
	public float[] getfv() {
		float[] color = new float[4];
		color[0] = r;
		color[1] = g;
		color[2] = b;
		color[3] = a;
		
		return color;
	}
	
	/**
	 * Returns a string representation of this color in the for (r, g, b, a) 
	 * @return string representation
	 */
	public String toString() {
		return new String("(r, g, b, a) = (" + r + ", " + g + ", " + b +  ", " + a + ")");
	}
}
