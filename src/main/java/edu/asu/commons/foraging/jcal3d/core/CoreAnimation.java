package edu.asu.commons.foraging.jcal3d.core;

import java.util.LinkedList;
import java.util.Vector;

public class CoreAnimation {

	public class CallbackRecord 
	{		
		public AnimationCallback callback;
		public float  minInterval;
	}
	  
	protected Vector<CallbackRecord> callbacks = new Vector<CallbackRecord>();
	protected float duration;
	protected LinkedList<CoreTrack> coreTracks = new LinkedList<CoreTrack>();
	protected String name;	
	protected String fileName;
	protected int referenceCount;
	
	/* Adds a core track.
	 *
	 * This function adds a core track to the core animation instance.
	 *
	 * @param pCoreTrack A pointer to the core track that should be added.
	 *
	 * @return One of the following values:
	 *         \li \b true if successful
	 *         \li \b false if an error happend
	 */
	public boolean addCoreTrack(CoreTrack coreTrack) {
		coreTracks.add(coreTrack);
	  return true;
	}

	/* Provides access to a core track.
	 *
	 * This function returns the core track for a given bone ID.
	 *
	 * @param coreBoneId The core bone ID of the core track that should be
	 *                   returned.
	 *
	 * @return One of the following values:
	 *         \li a pointer to the core track
	 *         \li \b 0 if an error happend
	 */
	public CoreTrack getCoreTrack(int coreBoneId) {
	  // loop through all core track	  
	  for(int coreTrackIndex = 0; coreTrackIndex < coreTracks.size(); ++coreTrackIndex) {
		  //get core track
		  CoreTrack coreTrack = coreTracks.get(coreTrackIndex);
		  // check if we found the matching core bone
		  if(coreTrack.getCoreBoneId() == coreBoneId) return coreTrack;
	  }

	  // no match found
	  return null;
	}

	/* Gets the number of core tracks for this core animation.
	 *
	 * This function returns the number of core tracks used for this core animation.
	 *
	 * @return The number of core tracks
	 */
	public int getTrackCount() {
		return coreTracks.size();
	}
	
	/* Returns the duration.
	 *
	 * This function returns the duration of the core animation instance.
	 *
	 * @return The duration in seconds.
	 */
	public float getDuration() {
	  return duration;
	}

	/* Sets the duration.
	 *
	 * This function sets the duration of the core animation instance.
	 *
	 * @param duration The duration in seconds that should be set.
	 */
	public void setDuration(float duration) {
		this.duration = duration;
	}

	/* Scale the core animation.
	 *
	 * This function rescale all the skeleton data that are in the core animation instance
	 *
	 * @param factor A float with the scale factor
	 *
	 */
	public void scale(float factor) {
	  // loop through all core track	  
	  for(int coreTrackIndex = 0; coreTrackIndex < coreTracks.size(); ++coreTrackIndex) {
		  coreTracks.get(coreTrackIndex).scale(factor);
	  }
	}

	
	/* 
	 * Set the name of the file in which the core animation is stored, if any.
	 *
	 * @param filename The path of the file.
	 */
	public void setFileName(String filename) {
		this.fileName = filename;
	}

	/* 
	 * Get the name of the file in which the core animation is stored, if any.
	 *
	 * @return One of the following values:
	 *         \li \b empty string if the animation was not stored in a file
	 *         \li \b the path of the file
	 *
	 */
	public String getFilename() {
	  return this.fileName;
	}
	 
	/* 
	 * Set the symbolic name of the core animation.
	 *
	 * @param name A symbolic name.
	 */
	public void setName(String name) {
	  this.name = name;
	}

	/* 
	 * Get the symbolic name the core animation.
	 *
	 * @return One of the following values:
	 *         \li \b empty string if the animation was no associated to a symbolic name
	 *         \li \b the symbolic name
	 *
	 */
	public String getName() {
	  return this.name;
	}

	/* 
	 * Add a callback to the current list of callbacks for this CoreAnim.
	 *
	 * @param  callback     Ptr to a subclass of this abstract class implementing the callback function.
	 * @param  min_interval Minimum interval (in seconds) between callbacks.  Specifying 0 means call every update().
	 *
	 */
	public void registerCallback(AnimationCallback callback, float min_interval){
	  CallbackRecord record = new CallbackRecord();
	  record.callback     = callback;
	  record.minInterval = min_interval;

	  callbacks.add(record);
	}

	/* 
	 * Remove a callback from the current list of callbacks for this Anim.
	 * Callback objects not removed this way will be deleted in the dtor of the Anim.
	 *
	 * @param  callback     Ptr to a subclass of this abstract class implementing the callback function to remove.
	 *
	 */
	public void removeCallback(AnimationCallback callback) {	
	  for (int callbackIndex = 0; callbackIndex < callbacks.size(); callbackIndex++) {
	    if (callbacks.get(callbackIndex).callback.equals(callback)) {
	    	callbacks.remove(callbackIndex);
	    	return;
	    }
	  }
	}
	
	/* Returns the core track list.
	 *
	 * This function returns the list that contains all core tracks of the core
	 * animation instance.
	 *
	 * @return A reference to the core track list.
	 */
	public LinkedList<CoreTrack> getCoreTracks() {
	  return coreTracks;
	}
	
	/* Returns the total number of core keyframes used for this animation.
	 *
	 * This function returns the total number of core keyframes used for this
	 * animation instance (i.e.: the sum of all core keyframes of all core tracks).
	 *
	 * @return A reference to the core track list.
	 */
	public int getTotalNumberOfKeyframes() {
		int nbKeys = 0;
		for (int coreTrackIndex = 0; coreTrackIndex < coreTracks.size(); ++coreTrackIndex) {
			CoreTrack coreTrack = coreTracks.get(coreTrackIndex);
			nbKeys += coreTrack.getCoreKeyframeCount();
		}
		return nbKeys;
	}
	
	public Vector<CallbackRecord> getCallbackList() { 
		return callbacks; 
	}

	/* 
	 * Increment the reference counter the core animation.
	 *
	 */
	public void incRef() {
	  referenceCount++;
	}

	/* 
	 * Decrement the reference counter the core animation.
	 *
	 * @return One of the following values:
	 *         \li \b true if there are nomore reference
	 *         \li \b false if there are another reference
	 *
	 */
	public boolean decRef()	{
		referenceCount--;
		return (referenceCount <= 0);
	}


}
