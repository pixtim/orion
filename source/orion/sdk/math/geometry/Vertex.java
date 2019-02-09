package orion.sdk.math.geometry;

import orion.sdk.graphics.shading.glsl.AShader;
import orion.sdk.graphics.shading.glsl.ShaderManager;
import orion.sdk.graphics.util.OpenGLContext;
import orion.sdk.math.IFloatMatrix;

public class Vertex
{
	private IFloatMatrix position = null;
	private IFloatMatrix color = null;
	private IFloatMatrix texture = null;
	private IFloatMatrix normal = null;

	public Vertex()
	{
		
	}
	
	public IFloatMatrix getPosition()
	{
		return position;
	}
	
	public void setPosition(IFloatMatrix position)
	{
		this.position = position;
	}
	
	public IFloatMatrix getColor()
	{
		return color;
	}
	
	public void setColor(IFloatMatrix color)
	{
		this.color = color;
	}
	
	public IFloatMatrix getTexture()
	{
		return texture;
	}
	
	public void setTexture(IFloatMatrix texture)
	{
		this.texture = texture;
	}
	
	public IFloatMatrix getNormal()
	{
		return normal;
	}
	
	public void setNormal(IFloatMatrix normal)
	{
		this.normal = normal;
	}
	
	public void apply(OpenGLContext c)
	{
		AShader shader = ShaderManager.getActiveShader();
		
		if (this.color != null)
		{
			shader.setVertexColor(this.color, c);
		}
		
		if (this.texture != null)
		{
			shader.setVertexTexture(this.texture, c);
		}
		
		if (this.normal != null)
		{
			shader.setVertexNormal(this.normal, c);
		}
		
		if (this.position != null)
		{
			shader.setVertexPosition(this.position, c);
		}
	}
}
