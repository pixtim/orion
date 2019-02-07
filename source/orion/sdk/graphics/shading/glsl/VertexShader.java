package orion.sdk.graphics.shading.glsl;

import javax.media.opengl.GL2;
import javax.media.opengl.GL2ES2;
import javax.media.opengl.GLException;
import javax.media.opengl.glu.gl2.GLUgl2;

import orion.sdk.graphics.util.OpenGLContext;

public class VertexShader extends AShaderProgram
{
	protected VertexShader(String name, String source, int shaderProgram)
	{
		super(name, source, shaderProgram);
	}

	@Override
	public void upload(OpenGLContext c) throws GLException
	{
      shader = c.gl().glCreateShader(GL2ES2.GL_VERTEX_SHADER);
      super.upload(c);
	}
	
	@Override
	public String getName()
	{
		return  name +  ".vertexShader";
	}
}
