package orion.sdk.graphics.drawables.primitives;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLException;

import orion.sdk.graphics.drawables.IDrawable;
import orion.sdk.graphics.util.OpenGLContext;
import orion.sdk.graphics.util.OpenGLManager;
import orion.sdk.math.FloatMatrix;
import orion.sdk.math.geometry.Box;


public class WireBox implements IDrawable
{
	public Box bounds;
	public String name;
	public FloatMatrix color;
	
	protected boolean visible = true;
	
	public WireBox(String name, Box bounds, FloatMatrix color)
	{
		this.bounds = bounds;
		this.name = name;
		this.color = color;
	}
	
	@Override
	public void draw(OpenGLContext c) throws GLException
	{
		OpenGLManager.getInstance().pushDebug("Drawing", this);
		
		/*
		 * Draw the box
		 */
		c.gl().glBegin(GL2.GL_LINES);
		
			/*
			 * Box color
			 */
			c.gl().glColor3f(color.getX(), color.getY(), color.getZ());
			
			/*
			 * Box geometry
			 */
			c.gl().glVertex3f(bounds.getLeft(), bounds.getTop(), bounds.getFront());
			c.gl().glVertex3f(bounds.getRight(), bounds.getTop(), bounds.getFront());

			c.gl().glVertex3f(bounds.getLeft(), bounds.getBottom(), bounds.getFront());
			c.gl().glVertex3f(bounds.getRight(), bounds.getBottom(), bounds.getFront());			
			
			c.gl().glVertex3f(bounds.getLeft(), bounds.getTop(), bounds.getBack());
			c.gl().glVertex3f(bounds.getRight(), bounds.getTop(), bounds.getBack());

			c.gl().glVertex3f(bounds.getLeft(), bounds.getBottom(), bounds.getBack());
			c.gl().glVertex3f(bounds.getRight(), bounds.getBottom(), bounds.getBack());

			c.gl().glVertex3f(bounds.getLeft(), bounds.getTop(), bounds.getFront());
			c.gl().glVertex3f(bounds.getLeft(), bounds.getTop(), bounds.getBack());
			
			c.gl().glVertex3f(bounds.getRight(), bounds.getTop(), bounds.getFront());
			c.gl().glVertex3f(bounds.getRight(), bounds.getTop(), bounds.getBack());

			c.gl().glVertex3f(bounds.getLeft(), bounds.getBottom(), bounds.getFront());
			c.gl().glVertex3f(bounds.getLeft(), bounds.getBottom(), bounds.getBack());
			
			c.gl().glVertex3f(bounds.getRight(), bounds.getBottom(), bounds.getFront());
			c.gl().glVertex3f(bounds.getRight(), bounds.getBottom(), bounds.getBack());

			c.gl().glVertex3f(bounds.getLeft(), bounds.getTop(), bounds.getFront());
			c.gl().glVertex3f(bounds.getLeft(), bounds.getBottom(), bounds.getFront());

			c.gl().glVertex3f(bounds.getRight(), bounds.getTop(), bounds.getFront());
			c.gl().glVertex3f(bounds.getRight(), bounds.getBottom(), bounds.getFront());

			c.gl().glVertex3f(bounds.getRight(), bounds.getTop(), bounds.getBack());
			c.gl().glVertex3f(bounds.getRight(), bounds.getBottom(), bounds.getBack());

			c.gl().glVertex3f(bounds.getLeft(), bounds.getTop(), bounds.getBack());
			c.gl().glVertex3f(bounds.getLeft(), bounds.getBottom(), bounds.getBack());

		c.gl().glEnd();
		
		OpenGLManager.getInstance().popDebug();
	}

	@Override
	public void upload(OpenGLContext c) throws GLException
	{
		
	}

	@Override
	public void release(OpenGLContext c) throws GLException
	{
		
	}
	
	@Override
	public boolean isReady()
	{
		return true;
	}

	@Override
	public int getFaceCount()
	{
		return 0;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public Box getBounds()
	{
		return bounds;
	}
	
	@Override
	public boolean isVisible()
	{
		return visible;
	}
}
