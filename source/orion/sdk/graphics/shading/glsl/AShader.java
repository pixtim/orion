package orion.sdk.graphics.shading.glsl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLException;

import orion.sdk.graphics.util.IStackable;
import orion.sdk.graphics.util.IUploadable;
import orion.sdk.graphics.util.OpenGLContext;
import orion.sdk.graphics.util.OpenGLManager;
import orion.sdk.graphics.util.OpenGLStack;
import orion.sdk.math.IFloatMatrix;
import orion.sdk.monitoring.incidents.Incident;
import orion.sdk.util.Parameter;
import orion.sdk.util.Transform;

public abstract class AShader extends ProgramBuilder implements IUploadable, IStackable
{	
	protected AShaderProgram vertexShader = null;
	protected AShaderProgram fragmentShader = null;
	protected int shaderProgram = -1;	
	protected static Stack<AShader> shaders = new Stack<AShader>();
	protected boolean ready = false;
	protected String name = "unknown";
	
	protected Map<String, Integer> uniforms = new TreeMap<String, Integer>();
	protected Map<String, Integer> attributes = new TreeMap<String, Integer>();

	protected AShader(String name)
	{
		this.name = name;
	}
	
	public abstract String getVertexSource();
	
	public abstract String getFragmentSource();
	
	@Override
	public void upload(OpenGLContext c) throws GLException
	{
		OpenGLManager.getInstance().pushDebug("Uploading", this);
		
		String vertexSource = getVertexSource();
		String fragmentSource = getFragmentSource();
		try
		{
			Parameter.nullcheck(vertexSource, "Vertex shader source");
			Parameter.nullcheck(fragmentSource, "Fragment shader source");
		} catch (Exception e)
		{
			throw new GLException("Shader source generation failed", e);
		}

		OpenGLManager.getInstance().checkError(c, this);

		shaderProgram = c.gl().glCreateProgram();		
		vertexShader = new VertexShader(getName() + "_vertex", vertexSource, shaderProgram);
		fragmentShader = new FragmentShader(getName() + "_fragment", fragmentSource, shaderProgram);
		
		/*
		 * Upload vertex and fragment shaders.
		 */
		vertexShader.upload(c);
		fragmentShader.upload(c);
		
		/*
		 * Attached vertex and fragment shaders
		 */
		c.gl().glAttachShader(shaderProgram, vertexShader.shader);
		c.gl().glAttachShader(shaderProgram, fragmentShader.shader);     
		
		/*
		 * Bind shader attributes
		 */      
		bindAttributes(c);
		
		/*
		 * Link the shader program
		*/      
		c.gl().glLinkProgram(shaderProgram);            
		OpenGLManager.getInstance().checkError(c, this);
		
		/*
		* Read active attribute and uniform locations
		*/
		setLinkVariables(c);      
		OpenGLManager.getInstance().checkError(c, this);
		
		/*
		 * Shader uploaded
		 */
		ready = true;
		
		/*
		 * Log shader information
		 */
		OpenGLManager.getInstance().logDebugIncident(Incident.newInformation("Shader source [" + shaderProgram + "]", new Object[] {this.getShaderInformation()}));
		
		OpenGLManager.getInstance().popDebug();
	}
	
	protected String[] getShaderInformation()
	{
		List<String> info = new ArrayList<String>();
		if (vertexShader != null)
		{
			info.add("Vertex shader source:");
			info.add("");
			String[] lines = vertexShader.source.split(NL);
			lines = addLineNumbers(lines, 4);
			for (String line : lines)
			{
				info.add(line);
			}
			info.add("");
		}
		if (fragmentShader != null)
		{
			info.add("Fragment shader source:");
			info.add("");
			String[] lines = fragmentShader.source.split(NL);
			lines = addLineNumbers(lines, 4);
			for (String line : lines)
			{
				info.add(line);
			}
			info.add("");
		}
		if (uniforms != null)
		{
			info.add("Active uniforms: " + Transform.getString(uniforms));
		}
		if (attributes != null)
		{
			info.add("Active attributes: " + Transform.getString(attributes));
		}
		
		return info.toArray(new String[info.size()]);
	}

	@Override
	public void release(OpenGLContext c) throws GLException
	{
		OpenGLManager.getInstance().pushDebug("Releasing", this);
		OpenGLManager.getInstance().checkError(c, this);

		if (vertexShader != null)
		{
			vertexShader.release(c);
			vertexShader = null;
		}
		
		if (fragmentShader != null)
		{
			fragmentShader.release(c);
			fragmentShader = null;
		}
		      
		if (shaderProgram != -1)
		{
			c.gl().glDeleteProgram(shaderProgram);
			shaderProgram = -1;
		}
		
		OpenGLManager.getInstance().checkError(c, this);
		OpenGLManager.getInstance().popDebug();
	}
	
	@Override
	public String getName()
	{
		return this.name;
	}
	
	public int getAttributeLocation(String name, OpenGLContext c)
	{
		return getAttributeLocation(name, c, false);
	}
	
	public int getAttributeLocation(String name, OpenGLContext c, boolean check)
	{
		if (!this.attributes.containsKey(name))
		{
			int attributeLocation = c.gl().glGetAttribLocation(this.shaderProgram, name);
			if (attributeLocation != -1)
			{
				this.attributes.put(name, attributeLocation);
			}
		}
		
		if (this.attributes.containsKey(name))
		{
			return this.attributes.get(name);
		}
		else
		{
			if (check)
			{
				throw new GLException("Attribute '" + name + "' could not be found. Active attributes: " + Transform.getString(attributes));
			}
			else
			{
				return -1;
			}			
		}	
	}
	
	/**
	 * Enables shaders and returns {@code true} if a shader was enabled.
	 */
	public static boolean enableAll(OpenGLContext c)
	{
		AShader shader = (AShader) OpenGLStack.peek(AShaderProgram.class);
		if (shader != null)
		{
			c.gl().glUseProgram(shader.shaderProgram);
			OpenGLManager.getInstance().checkError(c);
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * Disables shaders and returns {@code true} if a shader was disabled.
	 */
	public static boolean disableAll(OpenGLContext c)
	{
		AShader shader = (AShader) OpenGLStack.peek(AShaderProgram.class);
		if (shader != null)
		{
			c.gl().glUseProgram(0);
			OpenGLManager.getInstance().checkError(c);
			return true;
		}
		else
		{
			return false;
		}
	}	

	@Override
	public boolean isReady()
	{
		return 
			vertexShader != null && vertexShader.isReady() && 
			fragmentShader != null && fragmentShader.isReady() && 
			ready;
	}
	
	abstract protected void bindAttributes(OpenGLContext c) throws GLException;
	
	abstract protected void uploadUniforms(OpenGLContext c) throws GLException;	
	
	protected void setLinkVariables(OpenGLContext c)
	{		
		List<String> activeUniforms = getActiveUniforms(c);
		uniforms.clear();
		for (String uniform : activeUniforms)
		{
			int uniformLoc = c.gl().glGetUniformLocation(shaderProgram, uniform);
			if (uniformLoc != -1)
			{
				uniforms.put(uniform, uniformLoc);
			}
		}
		
		List<String> activeAttributes = getActiveAttributes(c);
		attributes.clear();
		for (String attribute : activeAttributes)
		{
			int attributeLoc = c.gl().glGetAttribLocation(shaderProgram, attribute);
			if (attributeLoc != -1)
			{
				attributes.put(attribute, attributeLoc);
			}
		}
	}
	
	protected List<String> getActiveUniforms(OpenGLContext c)
	{
		final int MAX_NAME_LENGTH = 256;
		int[] totals = new int[1];
		c.gl().glGetProgramiv( this.shaderProgram, GL2.GL_ACTIVE_UNIFORMS, totals, 0); 
		int total = totals[0];
		List<String> names = new ArrayList<String>();
		for(int i = 0; i < total; i++)  
		{
			int[] size = new int[1];
			int[] length = new int[1];
			int[] type = { GL2.GL_ZERO };
			byte[] data = new byte[MAX_NAME_LENGTH];
			c.gl().glGetActiveUniform(
				this.shaderProgram, 
				i, 
				MAX_NAME_LENGTH - 1, 
				length, 
				0, 
				size, 
				0, 
				type,
				0,
				data,
				0);
			
			String name = (new String(data)).substring(0, length[0]);
			names.add(name);
		}
		return names;
	}
	
	protected List<String> getActiveAttributes(OpenGLContext c)
	{
		final int MAX_NAME_LENGTH = 256;
		int[] totals = new int[1];
		c.gl().glGetProgramiv( this.shaderProgram, GL2.GL_ACTIVE_ATTRIBUTES, totals, 0); 
		int total = totals[0];
		List<String> names = new ArrayList<String>();
		for(int i = 0; i < total; i++)  
		{
			int[] size = new int[1];
			int[] length = new int[1];
			int[] type = { GL2.GL_ZERO };
			byte[] data = new byte[MAX_NAME_LENGTH];
			c.gl().glGetActiveAttrib(
				this.shaderProgram, 
				i, 
				MAX_NAME_LENGTH - 1, 
				length, 
				0, 
				size, 
				0, 
				type,
				0,
				data,
				0);
			
			String name = (new String(data)).substring(0, length[0]);
			names.add(name);
		}
		return names;
	}
	
	protected int getUniformLocation(String name) throws Exception
	{
		return getUniformLocation(name, false);
	}
	
	protected int getUniformLocation(String name, boolean check) throws Exception
	{
		if (uniforms.containsKey(name))
		{
			return uniforms.get(name);
		}
		else
		{
			if (check)
			{
				throw new Exception("Uniform '" + name + "' could not be found. Active uniforms: " + Transform.getString(uniforms));
			}
			else
			{
				return -1;
			}
		}
	}

	protected void setUniform(OpenGLContext c, String name, int x, boolean check) throws Exception
	{
		int location = getUniformLocation(name, check);
		if (location != -1)
		{
			c.gl().glUniform1iARB(location, x);
			
			OpenGLManager.getInstance().checkError(c, this);
		}
	}

	protected void setUniform(OpenGLContext c, String name, float x, boolean check) throws Exception
	{
		int location = getUniformLocation(name, check);
		if (location != -1)
		{
			c.gl().glUniform1f(location, x);
			
			OpenGLManager.getInstance().checkError(c, this);
		}
	}
	
	protected void setUniform(OpenGLContext c, String name, IFloatMatrix v, boolean check) throws Exception
	{
		int location = getUniformLocation(name, check);
		if (location != -1)
		{
			int length = v.getLength();
			switch (length)
			{
				case 3:
					c.gl().glUniform3fv(location, 1, v.columnMajor(), 0);
					break;
				case 4:
					c.gl().glUniform4fv(location, 1, v.columnMajor(), 0);
					break;
				case 9:
					c.gl().glUniformMatrix3fv(location, 1, true, v.columnMajor(), 0);
					break;
				case 16:
					c.gl().glUniformMatrix4fv(location, 1, true, v.columnMajor(), 0);
					break;
			}
			
			OpenGLManager.getInstance().checkError(c, this);
		}
	}
	
	@Override
	public void apply(OpenGLContext c)
	{
		OpenGLManager.getInstance().checkError(c, this);

		/* 
		 * Upload the shader if it has not been allocated already
		 */
		if (shaderProgram == -1)
		{
			upload(c);
		}
		
		/*
		 * Use the shader program
		 */
		c.gl().glUseProgram(shaderProgram);	
				
		/*
		 * Upload shader uniform values
		 */
		uploadUniforms(c);	
		
		OpenGLManager.getInstance().checkError(c, this);		
	}
	
	@Override
	public void clear(OpenGLContext c)
	{
		c.gl().glUseProgram(0);
		OpenGLManager.getInstance().checkError(c, this);
	}
	
	@Override
	public void push(OpenGLContext c) throws GLException
	{
		
	}
	
	@Override
	public void pop(OpenGLContext c) throws GLException
	{
		
	}
	
	public void setAttribute(String name, IFloatMatrix value, OpenGLContext c)
	{
		int location = this.getAttributeLocation(name, c);
		
		if (location != -1)
		{
			switch (value.getLength())
			{
				case 2:
					c.gl().glVertexAttrib2f(location, value.getX(), value.getY());
					return;
				case 3:
					c.gl().glVertexAttrib3f(location, value.getX(), value.getY(), value.getZ());
					return;
				case 4:
					c.gl().glVertexAttrib4f(location, value.getX(), value.getY(), value.getZ(), value.getU());
					return;
			}
			
			throw new GLException("Unsupported attribute dimension: " + value.getLength());
		}
	}
	
	public abstract void setVertexPosition(IFloatMatrix position, OpenGLContext c);
	
	public abstract void setVertexColor(IFloatMatrix color, OpenGLContext c);
	
	public abstract void setVertexTexture(IFloatMatrix texture, OpenGLContext c);
	
	public abstract void setVertexNormal(IFloatMatrix normal, OpenGLContext c);

	public static class Functions
	{

	}
}
