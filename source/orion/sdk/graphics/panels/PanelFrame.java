package orion.sdk.graphics.panels;

import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

import javax.media.opengl.GLException;

import orion.sdk.graphics.buffers.FrameBuffer;
import orion.sdk.graphics.drawables.IDrawable;
import orion.sdk.graphics.shading.texturing.Texture;
import orion.sdk.graphics.util.OpenGLBuffers;
import orion.sdk.graphics.util.OpenGLContext;
import orion.sdk.graphics.util.OpenGLManager;
import orion.sdk.graphics.util.OpenGLStack;
import orion.sdk.math.geometry.Box;
import orion.sdk.monitoring.incidents.Incident;
import orion.sdk.monitoring.incidents.IncidentManager;

public class PanelFrame implements IDrawable
{
	protected Texture colorTexture = null;
	protected Texture depthTexture = null;
	protected FrameBuffer frameBuffer = null;
	protected List<IDrawable> drawables = null;
	protected String name = "unknown";
	
	public PanelFrame(String name, Texture colorTexture, Texture depthTexture)
	{
		this.name = name;
		this.colorTexture = colorTexture;
		this.depthTexture = depthTexture;
		this.frameBuffer = new FrameBuffer("frame buffer: " + this.getName(), this.colorTexture, this.depthTexture);
		this.drawables = new LinkedList<IDrawable>();
	}
	
	public PanelFrame(String name, Texture colorTexture)
	{
		this(name, colorTexture, null);
	}
	
	public List<IDrawable> getDrawables() throws GLException
	{
		return drawables;
	}
	
	public Texture getColorTexture()
	{
		return colorTexture;
	}
	
	public Texture getDepthTexture()
	{
		return depthTexture;
	}
	
	@Override
	public void upload(OpenGLContext c) throws GLException
	{
		OpenGLManager.getInstance().pushDebug("Uploading", this);
		if (this.frameBuffer != null)
		{
			this.frameBuffer.upload(c);
		}
		
		if (drawables != null)
		{
			for (int i = 0; i < this.drawables.size(); i++)
			{
				this.drawables.get(i).upload(c);
			}
		}
		OpenGLManager.getInstance().popDebug();
	}
	
	@Override
	public void release(OpenGLContext c) throws GLException
	{
		OpenGLManager.getInstance().pushDebug("Releasing", this);
		if (this.frameBuffer != null)
		{
			this.frameBuffer.release(c);
		}
		
		if (drawables != null)
		{
			for (int i = 0; i < this.drawables.size(); i++)
			{
				this.drawables.get(i).release(c);
			}
		}
		OpenGLManager.getInstance().popDebug();
	}
	
	@Override
	public boolean isReady()
	{
		if (this.frameBuffer != null && !this.frameBuffer.isReady())
		{
			return false;
		}
		
		return true;
	}

	@Override
	public String getName()
	{
		return this.name;
	}
	
	protected void beforeDraw(OpenGLContext c)
	{
		
	}
	
	protected void afterDraw(OpenGLContext c)
	{		
		//OpenGLManager.getInstance().logDebugScreens(this, c);
		
		OpenGLManager openGLManager = OpenGLManager.getInstance();
		
		if (openGLManager.isDebugEnabled())
		{
			openGLManager.pushDebug("Taking screenshots", this);
			
			List<BufferedImage> images = new LinkedList<BufferedImage>();
			
			if (this.colorTexture != null)
			{
				BufferedImage colorTextureImage = OpenGLBuffers.getTextureImage(this.colorTexture, c);
				images.add(colorTextureImage);
			}
			
			if (this.depthTexture != null)
			{
				BufferedImage depthTextureImage = OpenGLBuffers.getTextureImage(this.depthTexture, c);
				images.add(depthTextureImage);
			}
			
			openGLManager.popDebug();
			
			Incident debugInfo = Incident.newInformation(
				"Textures: " + openGLManager.getShortDescription(this),
				new Object[] { images.toArray(new BufferedImage[images.size()]) });
			IncidentManager.notifyIncident(debugInfo);
		}
	}

	@Override
	public void draw(OpenGLContext c) throws GLException
	{
		OpenGLManager.getInstance().pushDebug("Drawing", this);

		OpenGLStack.push(FrameBuffer.class, this.frameBuffer, c);
		
		this.beforeDraw(c);
		
		if (drawables != null)
		{
			for (int i = 0; i < this.drawables.size(); i++)
			{
				IDrawable drawable = this.drawables.get(i);
				if (drawable.isVisible() && drawable.isReady())
				{
					drawable.draw(c);
				}
			}
		}
		
		c.gl().glFinish();
		
		this.afterDraw(c);
		
		OpenGLStack.pop(FrameBuffer.class, this.frameBuffer, c);
		
		OpenGLManager.getInstance().popDebug();
	}

	@Override
	public int getFaceCount()
	{
		return 0;
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
	
	@Override
	public String toString()
	{
		return OpenGLManager.getInstance().getShortDescription(this);
	}
}
