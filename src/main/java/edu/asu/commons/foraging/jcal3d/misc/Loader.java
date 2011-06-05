package edu.asu.commons.foraging.jcal3d.misc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Vector;

import edu.asu.commons.foraging.fileplugin.FileLoader;
import edu.asu.commons.foraging.graphics.Point3D;
import edu.asu.commons.foraging.graphics.Vector3D;
import edu.asu.commons.foraging.jcal3d.core.CoreAnimation;
import edu.asu.commons.foraging.jcal3d.core.CoreBone;
import edu.asu.commons.foraging.jcal3d.core.CoreKeyframe;
import edu.asu.commons.foraging.jcal3d.core.CoreMesh;
import edu.asu.commons.foraging.jcal3d.core.CoreSkeleton;
import edu.asu.commons.foraging.jcal3d.core.CoreSubmesh;
import edu.asu.commons.foraging.jcal3d.core.CoreTrack;



public class Loader {

	private static int loadingMode;
		
	static final String SKELETON_FILE_EXTENSION = ".csf";
	static final String ANIMATION_FILE_EXTENSION = ".caf";
	static final String MESH_FILE_EXTENSION = ".cmf";
	static final String MATERIAL_FILE_EXTENSION = ".crf";
	static final String SKELETON_XML_FILE_EXTENSION = ".xsf";
	static final String ANIMATION_XML_FILE_EXTENSION = ".xaf";
	static final String MESH_XML_FILE_EXTENSION = ".xmf";
	static final String MATERIAL_XML_FILE_EXTENSION = ".xrf";
	
	static final String SKELETON_FILE_MAGIC = "CSF\0";
	static final String ANIMATION_FILE_MAGIC = "CAF\0";
	static final String MESH_FILE_MAGIC = "CMF\0";
	static final String MATERIAL_FILE_MAGIC = "CRF\0";
	static final String XML_SKELETON_FILE_MAGIC = "XSF";
	static final String XML_ANIMATION_FILE_MAGIC = "XAF";
	static final String XML_MESH_FILE_MAGIC = "XMF";
	static final String XML_MATERIAL_FILE_MAGIC = "XRF";
	
	static final int FILE_VERSION = 700;
	static final int NEW_FILE_VERSION = 910;
	static final String XML_FILE_VERSION = "900";
	static final String NEW_XML_FILE_VERSION = "910";
	
	static final int LOADER_ROTATE_X_AXIS = 1;
    static final int LOADER_INVERT_V_COORD = 2;
    static final int LOADER_FLIP_WINDING = 4;
    	
	/* Sets optional flags which affect how the model is loaded into memory.
	 *
	 * This function sets the loading mode for all future loader calls.
	 *
	 * @param flags A boolean OR of any of the following flags
	 *         \li LOADER_ROTATE_X_AXIS will rotate the mesh 90 degrees about the X axis,
	 *             which has the effect of swapping Y/Z coordinates.
	 *         \li LOADER_INVERT_V_COORD will substitute (1-v) for any v texture coordinate
	 *             to eliminate the need for texture inversion after export.
	 */
	public static void setLoadingMode(int flags) {
		loadingMode = flags;
	}

	//##################### Skeleton #####################
	public static CoreSkeleton loadCoreSkeleton(String fileName) {
		if(fileName.endsWith(SKELETON_XML_FILE_EXTENSION)) 
			//XML file
		    return loadXmlCoreSkeleton(fileName);

		else {
			try {
//				File file = new File(fileName);
                

                
                ByteArrayOutputStream byteStream = FileLoader.getByteArrayOutputStream(fileName);
                
//				ByteBuffer byteBuffer = ByteBuffer.allocate((int)file.length()).order(ByteOrder.LITTLE_ENDIAN);
//		        FileChannel fileChannel = new FileInputStream(file).getChannel();
//		        fileChannel.read(byteBuffer);
//				byte[] array = new byte[(int)file.length()];
//		        FileInputStream fileInputStream = new FileInputStream(file);		         
//		        fileInputStream.read(array);		        
//		        ByteArrayInputStream byteStream = new ByteArrayInputStream(array);
		        ByteBuffer byteBuffer = ByteBuffer.wrap(byteStream.toByteArray());
		        byteBuffer.order(ByteOrder.LITTLE_ENDIAN); 
		        		        
		        CoreSkeleton coreSkeleton = loadCoreSkeleton(byteBuffer);
		        		    
		        // Close the file
		        //fileChannel.close();
//		        fileInputStream.close();
		        byteStream.close();
		        return coreSkeleton;
		    } 
			catch (IOException ioe) {
				edu.asu.commons.foraging.jcal3d.misc.Error.setLastError(edu.asu.commons.foraging.jcal3d.misc.Error.FILE_NOT_FOUND, "", -1, fileName);
				ioe.printStackTrace();
		    }
		}
		return null;
	}
		
	public static CoreSkeleton loadCoreSkeleton(ByteBuffer inputBuffer) {
		//check if this is a valid file
		byte magic[] = new byte[4];
		try {
			inputBuffer.get(magic, 0, 4);			
			String string = new String(magic); 
						
			if (!string.equals(SKELETON_FILE_MAGIC)) {
				edu.asu.commons.foraging.jcal3d.misc.Error.setLastError(edu.asu.commons.foraging.jcal3d.misc.Error.INVALID_FILE_FORMAT, "", -1, "");
				return null;
			}
			
			//Check if the version is compatible with the library
			int version = inputBuffer.getInt();
			if (version != FILE_VERSION && version != NEW_FILE_VERSION) {
				edu.asu.commons.foraging.jcal3d.misc.Error.setLastError(edu.asu.commons.foraging.jcal3d.misc.Error.INCOMPATIBLE_FILE_VERSION, "", -1, "");
				return null;
			}
			
			//Read the number of bones
			int boneCount = inputBuffer.getInt();
			if(boneCount <= 0)
			{
				Error.setLastError(Error.INVALID_FILE_FORMAT, "", -1, "");
				return null;
			}
			
			//Allocate a new core skeleton instance
			CoreSkeleton coreSkeleton = new CoreSkeleton();
//			if(coreSkeleton == null)
//			{
//				Error.setLastError(Error.MEMORY_ALLOCATION_FAILED, "", -1, "");
//				return null;
//			}

			//Load all core bones
			for(int boneIndex = 0; boneIndex < boneCount; ++boneIndex) {				
				//Load the core bone
			    CoreBone coreBone = loadCoreBones(inputBuffer);
			    if(coreBone == null) return null;

			    // set the core skeleton of the core bone instance
			    coreBone.setCoreSkeleton(coreSkeleton);

			    // add the core bone to the core skeleton instance
			    coreSkeleton.addCoreBone(coreBone);

			    // add a core skeleton mapping of the bone's name for quick reference later
			    coreSkeleton.mapCoreBoneName(boneIndex, coreBone.getName());			    
			}		
			
			//Calculate state of the core skeleton
			coreSkeleton.calculateState();

			return coreSkeleton;
		}
		catch(BufferUnderflowException  bue) {
			bue.printStackTrace();
		}
		return null;		  
	}

	private static CoreSkeleton loadXmlCoreSkeleton(String strFilename) {
		return null;
	}
	
	/*****************************************************************************/
	/** Loads a core bone instance.
	  *
	  * This function loads a core bone instance from a data source.
	  *
	  * @param dataSrc The data source to load the core bone instance from.
	  *
	  * @return One of the following values:
	  *         \li a pointer to the core bone
	  *         \li \b 0 if an error happened
	  *****************************************************************************/

	private static CoreBone loadCoreBones(ByteBuffer inputBuffer) {	  
	  //Read length of bone name
		int boneNameLength = inputBuffer.getInt();
		
		// read the name of the bone
		byte charBuffer[] = new byte[boneNameLength];
		inputBuffer.get(charBuffer);			
			
		String boneName = new String(charBuffer); 
		boneName = boneName.substring(0, boneNameLength-1);

		// get the translation of the bone
		float tx = inputBuffer.getFloat();
		float ty = inputBuffer.getFloat();
		float tz = inputBuffer.getFloat();
		
		// get the rotation of the bone
		float rx = inputBuffer.getFloat();
		float ry = inputBuffer.getFloat();
		float rz = inputBuffer.getFloat();
		float rw = inputBuffer.getFloat();
	  
		// get the bone space translation of the bone
		float txBoneSpace = inputBuffer.getFloat();
		float tyBoneSpace = inputBuffer.getFloat();
		float tzBoneSpace = inputBuffer.getFloat();
				
		// get the bone space rotation of the bone
		float rxBoneSpace = inputBuffer.getFloat();
		float ryBoneSpace = inputBuffer.getFloat();
		float rzBoneSpace = inputBuffer.getFloat();
		float rwBoneSpace = inputBuffer.getFloat();
			
		// get the parent bone id
		int parentId = inputBuffer.getInt();
	  
		Quaternion rot = new Quaternion(rx,ry,rz,rw);
		Quaternion rotbs = new Quaternion(rxBoneSpace, ryBoneSpace, rzBoneSpace, rwBoneSpace);
		Vector3D trans = new Vector3D(tx,ty,tz);

		if ((loadingMode & LOADER_ROTATE_X_AXIS) == LOADER_ROTATE_X_AXIS)
		{
			if (parentId == -1) // only root bone necessary
		    {
		      // Root bone must have quaternion rotated
		      Quaternion x_axis_90 = new Quaternion(0.7071067811f,0.0f,0.0f,0.7071067811f);
		      rot = rot.multiply(x_axis_90);
		      // Root bone must have translation rotated also
		      trans = trans.multiply(x_axis_90);
		    }
	  }
	  
//	  // allocate a new core bone instance	  
	  CoreBone coreBone = new CoreBone(boneName);
//	  if(coreBone == null) {
//	    Error.setLastError(Error.MEMORY_ALLOCATION_FAILED, "", -1, "");
//	    return null;
//	  }

	  // set the parent of the bone
	  coreBone.setParentId(parentId);

	  // set all attributes of the bone
	  coreBone.setTranslation(trans);
	  coreBone.setRotation(rot);
	  coreBone.setTranslationBoneSpace(new Vector3D(txBoneSpace, tyBoneSpace, tzBoneSpace));
	  coreBone.setRotationBoneSpace(rotbs);

	  //read the number of children
	  int childCount = inputBuffer.getInt();
	  if(childCount < 0)
	  {
	    Error.setLastError(Error.INVALID_FILE_FORMAT, "", -1, "");
	    return null;
	  }

	  // load all children ids
	  for(; childCount > 0; childCount--)
	  {
	    int childId = inputBuffer.getInt();
	    if(childId < 0)
	    {	      
	      Error.setLastError(Error.INVALID_FILE_FORMAT, "", -1, "");
	      return null;
	    }

	    coreBone.addChildId(childId);
	  }

	  return coreBone;
	}

	
	//#################### Animation ###################
	/* Loads a core animation instance.
	  *
	  * This function loads a core animation instance from a file.
	  *
	  * @param strFilename The file to load the core animation instance from.
	  *
	  * @return One of the following values:
	  *         \li a pointer to the core animation
	  *         \li \b 0 if an error happened
	  
	  */
	public static CoreAnimation loadCoreAnimation(String fileName, CoreSkeleton skel) {
		if(fileName.endsWith(ANIMATION_XML_FILE_EXTENSION)) 
			//XML file
		    return loadXmlCoreAnimation(fileName, skel);

		else {
			try {
//				File file = new File(fileName);
//				byte[] array = new byte[(int)file.length()];
//		        FileInputStream fileInputStream = new FileInputStream(file);		         
//		        fileInputStream.read(array);
                ByteArrayOutputStream byteStream = FileLoader.getByteArrayOutputStream(fileName);
		        ByteBuffer byteBuffer = ByteBuffer.wrap(byteStream.toByteArray());
		        byteBuffer.order(ByteOrder.LITTLE_ENDIAN); 
		        
		        CoreAnimation coreAnimation = loadCoreAnimation(byteBuffer, skel);
		        if (coreAnimation != null) coreAnimation.setFileName(fileName);
		        // Close the file
		        byteStream.close();
		        return coreAnimation;
		    } 
			catch (IOException ioe) {
				ioe.printStackTrace();
		    }
		}
		return null;
	}
		
	public static CoreAnimation loadCoreAnimation(ByteBuffer inputBuffer, CoreSkeleton skel) {
		//check if this is a valid file
		  byte magic[] = new byte[4];
		  inputBuffer.get(magic);
		  String magicToken = new String(magic);
		  
		  if(!magicToken.equals(ANIMATION_FILE_MAGIC)) {
			  Error.setLastError(Error.INVALID_FILE_FORMAT, "", -1, "");
			  return null;
		  }

		  // check if the version is compatible with the library
		  int version = inputBuffer.getInt();
		  if(version != FILE_VERSION && version != NEW_FILE_VERSION) {
		    Error.setLastError(Error.INCOMPATIBLE_FILE_VERSION, "", -1, "");
		    return null;
		  }

		  // allocate a new core animation instance
		  CoreAnimation coreAnimation = new CoreAnimation();
//		  if(coreAnimation == null) {
//		    Error.setLastError(Error.MEMORY_ALLOCATION_FAILED, "", -1, "");
//		    return null;
//		  }

		  // get the duration of the core animation
		  float duration = inputBuffer.getFloat();
		  // check for a valid duration
		  if(duration <= 0.0f) {
		    Error.setLastError(Error.INVALID_ANIMATION_DURATION, "", -1, "");		    
		    return null;
		  }

		  // set the duration in the core animation instance
		  coreAnimation.setDuration(duration);

		  // read the number of tracks
		  int trackCount = inputBuffer.getInt();
		  if(trackCount <= 0) {
		    Error.setLastError(Error.INVALID_FILE_FORMAT, "", -1, "");
		    return null;
		  }

		  // load all core bones		  
		  for(int trackId = 0; trackId < trackCount; ++trackId)
		  {
		    // load the core track
		    CoreTrack coreTrack = loadCoreTrack(inputBuffer, skel);
		    if (coreTrack == null) {		      
		      return null;
		    }

		    // add the core track to the core animation instance
		    coreAnimation.addCoreTrack(coreTrack);
		  }

		  return coreAnimation;
	}
	
	private static CoreAnimation loadXmlCoreAnimation(String strFilename, CoreSkeleton skel) {
		return null;
	}
	
	public static CoreTrack loadCoreTrack(ByteBuffer inputBuffer, CoreSkeleton skel) {		
		  // read the bone id
		  int coreBoneId = inputBuffer.getInt();
		  if(coreBoneId < 0) {
		    Error.setLastError(Error.INVALID_FILE_FORMAT, "", -1, "");
		    return null;
		  }

		  // allocate a new core track instance
		  CoreTrack coreTrack = new CoreTrack();
		  // link the core track to the appropriate core bone instance
		  coreTrack.setCoreBoneId(coreBoneId);

		  // read the number of keyframes
		  int keyframeCount = inputBuffer.getInt();
		  if(keyframeCount <= 0) {
		    Error.setLastError(Error.INVALID_FILE_FORMAT, "", -1, "");
		    return null;
		  }

		  // load all core keyframes		  
		  for(int keyframeId = 0; keyframeId < keyframeCount; ++keyframeId)
		  {
		    // load the core keyframe
		    CoreKeyframe coreKeyframe = loadCoreKeyframe(inputBuffer);
		    if(coreKeyframe == null)
		    {		      
		      return null;
		    }
		    if ( (loadingMode & LOADER_ROTATE_X_AXIS) == LOADER_ROTATE_X_AXIS) {
		      // Check for anim rotation
		      if ( (skel != null) && (skel.getCoreBone(coreBoneId).getParentId() == -1) )  // root bone
		      {
		        // rotate root bone quaternion
		        Quaternion rot = coreKeyframe.getRotation();
		        Quaternion x_axis_90 = new Quaternion(0.7071067811f,0.0f,0.0f,0.7071067811f);
		        rot = rot.multiply(x_axis_90);
		        coreKeyframe.setRotation(rot);
		        // rotate root bone displacement
		        Vector3D vec = coreKeyframe.getTranslation();
		        vec = vec.multiply(x_axis_90);
		        coreKeyframe.setTranslation(vec);
		      }
		    }    

		    // add the core keyframe to the core track instance
		    coreTrack.addCoreKeyframe(coreKeyframe);
		  }

		  return coreTrack;
	}
	
	public static CoreKeyframe loadCoreKeyframe(ByteBuffer inputBuffer) {
		 // get the time of the keyframe
		  float time = inputBuffer.getFloat();
		  
		  // get the translation of the bone
		  float tx = inputBuffer.getFloat();
		  float ty = inputBuffer.getFloat();
		  float tz = inputBuffer.getFloat();
		  
		  // get the rotation of the bone
		  float rx = inputBuffer.getFloat();
		  float ry = inputBuffer.getFloat();
		  float rz = inputBuffer.getFloat();
		  float rw = inputBuffer.getFloat();
		  		  
		  // allocate a new core keyframe instance
		  CoreKeyframe coreKeyframe = new CoreKeyframe();
		  // set all attributes of the keyframe
		  coreKeyframe.setTime(time);
		  coreKeyframe.setTranslation(new Vector3D(tx, ty, tz));
		  coreKeyframe.setRotation(new Quaternion(rx, ry, rz, rw));

		  return coreKeyframe;
	}

	//################# Mesh ###################
	public static CoreMesh loadCoreMesh(String fileName) {
		if(fileName.endsWith(MESH_XML_FILE_EXTENSION)) 
			//XML file
		    return loadXmlCoreMesh(fileName);

		else {
			try {
//				File file = new File(fileName);
//				byte[] array = new byte[(int)file.length()];
//		        FileInputStream fileInputStream = new FileInputStream(file);		         
//		        fileInputStream.read(array);
                ByteArrayOutputStream byteStream = FileLoader.getByteArrayOutputStream(fileName);
		        ByteBuffer byteBuffer = ByteBuffer.wrap(byteStream.toByteArray());
		        byteBuffer.order(ByteOrder.LITTLE_ENDIAN); 
		        
		        CoreMesh coreMesh = loadCoreMesh(byteBuffer);
		        if (coreMesh != null) coreMesh.setFileName(fileName);
		    
		        // Close the file
		        byteStream.close();
		        return coreMesh;
		    } 
			catch (IOException ioe) {
				ioe.printStackTrace();
		    }
		}
		return null;		
	}
	
	public static CoreMesh loadCoreMesh(ByteBuffer inputBuffer) {
		//check if this is a valid file
		byte magic[] = new byte[4];
		inputBuffer.get(magic);
		String magicToken = new String(magic);
		  
		if(!magicToken.equals(MESH_FILE_MAGIC)) {
			Error.setLastError(Error.INVALID_FILE_FORMAT, "", -1, "");
			return null;
		}
		
		// check if the version is compatible with the library
		int version = inputBuffer.getInt();
		if (version != FILE_VERSION && version != NEW_FILE_VERSION) {
		    Error.setLastError(Error.INCOMPATIBLE_FILE_VERSION, "", -1, "");
		    return null;
		  }

		  // get the number of submeshes
		  int submeshCount = inputBuffer.getInt();		  
		  // allocate a new core mesh instance
		  CoreMesh coreMesh = new CoreMesh();
		  // load all core submeshes
		  for(int submeshId = 0; submeshId < submeshCount; ++submeshId) {
		    // load the core submesh
		    CoreSubmesh coreSubmesh = loadCoreSubmesh(inputBuffer);
		    if(coreSubmesh == null) {		      
		      return null;
		    }

		    // add the core submesh to the core mesh instance
		    coreMesh.addCoreSubmesh(coreSubmesh);
		  }

		  return coreMesh;
	}

	private static CoreMesh loadXmlCoreMesh(String strFilename) {
		return null;
	}
		
	private static CoreSubmesh loadCoreSubmesh(ByteBuffer inputBuffer) {
		// get the material thread id of the submesh
		int coreMaterialThreadId = inputBuffer.getInt();
		
		// get the number of vertices, faces, level-of-details and springs
		int vertexCount = inputBuffer.getInt();
		
		int faceCount = inputBuffer.getInt();		
		int lodCount = inputBuffer.getInt();
		int springCount = inputBuffer.getInt();		

		// get the number of texture coordinates per vertex
		int textureCoordinateCount = inputBuffer.getInt();
				  
		  // allocate a new core submesh instance
		  CoreSubmesh coreSubmesh = new CoreSubmesh();
		  // set the LOD step count
		  coreSubmesh.setLodCount(lodCount);

		  // set the core material id
		  coreSubmesh.setCoreMaterialThreadId(coreMaterialThreadId);

		  // reserve memory for all the submesh data
		  if(!coreSubmesh.reserve(vertexCount, textureCoordinateCount, faceCount, springCount))
		  {
		    Error.setLastError(Error.MEMORY_ALLOCATION_FAILED, "", -1, "");		    
		    return null;
		  }

		  // load the tangent space enable flags.		  
		  for (int textureCoordinateId = 0; textureCoordinateId < textureCoordinateCount; textureCoordinateId++)
		  {
		    coreSubmesh.enableTangents(textureCoordinateId, false);
		  }

		  // load all vertices and their influences		  
		  for(int vertexId = 0; vertexId < vertexCount; ++vertexId)
		  {
		    CoreSubmesh.Vertex vertex = coreSubmesh.new Vertex();

		    // load data of the vertex
		    vertex.position.x = inputBuffer.getFloat();
		    vertex.position.y = inputBuffer.getFloat();
		    vertex.position.z = inputBuffer.getFloat();
		    
		    vertex.normal.x = inputBuffer.getFloat();
		    vertex.normal.y = inputBuffer.getFloat();
		    vertex.normal.z = inputBuffer.getFloat();
		    		    
		    vertex.collapseId = inputBuffer.getInt(); 
		    vertex.faceCollapseCount = inputBuffer.getInt();
		    
		    // load all texture coordinates of the vertex		    
		    for(int textureCoordinateId = 0; textureCoordinateId < textureCoordinateCount; ++textureCoordinateId)
		    {
		      CoreSubmesh.TextureCoordinate textureCoordinate = coreSubmesh.new TextureCoordinate();

		      // load data of the influence
		      textureCoordinate.u = inputBuffer.getFloat();
		      textureCoordinate.v = inputBuffer.getFloat();
		      
		      if ( (loadingMode & LOADER_INVERT_V_COORD) == LOADER_INVERT_V_COORD) {
		          textureCoordinate.v = 1.0f - textureCoordinate.v;
		      }

		      // set texture coordinate in the core submesh instance
		      coreSubmesh.setTextureCoordinate(vertexId, textureCoordinateId, textureCoordinate);
		    }

		    // get the number of influences
		    int influenceCount = inputBuffer.getInt();
		    if(influenceCount < 0) {		      
		      return null;
		    }

		    // reserve memory for the influences in the vertex
		    vertex.vectorInfluence.setSize(influenceCount);
		    
		    // load all influences of the vertex		    
		    for(int influenceId = 0; influenceId < influenceCount; ++influenceId)
		    {
		      // load data of the influence
		    	CoreSubmesh.Influence influence = coreSubmesh.new Influence();
		    	influence.boneId = inputBuffer.getInt();
		    	influence.weight = inputBuffer.getFloat();		    	
		    	vertex.vectorInfluence.setElementAt(influence, influenceId);		    	
		    }

		    // set vertex in the core submesh instance
		    coreSubmesh.setVertex(vertexId, vertex);

		    // load the physical property of the vertex if there are springs in the core submesh
		    if(springCount > 0)
		    {
		      CoreSubmesh.PhysicalProperty physicalProperty = coreSubmesh.new PhysicalProperty();

		      // load data of the physical property
		      physicalProperty.weight = inputBuffer.getFloat();
		      		      
		      // set the physical property in the core submesh instance
		      coreSubmesh.setPhysicalProperty(vertexId, physicalProperty);
		    }
		  }//end vertices

		  // load all springs		  
		  for(int springId = 0; springId < springCount; ++springId)
		  {
		    CoreSubmesh.Spring spring = coreSubmesh.new Spring();

		    // load data of the spring
		    spring.vertexId[0] = inputBuffer.getInt();
		    spring.vertexId[1] = inputBuffer.getInt();
		    spring.springCoefficient = inputBuffer.getFloat();
		    spring.idleLength = inputBuffer.getFloat();
		    		    
		    // set spring in the core submesh instance
		    coreSubmesh.setSpring(springId, spring);
		  }


		  // load all faces		  
		  int justOnce = 0;
		  boolean flipModel = false;
		  for(int faceId = 0; faceId < faceCount; ++faceId)
		  {
		    CoreSubmesh.Face face = coreSubmesh.new Face();

		    // load data of the face		  		  
		    face.vertexId[0] = inputBuffer.getInt();
		    face.vertexId[1] = inputBuffer.getInt();
		    face.vertexId[2] = inputBuffer.getInt();
		  		  
		    // check if left-handed coord system is used by the object
		    // can be done only once since the object has one system for all faces
		    if (justOnce == 0)
		    {
		      // get vertexes of first face
		      Vector<CoreSubmesh.Vertex> vertices = coreSubmesh.getVertices();
		      CoreSubmesh.Vertex v1 = vertices.get(face.vertexId[0]);
		      CoreSubmesh.Vertex v2 = vertices.get(face.vertexId[1]);
		      CoreSubmesh.Vertex v3 = vertices.get(face.vertexId[2]);

		      Point3D point1 = v1.position;
		      Point3D point2 = v2.position;
		      Point3D point3 = v3.position;

		      // gets vectors (v1-v2) and (v3-v2)
		      Vector3D vect1 = new Vector3D(point1.subtract(point2));
		      Vector3D vect2 = new Vector3D(point3.subtract(point2));

		      // calculates normal of face
		      Vector3D cross = vect1.cross(vect2);
		      cross.normalize();
		      Vector3D faceNormal = cross;

		      // compare the calculated normal with the normal of a vertex
		      Vector3D maxNorm = v1.normal;

		      // if the two vectors point to the same direction then the poly needs flipping
		      // so if the dot product > 0 it needs flipping
		      if (faceNormal.dot(maxNorm) > 0)
		    	  flipModel = true;

		      justOnce = 1;
		    }

		    // flip if needed
		    if (flipModel) {		    	
		      int tmp = face.vertexId[1];
		      face.vertexId[1] = face.vertexId[2];
		      face.vertexId[2] = tmp;
		    }

		    // set face in the core submesh instance
		    coreSubmesh.setFace(faceId, face);
		  }

		  return coreSubmesh;
	}
	
	//#########  Material #############
/*	public static CoreMaterial loadCoreMaterial(String fileName) {
		if(fileName.endsWith(MATERIAL_XML_FILE_EXTENSION)) 
			//XML file
		    return loadXmlCoreMaterial(fileName);

		else {
			try {
//				File file = new File(fileName);
//				byte[] array = new byte[(int)file.length()];
//		        FileInputStream fileInputStream = new FileInputStream(file);		         
//		        fileInputStream.read(array);
                ByteArrayOutputStream byteStream = FileLoader.getByteArrayOutputStream(fileName);

		        ByteBuffer byteBuffer = ByteBuffer.wrap(byteStream.toByteArray());
		        byteBuffer.order(ByteOrder.LITTLE_ENDIAN); 
		        
		        CoreMaterial coreMaterial = loadCoreMaterial(byteBuffer);
		        if (coreMaterial != null) coreMaterial.setFileName(fileName);
		    
		        // Close the file
		        byteStream.close();
		        return coreMaterial;
		    } 
			catch (IOException ioe) {
				ioe.printStackTrace();
		    }
		}
		return null;
	}
	
	public static CoreMaterial loadCoreMaterial(ByteBuffer inputBuffer) {
		//check if this is a valid file
		  byte magic[] = new byte[4];
		  inputBuffer.get(magic);
		  String magicToken = new String(magic);
		  if(!magicToken.equals(MATERIAL_FILE_MAGIC)) {
		    Error.setLastError(Error.INVALID_FILE_FORMAT, "", -1, "");
		    return null;
		  }

		  // check if the version is compatible with the library
		  int version = inputBuffer.getInt();
		  if(version != FILE_VERSION && version != NEW_FILE_VERSION) {
		    Error.setLastError(Error.INCOMPATIBLE_FILE_VERSION, "", -1, "");
		    return null;
		  }

		  // allocate a new core material instance
		  CoreMaterial coreMaterial = new CoreMaterial();
		  if(coreMaterial == null) {
		    Error.setLastError(Error.MEMORY_ALLOCATION_FAILED, "", -1, "");
		    return null;
		  }

		  // get the ambient color of the core material
		  RGBA ambientColor = new RGBA(inputBuffer.get(), inputBuffer.get(), inputBuffer.get(), inputBuffer.get());
		  
		  // get the diffuse color of the core material
		  RGBA diffuseColor = new RGBA(inputBuffer.get(), inputBuffer.get(), inputBuffer.get(), inputBuffer.get());
		  
		  // get the specular color of the core material
		  RGBA specularColor = new RGBA(inputBuffer.get(), inputBuffer.get(), inputBuffer.get(), inputBuffer.get());

		  // get the shininess factor of the core material
		  float shininess = inputBuffer.getFloat();

		  // set the colors and the shininess
		  coreMaterial.setAmbientColor(ambientColor);
		  coreMaterial.setDiffuseColor(diffuseColor);
		  coreMaterial.setSpecularColor(specularColor);
		  coreMaterial.setShininess(shininess);

		  // read the number of maps
		  int mapCount = inputBuffer.getInt();
		  if(mapCount < 0)
		  {
		    Error.setLastError(Error.INVALID_FILE_FORMAT, "", -1, "");
		    return null;
		  }

		  // reserve memory for all the material data
		  if(!coreMaterial.reserve(mapCount))
		  {
		    Error.setLastError(Error.MEMORY_ALLOCATION_FAILED, "", -1, "");		    
		    return null;
		  }

		  // load all maps		  
		  for(int mapId = 0; mapId < mapCount; ++mapId)
		  {
		    CoreMaterial.Map map = coreMaterial.new Map();

		    // read the filename of the map
		    int textureNameLength = inputBuffer.getInt();
		    byte textureNameBuffer[] = new byte[textureNameLength];
		    inputBuffer.get(textureNameBuffer);
		    String textureName = new String(textureNameBuffer);
		    map.filename = textureName;

		    // set map in the core material instance
		    coreMaterial.setMap(mapId, map);
		  }

		  return coreMaterial;
	}
	
	private static CoreMaterial loadXmlCoreMaterial(String fileName) {	
		final String XML_HEADER = "HEADER";
	    final String XML_MAGIC = "MAGIC";
	    final String XML_VERSION = "VERSION";
	    
	    final String XRF_MATERIAL = "MATERIAL";    
	    final String XRF_NUMMAPS = "NUMMAPS";
	    final String XRF_AMBIENT = "AMBIENT";
	    final String XRF_DIFFUSE = "DIFFUSE";
	    final String XRF_SPECULAR = "SPECULAR";
	    final String XRF_SHININESS = "SHININESS";
	    final String XRF_MAP = "MAP";
	    
		//Note: This XML file has two root nodes.
		//Hence, we add a parent node as a root node
		//and write this modified XML in a temporary file
		//so that we can parse it using SAXParser
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse( FileLoader.getInputStream(fileName) );
	        
	        //<ROOT>
	        //	<HEADER MAGIC="XRF" VERSION="900" />
	        //	<MATERIAL NUMMAPS="0">
	        //		<AMBIENT>22 16 46 0</AMBIENT>
	        //		<DIFFUSE>49 49 49 0</DIFFUSE>
	        //		<SPECULAR>229 229 229 0</SPECULAR>
	        //		<SHININESS>0.8</SHININESS>
	        //	</MATERIAL>
	        //</ROOT>

	        Element root = document.getDocumentElement();
	        if(root.getTagName().equals("ROOT")) {
	        	NodeList childNodes = root.getChildNodes();

	        	int childIndex = 0;
		        //Make sure that the first child element of root is HEADER
	        	Node node = childNodes.item(childIndex);	        	
	        	while (!node.getNodeName().equals(XML_HEADER) && childNodes.getLength() > childIndex) {	        		
	        		node = childNodes.item(++childIndex);
	        	}
		        
	        	//Check if the value of MAGIC attribute is same as XML_MATERIAL_FILE_MAGIC
	        	NamedNodeMap attributes = node.getAttributes();
	        		
	        	Node attribute = attributes.item(0);
	        	if (! (attribute.getNodeName().equals(XML_MAGIC) && attribute.getNodeValue().equals(XML_MATERIAL_FILE_MAGIC)))
	        			return null;
		        //Check if value of VERSION attribute is same as XML_FILE_VERSION	        		
	        	attribute = attributes.item(1);
	        	if (! (attribute.getNodeName().equals(XML_VERSION) && (attribute.getNodeValue().equals(XML_FILE_VERSION) || attribute.getNodeValue().equals(NEW_XML_FILE_VERSION))))
	        	{
	        		System.out.println("File version does not match");
	        		return null;
	        	}
	        	
	        	//Make sure the next child element of root is MATERIAL
	        	while (!node.getNodeName().equals(XRF_MATERIAL)) {	        		
	        		node = childNodes.item(++childIndex);
	        	}
	        	
	        	//node = childNodes.item(++childIndex);
	        	if (node.getNodeName().equals(XRF_MATERIAL)) {
	        		float factor = 255.0f;
		        	CoreMaterial coreMaterial = new CoreMaterial();
		        	
		        	//Make sure the child elements of MATERIAL are
	        		childNodes = node.getChildNodes();
	        		
	        		//AMBIENT
	        		childIndex = 0;
		        	node = childNodes.item(childIndex);
		        	while (!node.getNodeName().equals(XRF_AMBIENT)) {		        		
		        		node = childNodes.item(++childIndex);
		        	} 
		        	//Read and store the values
		        	String color = node.getTextContent();
		        	StringTokenizer colorTokens = new StringTokenizer(color);
		        	float r = Float.parseFloat(colorTokens.nextToken());
		        	float g = Float.parseFloat(colorTokens.nextToken());
		        	float b = Float.parseFloat(colorTokens.nextToken());
		        	float a = Float.parseFloat(colorTokens.nextToken());
		        	coreMaterial.setAmbientColor(new RGBA(r/factor, g/factor, b/factor, a/factor));
		        	
		        	//DIFFUSE 
		        	node = childNodes.item(++childIndex);
		        	while (!node.getNodeName().equals(XRF_DIFFUSE)) {		        		
		        		node = childNodes.item(++childIndex);
		        	}		  
		        	//Read and store the values
		        	color = node.getTextContent();
		        	colorTokens = new StringTokenizer(color);
		        	r = Float.parseFloat(colorTokens.nextToken());
		        	g = Float.parseFloat(colorTokens.nextToken());
		        	b = Float.parseFloat(colorTokens.nextToken());
		        	a = Float.parseFloat(colorTokens.nextToken());		        		        			        	
		        	coreMaterial.setDiffuseColor(new RGBA(r/factor, g/factor, b/factor, a/factor));

		        	//SPECULAR
		        	node = childNodes.item(++childIndex);
		        	while (!node.getNodeName().equals(XRF_SPECULAR)) {		        		
		        		node = childNodes.item(++childIndex);
		        	}
		        	//Read and store the values		        	
		        	color = node.getTextContent();
		        	colorTokens = new StringTokenizer(color);
		        	r = Float.parseFloat(colorTokens.nextToken());
		        	g = Float.parseFloat(colorTokens.nextToken());
		        	b = Float.parseFloat(colorTokens.nextToken());
		        	a = Float.parseFloat(colorTokens.nextToken());		        		        			        	
		        	coreMaterial.setSpecularColor(new RGBA(r/factor, g/factor, b/factor, a/factor));

		        	//SHININESS 
		        	node = childNodes.item(++childIndex);
		        	while (!node.getNodeName().equals(XRF_SHININESS)) {		        		
		        		node = childNodes.item(++childIndex);
		        	}
		        	//Read and store the values		        	
		        	color = node.getTextContent();
		        	coreMaterial.setShininess(Float.parseFloat(color));
		        	
		        	//TODO: Add code to read and store map (texture) files
		        	return coreMaterial;
	        	}	        	

	        }
        } 
        catch (Throwable t) {
            t.printStackTrace();
            throw new RuntimeException(t);
        }
        return null;
	}
*/		 

//		  std::vector<std::string> MatFileName;
//
//		  TiXmlElement* map;
//
//		  for( map = shininess->NextSiblingElement();map;map = map->NextSiblingElement() )
//		  {
//		    if(!map||stricmp(map->Value(),"MAP")!=0)
//		    {
//		      CalError::setLastError(CalError::INVALID_FILE_FORMAT, __FILE__, __LINE__, strFilename);
//		      delete pCoreMaterial;    
//		      return false;
//		    }
//		    
//
//		    node= map->FirstChild();
//		    if(!node)
//		    {
//		      CalError::setLastError(CalError::INVALID_FILE_FORMAT, __FILE__, __LINE__, strFilename);
//		      delete pCoreMaterial;    
//		      return false;
//		    }
//
//		    TiXmlText* mapfile = node->ToText();
//		    if(!mapfile)
//		    {
//		      CalError::setLastError(CalError::INVALID_FILE_FORMAT, __FILE__, __LINE__, strFilename);
//		      delete pCoreMaterial;    
//		      return false;
//		    }
//
//		    MatFileName.push_back(mapfile->Value());
//		  }
//
//		  pCoreMaterial->reserve(MatFileName.size());
//
//
//
//		  for (unsigned int mapId=0; mapId < MatFileName.size(); ++mapId)
//		  {
//		    CalCoreMaterial::Map Map;
//		    // initialize the user data
//		    Map.userData = 0;
//
//		    Map.strFilename= MatFileName[mapId];    
//
//		    // set map in the core material instance
//		    pCoreMaterial->setMap(mapId, Map);
//		  }
//
//		  doc.Clear();
//        return coreMaterial;		  
}

