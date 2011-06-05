package edu.asu.commons.foraging.jcal3d.misc;

import edu.asu.commons.foraging.graphics.Vector3D;

public class Quaternion {

	public float x;
	public float y;
	public float z;
	public float w;

	//Constructor
	public Quaternion() {
		x = 0.0f;
		y = 0.0f;
		z = 0.0f;
		w = 1.0f;
	}
	
	public Quaternion(float qx, float qy, float qz, float qw)
	{
		x = qx;
		y = qy;
		z = qz;
		w = qw;
	}
	
	public Quaternion(double qx, double qy, double qz, double qw)
	{
		x = (float)qx;
		y = (float)qy;
		z = (float)qz;
		w = (float)qw;
	}
	
	//phi about z-axis, theta in [0, pi] about x-axis, psi about the z-axis (again)
	public Quaternion(double phi, double theta, double psi)
	{
		phi = Math.toDegrees(phi);
		theta = Math.toDegrees(theta);
		psi = Math.toDegrees(psi);

		double c1 = Math.cos(phi/2.0f);
		double c2 = Math.cos(theta/2.0f);
		double c3 = Math.cos(psi/2.0f);

		double s1 = Math.sin(phi/2.0f);
		double s2 = Math.sin(theta/2.0f);
		double s3 = Math.sin(psi/2.0f);

		x = (float)(c1*c2*c3 + s1*s2*s3);
		y = (float)(s1*c2*c3 - c1*s2*s3);
		z = (float)(c1*s2*c3 + s1*c2*s3);
		w = (float)(c1*c2*s3 - s1*s2*c3);
	}
	
	//Copy constrcutor
	public Quaternion(Quaternion q) {
		x = q.x;
		y = q.y;
		z = q.z;
		w = q.w;
	}
	
	public Quaternion multiply(Quaternion q) {			
		Quaternion newQuaternion = new Quaternion();
		newQuaternion.x = w * q.x + x * q.w + y * q.z - z * q.y;
		newQuaternion.y = w * q.y - x * q.z + y * q.w + z * q.x;
		newQuaternion.z = w * q.z + x * q.y - y * q.x + z * q.w;
		newQuaternion.w = w * q.w - x * q.x - y * q.y - z * q.z;
		
		return newQuaternion;
	}
	
	public Quaternion multiply(Vector3D v) {
		Quaternion newQuaternion = new Quaternion();
		newQuaternion.x = w * v.x           + y * v.z - z * v.y;
		newQuaternion.y = w * v.y - x * v.z           + z * v.x;
		newQuaternion.z = w * v.z + x * v.y - y * v.x;
		newQuaternion.w =         - x * v.x - y * v.y - z * v.z;
		
		return newQuaternion;
	}

	public boolean equals(Quaternion rhs) {
	    return x == rhs.x &&
	           y == rhs.y &&
	           z == rhs.z &&
	           w == rhs.w;
	}

	/*	
		static inline CalQuaternion operator*(const CalQuaternion& q, const CalQuaternion& r)
		{
			return CalQuaternion(
				r.w * q.x + r.x * q.w + r.y * q.z - r.z * q.y,
				r.w * q.y - r.x * q.z + r.y * q.w + r.z * q.x,
				r.w * q.z + r.x * q.y - r.y * q.x + r.z * q.w,
				r.w * q.w - r.x * q.x - r.y * q.y - r.z * q.z
				);
		}
	*/	
	
	public Quaternion blend(float d, Quaternion q) {
		float norm;
		norm = x * q.x + y * q.y + z * q.z + w * q.w;
		
		boolean bFlip = false;
		
		if(norm < 0.0f)
		{
			norm = -norm;
			bFlip = true;
		}
		
		float inv_d;
		if(1.0f - norm < 0.000001f)
		{
			inv_d = 1.0f - d;
		}
		else
		{
			float theta;
			theta = (float) Math.acos(norm);
			
			float s;
			s = (float) (1.0f / Math.sin(theta));
			
			inv_d = (float) Math.sin((1.0f - d) * theta) * s;
			d = (float) Math.sin(d * theta) * s;
		}
		
		if(bFlip)
		{
			d = -d;
		}
		
		return new Quaternion(inv_d * x + d * q.x, 
					inv_d * y + d * q.y,
					inv_d * z + d * q.z,
					inv_d * w + d * q.w);
	}
		
	public void clear()
	{
		x = 0.0f;
		y = 0.0f;
		z = 0.0f;
		w = 1.0f;
	}
	
	public void conjugate()
	{
		x = -x;
		y = -y;
		z = -z;
	}
	
	public void invert()
	{
		conjugate();
		final float norm = (x*x) + (y*y) + (z*z) + (w*w);
		
		if (norm == 0.0f) return;
		
		final float inv_norm = 1 / norm;
		x *= inv_norm;
		y *= inv_norm;
		z *= inv_norm;
		w *= inv_norm;
	}
	
	public void set(float qx, float qy, float qz, float qw) {
		x = qx;
		y = qy;
		z = qz;
		w = qw;
	}
	/*	
		static inline CalQuaternion shortestArc( const CalVector& from, const CalVector& to )
		{
			CalVector cross = from % to; //Compute vector cross product
			float dot = from * to ;      //Compute dot product
			
			dot = (float) sqrt( 2*(dot+1) ) ; //We will use this equation twice
			
			cross /= dot ; //Get the x, y, z components
			
			//Return with the w component (Note that w is inverted because Cal3D has
			// left-handed rotations )
			return CalQuaternion( cross[0], cross[1], cross[2], -dot/2 ) ; 
			
		}	
	  
	};*/
	
	
	public static Quaternion multiply(Quaternion q, Quaternion r) {
		return new Quaternion(
			r.w * q.x + r.x * q.w + r.y * q.z - r.z * q.y,
			r.w * q.y - r.x * q.z + r.y * q.w + r.z * q.x,
			r.w * q.z + r.x * q.y - r.y * q.x + r.z * q.w,
			r.w * q.w - r.x * q.x - r.y * q.y - r.z * q.z
			);
	}
	
	public static Quaternion shortestArc(Vector3D from, Vector3D to ) {
		Vector3D cross = from.cross(to); //Compute vector cross product
		float dot = from.dot(to);      //Compute dot product
	
		dot = (float) Math.sqrt( 2*(dot+1) ) ; //We will use this equation twice
	
		cross = (Vector3D)cross.divide(dot) ; //Get the x, y, z components
	
		//Return with the w component (Note that w is inverted because Cal3D has
		// left-handed rotations )
		return new Quaternion( cross.x, cross.y, cross.z, -dot/2 ) ;		
	}

	public String toString() {
		return "(" + x + ", " + y + ", " + z + ", " + w + ")";
	}
}
