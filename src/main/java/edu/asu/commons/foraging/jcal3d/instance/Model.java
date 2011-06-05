package edu.asu.commons.foraging.jcal3d.instance;

import java.util.Vector;

import edu.asu.commons.foraging.graphics.Point3D;
import edu.asu.commons.foraging.graphics.Vector3D;
import edu.asu.commons.foraging.jcal3d.core.CoreMesh;
import edu.asu.commons.foraging.jcal3d.core.CoreModel;
import edu.asu.commons.foraging.jcal3d.misc.BoundingBox;
import edu.asu.commons.foraging.jcal3d.misc.Error;



public class Model {

	protected CoreModel coreModel = null;
	protected Skeleton skeleton = null;
	protected AbstractMixer mixer = null;
	protected MorphTargetMixer morphTargetMixer = null;
	protected Physique physique = null;
	protected SpringSystem springSystem = null;
	protected Renderer renderer = null;
	protected Object userData = null;
	protected Vector<Mesh> meshes = new Vector<Mesh>();
	protected BoundingBox boundingBox;
	  
	/*****************************************************************************/
	/** Constructs the model instance.
	  *
	  * This function is the default constructor of the model instance.
	  *****************************************************************************/
	public Model(CoreModel coreModel) {
	  //FIXME: Make sure that coreModel is not null		

	  this.coreModel = coreModel;
	  skeleton = new Skeleton(coreModel.getCoreSkeleton());
	  mixer = new Mixer(this);
	  morphTargetMixer = new MorphTargetMixer(this);
	  physique = new Physique(this);
	  springSystem = new SpringSystem(this);
	  renderer = new Renderer(this);
	}
	 
	 /*****************************************************************************/
	/** Attachs a mesh.
	  *
	  * This function attachs a mesh to the model instance.
	  *
	  * @param coreMeshId The ID of the mesh that should be attached.
	  *
	  * @return One of the following values:
	  *         \li \b true if successful
	  *         \li \b false if an error happend
	  *****************************************************************************/

	public boolean attachMesh(int coreMeshId) {
	  // check if the id is valid
	  if((coreMeshId < 0) ||(coreMeshId >= coreModel.getCoreMeshCount()))
	  {
	    Error.setLastError(Error.INVALID_HANDLE, "", -1, "");
	    return false;
	  }

	  // get the core mesh
	  CoreMesh coreMesh = coreModel.getCoreMesh(coreMeshId);

	  // check if the mesh is already attached
	  int meshId;
	  for(meshId = 0; meshId < meshes.size(); ++meshId) {
	    // check if we found the matching mesh
	    if(meshes.get(meshId).getCoreMesh().equals(coreMesh)) {
	      // mesh is already active -> do nothing
	      return true;
	    }
	  }

	  // allocate a new mesh instance
	  Mesh mesh = new Mesh(coreMesh);
	  // set model in the mesh instance
	  mesh.setModel(this);

	  // insert the new mesh into the active list
	  meshes.add(mesh);

	  return true;
	}


	 /*****************************************************************************/
	/** Detaches a mesh.
	  *
	  * This function detaches a mesh from the model instance.
	  *
	  * @param coreMeshId The ID of the mesh that should be detached.
	  *
	  * @return One of the following values:
	  *         \li \b true if successful
	  *         \li \b false if an error happend
	  *****************************************************************************/

	public boolean detachMesh(int coreMeshId) {
	  // check if the id is valid
	  if((coreMeshId < 0) ||(coreMeshId >= coreModel.getCoreMeshCount())) {
	    Error.setLastError(Error.INVALID_HANDLE, "", -1, "");
	    return false;
	  }

	  // get the core mesh
	  CoreMesh coreMesh = coreModel.getCoreMesh(coreMeshId);

	  // find the mesh for the given id	  
	  for(int meshIndex = 0; meshIndex < meshes.size(); ++meshIndex) {	  
	    // get the mesh
	    Mesh mesh = meshes.get(meshIndex);

	    // check if we found the matching mesh
	    if(mesh.getCoreMesh().equals(coreMesh)) {
	      // erase the mesh out of the active mesh list
	      meshes.remove(meshIndex);
	      return true;
	    }
	  }

	  return false;
	}

	/** Provides access to the core model.
	  *
	  * This function returns the core model on which this model instance is based
	  * on.
	  *
	  * @return One of the following values:
	  *         \li a pointer to the core model
	  *         \li \b 0 if an error happend
	  */
	public CoreModel getCoreModel() {
	  return coreModel;
	}
	 
	/** Provides access to an attached mesh.
	  *
	  * This function returns the attached mesh with the given core mesh ID.
	  *
	  * @param coreMeshId The core mesh ID of the mesh that should be returned.
	  *
	  * @return One of the following values:
	  *         \li a pointer to the mesh
	  *         \li \b 0 if an error happend
	  */
	public Mesh getMesh(int coreMeshId) {
	  // check if the id is valid
	  if((coreMeshId < 0) ||(coreMeshId >= coreModel.getCoreMeshCount())) {
	    Error.setLastError(Error.INVALID_HANDLE, "", -1, "");
	    return null;
	  }

	  // get the core mesh
	  CoreMesh coreMesh = coreModel.getCoreMesh(coreMeshId);

	  // search the mesh
	  for(int meshId = 0; meshId < meshes.size(); ++meshId) {
	    // check if we found the matching mesh
	    if(meshes.get(meshId).getCoreMesh().equals(coreMesh))
	    {
	      return meshes.get(meshId);
	    }
	  }

	  return null;
	}

	/*****************************************************************************/
	/** Returns the mixer.
	 *
	 * If a mixer that is not an instance of CalMixer was set with the
	 * CalModel::setAbstractMixer method, an INVALID_MIXER_TYPE error (see
	 * CalError) is set and 0 is returned.
	 *
	 * @return \li a pointer to the mixer
	 *         \li \b 0 if an error happend
	 *****************************************************************************/

	public Mixer getMixer() {
	  if(mixer == null)
	    return null;

	  if(mixer.isDefaultMixer() == false) {
	    Error.setLastError(Error.INVALID_MIXER_TYPE, "", -1, "");
	    return null;
	  } 
	  else {
	    return (Mixer)(mixer);
	  }
	}

	/*****************************************************************************/
	/** Returns the mixer. 
	 *
	 * @return \li a pointer to the mixer
	 *         \li \b 0 if no mixer was set
	 *****************************************************************************/

	public AbstractMixer getAbstractMixer() {
	  return mixer;
	}

	/*****************************************************************************/
	/** Sets the mixer to a CalAbstractMixer subclass instance.
	 *
	 * If a mixer was already set (with CalModel::setAbstractMixer or
	 * because the CalModel::create method created a CalMixer instance),
	 * its \b destroy method is called. The existing mixer is not
	 * deallocated, it is the responsibility of the caller to call the
	 * getAbstractMixer method and deallocate the returned instance if
	 * appropriate.
	 *
	 * \b pMixer will be deallocated by cal3d if and only if the
	 * CalModel::destroy function is called.
	 *
	 * The \b create method of pMixer is called.
	 *
	 * pMixer may be null. After setting a null pointer, the caller MUST
	 * call CalModel::create or CalModel::setAbstractMixer with a non-null
	 * pointer before any other method is called.
	 *
	 * @param pMixer is a pointer to a CalAbstractMixer subclass instance.
	 *
	 *****************************************************************************/

	public void setAbstractMixer(AbstractMixer mixer){
	  this.mixer = mixer;
	}

	/*****************************************************************************/
	/** Provides access to the morph target mixer.
	  *
	  * This function returns the morph target mixer.
	  *
	  * @return One of the following values:
	  *         \li a pointer to the morph target mixer
	  *         \li \b 0 if an error happend
	  *****************************************************************************/
	public MorphTargetMixer getMorphTargetMixer() {
	  return morphTargetMixer;
	}

	 /*****************************************************************************/
	/** Provides access to the physique.
	  *
	  * This function returns the physique.
	  *
	  * @return One of the following values:
	  *         \li a pointer to the physique
	  *         \li \b 0 if an error happend
	  *****************************************************************************/

	public Physique getPhysique() {
	  return physique;
	}

	 /*****************************************************************************/
	/** Provides access to the renderer.
	  *
	  * This function returns the renderer.
	  *
	  * @return One of the following values:
	  *         \li a pointer to the renderer
	  *         \li \b 0 if an error happend
	  *****************************************************************************/

	public Renderer getRenderer() {
	  return renderer;
	}

	 /*****************************************************************************/
	/** Provides access to the skeleton.
	  *
	  * This function returns the skeleton.
	  *
	  * @return One of the following values:
	  *         \li a pointer to the skeleton
	  *         \li \b 0 if an error happend
	  *****************************************************************************/

	public Skeleton getSkeleton() {
	  return skeleton;
	}

	 /*****************************************************************************/
	/** Provides access to the spring system.
	  *
	  * This function returns the spring system.
	  *
	  * @return One of the following values:
	  *         \li a pointer to the spring system
	  *         \li \b 0 if an error happend
	  *****************************************************************************/

	public SpringSystem getSpringSystem() {
	  return springSystem;
	}

	 /*****************************************************************************/
	/** Returns the global bounding box of the model.
	  *
	  * This function returns the global bounding box of the model.
	  *
	  * @param precision : indicate if the function need to compute a 
	  *        correct bounding box
	  *
	  * @return bounding box.
	  *****************************************************************************/


	public BoundingBox getBoundingBox(boolean precision) {
		Vector3D v;
		v = new Vector3D(1.0f,0.0f,0.0f);	
		boundingBox.plane[0].setNormal(v);
		v = new Vector3D(-1.0f,0.0f,0.0f);	
		boundingBox.plane[1].setNormal(v);
		v = new Vector3D(0.0f,1.0f,0.0f);	
		boundingBox.plane[2].setNormal(v);
		v = new Vector3D(0.0f,-1.0f,0.0f);	
		boundingBox.plane[3].setNormal(v);
		v = new Vector3D(0.0f,0.0f,1.0f);	
		boundingBox.plane[4].setNormal(v);
		v = new Vector3D(0.0f,0.0f,-1.0f);	
		boundingBox.plane[5].setNormal(v);

		if(precision)
			skeleton.calculateBoundingBoxes();

		
		Vector<Bone> bones =  skeleton.getBones();				
		for(int boneIndex = 0; boneIndex < bones.size(); boneIndex++) {
			Bone bone = bones.get(boneIndex);
		
			// If it's just an approximation that are needed then
			// we just compute the bounding box from the skeleton

			if(!precision || !bone.getCoreBone().isBoundingBoxPrecomputed())
			{
				
				Vector3D translation = bone.getTranslationAbsolute();
				
				int planeId;
				for(planeId = 0; planeId < 6; ++planeId)
				{
					if(boundingBox.plane[planeId].eval(translation) < 0.0f)
					{
						boundingBox.plane[planeId].setPosition(translation);
					}
				}
			}
			else
			{
				BoundingBox localBoundingBox = bone.getBoundingBox();
				Vector<Point3D> points = localBoundingBox.computePoints();
				
				for(int i=0; i < 8; i++)
				{				
					int planeId;
					for(planeId = 0; planeId < 6; ++planeId)
					{
						if(boundingBox.plane[planeId].eval(points.get(i)) < 0.0f)
						{
							boundingBox.plane[planeId].setPosition(points.get(i));
						}
					}
				}				
			}
		}
		
		return boundingBox;
	}

	 /*****************************************************************************/
	/** Provides access to the user data.
	  *
	  * This function returns the user data stored in the model instance.
	  *
	  * @return The user data stored in the model instance.
	  *****************************************************************************/

	public Object getUserData() {
	  return userData;
	}

	 /*****************************************************************************/
	/** Returns the mesh vector.
	  *
	  * This function returns the vector that contains all attached meshes of the
	  * model instance.
	  *
	  * @return A reference to the mesh vector.
	  *****************************************************************************/

	public Vector<Mesh> getMeshes() {
	  return meshes;
	}

	 /*****************************************************************************/
	/** Sets the LOD level.
	  *
	  * This function sets the LOD level of all attached meshes.
	  *
	  * @param lodLevel The LOD level in the range [0.0, 1.0].
	  *****************************************************************************/
	public void setLodLevel(float lodLevel) {
	  // set the lod level in all meshes	  
	  for(int meshIndex = 0; meshIndex < meshes.size(); ++meshIndex) {
	    // set the lod level in the mesh
	    meshes.get(meshIndex).setLodLevel(lodLevel);
	  }
	}

	 /*****************************************************************************/
	/** Sets the material set.
	  *
	  * This function sets the material set of all attached meshes.
	  *
	  * @param setId The ID of the material set.
	  *****************************************************************************/

	public void setMaterialSet(int setId) {
	  // set the lod level in all meshes	  
	  for(int meshIndex = 0; meshIndex < meshes.size(); ++meshIndex) {
	    // set the material set in the mesh
	    meshes.get(meshIndex).setMaterialSet(setId);
	  }
	}

	 /*****************************************************************************/
	/** Stores user data.
	  *
	  * This function stores user data in the model instance.
	  *
	  * @param userData The user data that should be stored.
	  *****************************************************************************/

	public void setUserData(Object userData) {
	  this.userData = userData;
	}

	 /*****************************************************************************/
	/** Updates the model instance.
	  *
	  * This function updates the model instance for a given amount of time.
	  *
	  * @param deltaTime The elapsed time in seconds since the last update.
	  *****************************************************************************/

	public void update(float deltaTime)	{
	  mixer.updateAnimation(deltaTime);
	  mixer.updateSkeleton();	  
	  // m_pMorpher->update(...);
	  morphTargetMixer.update(deltaTime);
	  physique.update();
	  springSystem.update(deltaTime);
	}

	/*****************************************************************************/
	/** Disable internal data (and thus springs system)
	  *
	  *****************************************************************************/

	public void disableInternalData() {
	  // Disable internal data in all meshes	  
	  for(int meshIndex = 0; meshIndex < meshes.size(); ++meshIndex) {
	    // Disable internal data in the mesh
	    meshes.get(meshIndex).disableInternalData();
	  }
	}

	public void setSkeleton(Skeleton skeleton) {
		this.skeleton = skeleton;
	}

}
