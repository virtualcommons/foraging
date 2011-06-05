package edu.asu.commons.foraging.graphics;

/**
 * The MovingObject class encapsulates a moving object in graphical applications. It creates signatures of methods used to move the object.
 * 
 * @author <a href='deepali.bhagvat@asu.edu'>Deepali Bhagvat</a>
 * 
 */
public interface MovingObject {
		
	/**
	 * Moves the object in forward direction
	 */
	public void forward();
	
	/**
	 * Moves the object in backward direction
	 */
	public void reverse();
	
	/**
	 * Moves the object in left direction
	 */
	public void moveLeft();
	
	/**
	 * Moves the object in right direction
	 */
	public void moveRight();
		
}
