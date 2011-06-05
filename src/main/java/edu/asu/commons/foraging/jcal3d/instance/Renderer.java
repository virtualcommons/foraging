package edu.asu.commons.foraging.jcal3d.instance;

import java.util.Vector;

import edu.asu.commons.foraging.graphics.Point3D;
import edu.asu.commons.foraging.graphics.RGBA;
import edu.asu.commons.foraging.graphics.Vector3D;
import edu.asu.commons.foraging.jcal3d.core.CoreMaterial;
import edu.asu.commons.foraging.jcal3d.core.CoreSubmesh;
import edu.asu.commons.foraging.jcal3d.core.CoreSubmesh.TextureCoordinate;
import edu.asu.commons.foraging.jcal3d.instance.Submesh.Face;
import edu.asu.commons.foraging.jcal3d.misc.Error;


public class Renderer {

	protected Model model;
	protected Submesh selectedSubmesh = null;
	  
	 /*****************************************************************************/
	/** Constructs the renderer instance.
	  *
	  * This function is the default constructor of the renderer instance.
	  *****************************************************************************/

	public Renderer(Model model) {
		//FIXME: Make sure that model is not null	  

		this.model = model;
	}

	 /*****************************************************************************/
	/** Copy-constructor for the renderer instance.
	  *
	  * This function is the copy constructor of the renderer instance.
	  * This is useful for multi-pipe parallel rendering.
	  *****************************************************************************/

	public Renderer(Renderer renderer) {
	  this.model = renderer.model ;
	  this.selectedSubmesh = renderer.selectedSubmesh ;
	}

	 /*****************************************************************************/
	/** Initializes the rendering query phase.
	  *
	  * This function initializes the rendering query phase. It must be called
	  * before any rendering queries are executed.
	  *****************************************************************************/

	public boolean beginRendering() {
	  // get the attached meshes vector
	  Vector<Mesh> meshes = model.getMeshes();

	  // check if there are any meshes attached to the model
	  if(meshes.size() == 0) {
	    Error.setLastError(Error.INVALID_HANDLE, "", -1, "");
	    return false;
	  }

	  // select the default submesh
	  selectedSubmesh = meshes.get(0).getSubmesh(0);
	  if(selectedSubmesh == null) return false;

	  return true;
	}

	 /*****************************************************************************/
	/** Finishes the rendering query phase.
	  *
	  * This function finishes the rendering query phase. It must be called
	  * after all rendering queries have been executed.
	  *****************************************************************************/

	public void endRendering() {
	  // clear selected submesh
	  selectedSubmesh = null;
	}

	 /*****************************************************************************/
	/** Provides access to the ambient color.
	  *
	  * This function returns the ambient color of the material of the selected
	  * mesh/submesh.
	  *
	  * @param pColorBuffer A pointer to the user-provided buffer where the color
	  *                     data is written to.
	  *****************************************************************************/
	public RGBA getAmbientColor() {
		int materialId = selectedSubmesh.getCoreMaterialId();
		//if (materialId == -1) return new RGBA();
	  // get the core material		
	  CoreMaterial coreMaterial = model.getCoreModel().getCoreMaterial(materialId);
	  if(coreMaterial == null) 
	    // write default values to the color buffer	    
	    return new RGBA();
	  
	  // get the ambient color of the material
	  return coreMaterial.getAmbientColor();
	}

	 /*****************************************************************************/
	/** Provides access to the diffuse color.
	  *
	  * This function returns the diffuse color of the material of the selected
	  * mesh/submesh.
	  *
	  * @param pColorBuffer A pointer to the user-provided buffer where the color
	  *                     data is written to.
	  *****************************************************************************/

	public RGBA getDiffuseColor()
	{
		int materialId = selectedSubmesh.getCoreMaterialId();
		//if (materialId == -1) return new RGBA();
		//get the core material
		  CoreMaterial coreMaterial = model.getCoreModel().getCoreMaterial(materialId);
		  if(coreMaterial == null)
		  {
		    // write default values to the color buffer	    
		    return new RGBA(0.75f, 0.75f, 0.75f, 1.0f);
		  }

		  // get the ambient color of the material
		  return coreMaterial.getDiffuseColor();
	}

	 /*****************************************************************************/
	/** Returns the number of faces.
	  *
	  * This function returns the number of faces in the selected mesh/submesh.
	  *
	  * @return The number of faces.
	  *****************************************************************************/

	public int getFaceCount()
	{
	  return selectedSubmesh.getFaceCount();
	}

	 /*****************************************************************************/
	/** Provides access to the face data.
	  *
	  * This function returns the face data (vertex indices) of the selected
	  * mesh/submesh. The LOD setting is taken into account.
	  *
	  * @param pFaceBuffer A pointer to the user-provided buffer where the face
	  *                    data is written to.
	  *
	  * @return The number of faces written to the buffer.
	  *****************************************************************************/
	public Vector<Face> getFaces()
	{
	  return selectedSubmesh.getFaces();
	}

	 /*****************************************************************************/
	/** Returns the number of maps.
	  *
	  * This function returns the number of maps in the selected mesh/submesh.
	  *
	  * @return The number of maps.
	  *****************************************************************************/

	public int getMapCount()
	{
	  // get the core material
	  CoreMaterial coreMaterial = model.getCoreModel().getCoreMaterial(selectedSubmesh.getCoreMaterialId());
	  if(coreMaterial == null) return 0;

	  return coreMaterial.getMapCount();
	}

	 /*****************************************************************************/
	/** Provides access to a specified map user data.
	  *
	  * This function returns the user data stored in the specified map of the
	  * material of the selected mesh/submesh.
	  *
	  * @param mapId The ID of the map.
	  *
	  * @return One of the following values:
	  *         \li the user data stored in the specified map
	  *         \li \b 0 if an error happend
	  *****************************************************************************/

	public Object getMapUserData(int mapId)
	{
	  // get the core material
	  CoreMaterial coreMaterial = model.getCoreModel().getCoreMaterial(selectedSubmesh.getCoreMaterialId());
	  if(coreMaterial == null) return null;

	  // get the map vector
	  Vector<CoreMaterial.Map> maps = coreMaterial.getMaps();

	  // check if the map id is valid
	  if((mapId < 0) || mapId >= maps.size())
	  {
	    edu.asu.commons.foraging.jcal3d.misc.Error.setLastError(Error.INVALID_HANDLE, "", -1, "");
	    return null;
	  }

	  return maps.get(mapId).userData;
	}

	 /*****************************************************************************/
	/** Returns the number of attached meshes.
	  *
	  * This function returns the number of meshes attached to the renderer
	  * instance.
	  *
	  * @return The number of attached meshes.
	  *****************************************************************************/

	public int getMeshCount() {
	  // get the attached meshes vector
	  Vector<Mesh> meshes = model.getMeshes();

	  return meshes.size();
	}



	 /*****************************************************************************/
	/** Provides access to the tangent space data.
	  *
	  * This function returns the tangent space data of the selected mesh/submesh.
	  *
	  * @param mapID
	  *
	  * @param pTangentSpaceBuffer A pointer to the user-provided buffer where the normal
	  *                      data is written to.
	  *
	  * @return The number of tangent space written to the buffer.
	  *****************************************************************************/
	public Vector<Submesh.TangentSpace> getTangentSpaces(int mapId, int stride)
	{
	  // get the texture coordinate vector vector
	  Vector<Vector<CoreSubmesh.TangentSpace>> vectorTangentSpaces = selectedSubmesh.getCoreSubmesh().getVectorTangentSpaces();
	  
	  // check if the map id is valid
	  if((mapId < 0) || (mapId >= vectorTangentSpaces.size()) || !selectedSubmesh.isTangentsEnabled(mapId))
	  {    
	    Error.setLastError(Error.INVALID_HANDLE, "", -1, "");
	    return null;
	  }

	  // check if the submesh handles vertex data internally
	  if(selectedSubmesh.hasInternalData())
	  {
	    // get the normal vector of the submesh
	    return selectedSubmesh.getVectorTangentSpaces().get(mapId);
	  }

	  // submesh does not handle the vertex data internally, so let the physique calculate it now
	  return model.getPhysique().calculateTangentSpaces(selectedSubmesh, mapId);
	}


	 /*****************************************************************************/
	/** Provides access to the normal data.
	  *
	  * This function returns the normal data of the selected mesh/submesh.
	  *
	  * @param pNormalBuffer A pointer to the user-provided buffer where the normal
	  *                      data is written to.
	  *
	  * @return The number of normals written to the buffer.
	  *****************************************************************************/

	public Vector<Vector3D> getNormals()
	{
	  // check if the submesh handles vertex data internally
	  if(selectedSubmesh.hasInternalData())
	  {
	    // get the normal vector of the submesh
	    return selectedSubmesh.getNormals();
	  }

	  // submesh does not handle the vertex data internally, so let the physique calculate it now
	  return model.getPhysique().calculateNormals(selectedSubmesh);
	}

	 /*****************************************************************************/
	/** Returns the shininess factor.
	  *
	  * This function returns the shininess factor of the material of the selected
	  * mesh/submesh..
	  *
	  * @return The shininess factor.
	  *****************************************************************************/

	public float getShininess()
	{
		int materialId = selectedSubmesh.getCoreMaterialId();
		//if (materialId == -1) return 50.0f;
	  // get the core material
	  CoreMaterial coreMaterial = model.getCoreModel().getCoreMaterial(materialId);
	  if(coreMaterial == null) return 50.0f;

	  return coreMaterial.getShininess();
	}

	 /*****************************************************************************/
	/** Provides access to the specular color.
	  *
	  * This function returns the specular color of the material of the selected
	  * mesh/submesh.
	  *
	  * @param pColorBuffer A pointer to the user-provided buffer where the color
	  *                     data is written to.
	  *****************************************************************************/

	public RGBA getSpecularColor()
	{
		int materialId = selectedSubmesh.getCoreMaterialId();
		//if (materialId == -1) return new RGBA();
		
	  // get the core material
	  CoreMaterial coreMaterial = model.getCoreModel().getCoreMaterial(materialId);
	  if(coreMaterial == null)
	  {
	    // write default values to the color buffer
		  return new RGBA(1.0f, 1.0f, 1.0f, 1.0f);
	   }

	  // get the specular color of the material
	  return coreMaterial.getSpecularColor();
	}

	 /*****************************************************************************/
	/** Returns the number of submeshes.
	  *
	  * This function returns the number of submeshes in a given mesh.
	  *
	  * @param meshId The ID of the mesh for which the number of submeshes should
	  *               be returned..
	  *
	  * @return The number of submeshes.
	  *****************************************************************************/

	public int getSubmeshCount(int meshId)
	{
	  // get the attached meshes vector
	  Vector<Mesh> meshes = model.getMeshes();

	  // check if the mesh id is valid
	  if((meshId < 0) || (meshId >= meshes.size()))
	  {
	    Error.setLastError(Error.INVALID_HANDLE, "", -1, "");
	    return 0;
	  }

	  return meshes.get(meshId).getSubmeshCount();
	}

	 /*****************************************************************************/
	/** Provides access to the texture coordinate data.
	  *
	  * This function returns the texture coordinate data for a given map of the
	  * selected mesh/submesh.
	  *
	  * @param mapId The ID of the map to get the texture coordinate data from.
	  * @param pTextureCoordinateBuffer A pointer to the user-provided buffer where
	  *                    the texture coordinate data is written to.
	  *
	  * @return The number of texture coordinates written to the buffer.
	  *****************************************************************************/

	public Vector<TextureCoordinate> getTextureCoordinates(int mapId)
	{
	  // get the texture coordinate vector vector
	  Vector<Vector<CoreSubmesh.TextureCoordinate>> vectorTextureCoordinates = selectedSubmesh.getCoreSubmesh().getVectorTextureCoordinates();

	  // check if the map id is valid
	  if((mapId < 0) || (mapId >= vectorTextureCoordinates.size()))
	  {
	    Error.setLastError(Error.INVALID_HANDLE, "", -1, "");
	    return null;
	  }

	  return vectorTextureCoordinates.get(mapId);
	}

	 /*****************************************************************************/
	/** Returns the number of vertices.
	  *
	  * This function returns the number of vertices in the selected mesh/submesh.
	  *
	  * @return The number of vertices.
	  *****************************************************************************/

	public int getVertexCount()
	{
	  return selectedSubmesh.getVertexCount();
	}

	 /*****************************************************************************/
	/** Returns if tangent are enabled.
	  *
	  * This function returns if tangent of the current submesh are enabled
	  *
	  * @return True is tangent is enabled.
	  *****************************************************************************/

	public boolean isTangentsEnabled(int mapId)
	{
		return selectedSubmesh.isTangentsEnabled(mapId);
	}

	 /*****************************************************************************/
	/** Provides access to the vertex data.
	  *
	  * This function returns the vertex data of the selected mesh/submesh.
	  *
	  * @param pVertexBuffer A pointer to the user-provided buffer where the vertex
	  *                      data is written to.
	  *
	  * @return The number of vertices written to the buffer.
	  *****************************************************************************/

	public Vector<Point3D> getVertices()
	{
	  // check if the submesh handles vertex data internally
	  if(selectedSubmesh.hasInternalData())
	  {
	    // get the vertex vector of the submesh
	    return selectedSubmesh.getVertices();
	  }

	  // submesh does not handle the vertex data internally, so let the physique calculate it now
	  return model.getPhysique().calculateVertices(selectedSubmesh);
	}

	 /*****************************************************************************/
	/** Provides access to the submesh data.
	  *
	  * This function returns the vertex and normal data of the selected mesh/submesh.
	  *
	  * @param pVertexBuffer A pointer to the user-provided buffer where the vertex
	  *                      and normal data is written to.
	  *
	  * @return The number of vertex written to the buffer.
	  *****************************************************************************/

	public Vector<Point3D> getVerticesAndNormals()
	{
	  // check if the submesh handles vertex data internally
	  if(selectedSubmesh.hasInternalData())
	  {
	    // get the vertex vector of the submesh
	    Vector<Point3D> vertices = selectedSubmesh.getVertices();
		// get the normal vector of the submesh
	    Vector<Vector3D> normals = selectedSubmesh.getNormals();
	  
	    Vector<Point3D> verticesAndNormals = new Vector<Point3D>();
	    for (int normalIndex = 0; normalIndex < normals.size(); ++normalIndex) {
	    	verticesAndNormals.add(vertices.get(normalIndex));
	    	verticesAndNormals.add(normals.get(normalIndex));
	    }	    
	    return vertices;
	  }

	  // submesh does not handle the vertex data internally, so let the physique calculate it now
	  return model.getPhysique().calculateVerticesAndNormals(selectedSubmesh);
	}

	 /*****************************************************************************/
	/** Provides access to the submesh data.
	  *
	  * This function returns the vertex and normal data of the selected mesh/submesh.
	  *
	  * @param pVertexBuffer A pointer to the user-provided buffer where the vertex
	  *                      and normal data is written to.
	  *
	  * @return The number of vertex written to the buffer.
	  *****************************************************************************/


	public Vector<Point3D> getVerticesNormalsAndTexCoords(int NumTexCoords)
	{  

	  // check if the submesh handles vertex data internally
	  if(selectedSubmesh.hasInternalData())
	  {
	    // get the vertex vector of the submesh
	    Vector<Point3D> vertices = selectedSubmesh.getVertices();
		// get the normal vector of the submesh
	    Vector<Vector3D> normals = selectedSubmesh.getNormals();	
		// get the texture coordinate vector vector
	    Vector<Vector<CoreSubmesh.TextureCoordinate>> vectorTextureCoordinates = selectedSubmesh.getCoreSubmesh().getVectorTextureCoordinates();

		int TextureCoordinateCount = vectorTextureCoordinates.size();
		
		// check if the map id is valid
	    if((NumTexCoords < 0) || (NumTexCoords > TextureCoordinateCount))
		{
		   if(TextureCoordinateCount!=0)
		   {
			   Error.setLastError(Error.INVALID_HANDLE, "", -1, "");
			   return null;
		   }
		}

	    // get the number of vertices in the submesh
	    int vertexCount = selectedSubmesh.getVertexCount();

	    // copy the internal vertex data to the provided vertex buffer
		
	    Vector<Point3D> verticesAndNormalsAndTex = new Vector<Point3D>();
		if(TextureCoordinateCount==0)
		{
			for(int vertexId=0; vertexId < vertexCount; ++vertexId)
			{
				verticesAndNormalsAndTex.add(vertices.get(vertexId));
				verticesAndNormalsAndTex.add(normals.get(vertexId));
				verticesAndNormalsAndTex.add(new Point3D());				
			}
		}
		else
		{
			if(NumTexCoords==1)
			{
				for(int vertexId=0; vertexId < vertexCount; ++vertexId)
				{
					verticesAndNormalsAndTex.add(vertices.get(vertexId));
					verticesAndNormalsAndTex.add(normals.get(vertexId));
					TextureCoordinate texCoord = vectorTextureCoordinates.get(0).get(vertexId);
					verticesAndNormalsAndTex.add(new Point3D(texCoord.u, texCoord.v, 0));
				}
			}
		    else
			{
				for(int vertexId=0; vertexId < vertexCount; ++vertexId)
				{
					verticesAndNormalsAndTex.add(vertices.get(vertexId));
					verticesAndNormalsAndTex.add(normals.get(vertexId));					
					for(int mapId=0; mapId < NumTexCoords; ++mapId)
					{
						TextureCoordinate texCoord = vectorTextureCoordinates.get(mapId).get(vertexId);
						verticesAndNormalsAndTex.add(new Point3D(texCoord.u, texCoord.v, 0));					
					}
				}
			}		
		}

	    return verticesAndNormalsAndTex;
	  }
	  // submesh does not handle the vertex data internally, so let the physique calculate it now
	  return model.getPhysique().calculateVerticesNormalsAndTexCoords(selectedSubmesh, NumTexCoords);
	}

	 /*****************************************************************************/
	/** Selects a mesh/submesh for rendering data queries.
	  *
	  * This function selects a mesh/submesh for further rendering data queries.
	  *
	  * @param meshId The ID of the mesh that should be used for further rendering
	  *               data queries.
	  * @param submeshId The ID of the submesh that should be used for further
	  *                  rendering data queries.
	  *
	  * @return One of the following values:
	  *         \li \b true if successful
	  *         \li \b false if an error happend
	  *****************************************************************************/

	public boolean selectMeshSubmesh(int meshId, int submeshId)
	{
	  // get the attached meshes vector
	  Vector<Mesh> meshes = model.getMeshes();

	  // check if the mesh id is valid
	  if((meshId < 0) || (meshId >= meshes.size()))
	  {
	    Error.setLastError(Error.INVALID_HANDLE, "", -1, "");
	    return false;
	  }

	  // get the core submesh
	  selectedSubmesh = meshes.get(meshId).getSubmesh(submeshId);
	  if(selectedSubmesh == null) return false;

	  return true;
	}

	 /*****************************************************************************/
	/** Sets the normalization flag to true or false.
	  *
	  * This function sets the normalization flag on or off. If off, the normals
	  * calculated by Cal3D will not be normalized. Instead, this transform is left
	  * up to the user.
	  *****************************************************************************/

	public void setNormalization(boolean normalize)
	{ 
		model.getPhysique().setNormalization(normalize); 
	}
}
