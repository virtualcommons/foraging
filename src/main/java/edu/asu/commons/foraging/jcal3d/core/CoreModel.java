package edu.asu.commons.foraging.jcal3d.core;

import java.awt.Color;
import java.util.HashMap;
import java.util.Vector;

import edu.asu.commons.foraging.graphics.RGBA;
import edu.asu.commons.foraging.jcal3d.misc.Loader;
import edu.asu.commons.foraging.jcal3d.misc.Saver;


public class CoreModel {	  
	protected String name;
	protected CoreSkeleton coreSkeleton;	
	protected Vector<CoreAnimation> coreAnimations = new Vector<CoreAnimation>();
	protected Vector<CoreMorphAnimation> coreMorphAnimations = new Vector<CoreMorphAnimation>();
	protected Vector<CoreMaterial> coreMaterials = new Vector<CoreMaterial>();		
	protected HashMap<Integer, HashMap<Integer, Integer>> mapMapCoreMaterialThreads = new HashMap<Integer, HashMap<Integer, Integer>>();
	protected Vector<CoreMesh> coreMeshes = new Vector<CoreMesh>();
	Object userData;
	protected HashMap<String, Integer> animationNames = new HashMap<String, Integer>();
	protected HashMap<String, Integer> materialNames = new HashMap<String, Integer>();
	protected HashMap<String, Integer> meshNames = new HashMap<String, Integer>();
	
	//Constructor
	public CoreModel(String name) {
		this.name = name;
		coreSkeleton = null;
		userData = null;
	}
		
	//############## Loading Functions #####################
	public boolean loadCoreSkeleton(String file) {		
		//load a new core skeleton
		coreSkeleton = Loader.loadCoreSkeleton(file);
		return coreSkeleton != null;
	}

	/* Loads a core animation.
	 *
	 * This function loads a core animation from a file.
	 *
	 * @param strFilename The file from which the core animation should be loaded
	 *                    from.
	 *
	 * @return One of the following values:
	 *         \li the assigned \b ID of the loaded core animation
	 *         \li \b -1 if an error happend
	 */
	public int loadCoreAnimation(String fileName) {
	  // the core skeleton has to be loaded already
	  if(coreSkeleton == null) {
		  edu.asu.commons.foraging.jcal3d.misc.Error.setLastError(edu.asu.commons.foraging.jcal3d.misc.Error.INVALID_HANDLE, "", -1, "");	    
		  return -1;
	  }

	  // load a new core animation
	  CoreAnimation coreAnimation = Loader.loadCoreAnimation(fileName, coreSkeleton);
	  if(coreAnimation == null) return -1;	  

	  // add core animation to this core model
	  return addCoreAnimation(coreAnimation);
	}

	/* Loads a core animation and bind it to a name.
	 *
	 * This function loads a core animation from a file. It is equivalent
	 * to calling addAnimName(strAnimationName, loadCoreAnimation(strFilename)).
	 * If strAnimationName is already associated to a coreAnimationId because
	 * of a previous call to addAnimName, the same coreAnimationId will
	 * be used. 
	 *
	 * @param strFilename The file from which the core animation should be loaded
	 *                    from.
	 * @param strAnimationName A string that is associated with an anim ID number.
	 *
	 * @return One of the following values:
	 *         \li the assigned \b ID of the loaded core animation
	 *         \li \b -1 if an error happend
	 */
	public int loadCoreAnimation(String fileName, String animationName)	{
	  Integer animationId = animationNames.get(animationName);
	  
	  if (animationId != null) {
		  // the core skeleton has to be loaded already
		  if(coreSkeleton == null) {
		      edu.asu.commons.foraging.jcal3d.misc.Error.setLastError(edu.asu.commons.foraging.jcal3d.misc.Error.INVALID_HANDLE, "", -1, "");
		      return -1;
		  }
		  if(coreAnimations.get(animationId) == null) {
			  edu.asu.commons.foraging.jcal3d.misc.Error.setLastError(edu.asu.commons.foraging.jcal3d.misc.Error.INDEX_BUILD_FAILED, "", -1, "");
		      return -1;
		  }
		  CoreAnimation coreAnimation = Loader.loadCoreAnimation(fileName, coreSkeleton);
		  if(coreAnimation == null) return -1;
		  coreAnimation.setName(animationName);
		  coreAnimations.setElementAt(coreAnimation, animationId);
	  }
	  else {
	    animationId = loadCoreAnimation(fileName);
	    if(animationId >= 0) addAnimationName(animationName, animationId);
	  }
	  return animationId;
	}

	/* Loads a core material.
	 *
	 * This function loads a core material from a file.
	 *
	 * @param strFilename The file from which the core material should be loaded
	 *                    from.
	 *
	 * @return One of the following values:
	 *         \li the assigned \b ID of the loaded core material
	 *         \li \b -1 if an error happend
	 */
/*	public int loadCoreMaterial(String fileName) {
		// the core skeleton has to be loaded already
		if(coreSkeleton == null) {
			edu.asu.shesc.csan3d.charanim.misc.Error.setLastError(edu.asu.shesc.csan3d.charanim.misc.Error.INVALID_HANDLE, "", -1, "");
			return -1;
	  }

	  // load a new core material
	  CoreMaterial coreMaterial = Loader.loadCoreMaterial(fileName);
	  if(coreMaterial == null) return -1;

	  // add core material to this core model
	  return addCoreMaterial(coreMaterial);
	}
*/		
	/* Loads a core material and bind it to a name.
	 *
	 * This function loads a core material from a file. It is equivalent
	 * to calling addMaterialName(strMaterialName, loadCoreMaterial(strFilename)).
	 * If strMaterialName is already associated to a coreMaterialId because
	 * of a previous call to addMaterialName, the same coreMaterialId will
	 * be used. 
	 *
	 * @param strFilename The file from which the core material should be loaded
	 *                    from.
	 * @param strMaterialName A string that is associated with an anim ID number.
	 *
	 * @return One of the following values:
	 *         \li the assigned \b ID of the loaded core material
	 *         \li \b -1 if an error happend
	 */
/*	public int loadCoreMaterial(String fileName, String materialName) {
	  Integer materialId = materialNames.get(materialName);
	  if (materialId != null) {
		  // the core skeleton has to be loaded already
		  if(coreSkeleton == null) {
			  edu.asu.shesc.csan3d.charanim.misc.Error.setLastError(edu.asu.shesc.csan3d.charanim.misc.Error.INVALID_HANDLE, "", -1, "");
			  return -1;
		  }
		  if(coreMaterials.get(materialId) == null) {
			  edu.asu.shesc.csan3d.charanim.misc.Error.setLastError(edu.asu.shesc.csan3d.charanim.misc.Error.INDEX_BUILD_FAILED, "", -1, "");
			  return -1;
		  }
		  CoreMaterial coreMaterial = Loader.loadCoreMaterial(fileName);
		  if(coreMaterial == null) return -1;
		  coreMaterial.setName(materialName);
		  coreMaterials.setElementAt(coreMaterial, materialId);
	  }
	  else
	  {
		  materialId = loadCoreMaterial(fileName);
		  if(materialId >= 0) addMaterialName(materialName, materialId);
	  }

	  return materialId;
	}
*/		
	public int createCoreMaterial(Color color) {
		 CoreMaterial coreMaterial = new CoreMaterial();
//		 if(coreMaterial == null) {
//		    Error.setLastError(Error.MEMORY_ALLOCATION_FAILED, "", -1, "");
//		    return -1;
//		 }
		 setCoreMaterial(coreMaterial, color);
		 return addCoreMaterial(coreMaterial);
	}
	
	//Sets the specified color as the material color
	private static void setCoreMaterial(CoreMaterial coreMaterial, Color color) {
		  // get the ambient color of the core material
		  RGBA ambientColor = new RGBA(color);
		  
		  // get the diffuse color of the core material
		  RGBA diffuseColor = new RGBA(color);
		  
		  // get the specular color of the core material
		  RGBA specularColor = new RGBA(color);

		  // get the shininess factor of the core material
		  float shininess = 32.0f;

		  // set the colors and the shininess
		  coreMaterial.setAmbientColor(ambientColor);
		  coreMaterial.setDiffuseColor(diffuseColor);
		  coreMaterial.setSpecularColor(specularColor);
		  coreMaterial.setShininess(shininess);

		  // Textures will go here
		  
	}
	
	public void setCoreMaterial(int coreMaterialIndex, Color color) {
		CoreMaterial coreMaterial = coreMaterials.elementAt(coreMaterialIndex);
		setCoreMaterial(coreMaterial, color);
	}
	
	/* Loads a core mesh.
	 *
	 * This function loads a core mesh from a file.
	 *
	 * @param strFilename The file from which the core mesh should be loaded from.
	 *
	 * @return One of the following values:
	 *         \li the assigned \b ID of the loaded core mesh
	 *         \li \b -1 if an error happend
	 */
	public int loadCoreMesh(String fileName) {
		// the core skeleton has to be loaded already
		if(coreSkeleton == null) {
			edu.asu.commons.foraging.jcal3d.misc.Error.setLastError(edu.asu.commons.foraging.jcal3d.misc.Error.INVALID_HANDLE, "", -1, "");
			return -1;
		}

		// load a new core mesh
		CoreMesh coreMesh = Loader.loadCoreMesh(fileName);
		if(coreMesh == null) return -1;
		
		//add core mesh to this core model
		return addCoreMesh(coreMesh);
	}

	/* Loads a core mesh and bind it to a name.
	 *
	 * This function loads a core mesh from a file. It is equivalent
	 * to calling addMeshName(strMeshName, loadCoreMesh(strFilename)).
	 * If strMeshName is already associated to a coreMeshId because
	 * of a previous call to addMeshName, the same coreMeshId will
	 * be used. 
	 *
	 * @param strFilename The file from which the core mesh should be loaded
	 *                    from.
	 * @param strMeshName A string that is associated with an anim ID number.
	 *
	 * @return One of the following values:
	 *         \li the assigned \b ID of the loaded core mesh
	 *         \li \b -1 if an error happend
	 */
	public int loadCoreMesh(String fileName, String meshName) {
	  Integer meshId = meshNames.get(meshName);
	  if (meshId != null) {
		  // the core skeleton has to be loaded already
		  if(coreSkeleton == null) {
			  edu.asu.commons.foraging.jcal3d.misc.Error.setLastError(edu.asu.commons.foraging.jcal3d.misc.Error.INVALID_HANDLE, "", -1, "");
			  return -1;
		  }
		  if(coreMeshes.get(meshId) == null) {
			  edu.asu.commons.foraging.jcal3d.misc.Error.setLastError(edu.asu.commons.foraging.jcal3d.misc.Error.INDEX_BUILD_FAILED, "", -1, "");
			  return -1;
		  }
		  CoreMesh coreMesh = Loader.loadCoreMesh(fileName);
		  if(coreMesh == null) return -1;
		  coreMesh.setName(meshName);
		  coreMeshes.setElementAt(coreMesh, meshId);
	  }
	  else
	  {
		  meshId = loadCoreMesh(fileName);
		  if(meshId >= 0) addMeshName(meshName, meshId);
	  }
	  return meshId;
	}

	//################  Unloading Functions  ####################	
	/* Delete the resources used by the named core animation. The name must 
	 * be associated with a valid core animation Id with the function
	 * getAnimationId. The caller must ensure that the corresponding is not
	 * referenced anywhere otherwise unpredictable results will occur.
	 *
	 * @param name The symbolic name of the core animation to unload.
	 *
	 * @return One of the following values:
	 *         \li the core \b ID of the unloaded core animation
	 *         \li \b -1 if an error happend
	 */
	public int unloadCoreAnimation(String animationName) {	
		int animationId = getCoreAnimationId(animationName);
		if(animationId >= 0)
			return unloadCoreAnimation(animationId);
		else
			return -1;
	}
		
	/* Delete the resources used by a core animation. The caller must
	 * ensure that the corresponding is not referenced anywhere otherwise
	 * unpredictable results will occur.
	 *
	 * @param coreAnimationId The ID of the core animation that should be unloaded.
	 *
	 * @return One of the following values:
	 *         \li the core \b ID of the unloaded core animation
	 *         \li \b -1 if an error happend
	 */
	public int unloadCoreAnimation(int animationId) {		
		coreAnimations.setElementAt(null, animationId);
		return animationId;
	}
	
	/* Delete the resources used by the named core material. The name must 
	 * be associated with a valid core material Id with the function
	 * getMaterialId. The caller must ensure that the corresponding is not
	 * referenced anywhere otherwise unpredictable results will occur.
	 *
	 * @param name The symbolic name of the core material to unload.
	 *
	 * @return One of the following values:
	 *         \li the core \b ID of the unloaded core material
	 *         \li \b -1 if an error happend
	 */
	public int unloadCoreMaterial(String materialName) {
		int materialId = getCoreMaterialId(materialName);
		if(materialId >= 0) return unloadCoreMaterial(materialId);
		else return -1;
	}
		
	/* Delete the resources used by a core material. The caller must
	 * ensure that the corresponding is not referenced anywhere otherwise
	 * unpredictable results will occur.
	 *
	 * @param coreMaterialId The ID of the core material that should be unloaded.
	 *
	 * @return One of the following values:
	 *         \li the core \b ID of the unloaded core material
	 *         \li \b -1 if an error happend
	 */
	public int unloadCoreMaterial(int materialId) {  
	  coreMaterials.setElementAt(null, materialId);
	  return materialId;
	}
		
	/* Delete the resources used by the named core mesh. The name must 
	 * be associated with a valid core mesh Id with the function
	 * getMeshId. The caller must ensure that the corresponding is not
	 * referenced anywhere otherwise unpredictable results will occur.
	 *
	 * @param name The symbolic name of the core mesh to unload.
	 *
	 * @return One of the following values:
	 *         \li the core \b ID of the unloaded core mesh
	 *         \li \b -1 if an error happend
	 */
	public int unloadCoreMesh(String meshName) {
		int meshId = getCoreMeshId(meshName);
		if(meshId >= 0) return unloadCoreMesh(meshId);
		else return -1;
	}
		
	/* Delete the resources used by a core mesh. The caller must
	 * ensure that the corresponding is not referenced anywhere otherwise
	 * unpredictable results will occur.
	 *
	 * @param coreMeshId The ID of the core mesh that should be unloaded.
	 *
	 * @return One of the following values:
	 *         \li the core \b ID of the unloaded core mesh
	 *         \li \b -1 if an error happend
	 */
	public int unloadCoreMesh(int meshId) {
	  coreMeshes.setElementAt(null, meshId);
	  return meshId;
	}
	
	//################ Save Methods ###################
	/* Saves the core skeleton.
	 *
	 * This function saves the core skeleton to a file.
	 *
	 * @param strFilename The file to which the core skeleton should be saved to.
	 *
	 * @return One of the following values:
	 *         \li \b true if successful
	 *         \li \b false if an error happend
	 */
	public boolean saveCoreSkeleton(String fileName) {
	  // check if we have a core skeleton in this code model
		if(coreSkeleton == null) {
			edu.asu.commons.foraging.jcal3d.misc.Error.setLastError(edu.asu.commons.foraging.jcal3d.misc.Error.INVALID_HANDLE, "", -1, "");
			return false;
		}

		//save the core skeleton
		return Saver.saveCoreSkeleton(fileName, coreSkeleton);
	}
	
	/* Saves a core animation.
	 *
	 * This function saves a core animation to a file.
	 *
	 * @param strFilename The file to which the core animation should be saved to.
	 * @param coreAnimationId The ID of the core animation that should be saved.
	 *
	 * @return One of the following values:
	 *         \li \b true if successful
	 *         \li \b false if an error happend
	 */
	public boolean saveCoreAnimation(String fileName, int animationId) {
	  // check if the core animation id is valid
	  if((animationId < 0) || (animationId >= coreAnimations.size())) {
		  edu.asu.commons.foraging.jcal3d.misc.Error.setLastError(edu.asu.commons.foraging.jcal3d.misc.Error.INVALID_HANDLE, "", -1, "");
		  return false;
	  }

	  // save the core animation
	  if(!Saver.saveCoreAnimation(fileName, coreAnimations.get(animationId))) return false;
	  return true;
	}
		
	/* Saves a core material.
	 *
	 * This function saves a core material to a file.
	 *
	 * @param strFilename The file to which the core material should be saved to.
	 * @param coreMaterialId The ID of the core material that should be saved.
	 *
	 * @return One of the following values:
	 *         \li \b true if successful
	 *         \li \b false if an error happend
	 */
	public boolean saveCoreMaterial(String fileName, int materialId) {
	  // check if the core material id is valid
	  if((materialId < 0) || (materialId >= coreMaterials.size())) {
		  edu.asu.commons.foraging.jcal3d.misc.Error.setLastError(edu.asu.commons.foraging.jcal3d.misc.Error.INVALID_HANDLE, "", -1, "");
		  return false;
	  }
	  // save the core animation
	  return Saver.saveCoreMaterial(fileName, coreMaterials.get(materialId));
	}
		
	/* Saves a core mesh.
	 *
	 * This function saves a core mesh to a file.
	 *
	 * @param strFilename The file to which the core mesh should be saved to.
	 * @param coreMeshId The ID of the core mesh that should be saved.
	 *
	 * @return One of the following values:
	 *         \li \b true if successful
	 *         \li \b false if an error happend
	 */
	public boolean saveCoreMesh(String fileName, int meshId) {
	  // check if the core mesh id is valid
	  if((meshId < 0) || (meshId >= coreMeshes.size())) {
		  edu.asu.commons.foraging.jcal3d.misc.Error.setLastError(edu.asu.commons.foraging.jcal3d.misc.Error.INVALID_HANDLE, "", -1, "");
		  return false;
	  }

	  // save the core animation
	  return Saver.saveCoreMesh(fileName, coreMeshes.get(meshId));
	}
	
	//################ Create Methods ###############
	public void createCoreMaterialThread(int materialIndex) {
		//insert an empty core material thread with a given id
		HashMap<Integer, Integer> mapCoreMaterialThreadId = new HashMap<Integer, Integer>();
		mapMapCoreMaterialThreads.put(materialIndex, mapCoreMaterialThreadId);		
	}
	
	
	//################# Add Methods #############
	/* Adds a core animation.
	  *
	  * This function adds a core animation to the core model instance.
	  *
	  * @param pCoreAnimation A pointer to the core animation that should be added.
	  *
	  * @return \li the assigned animation \b ID of the added core animation
	  */
	public int addCoreAnimation(CoreAnimation coreAnimation) {
		int animationId = coreAnimations.size();
		coreAnimations.add(coreAnimation);
		return animationId;
	}

	/*
	 * Adds a core morph animation.
	 *
	 * This function adds a core morph animation to the core model instance.
	 *
	 * @param pCoreMorphAnimation A pointer to the core morph animation that 
	 *                            should be added.
	 *
	 * @return One of the following values:
	 *         \li the assigned morph animation \b ID of the added core morph animation
	 *         \li \b -1 if an error happend
	 */
	public int addCoreMorphAnimation(CoreMorphAnimation coreMorphAnimation) {
		//get the id of the core morph animation
		int morphAnimationId = coreMorphAnimations.size();	
		coreMorphAnimations.add(coreMorphAnimation);	
		return morphAnimationId;
	}
		 
	/* Adds a core material.
	 *
	 * This function adds a core material to the core model instance.
	 *
	 * @param pCoreMaterial A pointer to the core material that should be added.
	 *
	 * @return One of the following values:
	 *         \li the assigned material \b ID of the added core material
	 *         \li \b -1 if an error happend
	 */
	public int addCoreMaterial(CoreMaterial coreMaterial) {
		// get the id of the core material
		int materialId = coreMaterials.size();
		coreMaterials.add(coreMaterial);
		return materialId;
	}
	
	/* Adds a core mesh.
	 *
	 * This function adds a core mesh to the core model instance.
	 *
	 * @param pCoreMesh A pointer to the core mesh that should be added.
	 *
	 * @return One of the following values:
	 *         \li the assigned mesh \b ID of the added core material
	 *         \li \b -1 if an error happend
	 */

	public int addCoreMesh(CoreMesh coreMesh) {
	  //get the id of the core mesh
	  int meshId = coreMeshes.size();
	  coreMeshes.add(coreMesh);
	  return meshId;
	}
		 
	/* Creates or overwrites a string-to-animation ID mapping
	 *
	 * This function makes an animation ID reference-able by a string name.
	 * Note that we don't verify that the ID is valid because the animation
	 * may be added later.
	 * Also, if there is already a helper with this name, it will be overwritten
	 * without warning.
	 *
	 * @param strAnimationName The string that will be associated with the ID.
	 * @param coreAnimationId The ID number of the animation to be referenced by the string.
	 */
	public boolean addAnimationName(String animationName, int animationId) {
		// check if the core animation id is valid
		coreAnimations.get(animationId).setName(animationName);
		animationNames.put(animationName, animationId);
		return true;
	}
		
	/* Creates or overwrites a string-to-core-material ID mapping
	 *
	 * This function makes a core material ID reference-able by a string name.
	 * Note that we don't verify that the ID is valid because the material
	 * may be added later.
	 * Also, if there is already a helper with this name, it will be overwritten
	 * without warning.
	 *
	 * @param strMaterialName The string that will be associated with the ID.
	 * @param coreMaterialId The core ID number of the material to be referenced by the string.
	 */
	public boolean addMaterialName(String materialName, int coreMaterialId) {	  
	  // check if the core material id is valid	  
	  coreMaterials.get(coreMaterialId).setName(materialName);
	  materialNames.put(materialName, coreMaterialId);
	  return true;
	}
		
	/* Creates or overwrites a string-to-core-mesh ID mapping
	 *
	 * This function makes a core mesh ID reference-able by a string name.
	 * Note that we don't verify that the ID is valid because the mesh
	 * may be added later.
	 * Also, if there is already a helper with this name, it will be overwritten
	 * without warning.
	 *
	 * @param strMeshName The string that will be associated with the ID.
	 * @param coreMeshId The core ID number of the mesh to be referenced by the string.
	 */
	public boolean addMeshName(String meshName, int coreMeshId) {
		coreMeshes.get(coreMeshId).setName(meshName);
	  	meshNames.put(meshName, coreMeshId);
	  	return true;
	}
	
	/* Creates or overwrites a string-to-boneId mapping
	 *
	 * This function makes a bone ID reference-able by a string name.
	 *
	 * @param strBoneName The string that will be associated with the ID.
	 * @param boneId The ID number of the bone that will be referenced by the string.
	 */
	public void addBoneName(String boneName, int boneId) {
	  //Make sure the skeleton has been loaded first
	  if (coreSkeleton != null) {
		  //Map the bone ID to the name
		  coreSkeleton.mapCoreBoneName(boneId, boneName);
	  }
	}

	//###########  Get Methods  #################	
	/* Provides access to the core skeleton.
	 *
	 * This function returns the core skeleton.
	 *
	 * @return One of the following values:
	 *         \li a pointer to the core skeleton
	 *         \li \b 0 if an error happend
	 */
	public CoreSkeleton getCoreSkeleton() {
	  return coreSkeleton;
	}
	
	/* Provides access to a core animation.
	 *
	 * This function returns the core animation with the given ID.
	 *
	 * @param coreAnimationId The ID of the core animation that should be returned.
	 *
	 * @return One of the following values:
	 *         \li a pointer to the core animation
	 *         \li \b 0 if an error happend
	 */
	public CoreAnimation getCoreAnimation(int coreAnimationId) {	  
	  return coreAnimations.get(coreAnimationId);
	}
		
	/* Provides access to a core morph animation.
	 *
	 * This function returns the core morph animation with the given ID.
	 *
	 * @param coreMorphAnimationId The ID of the core morph animation that should be returned.
	 *
	 * @return One of the following values:
	 *         \li a pointer to the core morph animation
	 *         \li \b 0 if an error happend
	 */
	public CoreMorphAnimation getCoreMorphAnimation(int coreMorphAnimationId) {
		return coreMorphAnimations.get(coreMorphAnimationId);
	}
		
	/* Returns the number of core animations.
	 *
	 * This function returns the number of core animations in the core model
	 * instance.
	 *
	 * @return The number of core animations.
	 */
	public int getCoreAnimationCount() {
	  return coreAnimations.size();
	}
	 
	/* Returns the number of core morph animations.
	 *
	 * This function returns the number of core morph animations in the core model
	 * instance.
	 *
	 * @return The number of core morph animations.
	 */
	public int getCoreMorphAnimationCount() {
	  return coreMorphAnimations.size();
	}

	public CoreMaterial getCoreMaterial(int materialIndex) {		
		return coreMaterials.get(materialIndex);
	}
	
	public int getCoreMaterialCount() {
		return coreMaterials.size();
	}

	/* Returns a specified core material ID.
	 *
	 * This function returns the core material ID for a specified core material
	 * thread / core material set pair.
	 *
	 * @param coreMaterialThreadId The ID of the core material thread.
	 * @param coreMaterialSetId The ID of the core material set.
	 *
	 * @return One of the following values:
	 *         \li the \b ID of the core material
	 *         \li \b -1 if an error happend
	 */
	public int getCoreMaterialId(int coreMaterialThreadId, int coreMaterialSetId) {
		//find the core material thread	  
		HashMap<Integer, Integer> coreMaterialThread = mapMapCoreMaterialThreads.get(coreMaterialThreadId);
	  
		//find the material id for the given set		
		return coreMaterialThread.get(coreMaterialSetId);		
	}

	/* Retrieves the ID of the core material referenced by a string
	 *
	 * This function returns a core material ID
	 *
	 * @param strMaterialName A string that is associated with a core material ID number.
	 * @return Returns:
	 *         \li \b -1 if there is no core material ID associated with the input string
	 *         \li \b the core ID number of the material asssociated with the input string
	 */
	public int getCoreMaterialId(String materialName) {
		if (! materialNames.containsKey(materialName)) return -1;
		if (getCoreMaterial(materialNames.get(materialName)) == null) return -1;  

		return materialNames.get(materialName);
	}
		
	/* Provides access to a core mesh.
	 *
	 * This function returns the core mesh with the given ID.
	 *
	 * @param coreMeshId The ID of the core mesh that should be returned.
	 *
	 * @return One of the following values:
	 *         \li a pointer to the core mesh
	 *         \li \b 0 if an error happend
	 */
	public CoreMesh getCoreMesh(int coreMeshId) {
	  return coreMeshes.get(coreMeshId);
	}
	
	public int getCoreMeshCount() {
		return coreMeshes.size();
	}
	
	/* Retrieves the ID of the animation referenced by a string
	 *
	 * This function returns an animation ID
	 *
	 * @param strAnimationName A string that is associated with an anim ID number.
	 * @return Returns:
	 *         \li \b -1 if there is no anim ID associated with the input string
	 *         \li \b the ID number of the anim asssociated with the input string
	 */
	public int getCoreAnimationId(String animationName) {
	  if (! animationNames.containsKey(animationName)) return -1;	    
	  if (getCoreAnimation(animationNames.get(animationName)) == null) return -1;
	  
	  return animationNames.get(animationName);
	}
		
	/* Retrieves the ID of the core mesh referenced by a string
	 *
	 * This function returns a core mesh ID
	 *
	 * @param strMeshName A string that is associated with a core mesh ID number.
	 * @return Returns:
	 *         \li \b -1 if there is no core mesh ID associated with the input string
	 *         \li \b the core ID number of the mesh asssociated with the input string
	 */
	public int getCoreMeshId(String meshName) {
	  if (! meshNames.containsKey(meshName)) return -1;	  
	  if (getCoreMesh(meshNames.get(meshName)) == null) return -1;
	  return meshNames.get(meshName);
	}
	
	/* Retrieves the ID of the bone referenced by a string
	 *
	 * This function returns a bone ID
	 *
	 * @param strBoneName A string that is associated with a bone ID number.
	 * @return Returns:
	 *         \li \b -1 if there is no bone ID associated with the input string
	 *         \li \b the ID number of the bone asssociated with the input string
	 */
	public int getBoneId(String boneName) {
	  if (coreSkeleton != null) {
		  return coreSkeleton.getCoreBoneId(boneName);
	  }
	  return -1;
	}
	
	/* Provides access to the user data.
	 *
	 * This function returns the user data stored in the core model instance.
	 *
	 * @return The user data stored in the core model instance.
	 */
	public Object getUserData() {
	  return userData;
	}

	//######### Set Methods ###################
	/* Sets a core material ID.
	 *
	 * This function sets a core material ID for a core material thread / core
	 * material set pair.
	 *
	 * @param coreMaterialThreadId The ID of the core material thread.
	 * @param coreMaterialSetId The ID of the core maetrial set.
	 * @param coreMaterialId The ID of the core maetrial.
	 *
	 * @return One of the following values:
	 *         \li \b true if successful
	 *         \li \b false if an error happend
	 */
	public boolean setCoreMaterialId(int coreMaterialThreadId, int coreMaterialSetId, int coreMaterialId) {
	  // find the core material thread	  
	  HashMap<Integer, Integer> coreMaterialThread = mapMapCoreMaterialThreads.get(coreMaterialThreadId);
	  if(coreMaterialThread == null) {
		  edu.asu.commons.foraging.jcal3d.misc.Error.setLastError(edu.asu.commons.foraging.jcal3d.misc.Error.INVALID_HANDLE, "", -1, "");
		  return false;
	  }
	  
	  // set the given set id in the core material thread to the given core material id
	  coreMaterialThread.put(coreMaterialSetId, coreMaterialId);
	  
	  mapMapCoreMaterialThreads.put(coreMaterialThreadId, coreMaterialThread);

	  return true;
	}
	 
	/* Sets the core skeleton.
	 *
	 * This function sets the core skeleton of the core model instance..
	 *
	 * @param pCoreSkeleton The core skeleton that should be set.
	 */
	public void setCoreSkeleton(CoreSkeleton coreSkeleton) {
		if(coreSkeleton == null) {
			edu.asu.commons.foraging.jcal3d.misc.Error.setLastError(edu.asu.commons.foraging.jcal3d.misc.Error.INVALID_HANDLE, "", -1, "");
			return;
		}
		this.coreSkeleton = coreSkeleton;  
	}
	 
	/* Stores user data.
	 *
	 * This function stores user data in the core model instance.
	 *
	 * @param userData The user data that should be stored.
	 */
	public void setUserData(Object userData){
		this.userData = userData;
	}
	
	/* Scale the core model.
	 *
	 * This function rescale all data that are in the core model instance
	 *
	 * @param factor A float with the scale factor
	 *
	 */
	public void scale(float factor) {
	  coreSkeleton.scale(factor);
	  for(int animationId = 0; animationId < coreAnimations.size(); animationId++) {
		  coreAnimations.get(animationId).scale(factor);
	  }

	  for(int meshId = 0; meshId < coreMeshes.size(); meshId++) {
		  coreMeshes.get(meshId).scale(factor);
	  }
	}

}
