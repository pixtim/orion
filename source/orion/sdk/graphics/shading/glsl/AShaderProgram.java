package orion.sdk.graphics.shading.glsl;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.GLException;

import orion.sdk.graphics.util.IUploadable;
import orion.sdk.graphics.util.OpenGLContext;
import orion.sdk.graphics.util.OpenGLManager;

public abstract class AShaderProgram extends ProgramBuilder implements IUploadable
{
	protected String source = null;
	protected int shader = -1;
	protected int program = -1;
	protected String name = "unknown";
	
	protected AShaderProgram(String name, String source, int shaderProgram)
	{
		this.source = source;
		this.program = shaderProgram;
		this.name = name;
	}
	
	@Override
	public void release(OpenGLContext c) throws GLException
	{
		OpenGLManager.getInstance().pushDebug("Releasing", this);
		c.gl().glDetachShader(program, shader);
		c.gl().glDeleteShader(shader);
		OpenGLManager.getInstance().popDebug();
	}

	@Override
	public void upload(OpenGLContext c) throws GLException
	{
		OpenGLManager.getInstance().pushDebug("Uploading", this);
		
		/*
		 * Compile the source string into a shader program.
		 */
		String[] lines = new String[] { source };
		int[] lengths = new int[] { lines[0].length() };
		c.gl().glShaderSource(shader, lines.length, lines, lengths, 0);
		c.gl().glCompileShader(shader);

		/*
		 * Check compile status.
		 */
		int[] outcome = new int[1];
		c.gl().glGetShaderiv(shader, GL2ES2.GL_COMPILE_STATUS, outcome, 0);
		if (outcome[0] != GL2.GL_TRUE)
		{
			int[] logLength = new int[1];
			c.gl().glGetShaderiv(shader, GL2ES2.GL_INFO_LOG_LENGTH, logLength, 0);

			byte[] log = new byte[logLength[0]];
			c.gl().glGetShaderInfoLog(shader, logLength[0], null, 0, log, 0);

			throw new GLException("Shader could not be compiled successfully: " + new String(log));
		}
		
		OpenGLManager.getInstance().popDebug();
	}
	
	@Override
	public boolean isReady()
	{
		return program != -1;
	}
	
	@Override
	public String getName()
	{
		return "'" + name +  "' (Shader)";
	}
}
