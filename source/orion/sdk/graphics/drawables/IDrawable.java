package orion.sdk.graphics.drawables;

import javax.media.opengl.GLException;

import orion.sdk.graphics.util.OpenGLContext;
import orion.sdk.math.geometry.Box;
import orion.sdk.graphics.util.IUploadable;

public interface IDrawable extends IUploadable
{
	/**
	 * Handles OpenGL drawing events
	 */
	public void draw(OpenGLContext c) throws GLException;
	
	/**
	 * @return the number of faces needed to render this drawable.
	 */
	public int getFaceCount();
	
	public Box getBounds();
	
	public boolean isVisible();	
}
