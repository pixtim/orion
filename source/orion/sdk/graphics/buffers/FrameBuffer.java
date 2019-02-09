package orion.sdk.graphics.buffers;

import java.util.Stack;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLException;

import orion.sdk.graphics.shading.texturing.Texture;
import orion.sdk.graphics.util.IStackable;
import orion.sdk.graphics.util.IUploadable;
import orion.sdk.graphics.util.OpenGLContext;
import orion.sdk.graphics.util.OpenGLManager;
import orion.sdk.graphics.util.OpenGLStack;

public class FrameBuffer implements IUploadable, IStackable
{
	protected String name = "unknown";
	protected boolean ready = false;

	protected Texture colorTexture = null;
	protected Texture depthTexture = null;

	protected int frameBufferObj = -1;
	protected int renderBufferObj = -1;

	protected static Stack<FrameBuffer> frameBuffers = new Stack<FrameBuffer>();

	public FrameBuffer(String name, Texture colorTexture, Texture depthTexture)
	{
		this.name = name;
		this.colorTexture = colorTexture;
		this.depthTexture = depthTexture;
	}
	
	public FrameBuffer(String name, Texture colorTexture)
	{
		this(name, colorTexture, null);
	}

	@Override
	public String getName()
	{
		return this.name;
	}

	@Override
	public void upload(OpenGLContext c) throws GLException
	{
		if (!this.isReady())
		{
			OpenGLManager.getInstance().pushDebug("Uploading", this);
			OpenGLManager.getInstance().checkError(c, this);

			/*
			 * Bind the new frame buffer
			 */
			OpenGLStack.push(FrameBuffer.class, this, c);

			/*
			 * Prepare the texture that will be used as the color attachment and
			 * attach it.
			 */
			c.gl().glFramebufferTexture2D(GL2.GL_FRAMEBUFFER, GL2.GL_COLOR_ATTACHMENT0,
					GL2.GL_TEXTURE_2D, this.colorTexture.getTextureID(), 0);
			OpenGLManager.getInstance().checkError(c, this);
			
			/*
			 * Prepare the texture that will be used as the depth attachment, if
			 * any. Otherwise, attach a render buffer.
			 */
			if (this.depthTexture != null)
			{
				c.gl().glFramebufferTexture2D(GL2.GL_FRAMEBUFFER, GL2.GL_DEPTH_ATTACHMENT,
						GL2.GL_TEXTURE_2D, this.depthTexture.getTextureID(), 0);
				OpenGLManager.getInstance().checkError(c, this);
			}
			else
			{					
				/*
				 * Create a render buffer that will be used as the depth attachment.
				 */
				int width = this.colorTexture.getWidth();
				int height = this.colorTexture.getHeight();
	
				int[] renderBuffers = new int[1];
				c.gl().glGenRenderbuffers(1, renderBuffers, 0);
				this.renderBufferObj = renderBuffers[0];
				c.gl().glBindRenderbuffer(GL2.GL_RENDERBUFFER, this.renderBufferObj);
				
				c.gl().glRenderbufferStorage(
					GL2.GL_RENDERBUFFER, 
					GL2.GL_DEPTH_COMPONENT, 
					width,
					height);
				OpenGLManager.getInstance().checkError(c, this);

				/*
				 * Attach the render buffer to the frame buffer
				 */
				c.gl().glFramebufferRenderbuffer(
					GL2.GL_FRAMEBUFFER, 
					GL2.GL_DEPTH_ATTACHMENT,
					GL2.GL_RENDERBUFFER, 
					this.renderBufferObj);
				OpenGLManager.getInstance().checkError(c, this);
			}


			/*
			 * Check the frame buffer status
			 */
			int status = c.gl().glCheckFramebufferStatus(GL2.GL_FRAMEBUFFER);
			if (status != GL2.GL_FRAMEBUFFER_COMPLETE)
			{
				throw new GLException("Failed to create frame buffer");
			}			

			/*
			 * Use the default frame buffer
			 */
			OpenGLStack.pop(FrameBuffer.class, this, c);

			OpenGLManager.getInstance().checkError(c, this);

			/*
			 * Frame buffer uploaded
			 */
			this.ready = true;
			OpenGLManager.getInstance().popDebug();
		}
	}

	@Override
	public void release(OpenGLContext c) throws GLException
	{		
		if (isReady())
		{
			OpenGLManager.getInstance().pushDebug("Releasing", this);
			/*
			 * Delete the frame buffer
			 */
			int[] frameBuffers = new int[1];
			c.gl().glDeleteFramebuffers(1, frameBuffers, 0);
			OpenGLManager.getInstance().popDebug();
		}
	}

	@Override
	public boolean isReady()
	{
		return ready;
	}

	@Override
	public void push(OpenGLContext c)
	{
		/*
		 * Push the required textures
		 */
		if (this.colorTexture != null)
		{
			this.colorTexture.upload(c);
			OpenGLStack.push(Texture.class, this.colorTexture, c, this.colorTexture.getLayer());
		}
		if (this.depthTexture != null)
		{
			this.depthTexture.upload(c);
			OpenGLStack.push(Texture.class, this.depthTexture, c, this.depthTexture.getLayer());
		}
	}

	@Override
	public void pop(OpenGLContext c)
	{
		/*
		 * Pop the textures used
		 */
		if (this.colorTexture != null)
		{
			OpenGLStack.pop(Texture.class, this.colorTexture, c, this.colorTexture.getLayer());			
		}
		if (this.depthTexture != null)
		{
			OpenGLStack.pop(Texture.class, this.depthTexture, c, this.depthTexture.getLayer());
		}
	}		
	
	@Override
	public void apply(OpenGLContext c) throws GLException
	{
		/*
		 * Generate the frame buffer
		 */
		if (this.frameBufferObj == -1)
		{
			int[] frameBuffers = new int[1];
			c.gl().glGenFramebuffers(1, frameBuffers, 0);
			this.frameBufferObj = frameBuffers[0];
			OpenGLManager.getInstance().checkError(c, this);
		}

		/*
		 * Apply the required textures
		 */
		if (this.colorTexture != null)
		{
			this.colorTexture.upload(c);
			OpenGLStack.apply(this.colorTexture, c);
		}
		if (this.depthTexture != null)
		{
			this.depthTexture.upload(c);
			OpenGLStack.apply(this.depthTexture, c);
		}

		/*
		 * Use the frame buffer
		 */
		c.gl().glBindFramebuffer(GL2.GL_FRAMEBUFFER, frameBufferObj);
		
		OpenGLManager.getInstance().checkError(c, this);		
	}
	
	@Override
	public void clear(OpenGLContext c) throws GLException
	{
		OpenGLManager.getInstance().checkError(c, this);

		c.gl().glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);
		
		OpenGLManager.getInstance().checkError(c, this);		
	}
}
