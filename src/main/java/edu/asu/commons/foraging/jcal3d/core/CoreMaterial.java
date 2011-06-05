package edu.asu.commons.foraging.jcal3d.core;

import java.util.Vector;

import edu.asu.commons.foraging.graphics.RGBA;
import edu.asu.commons.foraging.jcal3d.misc.Error;



public class CoreMaterial {
	
	public class Map {
	    public String filename;
	    public Object userData = null;
	}
	
	protected RGBA ambientColor;
	protected RGBA diffuseColor;
	protected RGBA specularColor;
	protected float shininess;
	protected Vector<Map> maps = new Vector<Map>();
	protected Object userData = null;
	protected String name;
	protected String filename;
	protected int referenceCount;
	  
	/* Returns the ambient color.
	 *
	 * This function returns the ambient color of the core material instance.
	 *
	 * @return A reference to the ambient color.
	 */
	public RGBA getAmbientColor() {
	  return ambientColor;
	}

	/* Returns the diffuse color.
	 *
	 * This function returns the diffuse color of the core material instance.
	 *
	 * @return A reference to the diffuse color.
	 */
	public RGBA getDiffuseColor() {
	  return diffuseColor;
	}
	 
	/* Returns the number of maps.
	 *
	 * This function returns the number of mapss in the core material instance.
	 *
	 * @return The number of maps.
	 */
	public int getMapCount() {
	  return maps.size();
	}

	/* Returns a specified map texture filename.
	 *
	 * This function returns the texture filename for a specified map ID of the
	 * core material instance.
	 *
	 * @param mapId The ID of the map.
	 *
	 * @return One of the following values:
	 *         \li the filename of the map texture
	 *         \li an empty string if an error happend
	 */
	public String getMapFilename(int mapId) {
	  // check if the map id is valid
	  if((mapId < 0) || (mapId >= maps.size()))
	  {
	    Error.setLastError(Error.INVALID_HANDLE, "", -1, "");
	    return null;
	  }

	  return maps.get(mapId).filename;
	}
	 
	/* Provides access to a specified map user data.
	 *
	 * This function returns the user data stored in the specified map of the core
	 * material instance.
	 *
	 * @param mapId The ID of the map.
	 *
	 * @return One of the following values:
	 *         \li the user data stored in the specified map
	 *         \li \b 0 if an error happend
	 */
	public Object getMapUserData(int mapId) {
	  // check if the map id is valid
	  if((mapId < 0) || (mapId >= maps.size())) {
		  Error.setLastError(Error.INVALID_HANDLE, "", -1, "");
		  return null;
	  }
	  return maps.get(mapId).userData;
	}

	/* Returns the shininess factor.
	 *
	 * This function returns the shininess factor of the core material instance.
	 *
	 * @return The shininess factor.
	 */
	public float getShininess() {
	  return shininess;
	}

	/* Returns the specular color.
	 *
	 * This function returns the specular color of the core material instance.
	 *
	 * @return A reference to the specular color.
	 */
	public RGBA getSpecularColor() {
	  return specularColor;
	}
	 
	/* Provides access to the user data.
	 *
	 * This function returns the user data stored in the core material instance.
	 *
	 * @return The user data stored in the core material instance.
	 */
	public Object getUserData() {
	  return userData;
	}

	/* Returns the map vector.
	 *
	 * This function returns the vector that contains all maps of the core material
	 * instance.
	 *
	 * @return A reference to the map vector.
	 */
	public Vector<Map> getMaps() {
	  return maps;
	}

	/* Reserves memory for the maps.
	 *
	 * This function reserves memory for the maps of the core material instance.
	 *
	 * @param mapCount The number of maps that this core material instance should
	 *                 be able to hold.
	 *
	 * @return One of the following values:
	 *         \li \b true if successful
	 *         \li \b false if an error happend
	 */
	public boolean reserve(int mapCount) {
	  // reserve the space needed in all the vectors	  
		maps.setSize(mapCount);
		return true;
	}

	/* Sets the ambient color.
	 *
	 * This function sets the ambient color of the core material instance.
	 *
	 * @param ambientColor The ambient color that should be set.
	 */
	public void setAmbientColor(RGBA ambientColor){
		this.ambientColor = ambientColor;
	}

	/* Sets the diffuse color.
	 *
	 * This function sets the diffuse color of the core material instance.
	 *
	 * @param ambientColor The diffuse color that should be set.
	 */
	public void setDiffuseColor(RGBA diffuseColor) {
	  this.diffuseColor = diffuseColor;
	}

	/* Sets a specified map.
	 *
	 * This function sets a specified map in the core material instance.
	 *
	 * @param mapId  The ID of the map.
	 * @param map The map that should be set.
	 *
	 * @return One of the following values:
	 *         \li \b true if successful
	 *         \li \b false if an error happend
	 */
	public boolean setMap(int mapId, Map map) {
	  if((mapId < 0) || (mapId >= maps.size())) return false;
	  maps.setElementAt(map, mapId);
	  return true;
	}

	/* Stores specified map user data.
	 *
	 * This function stores user data in a specified map of the core material
	 * instance.
	 *
	 * @param mapId  The ID of the map.
	 * @param userData The user data that should be stored.
	 *
	 * @return One of the following values:
	 *         \li \b true if successful
	 *         \li \b false if an error happend
	 */
	public boolean setMapUserData(int mapId, Object userData) {
	  if((mapId < 0) || (mapId >= maps.size())) return false;
	  maps.get(mapId).userData = userData;
	  return true;
	}

	/* Sets the shininess factor.
	 * 
	 * This function sets the shininess factor of the core material instance.
	 *
	 * @param shininess The shininess factor that should be set.
	 */
	public void setShininess(float shininess){
	  this.shininess = shininess;
	}

	/* Sets the specular color.
	 *
	 * This function sets the specular color of the core material instance.
	 *
	 * @param ambientColor The specular color that should be set.
	 */
	public void setSpecularColor(RGBA specularColor) {
	  this.specularColor = specularColor;
	}

	/* 
	 * Set the name of the file in which the core material is stored, if any.
	 *
	 * @param filename The path of the file.
	 */
	public void setFileName(String filename) {
		this.filename = filename;
	}
	 
	/* 
	 * Get the name of the file in which the core material is stored, if any.
	 *
	 * @return One of the following values:
	 *         \li \b empty string if the material was not stored in a file
	 *         \li \b the path of the file
	 *
	 */
	public String getFileName() {
	  return this.filename;
	}
	
	/* 
	 * Set the symbolic name of the core material.
	 *
	 * @param name A symbolic name.
	 */
	public void setName(String name) {
	  this.name = name;
	}

	/* 
	 * Get the symbolic name the core material.
	 *
	 * @return One of the following values:
	 *         \li \b empty string if the material was no associated to a symbolic name
	 *         \li \b the symbolic name
	 *
	 */
	public String getName() {
	  return name;
	}

	/* Stores user data.
	 *
	 * This function stores user data in the core material instance.
	 *
	 * @param userData The user data that should be stored.
	 */
	public void setUserData(Object userData) {
	  this.userData = userData;
	}
	 
	/* 
	 * Increment the reference counter the core material.
	 *
	 */
	public void incRef()
	{
	  referenceCount++;
	}
	 
	/* 
	 * Decrement the reference counter the core material.
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
