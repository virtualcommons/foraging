package edu.asu.commons.foraging.jcal3d.instance;

import java.util.Vector;

import edu.asu.commons.foraging.jcal3d.core.CoreMorphAnimation;

public class MorphTargetMixer {

	protected Vector<Float> currentWeights = new Vector<Float>();
	protected Vector<Float> endWeights = new Vector<Float>();
	protected Vector<Float> durations = new Vector<Float>();
	protected Model model;
	  
	/*****************************************************************************/
	/** Constructs the morph target mixer instance.
	  *
	  * This function is the default constructor of the morph target mixer instance.
	  *****************************************************************************/

	public MorphTargetMixer(Model model) {
		//FIXME: Make sure that model is not null		
	  this.model = model;

	  if(model.getCoreModel().getCoreMorphAnimationCount() != 0) {
	    int morphAnimationCount = model.getCoreModel().getCoreMorphAnimationCount();
	    // reserve the space needed in all the vectors
	    currentWeights.setSize(morphAnimationCount);
	    endWeights.setSize(morphAnimationCount);
	    durations.setSize(morphAnimationCount);
	    for (int currentWeightIndex = 0; currentWeightIndex < morphAnimationCount; ++currentWeightIndex) {
	    	currentWeights.setElementAt(0.0f, currentWeightIndex);
	    	endWeights.setElementAt(0.0f, currentWeightIndex);
	    	durations.setElementAt(0.0f, currentWeightIndex);
	    }	    
	  }
	}

	/*****************************************************************************/
	/** Interpolates the weight of a morph target.
	  *
	  * This function interpolates the weight of a morph target a new value
	  * in a given amount of time.
	  *
	  * @param id The ID of the morph target that should be blended.
	  * @param weight The weight to interpolate the morph target to.
	  * @param delay The time in seconds until the new weight should be reached.
	  *
	  * @return One of the following values:
	  *         \li \b true if successful
	  *         \li \b false if an error happend
	  *****************************************************************************/
	public boolean blend(int id, float weight, float delay) {	  
	  endWeights.setElementAt(weight, id);
	  durations.setElementAt(delay, id);
	  return true;
	}

	 /*****************************************************************************/
	/** Fades a morph target out.
	  *
	  * This function fades a morph target out in a given amount of time.
	  *
	  * @param id The ID of the morph target that should be faded out.
	  * @param delay The time in seconds until the the morph target is
	  *              completely removed.
	  *
	  * @return One of the following values:
	  *         \li \b true if successful
	  *         \li \b false if an error happend
	  *****************************************************************************/

	public boolean clear(int id, float delay) {	  
	  endWeights.setElementAt(0.0f, id);
	  durations.setElementAt(delay, id);
	  return true;
	}

	 /*****************************************************************************/
	/** Get the weight of a morph target.
	  *
	  * @param id The id of the morph target which weight you want.
	  *
	  * @return The weight of the morph target with the given id.
	  *****************************************************************************/
	public float getCurrentWeight(int id){
	  return currentWeights.get(id);
	}

	 /*****************************************************************************/
	/** Get the weight of the base vertices.
	  *
	  * @return The weight of the base vertices.
	  *****************************************************************************/
	public float getCurrentWeightBase() {
	  float currentWeight = 1.0f;
	  for (int currentWeightIndex = 0; currentWeightIndex < currentWeights.size(); ++currentWeightIndex) {	  
	    currentWeight -= currentWeights.get(currentWeightIndex);	    
	  }
	  return currentWeight;
	}

	 /*****************************************************************************/
	/** Updates all morph targets.
	  *
	  * This function updates all morph targets of the mixer instance for a
	  * given amount of time.
	  *
	  * @param deltaTime The elapsed time in seconds since the last update.
	  *****************************************************************************/

	public void update(float deltaTime) {
	  for (int currentWeightIndex = 0; currentWeightIndex < currentWeights.size(); ++currentWeightIndex) {
		float currentWeight = currentWeights.get(currentWeightIndex);
		float endWeight = endWeights.get(currentWeightIndex);
		float duration = durations.get(currentWeightIndex);
		  
	    if(deltaTime >= duration) {
	      currentWeights.setElementAt(endWeight, currentWeightIndex);
	      durations.setElementAt(0.0f, currentWeightIndex);
	    }
	    else {
	    	
	    	currentWeights.setElementAt(currentWeight + (endWeight - currentWeight) * deltaTime / duration, 
	    			currentWeightIndex);
	    	durations.setElementAt(duration - deltaTime, currentWeightIndex);	    	
	      
	    }	    
	  }
	  
	  int morphAnimationID = 0;
	  while(morphAnimationID < getMorphTargetCount()) {
	    CoreMorphAnimation coreMorphAnimation = model.getCoreModel().getCoreMorphAnimation(morphAnimationID);
	    Vector<Integer> coreMeshIds = coreMorphAnimation.getCoreMeshIds();
	    Vector<Integer> morphTargetIds = coreMorphAnimation.getMorphTargetIds();
	    for (int meshIndex = 0; meshIndex < coreMeshIds.size(); ++meshIndex) {
	    	Vector<Submesh> submeshes = model.getMesh(coreMeshIds.get(meshIndex)).getSubmeshes();	       	       
	       for(int submeshId = 0; submeshId < submeshes.size(); ++submeshId) {
	         submeshes.get(submeshId).setMorphTargetWeight(morphTargetIds.get(meshIndex), currentWeights.get(morphAnimationID));
	       }
	    }	    
	    ++morphAnimationID;
	  }
	}

	 /*****************************************************************************/
	/** Returns the number of morph targets this morph target mixer mixes.
	  *
	  * @return The number of morph targets this morph target mixer mixes.
	  *****************************************************************************/

	public int getMorphTargetCount() {
	  return currentWeights.size();
	}
}
