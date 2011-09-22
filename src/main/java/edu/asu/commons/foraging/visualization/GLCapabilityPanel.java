package edu.asu.commons.foraging.visualization;

import java.awt.BorderLayout;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.swing.JButton;
import javax.swing.JPanel;

import edu.asu.commons.foraging.ui.GameWindow3D;

@SuppressWarnings("serial")
public class GLCapabilityPanel extends JPanel implements GLEventListener{
	
	private GLCanvas glCanvas;
	
	public GLCapabilityPanel() {
		glCanvas = new GLCanvas(new GLCapabilities());
				
		//Add it to the view
		setLayout(new BorderLayout());
		add(glCanvas, BorderLayout.CENTER);
		add(new JButton("Test"), BorderLayout.NORTH);
		
		glCanvas.addGLEventListener(this);
		
//		GLContext context = glCanvas.createContext(null);
//		GL gl = context.getGL();
//		checkExtensionSupport(gl);
	}
	
	public void update() {
		glCanvas.repaint();
	}
	
	private void checkExtensionSupport(GL gl) {
		String versionStr = gl.glGetString( GL.GL_VERSION );
		System.out.println("GL version: " + versionStr); 
		versionStr = versionStr.substring( 0, 4);
		
		// Check if extensions are available.
		if (gl.isExtensionAvailable("GL_ARB_vertex_program") && gl.isExtensionAvailable("GL_ARB_fragment_program")
			&& gl.isFunctionAvailable("glVertexAttribPointer") ) {	
			GameWindow3D.featureSupported = GameWindow3D.VideoCardSupport.SHADER_SUPPORT;			
		}
		else if (gl.isExtensionAvailable("GL_ARB_vertex_buffer_object")) {	//VBO support
//			  gl.isFunctionAvailable("glGenBuffersARB") &&
//            gl.isFunctionAvailable("glBindBufferARB") &&
//            gl.isFunctionAvailable("glBufferDataARB") &&
//            gl.isFunctionAvailable("glDrawArrays") &&
//            gl.isFunctionAvailable("glVertexPointer") &&
//            gl.isFunctionAvailable("glDeleteBuffersARB")
			GameWindow3D.featureSupported = GameWindow3D.VideoCardSupport.VBO_SUPPORT;
		}
		else if (gl.isExtensionAvailable("GL_EXT_vertex_array")) {
			GameWindow3D.featureSupported = GameWindow3D.VideoCardSupport.VERTEX_ARRAY_SUPPORT;
		}
		//GLDrawableFactory.canCreateGLPbuffer()
		else {
			throw new RuntimeException("Vertex array not supported. No billboard support yet.");
		}
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		checkExtensionSupport(drawable.getGL());		
	}

	public void display(GLAutoDrawable drawable) {
		// TODO Auto-generated method stub
		
	}

	public void displayChanged(GLAutoDrawable drawable, boolean arg1, boolean arg2) {
		// TODO Auto-generated method stub
		
	}
	
	public void reshape(GLAutoDrawable drawable, int arg1, int arg2, int arg3, int arg4) {
		// TODO Auto-generated method stub
		
	}
}
