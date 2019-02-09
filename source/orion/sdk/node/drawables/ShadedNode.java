package orion.sdk.node.drawables;

import com.jogamp.opengl.GLException;

import orion.sdk.graphics.drawables.IDrawable;
import orion.sdk.graphics.shading.glsl.AShader;
import orion.sdk.graphics.shading.lighting.Material;
import orion.sdk.graphics.util.OpenGLContext;
import orion.sdk.graphics.util.OpenGLManager;
import orion.sdk.graphics.util.OpenGLStack;

public class ShadedNode extends DrawableNode
{
	private Material material = null;
	private AShader shader = null;
	
	public ShadedNode(String name, IDrawable drawable, AShader shader, Material material)
	{
		super(name, drawable);
		this.setShader(shader);
		this.setMaterial(material);
	}
	
	@Override
	public void draw(OpenGLContext c) throws GLException
	{
		OpenGLManager.getInstance().pushDebug("Drawing", this);
		
		try
		{
			if (this.getMaterial() != null)
			{
				OpenGLStack.push(Material.class, this.getMaterial(), c);
			}
			if (this.getShader() != null)
			{
				OpenGLStack.push(AShader.class, getShader(), c);
			}

			super.draw(c);
			
			if (this.getMaterial() != null)
			{
				OpenGLStack.pop(AShader.class, getShader(), c);
			}
			if (this.getShader() != null)
			{
				OpenGLStack.pop(Material.class, this.getMaterial(), c);
			}
		}
		catch (Exception e)
		{
			throw new GLException("Failed to draw solid node '" + name + "'", e);
		}
		
		OpenGLManager.getInstance().popDebug();
	}
	
	@Override
	public void release(OpenGLContext c) throws GLException
	{		
		try
		{
			if (getShader() != null)
			{
				getShader().release(c);
			}
		}
		catch (Exception e)
		{
			throw new GLException("Failed to release shader for solid node '" + name + "'", e);
		}
		super.release(c);
	}
	
	@Override
	public void upload(OpenGLContext c) throws GLException
	{	
		Material material = this.material;		
		if (material != null)
		{
			material.upload(c);
		}
		
		super.upload(c);
		
		AShader shader = this.getShader();
		if (shader != null)
		{
			shader.upload(c);
		}
	}

	public Material getMaterial()
	{
		return material;
	}

	public synchronized void setMaterial(Material material)
	{
		this.material = material;
	}

	public AShader getShader()
	{
		return shader;
	}

	public synchronized void setShader(AShader shader)
	{
		this.shader = shader;
	}
}
