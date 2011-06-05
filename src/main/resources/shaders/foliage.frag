varying vec3 Vertex_es;	
varying vec3 Normal_es;	
uniform int TextureApplied;
uniform sampler2D Texture;

void main (void) {
	vec3 L_es = normalize(gl_LightSource[0].position.xyz - Vertex_es);
	
	// we are in Eye Coordinates, so EyePos is (0,0,0)
	vec3 E_es = normalize(-Vertex_es);
	vec3 R_es = normalize(-reflect(L_es, Normal_es));

	vec4 Iamb, Idiff, Ispec;	
	
	//calculate Ambient Term:
	Iamb = gl_FrontMaterial.ambient * gl_LightSource[0].ambient;
			
	//calculate Diffuse Term:
	Idiff = gl_FrontMaterial.diffuse * gl_LightSource[0].diffuse * max(dot(Normal_es, L_es),0.0);
			
	// calculate Specular Term:
	Ispec = gl_FrontMaterial.specular * gl_LightSource[0].specular * pow(max(dot(R_es, E_es),0.0), gl_FrontMaterial.shininess);
		
	//if (TextureApplied == 1)
	//{
		vec4 texColor = texture2D(Texture, gl_TexCoord[0].st);
		
		if (texColor.a == 0.0)
			discard;
			
		else 
		{
			//Similar to GL_MODULATE texture function
			Iamb = texColor * Iamb;		
			Idiff = texColor * Idiff; 		
			Ispec = texColor * Ispec;
		}
//	}
	// write Total Color: 
	gl_FragColor = Iamb + Idiff + Ispec;
	
}
