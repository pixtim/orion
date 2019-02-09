package orion.sdk.graphics.shading.lighting;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLException;

import orion.sdk.graphics.shading.texturing.Texture;
import orion.sdk.graphics.shading.texturing.TextureStack;
import orion.sdk.graphics.util.IStackable;
import orion.sdk.graphics.util.IUploadable;
import orion.sdk.graphics.util.OpenGLContext;
import orion.sdk.graphics.util.OpenGLManager;
import orion.sdk.graphics.util.OpenGLStack;
import orion.sdk.math.FloatMatrix;

public class Material implements IStackable, IUploadable
{
	protected String name;
	protected TextureStack textureStack = null;
	
	public FloatMatrix ambient = FloatMatrix.vector(0.01f, 0.01f, 0.01f, 1f);
	public FloatMatrix diffuse = FloatMatrix.vector(1f, 1f, 1f, 1f);
	public FloatMatrix specular = FloatMatrix.vector(1f, 1f, 1f, 1f);
	public int shininess = 10;
	
	public Material(String name)
	{
		this.name = name;
		this.textureStack = new TextureStack(name + ".textureStack");
	}	
	
	@Override
	public String getName()
	{
		return this.name;
	}
	
	public void setTexture(Texture.ETextureType textureType, Texture texture)
	{
		texture.setTextureType(textureType);
		this.setTexture(texture);
	}
	
	public void setTexture(Texture texture)
	{
		this.textureStack.add(texture);
	}
	
	public void removeTexture(Texture texture)
	{
		this.textureStack.remove(texture);
	}
	
	public boolean hasTexture(Texture.ETextureType textureType)
	{
		return this.textureStack.contains(textureType);
	}
	
	public Texture getTexture(Texture.ETextureType textureType)
	{
		if (this.hasTexture(textureType))
		{
			return this.textureStack.getTexture(textureType);
		}
		else
		{
			return null; 
		}
	}
	
	@Override
	public void push(OpenGLContext c) throws GLException
	{
		OpenGLStack.push(TextureStack.class, this.textureStack, c);
	}
	
	@Override
	public void pop(OpenGLContext c) throws GLException
	{
		OpenGLStack.pop(TextureStack.class, this.textureStack, c);
	}

	@Override
	public void apply(OpenGLContext c)
	{
		c.gl().glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT, ambient.rowMajor(), 0);
		c.gl().glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_DIFFUSE, diffuse.rowMajor(), 0);
		c.gl().glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, specular.rowMajor(), 0);
		c.gl().glMateriali(GL2.GL_FRONT_AND_BACK, GL2.GL_SHININESS, shininess);
	
		OpenGLManager.getInstance().checkError(c);
		
		OpenGLStack.apply(textureStack, c);
	}

	@Override
	public void clear(OpenGLContext c)
	{
		c.gl().glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT, new float[] {0f, 0f, 0f, 1f}, 0);
		c.gl().glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_DIFFUSE, new float[] {0f, 0f, 0f, 1f}, 0);
		c.gl().glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, new float[] {0f, 0f, 0f, 1f}, 0);
		c.gl().glMateriali(GL2.GL_FRONT_AND_BACK, GL2.GL_SHININESS, 1);
		
		OpenGLManager.getInstance().checkError(c);
		
		OpenGLStack.clear(textureStack, c);
	}

	@Override
	public void upload(OpenGLContext c) throws GLException
	{
		OpenGLManager.getInstance().pushDebug("Uploading", this);
		this.textureStack.upload(c);
		OpenGLManager.getInstance().popDebug();
	}

	@Override
	public void release(OpenGLContext c) throws GLException
	{
		OpenGLManager.getInstance().pushDebug("Releasing", this);
		this.textureStack.release(c);
		OpenGLManager.getInstance().popDebug();
	}

	@Override
	public boolean isReady()
	{
		return this.textureStack.isReady();
	}
}
