package edu.asu.commons.foraging.graphics;

/**
 * The Matrix4 class encapsulates a 4x4 matrix holding float values. This class also encapsulates basic operations on a 4x4 matrix.
 *  
 * @author <a href='deepali.bhagvat@asu.edu'>Deepali Bhagvat</a>
 * @version 
 *
 */
public class Matrix4 {
	/**
	 * A 2D array holding values in this matrix
	 */
	public float[][] mm = new float[4][4];
	
	/**
	 * Constructs an identity matrix
	 */
	public Matrix4()
	{
		mm[0][0] = mm[1][1] = mm[2][2] = mm[3][3] = 1.0f;
		mm[0][1] = mm[0][2] = mm[0][3] = mm[1][0] = mm[1][2] = mm[1][3] = 0.0f; 
		mm[2][0] = mm[2][1] = mm[2][3] = mm[3][0] = mm[3][1] = mm[3][2] = 0.0f;
	}

	/**
	 * Constructs a matrix using another matrix
	 * @param mat matrix used to create this matrix
	 */
	public Matrix4(Matrix4 mat)
	{
	  for(int i = 0;i < 4; i++) {
		  for (int j = 0; j < 4; j++) {
			  mm[i][j] = mat.mm[i][j];
		  }
	  }
	}

	/**
	 * Constructs a rotation matrix  
	 * @param theta angle
	 * @param axis axis of rotation
	 */
	public Matrix4(float theta, Vector3D axis)
	{
		float c = (float)Math.cos(Math.toRadians(theta) );
		float s = (float)Math.sin(Math.toRadians(theta) );
		float t = 1 - c;

		mm[0][0] = t*axis.x*axis.x+c;		 mm[0][1] = t*axis.x*axis.y-s*axis.z;  mm[0][2] = t*axis.x*axis.z+s*axis.y;  mm[0][3] = 0;
		mm[1][0] = t*axis.x*axis.y+s*axis.z; mm[1][1] = t*axis.y*axis.y+c;		   mm[1][2] = t*axis.y*axis.z-s*axis.x;  mm[1][3] = 0;
		mm[2][0] = t*axis.x*axis.y-s*axis.y; mm[2][1] = t*axis.y*axis.z+s*axis.x;  mm[2][2] = t*axis.z*axis.z+c;		 mm[2][3] = 0;
		mm[3][0] = 0;						 mm[3][1] = 0;						   mm[3][2] = 0;					     mm[3][3] = 1;
	}
	
	/**
	 * Constructs a translation or scaling matrix
	 * @param transformation translation or scale values
	 * @param translation specifies if this is a translation or scaling matrix
	 */
	public Matrix4(Point3D transformation, boolean translation)
	{
		if (translation)
		{
			mm[0][0] = 1;					mm[0][1] = 0;					mm[0][2] = 0;					mm[0][3] = transformation.x;
			mm[1][0] = 0;					mm[1][1] = 1;					mm[1][2] = 0;					mm[1][3] = transformation.y;
			mm[2][0] = 0;					mm[2][1] = 0;					mm[2][2] = 1;					mm[2][3] = transformation.z;
			mm[3][0] = 0;					mm[3][1] = 0;					mm[3][2] = 0;					mm[3][3] = 1;
		}
		else
		{
			mm[0][0] = transformation.x;	mm[0][1] = 0;					mm[0][2] = 0;					mm[0][3] = 0;
			mm[1][0] = 0;					mm[1][1] = transformation.y;	mm[1][2] = 0;					mm[1][3] = 0;
			mm[2][0] = 0;					mm[2][1] = 0;					mm[2][2] = transformation.z;	mm[2][3] = 0;
			mm[3][0] = 0;					mm[3][1] = 0;					mm[3][2] = 0;					mm[3][3] = 1;
		}
	}
	
	/**
	 * Assigns another matrix to this matrix
	 * @param mat 2D array containing values to be assigned
	 */
	public void assign(float[][] mat)
	{
	  for(int i = 0; i < 4; i++) {
		  for (int j = 0; j < 4; j++) {
			  mm[i][j] = mat[i][j];
		  }
	  }
	}

	/**
	 * Assigns another matrix to this matrix
	 * @param mat matrix whose values are assigned 
	 */
	public void assign(Matrix4 mat)
	{
		for(int i = 0; i < 4; i++) {
			  for (int j = 0; j < 4; j++) {
				  mm[i][j] = mat.mm[i][j];
			  }
		  }
	}
	
	/**
	 * Converts this matrix to a translation matrix 
	 * @param translation used to convert this matrix
	 */
	public void assign(Point3D translation)
	{
		mm[0][3] = translation.x; mm[1][3] = translation.y; mm[2][3] = translation.z;
		mm[0][0] = mm[1][1] = mm[2][2] = mm[3][3] = 1.0f;
		mm[0][1] = mm[0][2] = mm[1][0] = mm[1][2] = 0.0f; 
		mm[2][0] = mm[2][1] = mm[3][0] = mm[3][1] = mm[3][2] = 0.0f;
	}

	/**
	 * Converts this matrix to an identity matrix
	 * @return identity matrix
	 */
	public Matrix4 loadIdentity()
	{
	  for(int i=0;i<4;i++)
	    for(int j=0;j<4;j++)
	      if(i==j) mm[i][j] = 1; else mm[i][j] = 0;
	  return this;
	}
	
	/**
	 * Converts this matrix as its transpose matrix
	 * @return transpose matrix
	 */
	public Matrix4 transpose()
	{
	  Matrix4 aux = new Matrix4();
	  for(int i=0;i<4;i++)
	    for(int j=0;j<4;j++)
	      aux.mm[i][j]=mm[j][i];
	  assign(aux);
	  return this;  
	}

	/**
	 * Gets transpose of this matrix
	 * @param mat matrix that holds transpose of this matrix
	 */
	public void getTranspose(Matrix4 mat) 
	{ 
	  for(int i=0;i<4;i++)
	    for(int j=0;j<4;j++)
	      mat.mm[i][j]=mm[j][i];    
	}
	
	/**
	 * Multiplies this matrix with another matrix
	 * @param mat second matrix in the multiplication
	 * @return matrix holding multiplication result
	 */
	public Matrix4 multiply(Matrix4 mat)
	{
	 Matrix4 maux = new Matrix4();  
	  for(int i = 0; i < 4; i++)  
	  {
	    for(int j = 0; j <4; j++) 
	    {
	      float sum = 0;
	      for(int k = 0; k < 4; k++)  
	        sum += mm[i][k] * mat.mm[k][j];
	      maux.mm[i][j] = sum;
	    }
	  }
	  return maux;
	}

	/**
	 * Multiplies this matrix by a number
	 * @param a number with which to multiply
	 * @return matrix holding multiplication result
	 */
	public Matrix4 multiply(float a)
	{
	  Matrix4 m = new Matrix4(this);
	  for(int i=0;i<4;i++)
		  for (int j=0; j < 4; j++)
			  m.mm[i][j]*=a;
	  return m;
	}
	
	/**
	 * Multiples this matrix by a point
	 * @param point point with which to multiply
	 * @return resultant point
	 */
	public Point3D multiply(Point3D point)  
	{
	  Point3D newPoint = new Point3D();
	  newPoint.x = mm[0][0]*point.x + mm[0][1]*point.y + mm[0][2]*point.z + mm[0][3];
	  newPoint.y = mm[1][0]*point.x + mm[1][1]*point.y + mm[1][2]*point.z + mm[1][3];
	  newPoint.z = mm[2][0]*point.x + mm[2][1]*point.y + mm[2][2]*point.z + mm[2][3];
	  
	  return newPoint;
	}
	
	/**
	 * Multiplies this matrix by a point
	 * @param point point with which to multiply
	 * @param result point holding the result
	 */
	public void multiply(Point3D point, Point3D result) {
		result.x = mm[0][0]*point.x + mm[0][1]*point.y + mm[0][2]*point.z + mm[0][3];
		result.y = mm[1][0]*point.x + mm[1][1]*point.y + mm[1][2]*point.z + mm[1][3];
		result.z = mm[2][0]*point.x + mm[2][1]*point.y + mm[2][2]*point.z + mm[2][3];
	}

	/**
	 * Adds this matrix to another matrix
	 * @param mat second matrix in the addition
	 * @return matrix holding the result of addition
	 */
	public Matrix4 add(Matrix4 mat)
	{
	  Matrix4 M = new Matrix4();  
	  for(int i = 0; i < 4; i++)  
	    for(int j = 0; j < 4; j++)
	      M.mm[i][j] = mm[i][j] + mat.mm[i][j];
	  return M;
	}

	/**
	 * Subtracts a matrix from this matrix
	 * @param mat matrix to be subtracted 
	 * @return resultant matrix
	 */
	public Matrix4 subtract(Matrix4 mat)
	{
	  Matrix4 m = new Matrix4();
	  for(int i = 0; i < 4; i++)  
	    for(int j = 0; j < 4; j++)
	      m.mm[i][j] = mm[i][j] - mat.mm[i][j];
	  return m;  
	}
	
	/**
	 * Checks if a matrix equals this matrix
	 * @param mat matrix to be compared
	 * @return true if equal, false otherwise
	 */
	public boolean equals(Matrix4 mat)
	{
	  for(int i = 0; i < 4; i++)
		  for (int j = 0; j < 4; j++)
			  if(mm[i][j] != mat.mm[i][j])
	      return false;
	  return true;
	}
	
	/**
	 * Inverts this matrix
	 * @return inverted matrix
	 */
	public Matrix4 invert()
	{
	  Matrix4 mr = new Matrix4();
	  float mdet = getDeterminant();
	  Matrix3 mtemp;
	  int i, j, sign;
	  if(Math.abs(mdet)< 0.0005)
	    return mr.loadIdentity();
	    
	  for ( i = 0; i < 4; i++ )
	    for ( j = 0; j < 4; j++ )
	    {
	      sign = 1 - ( (i +j) % 2 ) * 2;
	      mtemp = subMatrix(i,j);      
	      mr.mm[j][i] = ( mtemp.getDeterminant() * sign ) / mdet;
	    }
	  assign(mr);
	  return this;
	}
	
	/**
	 * Gets inverse of this matrix 
	 * @param mr resultant matrix
	 */
	public void getInverse(Matrix4 mr) 
	{  
	  float mdet = getDeterminant();
	  Matrix3 mtemp;
	  int i, j, sign;
	  if(Math.abs(mdet)< 0.0005)
	  {
	    mr.loadIdentity();
	    return;
	  }
	    
	  for ( i = 0; i < 4; i++ )
	    for ( j = 0; j < 4; j++ )
	    {
	      sign = 1 - ( (i +j) % 2 ) * 2;
	      mtemp = subMatrix(i,j);      
	      mr.mm[j][i] = ( mtemp.getDeterminant() * sign ) / mdet;
	    }    
	}

	/**
	 * Returns determinant of this matrix
	 * @return determinant
	 */
	public float getDeterminant() 
	{
	  float det,result=0,i=1;
	  Matrix3 msub3;
	  int n;
	  for(n=0;n<4;n++,i*=-1)
	  {
	    msub3=subMatrix(0, n );
	    det = msub3.getDeterminant();
	    result += mm[0][n]*det*i;
	  }
	  return result;
	}

	/**
	 * Returns a 3x3 submatrix of this matrix
	 * @param i matrix row to be ignored while creating a submatrix 
	 * @param j matrix column to be ignored while creating a submatrix
	 * @return submatrix
	 */
	public Matrix3 subMatrix(int i, int j ) 
	{
	  Matrix3 mb = new Matrix3();
		int di, dj, si, sj;
		for( di = 0; di < 3; di ++ ) 
		{
		  for( dj = 0; dj < 3; dj ++ ) 
		  {	    
		    si = di + ((di >= i) ? 1 : 0 );
		    sj = dj + ((dj >= j) ? 1 : 0 );	    
		    mb.mm[di][dj] = mm[si][sj];
		  }
		}
	  return mb;
	}
	
	/**
	 * Checks if this matrix is symmetric m[i][j] = m[j][i] 
	 * @return true if symmetric, false otherwise
	 */
	public boolean isSymetric()
	{
	  for(int i=0;i<4;i++)
	    for(int j=0;j<4;j++)      
	      if(mm[i][j]!=mm[j][i])
	        return false;
	  return true;

	}
	
	/**
	 * Checks if this matrix is a identity matrix
	 * @return true if identity, false otherwise
	 */
	public boolean isIdentity() 
	{
	  for(int i=0;i<4;i++)
	    for(int j=0;j<4;j++)      
	    {
	      if(i==j)        
	      {
	        if(mm[i][j]!=1)
	          return false;
	      }
	      else
	        if(mm[i][j]!=0)
	          return false;
	    }
	  return true;

	}

	/**
	 * Checks if this is a diagonal matrix
	 * @return true if diagonal, false otherwise
	 */
	public boolean isDiagonal() 
	{
	  for(int i=0;i<4;i++)
	    for(int j=0;j<4;j++)      
	    {
	      if(i!=j)              
	        if(mm[i][j]!=0)
	          return false;
	    }
	  return true;

	}

	/**
	 * Checks if this matrix is invertible. The matrix is considered to be non-invertible if its determinant is smaller than some epsilon value 
	 * @return true if it is invertible, false otherwise
	 */
	public boolean isInvertible() 
	{
	  float det=getDeterminant();
	  if(Math.abs(det)< 0.000005f)
	    return false;
	  else
	    return true;
	}
	

	/**
	 * Returns negative of this matrix
	 * @return negative matrix
	 */
	public Matrix4 negate()
	{
		Matrix4 negMat = new Matrix4();
		for(int i = 0; i < 4; i++)
			for(int j = 0; j < 4; j++)
				negMat.mm[i][j] = -mm[i][j];

		return negMat;
	}
}

/**
 * This class encapsulates a 3x3 matrix holding float values.
 * 
 * @author <a href='deepali.bhagvat@asu.edu'>Deepali Bhagvat</a>
 * 
 */
class Matrix3
{
	/**
	 * A 2D array holding values in this matrix
	 */	
	float[][] mm = new float[3][3];
	
	/**
	 * Returns determinant of this matrix
	 * @return determinant
	 */
	public float getDeterminant() {
		return (mm[0][0] * (mm[1][1]*mm[2][2] - mm[2][1]*mm[1][2]) - mm[0][1] * (mm[1][0]*mm[2][2] - mm[2][0]*mm[1][2]) + mm[0][2] * (mm[1][0]*mm[2][1] - mm[2][0]*mm[1][1]));
	}
}