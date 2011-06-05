package edu.asu.commons.foraging.jcal3d.misc;

public class Matrix {

	public float dxdx,dydx,dzdx;
	public float dxdy,dydy,dzdy;
	public float dxdz,dydz,dzdz;
	
	public Matrix() {
		
	}
		
	public Matrix(Quaternion q) {
		float xx2=q.x*q.x*2;
		float yy2=q.y*q.y*2;
		float zz2=q.z*q.z*2;
		float xy2=q.x*q.y*2;
		float zw2=q.z*q.w*2;
		float xz2=q.x*q.z*2;
		float yw2=q.y*q.w*2;
		float yz2=q.y*q.z*2;
		float xw2=q.x*q.w*2;
  		
		dxdx=1-yy2-zz2;   dxdy=  xy2+zw2;  dxdz=  xz2-yw2;
		dydx=  xy2-zw2;   dydy=1-xx2-zz2;  dydz=  yz2+xw2;
		dzdx=  xz2+yw2;   dzdy=  yz2-xw2;  dzdz=1-xx2-yy2;
	}
		
	public Matrix(float weight, Matrix m) {
		dxdx = m.dxdx*weight;
		dxdy = m.dxdy*weight;
		dxdz = m.dxdz*weight;
		dydx = m.dydx*weight;
		dydy = m.dydy*weight;
		dydz = m.dydz*weight;
		dzdx = m.dzdx*weight;
		dzdy = m.dzdy*weight;
		dzdz = m.dzdz*weight;
	}
		 
	/* Quaternion to Matrix Conversion.
	 *
	 * This function converts a quaternion into a rotation matrix.
	 */
	public static Matrix convert(Quaternion q) {
	  float xx2=q.x*q.x*2;
	  float yy2=q.y*q.y*2;
	  float zz2=q.z*q.z*2;
	  float xy2=q.x*q.y*2;
	  float zw2=q.z*q.w*2;
	  float xz2=q.x*q.z*2;
	  float yw2=q.y*q.w*2;
	  float yz2=q.y*q.z*2;
	  float xw2=q.x*q.w*2;
	  
	  Matrix m = new Matrix();
	  m.dxdx=1-yy2-zz2;   m.dxdy=  xy2+zw2;  m.dxdz=  xz2-yw2;
	  m.dydx=  xy2-zw2;   m.dydy=1-xx2-zz2;  m.dydz=  yz2+xw2;
	  m.dzdx=  xz2+yw2;   m.dzdy=  yz2-xw2;  m.dzdz=1-xx2-yy2;
	  
	  return m;
	}
	
	public Matrix multiply(Matrix m) {
		Matrix newMatrix = new Matrix();
		
		float ndxdx=m.dxdx*dxdx+m.dxdy*dydx+m.dxdz*dzdx;
		float ndydx=m.dydx*dxdx+m.dydy*dydx+m.dydz*dzdx;
		float ndzdx=m.dzdx*dxdx+m.dzdy*dydx+m.dzdz*dzdx;
		
		float ndxdy=m.dxdx*dxdy+m.dxdy*dydy+m.dxdz*dzdy;
		float ndydy=m.dydx*dxdy+m.dydy*dydy+m.dydz*dzdy;
		float ndzdy=m.dzdx*dxdy+m.dzdy*dydy+m.dzdz*dzdy;
		
		float ndxdz=m.dxdx*dxdz+m.dxdy*dydz+m.dxdz*dzdz;
		float ndydz=m.dydx*dxdz+m.dydy*dydz+m.dydz*dzdz;
		float ndzdz=m.dzdx*dxdz+m.dzdy*dydz+m.dzdz*dzdz;
		
		newMatrix.dxdx=ndxdx;
		newMatrix.dydx=ndydx;
		newMatrix.dzdx=ndzdx;
		newMatrix.dxdy=ndxdy;
		newMatrix.dydy=ndydy;
		newMatrix.dzdy=ndzdy;
		newMatrix.dxdz=ndxdz;
		newMatrix.dydz=ndydz;
		newMatrix.dzdz=ndzdz;
		
		return newMatrix;
	}
		
	public Matrix multiply(float factor) {
		Matrix newMatrix = new Matrix();
		newMatrix.dxdx *= factor;
		newMatrix.dydx *= factor;
		newMatrix.dzdx *= factor;
		newMatrix.dxdy *= factor;
		newMatrix.dydy *= factor;
		newMatrix.dzdy *= factor;
		newMatrix.dxdz *= factor;
		newMatrix.dydz *= factor;
		newMatrix.dzdz *= factor;
		
		return newMatrix;
	}
	
	public void blend(float factor, Matrix m){
		dxdx += m.dxdx*factor;
		dydx += m.dydx*factor;
		dzdx += m.dzdx*factor;
		dxdy += m.dxdy*factor;
		dydy += m.dydy*factor;
		dzdy += m.dzdy*factor;
		dxdz += m.dxdz*factor;
		dydz += m.dydz*factor;
		dzdz += m.dzdz*factor;
	}
		
	public float det() {
		return dxdx * (dydy*dzdz-dydz*dzdy)
	            -dxdy* ( dydx*dzdz-dzdx*dydz)
				+dxdz* (dydx*dzdy-dzdx*dydy);
	}
}
