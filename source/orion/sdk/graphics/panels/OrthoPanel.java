package orion.sdk.graphics.panels;

import javax.media.opengl.GL2;
import javax.media.opengl.GLException;

import orion.sdk.graphics.util.OpenGLContext;
import orion.sdk.graphics.util.OpenGLManager;
import orion.sdk.graphics.util.OpenGLStack;
import orion.sdk.graphics.viewing.cameras.ACamera;
import orion.sdk.graphics.viewing.cameras.OrthographicCamera;
import orion.sdk.math.FloatMatrix;
import orion.sdk.math.geometry.Box;
import orion.sdk.math.geometry.Vertex;
import orion.sdk.monitoring.incidents.IncidentManager;

public class OrthoPanel extends APanel
{
	protected OrthographicCamera camera = null;
	
	protected float left = 0f;
	protected float top = 0f;
	protected float width = 1f;
	protected float height = 1f;
	
	protected Vertex[] vertices = new Vertex[4];
	private boolean relative = true;
	
	public OrthoPanel(String name, boolean relative, OrthographicCamera camera)
	{
		super(name);
		this.setRelative(relative);
		this.camera = camera;
	}
	
	public OrthoPanel(String name, OrthographicCamera camera)
	{
		this(name, true, camera);
	}

	protected boolean isRelative()
	{
		return relative;
	}

	protected void setRelative(boolean relativePositioned)
	{
		this.relative = relativePositioned;
	}
	
	protected float getLeft(OpenGLContext c)
	{
		if (this.isRelative())
		{
			return c.viewport[0] + c.viewport[2] * left;
		}
		else
		{
			return c.viewport[0] + left; 
		}
	}
	
	protected float getTop(OpenGLContext c)
	{
		if (this.isRelative())
		{
			return c.viewport[1] + c.viewport[3] * top;
		}
		else
		{
			return c.viewport[1] + top;
		}
	}
	
	protected float getWidth(OpenGLContext c)
	{
		if (this.isRelative())
		{
			return c.viewport[2] * width;
		}
		else
		{
			return width;
		}
	}	
	
	protected float getHeight(OpenGLContext c)
	{
		if (this.isRelative())
		{
			return c.viewport[3] * height;
		}
		else
		{
			return height;
		}
	}
	
	@Override
	public void draw(OpenGLContext c) throws GLException
	{
		OpenGLManager.getInstance().pushDebug("Drawing", this);
		
		OpenGLManager.getInstance().pushDebug("Drawing content", this);
		
		super.draw(c);
		
		OpenGLManager.getInstance().popDebug();	
		
		OpenGLManager.getInstance().pushDebug("Drawing panel", this);
		
		this.camera.bounds.set(0, 0, 0, c.viewport[2], c.viewport[3], 0);
		
		OpenGLStack.push(ACamera.class, camera, c);
		
		this.setCorners(c);
		
		c.gl().glBegin(GL2.GL_QUADS);
					
			for (Vertex vertex : this.vertices)
			{
				vertex.apply(c);
			}
						
		c.gl().glEnd();
		
		OpenGLStack.pop(ACamera.class, camera, c);
		
		OpenGLManager.getInstance().popDebug();	
		
		OpenGLManager.getInstance().popDebug();			
	}
	
	protected void setCorners(OpenGLContext c)
	{
		float
			left = this.getLeft(c),
			top = this.getTop(c),
			width = this.getWidth(c),
			height = this.getHeight(c);
		
		this.vertices[0] = new Vertex();
		this.vertices[0].setPosition(FloatMatrix.vector(left, top, 0, 1));
		this.vertices[0].setTexture(FloatMatrix.vector(0, 1));
		this.vertices[0].setColor(FloatMatrix.vector(1f, 1f, 1f, 1f));
				
		this.vertices[1] = new Vertex();
		this.vertices[1].setPosition(FloatMatrix.vector(left + width, top, 0, 1));
		this.vertices[1].setTexture(FloatMatrix.vector(1, 1));
		this.vertices[1].setColor(FloatMatrix.vector(1f, 1f, 1f, 1f));
		
		this.vertices[2] = new Vertex();
		this.vertices[2].setPosition(FloatMatrix.vector(left + width, top + height, 0, 1));
		this.vertices[2].setTexture(FloatMatrix.vector(1, 0));
		this.vertices[2].setColor(FloatMatrix.vector(1f, 1f, 1f, 1f));
		
		this.vertices[3] = new Vertex();
		this.vertices[3].setPosition(FloatMatrix.vector(left, top + height, 0, 1));
		this.vertices[3].setTexture(FloatMatrix.vector(0, 0));
		this.vertices[3].setColor(FloatMatrix.vector(1f, 1f, 1f, 1f));
	}

	@Override
	public int getFaceCount()
	{
		return 1;
	}

	@Override
	public Box getBounds()
	{
		return null;
	}
}
