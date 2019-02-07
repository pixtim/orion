package orion.sdk.graphics.buffers;

import java.nio.ByteBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLException;
import javax.media.opengl.glu.gl2.GLUgl2;

import orion.sdk.graphics.util.OpenGLManager;
import orion.sdk.graphics.util.IUploadable;
import orion.sdk.graphics.util.OpenGLContext;

public class VertexBuffer implements IUploadable
{
	public static final int SUPPORTED_BUFFERS = 16384;
	
	protected static int[] vertexBufferIndexes = new int[SUPPORTED_BUFFERS];
	protected static boolean[] vertexBufferUsed = new boolean[SUPPORTED_BUFFERS];
	
	protected int index = 0;
	protected boolean ready = false;
	public int vertexCount = 0;
	public IVertexFormatter vertexFormat = null;
	protected String name = "unknown";

	public VertexBuffer(String name, IVertexFormatter vertexFormat, int vertexCount)
	{
		this.vertexFormat = vertexFormat;
		this.vertexCount = vertexCount;
		this.index = nextBufferIndex();
		this.name = name;
	}
	public static int getBufferCount() throws GLException
	{
		int count = 0;
		for (int i = 0; i < SUPPORTED_BUFFERS; i++)
		{
			if (vertexBufferUsed[i])
			{
				count++;
			}
		}
		return count;
	}
	
	protected static int nextBufferIndex() throws GLException
	{
		for (int i = 0; i < SUPPORTED_BUFFERS; i++)
		{
			if (!vertexBufferUsed[i])
			{
				vertexBufferUsed[i] = true;
				return i;
			}
		}
		throw new GLException("All vertex buffers in use. " + getBufferCount() + " vertex buffers allocated.");
	}

	/**
	 * Releases graphics memory for this vertex buffer.
	 */
	public void release(OpenGLContext c)
	{
		OpenGLManager.getInstance().pushDebug("Releasing", this);
		c.gl().glDeleteBuffers(1, vertexBufferIndexes, index);
		vertexBufferUsed[index] = false;
		
		OpenGLManager.getInstance().checkError(c, this);
		OpenGLManager.getInstance().popDebug();
	}
	
	/**
	 * Uploads the given vertex data to this vertex buffer.
	 * 
	 * @param gl
	 * 	The The OpenGL wrapper object.
	 * @throws GLException
	 */
	public void upload(OpenGLContext c) throws GLException
	{
		OpenGLManager.getInstance().pushDebug("Uploading", this);
		OpenGLManager.getInstance().checkError(c, this);
		
		int vertexStride = vertexFormat.getBytesPerVertex();
		c.gl().glGenBuffers(1, vertexBufferIndexes, index);
		c.gl().glBindBuffer(GL.GL_ARRAY_BUFFER, vertexBufferIndexes[index]);
		c.gl().glBufferData(GL.GL_ARRAY_BUFFER, vertexCount * vertexStride, null,	GL2.GL_DYNAMIC_DRAW);			

		OpenGLManager.getInstance().checkError(c, this);
		
		c.gl().glBindBuffer(GL.GL_ARRAY_BUFFER, vertexBufferIndexes[index]);
		ByteBuffer bytebuffer = c.gl().glMapBuffer(GL.GL_ARRAY_BUFFER, GL2.GL_WRITE_ONLY);		
		vertexFormat.formatVertices(bytebuffer);
		OpenGLManager.getInstance().checkError(c, this);
		
		vertexFormat.setStates(c);
		
		c.gl().glUnmapBuffer(GL.GL_ARRAY_BUFFER);		
		OpenGLManager.getInstance().checkError(c, this);
		
		c.gl().glBindBuffer(GL.GL_ARRAY_BUFFER, 0);		
		OpenGLManager.getInstance().checkError(c, this);
		
		ready = true;
		OpenGLManager.getInstance().popDebug();
	}
	
	public void draw(OpenGLContext c, int drawMode)
	{
		/*
		 * Bind to this vertex buffer
		 */
      c.gl().glBindBuffer(GL.GL_ARRAY_BUFFER, vertexBufferIndexes[index]);


      /*
       * Specify the vertex attribute order and offset
       */
      vertexFormat.uploadFormat(c);

      /*
       * Draw the vertex buffer
       */
      c.gl().glDrawArrays(drawMode, 0, vertexCount);

      /*
       * Rebind to the default vertex buffer
       */
      c.gl().glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
      
      OpenGLManager.getInstance().checkError(c, this);
	}
	
	@Override
	public boolean isReady()
	{
		return ready;
	}
	@Override
	public String getName()
	{
		return this.name;
	}
}
