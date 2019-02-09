package orion.sdk.graphics.drawables;

import com.jogamp.opengl.GLException;

import orion.sdk.graphics.util.IUploadable;
import orion.sdk.graphics.util.OpenGLContext;
import orion.sdk.math.geometry.Box;

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
