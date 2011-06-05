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

HitRecord hitConicalCylinder(Ray ray)
{
	HitRecord hitRecord;
	hitRecord.hit = true;

	//Extend the conical frustum to a cone and use cone equation 
	//for calculating intersection with ray. 
	float o_angle = FrustumValues.x / FrustumValues.w;
	float o_angle_square = o_angle * o_angle;
	float o_minus_h = ray.origin.y - FrustumValues.w; 
	float a = (ray.direction.x * ray.direction.x) + (ray.direction.z * ray.direction.z) - (ray.direction.y * ray.direction.y)*o_angle_square;
	float b = 2.0*ray.origin.x*ray.direction.x + 2.0*ray.origin.z*ray.direction.z - 2.0*ray.direction.y*o_minus_h*o_angle_square;
	float c = (ray.origin.x*ray.origin.x) + (ray.origin.z*ray.origin.z) - (o_minus_h*o_minus_h)*o_angle_square;
	
	float D_sqrd = b*b - 4.0*a*c;
	
	if (D_sqrd < EPSILON)
	{
		hitRecord.hit = false;
	}
	else
	{
		float D = sqrt(D_sqrd);
		float two_a = 2.0*a;
		float t1 = (-b + D) / two_a;
		float t2 = (-b - D) / two_a;
		
		//choosing the smallest non-negative value between t1 and t2
		float t_intersect;
		if (t1 > 0.0)
		{
			if (t2 > 0.0)
			{
				if (t1 < t2) t_intersect = t1;
				else t_intersect = t2;
			}
			else
				t_intersect = t1;
		}
		else
		{
			if (t2 > 0.0)
				t_intersect = t2;
			else
				hitRecord.hit = false;
		}
					
		if (hitRecord.hit)
		{
			hitRecord.point = pointOnRay(ray, t_intersect);
			if (hitRecord.point.y < BaseCenter_os.y || hitRecord.point.y > (BaseCenter_os.y+FrustumValues.z) )
			{
				hitRecord.hit = false;
			}
			else
			{
				hitRecord.t = t_intersect;
				vec3 normal = vec3(hitRecord.point.x - BaseCenter_os.x, 0, hitRecord.point.z - BaseCenter_os.z);
				hitRecord.normal = normalize( normal / FrustumValues.x );
				hitRecord.texCoord.x = acos(dot(vec3(-1, 0, 0), hitRecord.normal))/PI;
				hitRecord.texCoord.y = (hitRecord.point.y - BaseCenter_os.y)/FrustumValues.z;
			}
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
	hitRecord = hitConicalCylinder(ray);
		
	vec4 color0;
	if (!hitRecord.hit)	 {
		discard;			
	}		
	else 
	{
		color0 = calculateColor(hitRecord, hitRecord.texCoord);
	}	
	
	gl_FragColor = color0;
}
