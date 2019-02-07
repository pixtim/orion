package orion.sdk.graphics.shading.glsl;

import javax.media.opengl.GLException;

import orion.sdk.graphics.util.OpenGLStack;

public class ShaderManager
{

	public static AShader getActiveShader()
	{
		AShader shader = (AShader) OpenGLStack.peek(AShader.class);
		
		if (shader != null)
		{
			return shader;
		}
		
		throw new GLException("No active shader");
	}
}
