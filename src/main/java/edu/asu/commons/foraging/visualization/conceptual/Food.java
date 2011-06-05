package edu.asu.commons.foraging.visualization.conceptual;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

import edu.asu.commons.foraging.graphics.Point3D;
import edu.asu.commons.foraging.graphics.RGBA;
import edu.asu.commons.foraging.graphics.Texture;
import edu.asu.commons.foraging.graphics.Vector3D;
import edu.asu.commons.foraging.model.Resource;

public class Food {
    public static int maxAge = 0;

    public static int oldAge = 0;

    public static float radius = 0;

    public static float initialHeight = 0;

    public static float heightIncrement = 0;

    public static RGBA color = null;

    public static RGBA oldAgeColor = null;

    public static RGBA selectedColor = null;
    
    public static RGBA selectedByOthersColor = null;

    public static RGBA specular = null;
    
    public static float shininess = 0.0f;

    public static Texture texture = null;

//    public static Texture coinTexture = null;

    public static GLUquadric quadric = null;

    protected Point3D position = null;

    protected int displayListID = -1;

    protected int hitCounter = 0;

    protected boolean harvested = false;
    
    private boolean selected = false;
    
    private boolean selectedByOthers = false;
    
    private boolean displayListDirty = true;

    // protected BoundingBox boundingBox = null;
    protected AbstractView parentView = null;

    private Resource resource;

    private static final int slices = 24; // The number of subdivisions around
                                            // the z-axis.

    private static final int stacks = 8; // The number of subdivisions along
                                            // the z-axis.

//    private static final float coinHeapRadiusFactor = 1.0f;
//    private static final float coinHeapHeightFactor = 1.5f;

    public Food(Point3D position, Resource resource, AbstractView parentView) {
        this.position = position;
        this.resource = resource;
        // System.out.println("Age is " + age);
        // System.out.println("Height should be " + age*Food.heightIncrement);
        this.parentView = parentView;
    }

//    public void updateAge(int delta) {
//        if (resource.getAge() < maxAge && !harvested) {
//            resource.updateAge(delta);
//            displayListDirty = true;
//        }
//    }

    public void setAge(int age) {
        if (resource.getAge() != age && !harvested) {
            resource.setAge(age);
            displayListDirty = true;
        }
    }

    public void display(GLAutoDrawable drawable) {
    	int age = resource.getAge();
    	if (age == 0) return;
    	
        final GL gl = drawable.getGL();
        final GLU glu = new GLU();

        gl.glPushMatrix();
        gl.glTranslatef(position.x, position.y, position.z);
        gl.glRotatef(-90, 1, 0, 0);

        //if (displayListDirty) {
            if (quadric == null) {
                quadric = glu.gluNewQuadric();
                glu.gluQuadricNormals(quadric, GLU.GLU_SMOOTH);
            }
            if (!harvested) {
                // Display food as a cylinder
                //if (displayListID != -1) {
                //    gl.glDeleteLists(displayListID, 1);
		//	System.out.println("Deleting the earlier display list");                            
                //}
                //displayListDirty = false;
                //displayListID = gl.glGenLists(1);
                //gl.glNewList(displayListID, GL.GL_COMPILE_AND_EXECUTE);

                RGBA youngColor = selected ? Food.selectedColor : selectedByOthers ? Food.selectedByOthersColor : Food.color;

                gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT_AND_DIFFUSE, youngColor.getfv(), 0);
                gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_SPECULAR, Food.specular.getfv(), 0);
                gl.glMaterialf(GL.GL_FRONT_AND_BACK, GL.GL_SHININESS, shininess);
                // Apply texture, if any

                if (age > Food.oldAge) {
                    // Display young part
                    float youngPartHeight = Food.initialHeight + Food.oldAge
                            * Food.heightIncrement;
                    glu.gluCylinder(quadric, Food.radius, Food.radius,
                            youngPartHeight, slices, stacks);

                    // Display old part
                    gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT_AND_DIFFUSE, Food.oldAgeColor.getfv(), 0);
                    gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_SPECULAR, Food.specular.getfv(), 0);
                    gl.glMaterialf(GL.GL_FRONT_AND_BACK, GL.GL_SHININESS, shininess);
                    
                    // Apply texture, if any
                    gl.glTranslatef(0, 0, youngPartHeight);
                    float oldPartHeight = (age - Food.oldAge)
                            * Food.heightIncrement;
                    glu.gluCylinder(quadric, Food.radius, Food.radius,
                            oldPartHeight, slices, stacks);

                    gl.glTranslatef(0, 0, oldPartHeight);
                    glu.gluDisk(quadric, 0, Food.radius, 16, 8);
                }
                else {
                    float height = Food.initialHeight + age
                            * Food.heightIncrement;
                    glu.gluCylinder(quadric, Food.radius, Food.radius, height,
                            slices, stacks);

                    gl.glTranslatef(0, 0, height);
                    glu.gluDisk(quadric, 0, Food.radius, slices, stacks);
                }
                //gl.glEndList();
            }
            ////else {
                //coinTexture.create(drawable);
               //// gl.glDeleteLists(displayListID, 1);
//                displayListID = gl.glGenLists(1);
//                gl.glNewList(displayListID, GL.GL_COMPILE_AND_EXECUTE);
//
//                coinTexture.load(gl);
//                glu.gluQuadricTexture(quadric, true);
//                gl.glEnable(GL.GL_TEXTURE_2D);
//
//                float[] color = { 1.0f, 1.0f, 1.0f, 1.0f };
//                gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT_AND_DIFFUSE, color, 0);
//                gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_SPECULAR, color, 0);
//
//                float baseRadius = getCoinHeapRadius(age);
//                glu.gluCylinder(quadric, baseRadius, 0.0f, baseRadius
//                        * coinHeapHeightFactor, slices, stacks);
//                gl.glDisable(GL.GL_TEXTURE_2D);
//
//                gl.glEndList();
            ////}
        //} 
        //else {
        //    gl.glCallList(displayListID);
        //}
        gl.glPopMatrix();

        //boundingBox.display(drawable);
    }
    
//    private float getCoinHeapRadius(int age) {
//    	return age > 8 ? 8 * coinHeapRadiusFactor : age * coinHeapRadiusFactor;
//    }

    /**
     * Returns true when a hit harvests a pillar.
     * @return
     */
    public boolean hit() {
        if (!harvested) {
            hitCounter++;
            //If pillar is hit by the agent sufficient no. of times, collapse it 
            if (hitCounter == resource.getAge()) {
                harvested = true;
                displayListDirty = true;
                return true;
            }
        }
        return false;
    }
    
    public Point3D getPosition() {
        return position;
    }

    public float getRadius() {
//        if (harvested) {
//            return getCoinHeapRadius(resource.getAge());
//        }
        return Food.radius;
    }

    public int getAge() {
    	return resource.getAge();
    }
    
    public void setSelected(boolean flag) {
        selected = flag;
        displayListDirty = true;
    }

    public Resource getResource() {
        return resource;
    }

    public boolean wasHit() {
        return hitCounter > 0 || harvested;
    }
    
    public int getHitCounter() {
    	return hitCounter;
    }

    public void setLocked(boolean locked) {
        this.selectedByOthers = locked;
        displayListDirty = true;
    }

    public void toggleLocked() {
        selectedByOthers = !selectedByOthers;
        displayListDirty = true;
    }

    public boolean isLocked() {
        return selectedByOthers;
    }
    
    public boolean isHarvested() {
    	return harvested;
    }
    
    public boolean isIntersecting(Point3D agentPosition, float agentRadius) {
    	//Check if distance between food center and agent center is < (food radius + agent radius)
    	float distance = new Vector3D(position, agentPosition).length();    	
    	if (distance <= getRadius()+agentRadius)
    		return true;
    	
    	return false;
    }
    
    public boolean isCloseBy(Point3D agentPosition, float agentRadius, float closeDistanceLimit) {
    	//Check if distance between food center and agent center is < (food radius + agent radius)
    	float distance = new Vector3D(position, agentPosition).length();
    	if (distance <= radius+agentRadius+closeDistanceLimit)
    		return true;
    	
    	return false;
    }
    
    public String toString()
    {
    	return String.format("Pillar: [%.2f, %.2f] Resource: %s", position.x, position.z, resource.toString());
    }
}
