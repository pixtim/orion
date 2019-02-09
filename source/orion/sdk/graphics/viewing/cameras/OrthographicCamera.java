package orion.sdk.graphics.viewing.cameras;

import com.jogamp.opengl.GL2;

import orion.sdk.graphics.util.OpenGLContext;
import orion.sdk.math.geometry.Box;

public class OrthographicCamera extends ACamera
{
	public float near = 0.1f;
	public float far = 200f;
	public Box bounds = new Box(-10f, -10f, -10f, 20f, 20f, 20f);
	
	public OrthographicCamera(String name)
	{
		super(name);
	}
	
	@Override
	public void apply(OpenGLContext c)
	{
		c.gl().glMatrixMode(GL2.GL_PROJECTION);
		c.gl().glOrtho(bounds.getLeft(), bounds.getRight(), bounds.getTop(), bounds.getBottom(), near, far);
				
		c.gl().glMatrixMode(GL2.GL_MODELVIEW);
		c.glu().gluLookAt(
				observer.getX(), observer.getY(), observer.getZ(),
				target.getX(), target.getY(), target.getZ(),
				up.getX(), up.getY(), up.getZ());
		
		super.apply(c);
	}

	@Override
	public OrthographicCamera clone() throws CloneNotSupportedException
	{
		OrthographicCamera clone = new OrthographicCamera(this.name);
		clone.observer = observer.clone();
		clone.target = target.clone();
		clone.up = up;
		clone.bounds = bounds;
		clone.near = near;
		clone.far = far;
		return clone;
	}
}
