#define EPSILON   	1e-15
#define PI			3.14159265

//================================================================
//		uniform variables
//================================================================
uniform vec3   	CameraPosition_ws;
uniform vec3	LightPosition_ws;
uniform sampler2D Texture;

//================================================================
//		varying variables
//================================================================
varying vec4 Vertex_ws;
varying mat4 Ws2osXform;
varying vec4 FrustumValues;
varying vec3 BaseCenter_os;

//=================================================================
//				Global variables
//=================================================================
vec3 CameraPosition_os = vec3( Ws2osXform * vec4(CameraPosition_ws, 1.0) );
vec3 LightPosition_os = vec3( Ws2osXform * vec4(LightPosition_ws, 1.0) );
vec3 Vertex_os = vec3(Ws2osXform * Vertex_ws);

//================================================================
// Structures
//================================================================
struct Ray
{
   vec3 origin;
   vec3 direction;
};

struct HitRecord
{
	bool hit;		//true if ray intersects the object
	float t;		//t value
	vec3 point;		//hit point
	vec3 normal;	//point normal
	vec2 texCoord;	//texture coordinates
};

//================================================================
//		Functions
//================================================================
vec4 calculateColor(HitRecord hitRecord, vec2 texCoord)
{
	vec3 L = normalize(LightPosition_os - hitRecord.point);
	vec3 E = normalize(CameraPosition_os - hitRecord.point);
	vec3 R = normalize(-reflect(L, hitRecord.normal));
	
	vec4 Iamb, Idiff, Ispec;
	Iamb = gl_LightSource[0].ambient * gl_FrontMaterial.ambient;
	Idiff = gl_LightSource[0].diffuse * gl_FrontMaterial.diffuse * max(dot(hitRecord.normal, L), 0.0);
	Ispec = gl_LightSource[0].specular * gl_FrontMaterial.specular * pow(max(dot(R,E),0.0), gl_FrontMaterial.shininess);
	
	vec4 texColor = texture2D(Texture, texCoord.st);
	
	//Similar to GL_MODULATE texture function
	Iamb = texColor * Iamb;
	Idiff = texColor * Idiff; 		
	Ispec = texColor * Ispec;
				
	return (Iamb + Idiff + Ispec);
}

vec3 pointOnRay(Ray ray, float t)
{
	return ray.origin + t * ray.direction;
}

HitRecord hitSphere(Ray ray)
{
	HitRecord hitRecord;
	
	vec3 c_minus_o = BaseCenter_os - ray.origin;

	float c_minus_o_sqrd = dot(c_minus_o, c_minus_o);
	float t_intersect = -1.0;

	// find the length of the ray along the ray closest to the 
	// center of the sphere ...
	float L = dot(c_minus_o, ray.direction);

	if( L < EPSILON ) // ray does not intersect the sphere ..
	{
		hitRecord.hit = false;
	}
	else
	{
		float radius_square = FrustumValues.x * FrustumValues.x;
		float D_sqr = c_minus_o_sqrd - ( L * L );
		float HC_sqrd = radius_square - D_sqr;
		
		if( HC_sqrd < EPSILON ) // ray misses the sphere
		{
			hitRecord.hit = false;
		}
		else
		{
			float HC = sqrt(HC_sqrd);
		
			// is ray origin outside the sphere??
			if( c_minus_o_sqrd >= radius_square )
			{
				// yes, ray originates outside the sphere
				t_intersect = L - HC;		
			}
			else 
			{
				// no, ray originates inside the sphere ...
				t_intersect = L + HC;
			}			
			hitRecord.hit = true;
			hitRecord.t = t_intersect;
			hitRecord.point = pointOnRay(ray, t_intersect );
			if (hitRecord.point.y > BaseCenter_os.y+FrustumValues.z || 
				hitRecord.point.y < BaseCenter_os.y-FrustumValues.z) 
			{
				hitRecord.normal = vec3(0.0, 1.0, 0.0);		//disk faces
			}
			else {
				hitRecord.normal = normalize( (hitRecord.point - BaseCenter_os) / FrustumValues.x );			
			}
			hitRecord.texCoord.x = acos(dot(vec3(-1, 0, 0), hitRecord.normal))/PI;
			hitRecord.texCoord.y = acos(dot(vec3(0, 0, 1), hitRecord.normal))/PI; 
		}
	}
	return hitRecord;
}

void main(void)
{
	Ray ray;
	
	//Calculate ray
	ray.origin = CameraPosition_os;
	ray.direction = normalize(Vertex_os - CameraPosition_os);
	
	//Check intersection
	HitRecord hitRecord;
	hitRecord = hitSphere(ray);
		
	vec4 color0;
	if (!hitRecord.hit)	 {
		discard;			
	}		
	else 
	{
		color0 = calculateColor(hitRecord, hitRecord.texCoord);
		if (color0.a == 0.0)
			discard;
	}	
	
	gl_FragColor = color0;
}
