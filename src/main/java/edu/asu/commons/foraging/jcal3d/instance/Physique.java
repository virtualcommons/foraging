package edu.asu.commons.foraging.jcal3d.instance;

import java.util.Vector;

import edu.asu.commons.foraging.graphics.Point3D;
import edu.asu.commons.foraging.graphics.Vector3D;
import edu.asu.commons.foraging.jcal3d.core.CoreSubMorphTarget;
import edu.asu.commons.foraging.jcal3d.core.CoreSubmesh;
import edu.asu.commons.foraging.jcal3d.misc.Error;


public class Physique {

	protected Model model = null;
	protected boolean normalize = true;
	
	/*****************************************************************************/
	/** Constructs the physique instance.
	  *
	  * This function is the default constructor of the physique instance.
	  *****************************************************************************/
	public Physique(Model model) {
		//FIXME: Make sure model is not null	  
		this.model = model;
	}

	 /*****************************************************************************/
	/** Calculates the transformed vertex data.
	  *
	  * This function calculates and returns the transformed vertex data of a
	  * specific submesh.
	  *
	  * @param pSubmesh A pointer to the submesh from which the vertex data should
	  *                 be calculated and returned.
	  * @param pVertexBuffer A pointer to the user-provided buffer where the vertex
	  *                      data is written to.
	  *
	  * @return The number of vertices written to the buffer.
	  *****************************************************************************/

	public Vector<Point3D> calculateVertices(Submesh submesh) {
		Vector<Point3D> vertices = new Vector<Point3D>();
		
	  // get bone vector of the skeleton
	  Vector<Bone> bones = model.getSkeleton().getBones();

	  // get vertex vector of the core submesh
	  Vector<CoreSubmesh.Vertex> coreMeshVertices = submesh.getCoreSubmesh().getVertices();

	  // get physical property vector of the core submesh
	  Vector<CoreSubmesh.PhysicalProperty> physicalProperties = submesh.getCoreSubmesh().getPhysicalProperties();

	  // get the number of vertices
	  int vertexCount = submesh.getVertexCount();
	  
	  // get the sub morph target vector from the core sub mesh
	  Vector<CoreSubMorphTarget> subMorphTargets = submesh.getCoreSubmesh().getCoreSubMorphTargets();

	  // calculate the base weight
	  float baseWeight = submesh.getBaseWeight();

	  // get the number of morph targets
	  int morphTargetCount = submesh.getMorphTargetWeightCount();

	  // calculate all submesh vertices	  
	  for(int vertexId = 0; vertexId < vertexCount; ++vertexId)
	  {
	    // get the vertex
	    CoreSubmesh.Vertex vertex = coreMeshVertices.get(vertexId);
	    
	    // blend the morph targets
	    Point3D position = new Point3D();
	    if(baseWeight == 1.0f) {
	       position.x = vertex.position.x;
	       position.y = vertex.position.y;
	       position.z = vertex.position.z;
	    }
	    else {
	      position.x = baseWeight*vertex.position.x;
	      position.y = baseWeight*vertex.position.y;
	      position.z = baseWeight*vertex.position.z;	      
	      for(int morphTargetId=0; morphTargetId < morphTargetCount;++morphTargetId)
	      {
	    	  CoreSubMorphTarget.BlendVertex blendVertex = subMorphTargets.get(morphTargetId).getBlendVertices().get(vertexId);
	    	  float currentWeight = submesh.getMorphTargetWeight(morphTargetId);
	    	  position.x += currentWeight*blendVertex.position.x;
	    	  position.y += currentWeight*blendVertex.position.y;
	    	  position.z += currentWeight*blendVertex.position.z;
	      }
	    }

	    // initialize vertex
	    float x, y, z;
	    x = 0.0f;
	    y = 0.0f;
	    z = 0.0f;

	    // blend together all vertex influences
	    int influenceCount = vertex.vectorInfluence.size();
	    if(influenceCount == 0) {
	      x = position.x;
	      y = position.y;
	      z = position.z;
	    } 
		else {
			for(int influenceId = 0; influenceId < influenceCount; ++influenceId) {
				// get the influence
				CoreSubmesh.Influence influence = vertex.vectorInfluence.get(influenceId);
				
				// get the bone of the influence vertex
				Bone bone = bones.get(influence.boneId);
				
				// transform vertex with current state of the bone
				Vector3D v = new Vector3D(position);
				v = v.multiply(bone.getTransformMatrix());
				v = v.add(bone.getTranslationBoneSpace());
				
				x += influence.weight * v.x;
				y += influence.weight * v.y;
				z += influence.weight * v.z;					
			}
		}

	    Point3D point = new Point3D();
	    
	    // save vertex position
	    if(submesh.getCoreSubmesh().getSpringCount() > 0 && submesh.hasInternalData()) {
	      // get the pgysical property of the vertex
	      CoreSubmesh.PhysicalProperty physicalProperty = physicalProperties.get(vertexId);
	      
	      // assign new vertex position if there is no vertex weight
	      if(physicalProperty.weight == 0.0f) {
	    	point.x = x;
	        point.y = y;
	        point.z = z;
	      }
	    }
	    else {
	    	point.x = x;
	    	point.y = y;
	    	point.z = z;
	    }
	    
	    // next vertex position in buffer
	    vertices.add(point);
	  }

	  return vertices;
	}

	 /*****************************************************************************/
	/** Calculates one transformed vertex.
	  *	  * This function calculates and returns a transformed vertex of a
	  * specific submesh.
	  *
	  * @param pSubmesh A pointer to the submesh from which the vertex should
	  *                 be calculated and returned.
	  * @param vertexId The id of the vertex that should be transformed.
	  *
	  * @return The number of vertices written to the buffer.
	  *****************************************************************************/

	public Point3D calculateVertex(Submesh submesh, int vertexId) {
	  // get bone vector of the skeleton
	  Vector<Bone> bones = model.getSkeleton().getBones();

	  // get vertex of the core submesh
	  Vector<CoreSubmesh.Vertex> coreSubmeshVertices = submesh.getCoreSubmesh().getVertices();

	  // get physical property vector of the core submesh
	  //std::vector<CalCoreSubmesh::PhysicalProperty>& vectorPhysicalProperty = pSubmesh->getCoreSubmesh()->getVectorPhysicalProperty();

	  // get the sub morph target vector from the core sub mesh
	  Vector<CoreSubMorphTarget> subMorphTargets = submesh.getCoreSubmesh().getCoreSubMorphTargets();

	  // calculate the base weight
	  float baseWeight = submesh.getBaseWeight();

	  // get the number of morph targets
	  int morphTargetCount = submesh.getMorphTargetWeightCount();

	  // get the vertex
	  CoreSubmesh.Vertex vertex = coreSubmeshVertices.get(vertexId);

	  // blend the morph targets
	  Point3D position = new Point3D();
	  if(baseWeight == 1.0f) {
	    position.x = vertex.position.x;
	    position.y = vertex.position.y;
	    position.z = vertex.position.z;
	  }
	  else {
	    position.x = baseWeight*vertex.position.x;
	    position.y = baseWeight*vertex.position.y;
	    position.z = baseWeight*vertex.position.z;
	    
	    for(int morphTargetId = 0; morphTargetId < morphTargetCount; ++morphTargetId) {
	      CoreSubMorphTarget.BlendVertex blendVertex = subMorphTargets.get(morphTargetId).getBlendVertices().get(vertexId);
	      float currentWeight = submesh.getMorphTargetWeight(morphTargetId);
	      position.x += currentWeight*blendVertex.position.x;
	      position.y += currentWeight*blendVertex.position.y;
	      position.z += currentWeight*blendVertex.position.z;
	    }
	  }

	  // initialize vertex
	  float x, y, z;
	  x = 0.0f;
	  y = 0.0f;
	  z = 0.0f;

	  // blend together all vertex influences
	  int influenceId;
	  int influenceCount=(int)vertex.vectorInfluence.size();
	  if(influenceCount == 0) 
	  {
	    x = position.x;
	    y = position.y;
	    z = position.z;
	  } 
	  else 
	  {
		  for(influenceId = 0; influenceId < influenceCount; ++influenceId)
		  {
			  // get the influence
			  CoreSubmesh.Influence influence = vertex.vectorInfluence.get(influenceId);
			  
			  // get the bone of the influence vertex
			  Bone bone = bones.get(influence.boneId);
			  
			  // transform vertex with current state of the bone
			  Vector3D v = new Vector3D(position);
			  v = v.multiply(bone.getTransformMatrix());
			  v = v.add(bone.getTranslationBoneSpace());
			  
			  x += influence.weight * v.x;
			  y += influence.weight * v.y;
			  z += influence.weight * v.z;
		  }
	  }
	  /* Commented code
	  // save vertex position
	  if(pSubmesh->getCoreSubmesh()->getSpringCount() > 0 && pSubmesh->hasInternalData())
	  {
	    // get the pgysical property of the vertex
	    CalCoreSubmesh::PhysicalProperty& physicalProperty = vectorPhysicalProperty[vertexId];

	    // assign new vertex position if there is no vertex weight
	    if(physicalProperty.weight == 0.0f)
	    {
	      pVertexBuffer[0] = x;
	      pVertexBuffer[1] = y;
	      pVertexBuffer[2] = z;
	    }
	  }
	  else
	  {
	    pVertexBuffer[0] = x;
	    pVertexBuffer[1] = y;
	    pVertexBuffer[2] = z;
	  }
	  */
	  // return the vertex
	  return new Point3D(x,y,z);
	}
	 /*****************************************************************************/
	/** Calculates the transformed tangent space data.
	  *
	  * This function calculates and returns the transformed tangent space data of a
	  * specific submesh.
	  *
	  * @param pSubmesh A pointer to the submesh from which the tangent space data 
	  *                 should be calculated and returned.
	  * @param mapId
	  * @param pTangentSpaceBuffer A pointer to the user-provided buffer where the tangent 
	  *                 space data is written to.
	  *
	  * @return The number of tangent spaces written to the buffer.
	  *****************************************************************************/
	public Vector<Submesh.TangentSpace> calculateTangentSpaces(Submesh submesh, int mapId) {
		Vector<Submesh.TangentSpace> tangentSpaces = new Vector<Submesh.TangentSpace>();
		
	  if((mapId < 0) || (mapId >= submesh.getCoreSubmesh().getVectorTangentSpaces().size())) return null;
	  
	  // get bone vector of the skeleton
	  Vector<Bone> bones = model.getSkeleton().getBones();

	  // get vertex vector of the submesh
	  Vector<CoreSubmesh.Vertex> coreSubmeshVertices = submesh.getCoreSubmesh().getVertices();

	  // get tangent space vector of the submesh
	  Vector<CoreSubmesh.TangentSpace> coreSubmeshTangentSpaces = submesh.getCoreSubmesh().getVectorTangentSpaces().get(mapId);
	  
	  // get the number of vertices
	  int vertexCount = submesh.getVertexCount();

	  // calculate normal for all submesh vertices	  
	  for(int vertexId = 0; vertexId < vertexCount; vertexId++) {
	    CoreSubmesh.TangentSpace tangentSpace = coreSubmeshTangentSpaces.get(vertexId);

	    // get the vertex
	    CoreSubmesh.Vertex vertex = coreSubmeshVertices.get(vertexId);

	    // initialize tangent
	    float tx, ty, tz;
	    tx = 0.0f;
	    ty = 0.0f;
	    tz = 0.0f;

	    // blend together all vertex influences	    
	    int influenceCount=(int)vertex.vectorInfluence.size();

	    for(int influenceId = 0; influenceId < influenceCount; influenceId++) {
	      // get the influence
	      CoreSubmesh.Influence influence = vertex.vectorInfluence.get(influenceId);

	      // get the bone of the influence vertex
	      Bone bone = bones.get(influence.boneId);

	      // transform normal with current state of the bone
	      Vector3D v = new Vector3D(tangentSpace.tangent);
	      v = v.multiply(bone.getTransformMatrix()); 

	      tx += influence.weight * v.x;
	      ty += influence.weight * v.y;
	      tz += influence.weight * v.z;
	    }

	    Submesh.TangentSpace submeshTangentSpace = submesh.new TangentSpace();
	    
	    // re-normalize tangent if necessary
	    if (normalize) {
	      float scale;
	      scale = (float)( 1.0f / Math.sqrt(tx * tx + ty * ty + tz * tz));

	      submeshTangentSpace.tangent.x = tx * scale;
	      submeshTangentSpace.tangent.y = ty * scale;
	      submeshTangentSpace.tangent.z = tz * scale;	  
	    }
	    else
	    {
	    	submeshTangentSpace.tangent.x = tx;
	    	submeshTangentSpace.tangent.y = ty;
	    	submeshTangentSpace.tangent.z = tz;
	    }

	    submeshTangentSpace.crossFactor = tangentSpace.crossFactor;
	    // next vertex position in buffer
	    tangentSpaces.add(submeshTangentSpace);
	  }

	  return tangentSpaces;
	}


	 /*****************************************************************************/
	/** Calculates the transformed normal data.
	  *
	  * This function calculates and returns the transformed normal data of a
	  * specific submesh.
	  *
	  * @param pSubmesh A pointer to the submesh from which the normal data should
	  *                 be calculated and returned.
	  * @param pNormalBuffer A pointer to the user-provided buffer where the normal
	  *                      data is written to.
	  *
	  * @return The number of normals written to the buffer.
	  *****************************************************************************/

	public Vector<Vector3D> calculateNormals(Submesh submesh) {
		Vector<Vector3D> normals = new Vector<Vector3D>();	  

		// get bone vector of the skeleton
		Vector<Bone> bones = model.getSkeleton().getBones();

		// get vertex vector of the submesh
		Vector<CoreSubmesh.Vertex> coreSubmeshVertices = submesh.getCoreSubmesh().getVertices();

	  // get the number of vertices
	  int vertexCount = submesh.getVertexCount();

	  // get the sub morph target vector from the core sub mesh
	  Vector<CoreSubMorphTarget> subMorphTargets = submesh.getCoreSubmesh().getCoreSubMorphTargets();

	  // calculate the base weight
	  float baseWeight = submesh.getBaseWeight();

	  // get the number of morph targets
	  int morphTargetCount = submesh.getMorphTargetWeightCount();

	  // calculate normal for all submesh vertices	  
	  for(int vertexId = 0; vertexId < vertexCount; ++vertexId)
	  {
	    // get the vertex
	    CoreSubmesh.Vertex vertex = coreSubmeshVertices.get(vertexId);

	    // blend the morph targets
	    Vector3D normal = new Vector3D();
	    if(baseWeight == 1.0f) {
	      normal.x = vertex.normal.x;
	      normal.y = vertex.normal.y;
	      normal.z = vertex.normal.z;
	    }
	    else {
	      normal.x = baseWeight*vertex.normal.x;
	      normal.y = baseWeight*vertex.normal.y;
	      normal.z = baseWeight*vertex.normal.z;
	      
	      for(int morphTargetId = 0; morphTargetId < morphTargetCount; ++morphTargetId) {
	        CoreSubMorphTarget.BlendVertex blendVertex = subMorphTargets.get(morphTargetId).getBlendVertices().get(vertexId);
	        float currentWeight = submesh.getMorphTargetWeight(morphTargetId);
	        normal.x += currentWeight*blendVertex.normal.x;
	        normal.y += currentWeight*blendVertex.normal.y;
	        normal.z += currentWeight*blendVertex.normal.z;
	      }
	    }

	    // initialize normal
	    float nx, ny, nz;
	    nx = 0.0f;
	    ny = 0.0f;
	    nz = 0.0f;

	    // blend together all vertex influences
	    int influenceId;
		int influenceCount=(int)vertex.vectorInfluence.size();
	    if(influenceCount == 0) 
		{
	      nx = normal.x;
	      ny = normal.y;
	      nz = normal.z;
	    } 
		else 
		{
			for(influenceId = 0; influenceId < influenceCount; ++influenceId)
			{
				// get the influence
				CoreSubmesh.Influence influence = vertex.vectorInfluence.get(influenceId);
				
				// get the bone of the influence vertex
				Bone bone = bones.get(influence.boneId);
				
				// transform normal with current state of the bone
				Vector3D v = new Vector3D(normal);
				v = v.multiply(bone.getTransformMatrix()); 
				
				nx += influence.weight * v.x;
				ny += influence.weight * v.y;
				nz += influence.weight * v.z;
			}
		}

	    Vector3D submeshNormal = new Vector3D();
	    // re-normalize normal if necessary
	    if (normalize) {
	      float scale;
	      scale = (float)( 1.0f / Math.sqrt(nx * nx + ny * ny + nz * nz));

	      submeshNormal.x = nx * scale;
	      submeshNormal.y = ny * scale;
	      submeshNormal.z = nz * scale;
	    }
	    else {
	    	submeshNormal.x = nx;
	    	submeshNormal.y = ny;
	    	submeshNormal.z = nz;
	    } 

	    // next vertex position in buffer
	    normals.add(submeshNormal);
	  }

	  return normals;
	}

	 /*****************************************************************************/
	/** Calculates the transformed vertex data.
	  *
	  * This function calculates and returns the transformed vertex and the transformed 
	  * normal datadata of a specific submesh.
	  *
	  * @param pSubmesh A pointer to the submesh from which the vertex data should
	  *                 be calculated and returned.
	  * @param pVertexBuffer A pointer to the user-provided buffer where the vertex
	  *                      data is written to.
	  *
	  * @return The number of vertices written to the buffer.
	  *****************************************************************************/

	public Vector<Point3D> calculateVerticesAndNormals(Submesh submesh) {
	  Vector<Point3D> vertices = new Vector<Point3D>();
		
	  // get bone vector of the skeleton
	  Vector<Bone> bones = model.getSkeleton().getBones();

	  // get vertex vector of the core submesh
	  Vector<CoreSubmesh.Vertex> coreSubmeshVertices = submesh.getCoreSubmesh().getVertices();

	  // get physical property vector of the core submesh
	  Vector<CoreSubmesh.PhysicalProperty> physicalProperties = submesh.getCoreSubmesh().getPhysicalProperties();

	  // get the number of vertices
	  int vertexCount = submesh.getVertexCount();

	  // get the sub morph target vector from the core sub mesh
	  Vector<CoreSubMorphTarget> subMorphTargets = submesh.getCoreSubmesh().getCoreSubMorphTargets();

	  // calculate the base weight
	  float baseWeight = submesh.getBaseWeight();

	  // get the number of morph targets
	  int morphTargetCount = submesh.getMorphTargetWeightCount();

	  // calculate all submesh vertices	  
	  for(int vertexId = 0; vertexId < vertexCount; ++vertexId) {
	    // get the vertex
	    CoreSubmesh.Vertex vertex = coreSubmeshVertices.get(vertexId);

	    // blend the morph targets
	    Point3D position = new Point3D();
	    Vector3D normal = new Vector3D();
	    if(baseWeight == 1.0f) {
	      position.x = vertex.position.x;
	      position.y = vertex.position.y;
	      position.z = vertex.position.z;
	      normal.x = vertex.normal.x;
	      normal.y = vertex.normal.y;
	      normal.z = vertex.normal.z;
	    }
	    else  {
	      position.x = baseWeight*vertex.position.x;
	      position.y = baseWeight*vertex.position.y;
	      position.z = baseWeight*vertex.position.z;
	      normal.x = baseWeight*vertex.normal.x;
	      normal.y = baseWeight*vertex.normal.y;
	      normal.z = baseWeight*vertex.normal.z;
	      
	      for(int morphTargetId=0; morphTargetId < morphTargetCount;++morphTargetId)
	      {
	        CoreSubMorphTarget.BlendVertex blendVertex = subMorphTargets.get(morphTargetId).getBlendVertices().get(vertexId);
	        float currentWeight = submesh.getMorphTargetWeight(morphTargetId);
	        position.x += currentWeight*blendVertex.position.x;
	        position.y += currentWeight*blendVertex.position.y;
	        position.z += currentWeight*blendVertex.position.z;
	        normal.x += currentWeight*blendVertex.normal.x;
	        normal.y += currentWeight*blendVertex.normal.y;
	        normal.z += currentWeight*blendVertex.normal.z;
	      }
	    }

	    // initialize vertex
	    float x, y, z;
	    x = 0.0f;
	    y = 0.0f;
	    z = 0.0f;

		// initialize normal
	    float nx, ny, nz;
	    nx = 0.0f;
	    ny = 0.0f;
	    nz = 0.0f;

	    // blend together all vertex influences
	    int influenceId;
		int influenceCount=(int)vertex.vectorInfluence.size();
	    if(influenceCount == 0) 
		{
	      x = position.x;
	      y = position.y;
	      z = position.z;
	      nx = normal.x;
	      ny = normal.y;
	      nz = normal.z;
	    } 
		else 
		{
			for(influenceId = 0; influenceId < influenceCount; ++influenceId)
			{
				// get the influence
				CoreSubmesh.Influence influence = vertex.vectorInfluence.get(influenceId);
				
				// get the bone of the influence vertex
				Bone bone = bones.get(influence.boneId);
				
				// transform vertex with current state of the bone
				Vector3D v = new Vector3D(position);
				v = v.multiply(bone.getTransformMatrix());
				v = v.add(bone.getTranslationBoneSpace());
				
				x += influence.weight * v.x;
				y += influence.weight * v.y;
				z += influence.weight * v.z;
				
				// transform normal with current state of the bone
				Vector3D n = new Vector3D(normal);
				n = n.multiply(bone.getTransformMatrix());
				
				nx += influence.weight * n.x;
				ny += influence.weight * n.y;
				nz += influence.weight * n.z;
			}
		}

	    Vector3D submeshVertex = new Vector3D();
	    // save vertex position
	    if(submesh.getCoreSubmesh().getSpringCount() > 0 && submesh.hasInternalData()) {
	      // get the pgysical property of the vertex
	      CoreSubmesh.PhysicalProperty physicalProperty = physicalProperties.get(vertexId);

	      // assign new vertex position if there is no vertex weight
	      if(physicalProperty.weight == 0.0f) {
	    	  submeshVertex.x = x;
	    	  submeshVertex.y = y;
	    	  submeshVertex.z = z;
	      }
	    }
	    else {
	    	submeshVertex.x = x;
	    	submeshVertex.y = y;
	    	submeshVertex.z = z;
	    }
	    
	    Vector3D submeshVertexNormal = new Vector3D();
	    // re-normalize normal if necessary
	    if (normalize) {
	      float scale;
	      scale = (float)( 1.0f / Math.sqrt(nx * nx + ny * ny + nz * nz));

	      submeshVertexNormal.x = nx * scale;
	      submeshVertexNormal.y = ny * scale;
	      submeshVertexNormal.z = nz * scale;
	    }
	    else {
	    	submeshVertexNormal.x = nx;
	    	submeshVertexNormal.y = ny;
	    	submeshVertexNormal.z = nz;
	    } 


		// next vertex position in buffer	
	    vertices.add(submeshVertex);
	    vertices.add(submeshVertexNormal);
	  }

	  return vertices;
	}

	 /*****************************************************************************/
	/** Calculates the transformed vertex data.
	  *
	  * This function calculates and returns the transformed vertex, the transformed 
	  * normal datadata and the texture coords of a specific submesh.
	  *
	  * @param pSubmesh A pointer to the submesh from which the vertex data should
	  *                 be calculated and returned.
	  * 
	  * @param pVertexBuffer A pointer to the user-provided buffer where the vertex
	  *                      data is written to.
	  *
	  * @param NumTexCoords A integer with the number of texture coords
	  *
	  * @return The number of vertices written to the buffer.
	  *****************************************************************************/
	public Vector<Point3D> calculateVerticesNormalsAndTexCoords(Submesh submesh, int NumTexCoords) {
		Vector<Point3D> vertices = new Vector<Point3D>();
		
	  // get bone vector of the skeleton
	  Vector<Bone> bones = model.getSkeleton().getBones();

	  // get vertex vector of the core submesh
	  Vector<CoreSubmesh.Vertex> coresubmeshVertices = submesh.getCoreSubmesh().getVertices();

	  // get the texture coordinate vector vector
	  Vector<Vector<CoreSubmesh.TextureCoordinate>> vectorTextureCoordinates = submesh.getCoreSubmesh().getVectorTextureCoordinates();

	  int textureCoordinateCount = vectorTextureCoordinates.size();

	  // check if the map id is valid
	  if(((NumTexCoords < 0) || (NumTexCoords > textureCoordinateCount))) {
		 if(textureCoordinateCount!=0) {    
			 Error.setLastError(Error.INVALID_HANDLE, "", -1, "");
			 return null;
		 }
	  }  

	  // get physical property vector of the core submesh
	  Vector<CoreSubmesh.PhysicalProperty> physicalProperties = submesh.getCoreSubmesh().getPhysicalProperties();

	  // get the number of vertices
	  int vertexCount = submesh.getVertexCount();

	  // get the sub morph target vector from the core sub mesh
	  Vector<CoreSubMorphTarget> subMorphTargets = submesh.getCoreSubmesh().getCoreSubMorphTargets();

	  // calculate the base weight
	  float baseWeight = submesh.getBaseWeight();

	  // get the number of morph targets
	  int morphTargetCount = submesh.getMorphTargetWeightCount();

	  // calculate all submesh vertices
	  int vertexId;
	  for(vertexId = 0; vertexId < vertexCount; ++vertexId) {
	    // get the vertex
	    CoreSubmesh.Vertex vertex = coresubmeshVertices.get(vertexId);

	    // blend the morph targets
	    Vector3D position = new Vector3D();
	    Vector3D normal = new Vector3D();
	    if(baseWeight == 1.0f) {
	      position.x = vertex.position.x;
	      position.y = vertex.position.y;
	      position.z = vertex.position.z;
	      normal.x = vertex.normal.x;
	      normal.y = vertex.normal.y;
	      normal.z = vertex.normal.z;
	    }
	    else {
	      position.x = baseWeight*vertex.position.x;
	      position.y = baseWeight*vertex.position.y;
	      position.z = baseWeight*vertex.position.z;
	      normal.x = baseWeight*vertex.normal.x;
	      normal.y = baseWeight*vertex.normal.y;
	      normal.z = baseWeight*vertex.normal.z;
	      
	      for(int morphTargetId = 0; morphTargetId < morphTargetCount; ++morphTargetId) {
	        CoreSubMorphTarget.BlendVertex blendVertex = subMorphTargets.get(morphTargetId).getBlendVertices().get(vertexId);
	        float currentWeight = submesh.getMorphTargetWeight(morphTargetId);
	        position.x += currentWeight*blendVertex.position.x;
	        position.y += currentWeight*blendVertex.position.y;
	        position.z += currentWeight*blendVertex.position.z;
	        normal.x += currentWeight*blendVertex.normal.x;
	        normal.y += currentWeight*blendVertex.normal.y;
	        normal.z += currentWeight*blendVertex.normal.z;
	      }
	    }

	    // initialize vertex
	    float x, y, z;
	    x = 0.0f;
	    y = 0.0f;
	    z = 0.0f;

		// initialize normal
	    float nx, ny, nz;
	    nx = 0.0f;
	    ny = 0.0f;
	    nz = 0.0f;

	    // blend together all vertex influences
	    int influenceId;
		int influenceCount=(int)vertex.vectorInfluence.size();
		if(influenceCount == 0) 
		{
	      x = position.x;
	      y = position.y;
	      z = position.z;
	      nx = normal.x;
	      ny = normal.y;
	      nz = normal.z;
	    } 
		else 
		{
			for(influenceId = 0; influenceId < influenceCount; ++influenceId)
			{
				// get the influence
				CoreSubmesh.Influence influence = vertex.vectorInfluence.get(influenceId);
				
				// get the bone of the influence vertex
				Bone bone = bones.get(influence.boneId);
				
				// transform vertex with current state of the bone
				Vector3D v = new Vector3D(position);
				v = v.multiply(bone.getTransformMatrix());
				v = v.add(bone.getTranslationBoneSpace());
				
				x += influence.weight * v.x;
				y += influence.weight * v.y;
				z += influence.weight * v.z;
				
				// transform normal with current state of the bone
				Vector3D n = new Vector3D(normal);	  
				n = n.multiply(bone.getTransformMatrix());
				
				nx += influence.weight * n.x;
				ny += influence.weight * n.y;
				nz += influence.weight * n.z;
			}
		}

		Point3D submeshVertex = new Point3D();
		Point3D submeshNormal = new Point3D();
		Point3D textureCoordinate = new Point3D();
		
	    // save vertex position
	    if(submesh.getCoreSubmesh().getSpringCount() > 0 && submesh.hasInternalData()) {
	      // get the pgysical property of the vertex
	      CoreSubmesh.PhysicalProperty physicalProperty = physicalProperties.get(vertexId);

	      // assign new vertex position if there is no vertex weight
	      if(physicalProperty.weight == 0.0f) {
	    	  submeshVertex.x = x;
	    	  submeshVertex.y = y;
	    	  submeshVertex.z = z;
	      }
	    }
	    else {
	    	submeshVertex.x = x;
	    	submeshVertex.y = y;
	    	submeshVertex.z = z;
	    }
	    
		 // re-normalize normal if necessary
	    if (normalize) {
	      float scale;
	      scale = (float) (1.0f / Math.sqrt(nx * nx + ny * ny + nz * nz));

	      submeshNormal.x = nx * scale;
	      submeshNormal.y = ny * scale;
	      submeshNormal.z = nz * scale;
	    }
	    else {
	    	submeshNormal.x = nx;
	    	submeshNormal.y = ny;
	    	submeshNormal.z = nz;
	    }
	    vertices.add(submeshVertex);
	    vertices.add(submeshNormal);
		
		if(textureCoordinateCount != 0) {			
			for(int mapId=0; mapId < NumTexCoords; ++mapId) {
				textureCoordinate.x = vectorTextureCoordinates.get(mapId).get(vertexId).u;
				textureCoordinate.y = vectorTextureCoordinates.get(mapId).get(vertexId).v;		
				vertices.add(textureCoordinate);
			}
		}		
	  }

	  return vertices;
	}


	 /*****************************************************************************/
	/** Updates all the internally handled attached meshes.
	  *
	  * This function updates all the attached meshes of the model that are handled
	  * internally.
	  *****************************************************************************/

	public void update() {
	  // get the attached meshes vector
	  Vector<Mesh> meshes = model.getMeshes();

	  // loop through all the attached meshes	  
	  for(int meshIndex = 0; meshIndex < meshes.size(); ++meshIndex) {
	    // get the submesh vector of the mesh
	    Vector<Submesh> submeshes = meshes.get(meshIndex).getSubmeshes();

	    // loop through all the submeshes of the mesh	    
	    for(int submeshIndex = 0; submeshIndex < submeshes.size(); ++ submeshIndex) {
	      // check if the submesh handles vertex data internally
	    	Submesh submesh = submeshes.get(submeshIndex);
	      if(submesh.hasInternalData()) {
	        // calculate the transformed vertices and store them in the submesh	        
	        submesh.setVertices(calculateVertices(submesh));

	        // calculate the transformed normals and store them in the submesh	        
	        submesh.setNormals(calculateNormals(submesh));
	        
	        for(int mapId = 0; mapId < submesh.getVectorTangentSpaces().size(); mapId++) {
	          if(submesh.isTangentsEnabled(mapId)) {	            
	            submesh.setTangentSpaceVector(calculateTangentSpaces(submesh, mapId), mapId);
	          }
	        }

	      }
	    }
	  }
	}

	 /*****************************************************************************/
	/** Sets the normalization flag to true or false.
	  *
	  * This function sets the normalization flag on or off. If off, the normals
	  * calculated by Cal3D will not be normalized. Instead, this transform is left
	  * up to the user.
	  *****************************************************************************/

	public void setNormalization(boolean normalize) {
	  this.normalize = normalize;
	}
}
