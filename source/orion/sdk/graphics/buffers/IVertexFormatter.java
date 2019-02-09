package orion.sdk.graphics.buffers;

import java.nio.ByteBuffer;

import com.jogamp.opengl.GLException;

import orion.sdk.graphics.util.OpenGLContext;

public interface IVertexFormatter
{
	public void uploadFormat(OpenGLContext c) throws GLException;
	public void formatVertices(ByteBuffer bytebuffer) throws GLException;
	public int getBytesPerVertex();
	public void setStates(OpenGLContext c) throws GLException;
}
