//===============================================================
//		Uniform variables
//===============================================================

//================================================================
//		varying variables
//================================================================
varying vec4 Vertex_ws;
varying mat4 Ws2osXform;
varying vec4 FrustumValues;
varying vec3 BaseCenter_os;

//=================================================================
//		Vertex attributes
//=================================================================
attribute vec4 FrustumValues_attrib;	//Rmax, Rmin, Height, Height_Cone
attribute vec4 Ws2OsXformRow1_attrib;
attribute vec4 Ws2OsXformRow2_attrib;
attribute vec4 Ws2OsXformRow3_attrib;
attribute vec4 Ws2OsXformRow4_attrib;
attribute vec3 FrustumBaseCenter_attrib;

void main(void)
{
	Vertex_ws = gl_Vertex;	
	Ws2osXform = mat4(Ws2OsXformRow1_attrib, Ws2OsXformRow2_attrib, Ws2OsXformRow3_attrib, Ws2OsXformRow4_attrib);
	
	BaseCenter_os = vec3( Ws2osXform * vec4(FrustumBaseCenter_attrib, 1.0) );
	
	FrustumValues = FrustumValues_attrib;
	
	//Set vertex position after world, eye and projection transformations
	gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
}
