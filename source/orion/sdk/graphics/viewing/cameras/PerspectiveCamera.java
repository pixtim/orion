package orion.sdk.graphics.viewing.cameras;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.gl2.GLUgl2;

import orion.sdk.graphics.util.OpenGLContext;

public class PerspectiveCamera extends ACamera
{
	public float fieldOfView = 60;
	public float near = 0.1f;
	public float far = 20f;

	public PerspectiveCamera(String name)
	{
		super(name);
	}

	
	@Override
	public void apply(OpenGLContext c)
	{
		if (c.viewport[3] != 0)
		{
			float aspectRatio = c.viewport[2] / c.viewport[3];
			
			c.gl().glMatrixMode(GL2.GL_PROJECTION);
			c.glu().gluPerspective(fieldOfView, aspectRatio, near, far);
			
			c.gl().glMatrixMode(GL2.GL_MODELVIEW);
			c.glu().gluLookAt(
					observer.getX(), observer.getY(), observer.getZ(),
					target.getX(), target.getY(), target.getZ(),
					up.getX(), up.getY(), up.getZ());
		}
		
		super.apply(c);
	}

	@Override
	public PerspectiveCamera clone() throws CloneNotSupportedException
	{
		PerspectiveCamera clone = new PerspectiveCamera(this.name);
		clone.observer = observer.clone();
		clone.target = target.clone();
		clone.up = up;
		clone.fieldOfView = fieldOfView;
		clone.near = near;
		clone.far = far;
		return clone;
	}
}
