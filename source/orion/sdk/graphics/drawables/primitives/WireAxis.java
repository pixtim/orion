package orion.sdk.graphics.drawables.primitives;

import javax.media.opengl.GL2;
import javax.media.opengl.GLException;

import orion.sdk.graphics.drawables.IDrawable;
import orion.sdk.graphics.shading.glsl.AShader;
import orion.sdk.graphics.shading.glsl.ShaderManager;
import orion.sdk.graphics.util.OpenGLContext;
import orion.sdk.graphics.util.OpenGLManager;
import orion.sdk.math.FloatMatrix;
import orion.sdk.math.geometry.Box;
import orion.sdk.math.geometry.Vertex;


public class WireAxis implements IDrawable
{
	private float lineWidth = 3f;
	private Vertex center = null;
	private Vertex up = null;
	private Vertex front = null;
	private Vertex right = null;
	
	public WireAxis(float size, float lineWidth)
	{
		this.lineWidth = lineWidth;
		
		this.center = new Vertex();
		this.center.setPosition(FloatMatrix.vector(0, 0, 0, 1));
		this.center.setColor(FloatMatrix.vector(1, 1, 1, 1));
		
		this.right = new Vertex();
		this.right.setPosition(FloatMatrix.vector(1, 0, 0, 1));
		this.right.setColor(FloatMatrix.vector(1, 0, 0, 1));
		
		this.up = new Vertex();
		this.up.setPosition(FloatMatrix.vector(0, 1, 0, 1));
		this.up.setColor(FloatMatrix.vector(0, 1, 0, 1));
		
		this.front = new Vertex();
		this.front.setPosition(FloatMatrix.vector(0, 0, 1, 1));
		this.front.setColor(FloatMatrix.vector(0, 0, 1, 1));
	}
	
	public WireAxis()
	{
		this(1, 1);
	}
	
	@Override
	public void draw(OpenGLContext c) throws GLException
	{
		OpenGLManager.getInstance().pushDebug("Drawing", this);
		c.gl().glLineWidth(this.lineWidth);
		c.gl().glBegin(GL2.GL_LINES);
			this.center.setColor(FloatMatrix.vector(1, 0, 0, 1));
			this.center.apply(c);
			this.right.apply(c);
			
			this.center.setColor(FloatMatrix.vector(0, 1, 0, 1));
			this.center.apply(c);
			this.up.apply(c);
			
			this.center.setColor(FloatMatrix.vector(0, 0, 1, 1));
			this.center.apply(c);
			this.front.apply(c);
		c.gl().glEnd();
		c.gl().glLineWidth(1f);		
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
	public int getFaceCount()
	{
		return 0;
	}

	@Override
	public String getName()
	{
		return "axis";
	}
	
	@Override
	public boolean isReady()
	{
		return true;
	}

	@Override
	public Box getBounds()
	{
		return null;
	}
	
	@Override
	public boolean isVisible()
	{
		return true;
	}
}
