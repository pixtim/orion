package orion.sdk.graphics.util;

import javax.media.opengl.GLException;

public interface IUploadable extends INamed
{	
	public void upload(OpenGLContext c) throws GLException;
	
	public void release(OpenGLContext c) throws GLException;
	
	public boolean isReady();
}
