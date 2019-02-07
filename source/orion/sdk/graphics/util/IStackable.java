package orion.sdk.graphics.util;

import javax.media.opengl.GLException;

public interface IStackable extends INamed
{
	public void push(OpenGLContext c) throws GLException;
	
	public void pop(OpenGLContext c) throws GLException;
	
	public void apply(OpenGLContext c) throws GLException;
	
	public void clear(OpenGLContext c) throws GLException;
}
