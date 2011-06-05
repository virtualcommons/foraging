package edu.asu.commons.foraging.jcal3d.core;

import java.util.HashMap;
import java.util.Vector;

public class CoreSkeleton {
	Vector<CoreBone> coreBones = new Vector<CoreBone>(); //index in this vector serves as an id.
	HashMap<String, Integer> coreBoneNames = new HashMap<String, Integer>();
	Vector<Integer> rootCoreBoneIds = new Vector<Integer>();
		
	public CoreSkeleton() {
	}
	
	/* Adds a core bone.
	 *
	 * This function adds a core bone to the core skeleton instance.
	 *
	 * @param pCoreBone A pointer to the core bone that should be added.
	 *
	 * @return One of the following values:
	 *         \li the assigned bone \b ID of the added core bone
	 *         \li \b -1 if an error happend
	 */
	public int addCoreBone(CoreBone coreBone) {
		// get next bone id
		int boneId;
		boneId = coreBones.size();

		coreBones.add(coreBone);

		// if necessary, add the core bone to the root bone list
		if(coreBone.getParentId() == -1) {
			rootCoreBoneIds.add(boneId);
		}

		// add a reference from the bone's name to its id
		mapCoreBoneName( boneId, coreBone.getName() );

		return boneId;
	}
		
	/* Calculates the current state.
	 *
	 * This function calculates the current state of the core skeleton instance by
	 * calculating all the core bone states.
	 */
	public void calculateState() {
	  // calculate all bone states of the skeleton	  
	  for(int rootCoreBoneId = 0; rootCoreBoneId < rootCoreBoneIds.size(); ++rootCoreBoneId) {
		  coreBones.get(rootCoreBoneId).calculateState();
	  }
	}
		
	/* Calculates bounding boxes.
	 *
	 * This function Calculates the bounding box of every bone in the core Skeleton.
	 *
	 * @param pCoreModel The coreModel (needed for vertices data).
	 */
	public void calculateBoundingBoxes(CoreModel coreModel)	{
	   for(int boneId = 0; boneId < coreBones.size(); ++boneId) {
	      coreBones.get(boneId).calculateBoundingBox(coreModel);
	   }
	}
	
	/* Maps the name of a bone to a specific bone id
	 *
	 * This function returns true or false depending on whether the mapping
	 * was successful or not. Note that it is possible to overwrite and existing
	 * mapping and no error will be given.
	 *
	 * @param coreBoneId The id of the core bone to be associated with the name.
	 * @param strName The name of the core bone that will be associated with the id.
	 *
	 * @return One of the following values:
	 *         \li true if the mapping was successful
	 *         \li false if an invalid ID was given
	 */
	public boolean mapCoreBoneName(int coreBoneId, String coreBoneName) {	   
	   //Add the mapping or overwrite an existing mapping
	   coreBoneNames.put(coreBoneName, coreBoneId);
	   return true;
	}

	/* Provides access to a core bone.
	 *
	 * This function returns the core bone with the given ID.
	 *
	 * @param coreBoneId The ID of the core bone that should be returned.
	 *
	 * @return One of the following values:
	 *         \li a pointer to the core bone
	 *         \li \b 0 if an error happend
	 */
	public CoreBone getCoreBone(int coreBoneId) {
	  return coreBones.get(coreBoneId);
	}
		
	/* Provides access to a core bone.
	 *
	 * This function returns the core bone with the given name.
	 *
	 * @param strName The name of the core bone that should be returned.
	 *
	 * @return One of the following values:
	 *         \li a pointer to the core bone
	 *         \li \b 0 if an error happend
	 */
	public CoreBone getCoreBone(String coreBoneName) {
	   return getCoreBone(getCoreBoneId( coreBoneName ));
	}
	
	/* Returns the ID of a specified core bone.
	 *
	 * This function returns the ID of a specified core bone.
	 *
	 * @param strName The name of the core bone that should be returned.
	 *
	 * @return One of the following values:
	 *         \li the \b ID of the core bone
	 *         \li \b -1 if an error happend
	 */
	public int getCoreBoneId(String coreBoneName) {
	  return coreBoneNames.get(coreBoneName);
	}
		
	/* Returns the root core bone id list.
	 *
	 * This function returns the list that contains all root core bone IDs of the
	 * core skeleton instance.
	 *
	 * @return A reference to the root core bone id list.
	 */
	public Vector<Integer> getRootCoreBoneIds() {
	  return rootCoreBoneIds;
	}
	
	/* Returns the core bone vector.
	 *
	 * This function returns the vector that contains all core bones of the core
	 * skeleton instance.
	 *
	 * @return A reference to the core bone vector.
	 */
	public Vector<CoreBone> getCoreBones() {
	  return coreBones;
	}
		
	/* Scale the core skeleton.
	 *
	 * This function rescale all the data that are in the core skeleton instance.
	 *
	 * @param factor A float with the scale factor
	 *
	 */
	public void scale(float factor)	{	  
	  for(int rootCoreBoneId = 0; rootCoreBoneId < rootCoreBoneIds.size(); ++rootCoreBoneId) {
		  coreBones.get(rootCoreBoneId).scale(factor);
	  }

	}
}
