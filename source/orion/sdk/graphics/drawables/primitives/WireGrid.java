package orion.sdk.graphics.drawables.primitives;

import java.util.LinkedList;
import java.util.List;

import javax.media.opengl.GL2;
import javax.media.opengl.GLException;

import orion.sdk.graphics.drawables.IDrawable;
import orion.sdk.graphics.shading.glsl.AShader;
import orion.sdk.graphics.shading.glsl.ShaderManager;
import orion.sdk.graphics.shading.lighting.Material;
import orion.sdk.graphics.util.OpenGLContext;
import orion.sdk.graphics.util.OpenGLManager;
import orion.sdk.graphics.util.OpenGLStack;
import orion.sdk.math.FloatMatrix;
import orion.sdk.math.geometry.Box;
import orion.sdk.math.geometry.Vertex;


public class WireGrid implements IDrawable
{
	public float lineWidth = 3f;
	private List<Vertex> vertices = new LinkedList<Vertex>();
	
	public WireGrid(float size, int resolution, float lineWidth, FloatMatrix color)
	{
		this.lineWidth = lineWidth;
		
		float hs = size / 2;
		for (int i = 0; i <= resolution; i++)
		{
			float alpha = i / (float) resolution;
			float p = -hs + alpha * size;
			
			Vertex horizontalStart = new Vertex();
			horizontalStart.setPosition(FloatMatrix.vector(-hs, 0, p));
			horizontalStart.setColor(color);
			Vertex horizontalEnd = new Vertex();
			horizontalEnd.setPosition(FloatMatrix.vector( hs, 0, p));
			horizontalEnd.setColor(color);
			
			Vertex verticleStart = new Vertex();
			verticleStart.setPosition(FloatMatrix.vector(p, 0, -hs));
			verticleStart.setColor(color);
			Vertex verticleEnd = new Vertex();
			verticleEnd.setPosition(FloatMatrix.vector(p, 0, hs));
			verticleEnd.setColor(color);
			
			vertices.add(horizontalStart);
			vertices.add(horizontalEnd);
			vertices.add(verticleStart);
			vertices.add(verticleEnd);			
		}		
	}
	
	public WireGrid(float size, int resolution, float lineWidth)
	{
		this(size, resolution, lineWidth, FloatMatrix.vector(0.4f, 0.4f, 0.4f, 1f));
	}
	
	@Override
	public void draw(OpenGLContext c) throws GLException
	{
		OpenGLManager.getInstance().pushDebug("Drawing", this);
		
		FloatMatrix clearColor = OpenGLManager.getInstance().getGLVector4(c, GL2.GL_COLOR_CLEAR_VALUE);
		
		c.gl().glLineWidth(this.lineWidth);		
		c.gl().glBegin(GL2.GL_LINES);
			for (Vertex vertex : this.vertices)
			{
				vertex.apply(c);
			}
		c.gl().glEnd();
		c.gl().glLineWidth(1);
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
		return "grid";
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
