package orion.sdk.graphics.panels;

import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GLException;

import orion.sdk.events.IUpdatable;
import orion.sdk.graphics.drawables.IDrawable;
import orion.sdk.graphics.shading.lighting.Material;
import orion.sdk.graphics.shading.texturing.Texture;
import orion.sdk.graphics.util.OpenGLContext;
import orion.sdk.graphics.util.OpenGLManager;

public abstract class APanel implements IDrawable, IUpdatable
{
	protected String name = null;
	protected List<PanelFrame> frames = null;
	protected int activeTextureCount = 0;
	
	public APanel(String name)
	{
		this.name = name;
		this.frames = new ArrayList<PanelFrame>();
	}

	public List<PanelFrame> getFrames()
	{
		return frames;
	}
	
	@Override
	public void draw(OpenGLContext c) throws GLException
	{
		for (PanelFrame frame : frames)
		{
			if (frame != null && frame.isReady())
			{
				frame.draw(c);
			}			
		}
	}
	
	@Override
	public boolean isReady()
	{
		for (PanelFrame frame : this.frames)
		{
			if (frame != null && !frame.isReady())
			{
				return false;
			}
		}
		
		return true;
	}

	@Override
	public boolean isVisible()
	{
		return true;
	}

	@Override
	public String getName()
	{
		return this.name;
	}

	@Override
	public void update(float dt) throws Exception
	{
		
	}

	@Override
	public float getUpdatePeriod()
	{
		return 0;
	}

	@Override
	public boolean shouldUpdate()
	{
		return false;
	}
	
	@Override
	public void upload(OpenGLContext c) throws GLException
	{
		OpenGLManager.getInstance().pushDebug("Uploading", this);
		for (PanelFrame frame : this.frames)
		{
			if (frame != null)
			{
				frame.upload(c);
			}
		}
		OpenGLManager.getInstance().popDebug();
	}
	
	@Override
	public void release(OpenGLContext c) throws GLException
	{
		OpenGLManager.getInstance().pushDebug("Releasing", this);
		for (PanelFrame frame : this.frames)
		{
			if (frame != null)
			{
				frame.release(c);
			}
		}
		OpenGLManager.getInstance().popDebug();
	}
	
	public void registerTextures(Material material)
	{
		for (PanelFrame panelFrame : this.frames)
		{
			Texture colorTexture = panelFrame.getColorTexture();
			Texture.ETextureType colorTextureType = colorTexture.getTextureType(); 
			if (!material.hasTexture(colorTextureType))
			{
				material.setTexture(colorTextureType, colorTexture);
			}

			Texture depthTexture = panelFrame.getDepthTexture();
			if (depthTexture != null)
			{
				Texture.ETextureType depthTextureType = depthTexture.getTextureType(); 
				if (!material.hasTexture(depthTextureType))
				{
					material.setTexture(depthTextureType, depthTexture);
				}
			}
		}
	}
}
