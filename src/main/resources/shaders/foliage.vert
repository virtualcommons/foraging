varying vec3 Vertex_es;
varying vec3 Normal_es;

void main(void)
{
	Vertex_es = vec3(gl_ModelViewMatrix * gl_Vertex);
	Normal_es = normalize(gl_NormalMatrix * gl_Normal);
	gl_TexCoord[0] = gl_MultiTexCoord0;
	gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
}
