package edu.asu.commons.foraging.jcal3d.core;

import edu.asu.commons.foraging.graphics.Vector3D;
import edu.asu.commons.foraging.jcal3d.misc.Quaternion;

public class CoreKeyframe {

	protected float time;
	protected Vector3D translation;
	protected Quaternion rotation;
	 
	
	/* Returns the rotation.
	 *
	 * This function returns the rotation of the core keyframe instance.
	 *
	 * @return The rotation as quaternion.
	 */
	public Quaternion getRotation() {
	  return rotation;
	}
	 
	/* Returns the time.
	 *
	 * This function returns the time of the core keyframe instance.
	 *
	 * @return The time in seconds.
	 */
	public float getTime() {
	  return time;
	}

	/* Returns the translation.
	 *
	 * This function returns the translation of the core keyframe instance.
	 *
	 * @return The translation as vector.
	 */
	public Vector3D getTranslation() {
	  return translation;
	}

	/* Sets the rotation.
	 *
	 * This function sets the rotation of the core keyframe instance.
	 *
	 * @param rotation The rotation as quaternion.
	 */
	public void setRotation(Quaternion rotation) {
		this.rotation = rotation;
	}

	/* Sets the time.
	 *
	 * This function sets the time of the core keyframe instance.
	 *
	 * @param rotation The time in seconds.
	 */
	public void setTime(float time) {
		this.time = time;
	}

	/* Sets the translation.
	 *
	 * This function sets the translation of the core keyframe instance.
	 *
	 * @param rotation The translation as vector.
	 */
	public void setTranslation(Vector3D translation) {
		this.translation = translation;
	}
}
