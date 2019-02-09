package orion.sdk.graphics.util;

import com.jogamp.opengl.GLException;

public interface IUploadable extends INamed
{	
	public void upload(OpenGLContext c) throws GLException;
	
	public void release(OpenGLContext c) throws GLException;
	
	public boolean isReady();
}
