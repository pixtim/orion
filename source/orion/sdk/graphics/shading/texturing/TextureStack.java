package orion.sdk.graphics.shading.texturing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

import javax.media.opengl.GLException;

import orion.sdk.graphics.util.IStackable;
import orion.sdk.graphics.util.IUploadable;
import orion.sdk.graphics.util.OpenGLContext;
import orion.sdk.graphics.util.OpenGLManager;
import orion.sdk.graphics.util.OpenGLStack;

public class TextureStack implements IUploadable, IStackable
{
	protected Map<Texture.ETextureType, Texture> textures = new TreeMap<Texture.ETextureType, Texture>();
	protected Stack<Texture.ETextureType> layers = new Stack<Texture.ETextureType>();
	protected String name = null;
	
	public TextureStack(String name)
	{
		this.name = name;
	}
	
	public void add(Texture texture) throws GLException
	{
		if (texture == null)
		{
			throw new GLException("Texture cannot be null");
		}
		
		Texture.ETextureType textureType = texture.getTextureType();
		this.layers.push(textureType);
		this.textures.put(textureType, texture);
		
		texture.setTextureStack(this);
	}
	
	public void remove(Texture texture)
	{
		Texture.ETextureType textureType = texture.getTextureType();
		this.layers.remove(textureType);
		this.textures.remove(textureType);
		texture.setTextureStack(null);
	}
	
	public Texture getTexture(Texture.ETextureType textureType)
	{
		if (this.contains(textureType))
		{
			return this.textures.get(textureType);
		}
		else
		{
			return null;
		}
	}
	
	public boolean contains(Texture.ETextureType textureType)
	{
		return this.layers.contains(textureType);
	}	
	
	public int getUnit(Texture texture) throws GLException
	{
		Texture.ETextureType textureType = texture.getTextureType();
		
		return textureType.ordinal();
	}

	@Override
	public String getName()
	{
		return this.name;
	}

	@Override
	public void upload(OpenGLContext c) throws GLException
	{
		OpenGLManager.getInstance().pushDebug("Uploading", this);
		
		for (Texture.ETextureType layer : this.layers)
		{
			Texture texture = this.textures.get(layer);
			texture.upload(c);
		}
		
		OpenGLManager.getInstance().popDebug();
	}

	@Override
	public void release(OpenGLContext c) throws GLException
	{
		OpenGLManager.getInstance().pushDebug("Releasing", this);
		
		for (Texture.ETextureType layer : this.layers)
		{
			Texture texture = this.textures.get(layer);
			texture.release(c);
		}
		
		OpenGLManager.getInstance().popDebug();
	}

	@Override
	public boolean isReady()
	{
		for (Texture.ETextureType layer : this.layers)
		{
			Texture texture = this.textures.get(layer);
			if (!texture.isReady())
			{
				return false;
			}
		}

		return true;
	}
	
	@Override
	public void push(OpenGLContext c)
	{
		ArrayList<Texture.ETextureType> list = new ArrayList<Texture.ETextureType>(this.layers);
		
		for (Texture.ETextureType layer : list)
		{
			Texture texture = this.textures.get(layer);
			OpenGLStack.push(Texture.class, texture, c, texture.getLayer());
		}
	}
	
	@Override
	public void pop(OpenGLContext c)
	{
		ArrayList<Texture.ETextureType> list = new ArrayList<Texture.ETextureType>(this.layers);
		Collections.reverse(list);
		
		for (Texture.ETextureType layer : list)
		{
			Texture texture = this.textures.get(layer);
			OpenGLStack.pop(Texture.class, texture, c, texture.getLayer());
		}
	}
	
	@Override
	public void apply(OpenGLContext c)
	{
		ArrayList<Texture.ETextureType> list = new ArrayList<Texture.ETextureType>(this.layers);
		
		for (Texture.ETextureType layer : list)
		{
			Texture texture = this.textures.get(layer);
			OpenGLStack.apply(texture, c);
		}
	}
	
	@Override
	public void clear(OpenGLContext c)
	{
		ArrayList<Texture.ETextureType> list = new ArrayList<Texture.ETextureType>(this.layers);
		
		for (Texture.ETextureType layer : list)
		{
			Texture texture = this.textures.get(layer);
			OpenGLStack.clear(texture, c);
		}
	}
}
