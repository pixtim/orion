package orion.sdk.graphics.drawables.surfaces;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLException;

import orion.sdk.graphics.buffers.VertexBuffer;
import orion.sdk.graphics.buffers.VertexFormatter;
import orion.sdk.graphics.drawables.IDrawable;
import orion.sdk.graphics.shading.glsl.AShader;
import orion.sdk.graphics.shading.glsl.ShaderManager;
import orion.sdk.graphics.util.OpenGLContext;
import orion.sdk.graphics.util.OpenGLManager;
import orion.sdk.math.FloatMatrix;
import orion.sdk.math.IFloatMatrix;
import orion.sdk.math.geometry.Box;

/**
 * Represents a mesh of triangles with {@code 4f} postitions, {@code 4f} colors and {@code 3f}
 * normals.
 * @author Tim
 * @since 1.0.00
 */
public class Mesh implements IDrawable
{
	public final static IFloatMatrix DEFAULT_CENTER = FloatMatrix.vector(0f, 0f, 0f, 1f);
	public final static IFloatMatrix DEFAULT_COLOR = FloatMatrix.vector(1f, 1f, 1f, 1f); 
	public int faceType = 0;
	
	public IFloatMatrix[] positions = null;
	public IFloatMatrix[] colors = null;
	public IFloatMatrix[] normals = null;
	public IFloatMatrix[] textures = null;
	public IFloatMatrix[] centers = null;
	public int[] indexes = new int[0];
	public VertexFormatter vertexFormat = null;
	protected VertexBuffer vertexBuffer = null;
	protected boolean visible = true;
	protected boolean ready = false;
	protected Box bounds = null;
	
	
	public Mesh(int faces, int faceType, Box bounds) throws GLException
	{
		this.faceType = faceType;
		this.setBounds(bounds);
		int size = faces * getVertexPerFaceCount();
		indexes = new int[size];
		positions = new IFloatMatrix[size];
		colors = new IFloatMatrix[size];
		normals = new IFloatMatrix[size];
		textures = new IFloatMatrix[size];
		centers = new IFloatMatrix[size];
	}
	
	public void setVertexFormatter(VertexFormatter formatter)
	{
		this.vertexFormat = formatter;
		this.vertexFormat.indexes = indexes;
		this.vertexFormat.setPositions(positions);
		this.vertexFormat.setColors(colors);
		this.vertexFormat.setNormals(normals);
		this.vertexFormat.setTextures(textures);
		this.vertexFormat.setCenters(centers);		
	}
	
	public int getVertexPerFaceCount() throws GLException
	{
		switch (faceType)
		{
			case FaceType.TRIANGLE:
				return 3;
			case FaceType.QUAD:
				return 4;
			default: 
				throw new GLException("Unknown face type");
		}		
	}
	
	/**
	 * Sets the vertex indexes for the specified face.
	 */
	public void setFace(int faceIndex, int... vertices)
	{
		int size = getVertexPerFaceCount();
		for (int i = 0; i < vertices.length; i++)
		{
			indexes[faceIndex * size + i] = vertices[i];
		}		 
	}
	
	public void setVertex(int index, IFloatMatrix postition, IFloatMatrix normal, IFloatMatrix texture, IFloatMatrix color, IFloatMatrix center)
	{
		positions[index] = postition;
		normals[index] = normal;
		textures[index] = texture;
		colors[index] = color;
		centers[index] = center;
	}
	
	public void setVertex(int index, IFloatMatrix postition, IFloatMatrix normal, IFloatMatrix texture, IFloatMatrix color)
	{
		setVertex(index, postition, normal, texture, color, DEFAULT_CENTER);
	}
	
	public void setVertex(int index, IFloatMatrix postition, IFloatMatrix normal, IFloatMatrix texture)
	{
		setVertex(index, postition, normal, texture, DEFAULT_COLOR);
	}
	
	
	public void setVertex(int index, IFloatMatrix postition, IFloatMatrix normal)
	{
		setVertex(index, postition, normal, null);
	}
	
	public void setVertex(int index, IFloatMatrix postition)
	{
		setVertex(index, postition, null);
	}

	@Override
	public void draw(OpenGLContext c)
	{		
		OpenGLManager.getInstance().pushDebug("Drawing", this);
		
		if (getFaceCount() > 0)
		{
			int drawMode = 0;
			switch (faceType)
			{
				case FaceType.TRIANGLE:
					drawMode = GL2.GL_TRIANGLES;				
					break;
				case FaceType.QUAD:
					drawMode = GL2.GL_QUADS;
					break;
			}
			
			if (vertexBuffer != null)
			{
				vertexBuffer.draw(c, drawMode);
			}
			else
			{
				AShader shader = ShaderManager.getActiveShader();
				
				c.gl().glBegin(drawMode);
				
				for (int i = 0; i < indexes.length; i++)
				{
					int index = indexes[i];
					
					shader.setVertexTexture(textures[index], c);
					shader.setVertexColor(colors[index], c);
					shader.setVertexNormal(normals[index], c);
					shader.setVertexPosition(positions[index], c);
				}
				
				c.gl().glEnd();
			}			
		}
		
		OpenGLManager.getInstance().popDebug();
	}
	
	@Override
	public void upload(OpenGLContext c) throws GLException
	{
		OpenGLManager.getInstance().pushDebug("Uploading", this);
		if (getFaceCount() > 0 && vertexFormat != null)
		{
			if (vertexBuffer != null)
			{
				vertexBuffer.release(c);
			}

			vertexBuffer = new VertexBuffer(this.getName() + "VertexBuffer", vertexFormat, indexes.length);

			OpenGLManager.getInstance().pushDebug("Uploading", vertexBuffer);
			vertexBuffer.upload(c);
			OpenGLManager.getInstance().popDebug();
		}		
		ready = true;
		OpenGLManager.getInstance().popDebug();
	}
	
	@Override
	public void release(OpenGLContext c) throws GLException
	{
		OpenGLManager.getInstance().pushDebug("Releasing", this);
		if (vertexBuffer != null && getFaceCount() > 0)
		{
			vertexBuffer.release(c);
			vertexBuffer = null;
		}
		OpenGLManager.getInstance().popDebug();
	}
	
	public static class FaceType
	{
		public static final int TRIANGLE = 0;
		public static final int QUAD = 1;
	}

	@Override
	public int getFaceCount()
	{
		return indexes.length / getVertexPerFaceCount();
	}

	@Override
	public String getName()
	{
		return "mesh";
	}
	
	@Override
	public boolean isVisible()
	{
		return visible;
	}

	@Override
	public boolean isReady()
	{
		return ready;
	}

	@Override
	public Box getBounds()
	{
		return bounds;
	}

	public void setBounds(Box bounds)
	{
		this.bounds = bounds;
	}
}
