package orion.sdk.graphics.util;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.gl2.GLUgl2;

public class OpenGLContext
{
	private GL2 glHandle = null;
	private GLUgl2 gluHandle= null;
	public float dt = -1;
	public float[] viewport = null;

	public OpenGLContext(GL2 gl, GLUgl2 glu, float dt, float[] viewport)
	{
		this.glHandle = gl;
		this.gluHandle = glu;
		this.dt = dt;
		this.viewport = viewport;
	}

	public OpenGLContext copy()
	{
		OpenGLContext clone = new OpenGLContext(glHandle, gluHandle, dt, viewport);
		return clone;
	}

	public GL2 gl()
	{
		OpenGLManager.getInstance().addCallStack();		
		return glHandle;
	}

	public GLUgl2 glu()
	{
		OpenGLManager.getInstance().addCallStack();
		return gluHandle;
	}
}
