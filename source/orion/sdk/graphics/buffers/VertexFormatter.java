package orion.sdk.graphics.buffers;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GLException;

import orion.sdk.graphics.shading.glsl.GenericShader;
import orion.sdk.graphics.util.INamed;
import orion.sdk.graphics.util.OpenGLContext;
import orion.sdk.graphics.util.OpenGLManager;
import orion.sdk.math.IFloatMatrix;

public class VertexFormatter implements IVertexFormatter, INamed
{
	public int[] indexes = null;
	private IFloatMatrix[] positions = null;
	private IFloatMatrix[] normals = null;
	private IFloatMatrix[] textures = null;
	private IFloatMatrix[] colors = null;
	private IFloatMatrix[] centers = null;
	private GenericShader shader = null;

	public VertexFormatter(String name)
	{
		this(name, null);
	}
	
	public VertexFormatter(String name, GenericShader shader)
	{
		this.setShader(shader);
	}
	
	public int getFloatsPerVertex()
	{
		int floatsPerVertex = getPositionSize();
		if (hasNormal())
		{
			floatsPerVertex = floatsPerVertex + getNormalSize();
		}
		if (hasTexture())
		{
			floatsPerVertex = floatsPerVertex + getTextureSize();
		}
		if (hasColor())
		{
			floatsPerVertex = floatsPerVertex + getColorSize();
		}
		if (hasCenter())
		{
			floatsPerVertex = floatsPerVertex + getCenterSize();
		}
		return floatsPerVertex;
	}
	
	public int getBytesPerVertex()
	{
		return getFloatsPerVertex() * 4;
	}
	
	protected int getNormalOffset()
	{
		int floatsPerVertex = getPositionSize();
		return floatsPerVertex * 4;
	}
	
	public int getTextureOffset()
	{
		int floatsPerVertex = getPositionSize();
		if (hasNormal())
		{
			floatsPerVertex = floatsPerVertex + getNormalSize();
		}
		return floatsPerVertex * 4;
	}
	
	public int getColorOffset()
	{
		int floatsPerVertex = getPositionSize();
		if (hasNormal())
		{
			floatsPerVertex = floatsPerVertex + getNormalSize();
		}
		if (hasTexture())
		{
			floatsPerVertex = floatsPerVertex + getTextureSize();
		}
		return floatsPerVertex * 4;
	}
	
	public int getCenterOffset()
	{
		int floatsPerVertex = getPositionSize();
		if (hasNormal())
		{
			floatsPerVertex = floatsPerVertex + getNormalSize();
		}
		if (hasTexture())
		{
			floatsPerVertex = floatsPerVertex + getTextureSize();
		}
		if (hasCenter())
		{
			floatsPerVertex = floatsPerVertex + getCenterSize();
		}
		return floatsPerVertex * 4;
	}

	
	public void formatVertices(ByteBuffer bytebuffer) throws GLException
	{
		FloatBuffer floatbuffer = bytebuffer.order(ByteOrder.nativeOrder()).asFloatBuffer();
		for (int i = 0; i < indexes.length; i++)
		{
			int j = indexes[i];
			int p = i * getFloatsPerVertex();
			IFloatMatrix position = getPositions()[j];
			floatbuffer.put(p++, position != null ? position.getX() : 0);
			floatbuffer.put(p++, position != null ? position.getY() : 0);
			floatbuffer.put(p++, position != null ? position.getZ() : 0);
			floatbuffer.put(p++, position != null ? position.getU() : 0);
			if (hasNormal())
			{
				IFloatMatrix normal = getNormals()[j]; 
				floatbuffer.put(p++, normal != null ? normal.getX() : 0);
				floatbuffer.put(p++, normal != null ? normal.getY() : 0);
				floatbuffer.put(p++, normal != null ? normal.getZ() : 0);
			}
			if (hasTexture())
			{
				IFloatMatrix texture = getTextures()[j];
				floatbuffer.put(p++, texture != null ? texture.getX() : 0);
				floatbuffer.put(p++, texture != null ? texture.getY() : 0);
				if (texture != null && texture.getLength() >= 3)
				{
					floatbuffer.put(p++, texture != null ? texture.getZ() : 0);
				}
			}
			if (hasColor())
			{
				IFloatMatrix color = getColors()[j];
				floatbuffer.put(p++, color != null ? color.getX() : 0);
				floatbuffer.put(p++, color != null ? color.getY() : 0);
				floatbuffer.put(p++, color != null ? color.getZ() : 0);
				floatbuffer.put(p++, color != null ? color.getU() : 0);
			}
			if (hasCenter())
			{
				IFloatMatrix center = getCenters()[j];
				floatbuffer.put(p++, center != null ? center.getX() : 0);
				floatbuffer.put(p++, center != null ? center.getY() : 0);
				floatbuffer.put(p++, center != null ? center.getZ() : 0);
				floatbuffer.put(p++, center != null ? center.getU() : 0);
			}
		}
	}
	
	/**
	 * Applies the vertex format where the faces are quads
	 */
	@Override
	public void uploadFormat(OpenGLContext c) throws GLException
	{
		OpenGLManager.getInstance().checkError(c);

		/*
		 * TODO Bug: Currently only works with shaders
		 */
		int vertexStride = getFloatsPerVertex() * 4;
		c.gl().glVertexAttribPointer(GenericShader.getPositionLocation(), getPositionSize(), GL.GL_FLOAT, false, vertexStride, 0);
      if (hasNormal())
      {
      	c.gl().glVertexAttribPointer(GenericShader.getNormalLocation(), getNormalSize(), GL.GL_FLOAT, false, vertexStride, getNormalOffset());
      }
      if (hasTexture())
      {  
      	c.gl().glVertexAttribPointer(GenericShader.getTextureLocation(), getTextureSize(), GL.GL_FLOAT, false, vertexStride, getTextureOffset());
      }
      if (hasColor())
      {  
      	c.gl().glVertexAttribPointer(GenericShader.getColorLocation(), getColorSize(), GL.GL_FLOAT, false, vertexStride, getColorOffset());      	
      }		
      if (hasCenter())
      {  
      	c.gl().glVertexAttribPointer(GenericShader.getCenterLocation(), getCenterSize(), GL.GL_FLOAT, false, vertexStride, getCenterOffset());      	
      }		
      
      OpenGLManager.getInstance().checkError(c);
	}
	
	public IFloatMatrix[] getPositions()
	{
		return positions;
	}

	public void setPositions(IFloatMatrix[] positions)
	{
		this.positions = positions;
	}

	public IFloatMatrix[] getNormals()
	{
		return normals;
	}

	public void setNormals(IFloatMatrix[] normals)
	{
		this.normals = normals;
	}

	public IFloatMatrix[] getTextures()
	{
		return textures;
	}

	public void setTextures(IFloatMatrix[] textures)
	{
		this.textures = textures;
	}

	public IFloatMatrix[] getColors()
	{
		return colors;
	}

	public void setColors(IFloatMatrix[] colors)
	{
		this.colors = colors;
	}

	public IFloatMatrix[] getCenters()
	{
		return centers;
	}

	public void setCenters(IFloatMatrix[] centers)
	{
		this.centers = centers;
	}

	public boolean hasNormal()
	{
		GenericShader shader = getShader();
		if (shader != null)
		{
			return getNormals() != null && shader.hasNormal();
		}
		else
		{
			return true;
		}
	}

	public boolean hasTexture()
	{
		GenericShader shader = getShader();
		if (shader != null)
		{
			return getTextures() != null && shader.hasTexture();
		}
		else
		{
			return true;
		}
	}

	public boolean hasColor()
	{
		GenericShader shader = getShader();
		if (shader != null)
		{
			return getColors() != null && shader.hasColor();
		}
		else
		{
			return true;
		}
	}

	public boolean hasCenter()
	{
		GenericShader shader = getShader();
		if (shader != null)
		{
			return getCenters() != null && getShader().hasCenter();
		}
		else
		{
			return false;
		}
	}
	
	public boolean hasPosition()
	{
		return true;
	}

	@Override
	public void setStates(OpenGLContext c) throws GLException
	{
		OpenGLManager.getInstance().checkError(c, this);
		
		if (this.hasPosition())
			c.gl().glEnableVertexAttribArray(GenericShader.getPositionLocation());
		else
			c.gl().glDisableVertexAttribArray(GenericShader.getPositionLocation());
		
		OpenGLManager.getInstance().checkError(c, this);

		if (hasNormal())
			c.gl().glEnableVertexAttribArray(GenericShader.getNormalLocation());
		else
			c.gl().glDisableVertexAttribArray(GenericShader.getNormalLocation());
		
		OpenGLManager.getInstance().checkError(c, this);

		if (hasTexture())
			c.gl().glEnableVertexAttribArray(GenericShader.getTextureLocation());
		else
			c.gl().glDisableVertexAttribArray(GenericShader.getTextureLocation());
		
		OpenGLManager.getInstance().checkError(c, this);

		if (hasColor())
			c.gl().glEnableVertexAttribArray(GenericShader.getColorLocation());
		else
			c.gl().glDisableVertexAttribArray(GenericShader.getColorLocation());
		
		OpenGLManager.getInstance().checkError(c, this);

		if (hasCenter())
			c.gl().glEnableVertexAttribArray(GenericShader.getCenterLocation());
		else
			c.gl().glDisableVertexAttribArray(GenericShader.getCenterLocation());
		
		OpenGLManager.getInstance().checkError(c, this);
	}
	
	public int getPositionSize()
	{
		return 4;
	}
	
	public int getNormalSize()
	{
		return 3;
	}
	
	public int getTextureSize()
	{
		if (textures.length > 0 && textures[0] != null)
		{
			return textures[0].getLength();
		}
		else
		{
			return 2;
		}
	}
	
	public int getColorSize()
	{
		return 4;
	}

	public int getCenterSize()
	{
		return 4;
	}

	private GenericShader getShader()
	{
		return shader;
	}

	private void setShader(GenericShader shader)
	{
		this.shader = shader;
	}

	@Override
	public String getName()
	{
		// TODO Auto-generated method stub
		return null;
	}
}
