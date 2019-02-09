package orion.sdk.graphics.util;

import com.jogamp.opengl.GLException;

public interface IAlteration extends INamed
{
	public void alter() throws GLException;
}
