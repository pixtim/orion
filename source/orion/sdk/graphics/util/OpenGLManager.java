package orion.sdk.graphics.util;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.GLPipelineFactory;

import orion.sdk.graphics.drawables.IDrawable;
import orion.sdk.graphics.drawables.primitives.WireBox;
import orion.sdk.math.FloatMatrix;
import orion.sdk.math.geometry.Box;
import orion.sdk.monitoring.incidents.Incident;
import orion.sdk.monitoring.incidents.IncidentManager;
import orion.sdk.monitoring.performance.CallStack;

public class OpenGLManager implements INamed
{
	public static final int DEFAULT_DEQUEQUE_SIZE = 10;
	public static final boolean DEFAULT_DEBUG = false;
	
	private static OpenGLManager instance = new OpenGLManager();
	
	private Queue<IUploadable> uploadQueue = new LinkedBlockingQueue<IUploadable>();
	private Queue<IAlteration> alterationQueue = new LinkedBlockingQueue<IAlteration>();
	
	private boolean debugEnabled = DEFAULT_DEBUG;
	private boolean debugAttached = false;
	private OpenGlDebugStream debugStream = new OpenGlDebugStream();  
	private List<CallStack> debugStack = new LinkedList<CallStack>();
	private int dequeueSize = DEFAULT_DEQUEQUE_SIZE;
	private Map<String, String> glConstants = new HashMap<String, String>();
	private List<String> glFunctions = new LinkedList<String>();
	
	private Object displayMutex = new Object();
	
	public OpenGLManager()
	{
		/*
		 * Construct a (hex, constant) map of the OpenGL constants
		 */
		try
		{
			Class<?>[] classes = new Class<?>[] {GL2.class, GL.class};
			for (Class<?> nextClass : classes)
			{
				Field[] fields = nextClass.getFields();
				for (Field field : fields)
				{
					String name = field.getName(); 
					if (name.startsWith("GL_"))
					{
						if (field.getType().equals(int.class))
						{
							String hex = Integer.toHexString(field.getInt(null)).toUpperCase();
							while (hex.startsWith("0") && hex.length() > 1)
							{
								hex = hex.substring(1);
							}
							this.glConstants.put("0x" + hex, name);
						}						
					}
				}
			}
		}
		catch (IllegalArgumentException | IllegalAccessException e)
		{
			IncidentManager.notifyIncident(Incident.newError("Failed to construct OpenGL constant map.", e));
		}
		
		/*
		 * Construct a list of the OpenGL functions
		 */
		try
		{
			Class<?>[] classes = new Class<?>[] {GL2.class, GL.class};
			for (Class<?> nextClass : classes)
			{
				Method[] methods = nextClass.getMethods();
				for (Method method : methods)
				{
					String name = method.getName(); 
					if (name.startsWith("gl"))
					{
						this.glFunctions.add(name);
					}
				}
			}
		}
		catch (IllegalArgumentException e)
		{
			IncidentManager.notifyIncident(Incident.newError("Failed to construct OpenGL function list.", e));
		}		
	}
	
	public Object getDisplayMutex()
	{
		return this.displayMutex;
	}
	
	public Map<String, String> getGLConstants()
	{
		return this.glConstants;
	}
	
	public List<String> getGLFunctions()
	{
		return this.glFunctions;
	}
	
	public FloatMatrix getGLVector4(OpenGLContext c, int constant)
	{
		float[] tuple = new float[4];
		c.gl().glGetFloatv(constant, tuple, 0);
		return FloatMatrix.vector(tuple);
	}
	
	public static OpenGLManager getInstance()
	{
		return instance;
	}
	
	public void addCallStack()
	{
		if (isDebugEnabled())
		{
			debugStack.add(CallStack.getTrace("OpenGL debug stack"));
		}
	}
	
	public void setDequeueSize(int dequeueSize)
	{
		this.dequeueSize = dequeueSize;
	}
	
	public int getDequeueSize()
	{
		return dequeueSize;
	}
	
	public boolean isDebugEnabled()
	{
		return debugEnabled;
	}
	
	public void setDebugEnabled(boolean enabled)
	{
		this.debugEnabled = enabled;
	}
	
	public void invalidateDebugger()
	{
		this.debugAttached = false;
	}
	
	public void checkError(OpenGLContext c, INamed namedObject) throws GLException
	{
		int error = c.gl().glGetError();
		String errorString = c.glu().gluErrorString(error);
		if (error != GL.GL_NO_ERROR)
		{
			String objectString = 
				(namedObject != null) ? " while processing " + getShortDescription(namedObject) : "";
			throw new GLException("OpenGL Error: 0x" + error + " ('" + errorString + "')" + objectString);
		}
	}
	
	public String getShortDescription(Object object)
	{
		if (object instanceof INamed)
		{
			INamed namedObject = (INamed) object;
			return "'" + namedObject.getName() + "' (" + namedObject.getClass().getSimpleName() + ")";
		}
		else
		{
			return object.toString();
		}		
	}
	
	public void checkError(OpenGLContext c) throws GLException
	{
		checkError(c, null);
	}
	
	public void queueUpload(IUploadable uploadable)
	{
		if (!uploadQueue.contains(uploadable))
		{
			uploadQueue.add(uploadable);
		}
	}
	
	public void queueAlteration(IAlteration alteration)
	{
		alterationQueue.add(alteration);
	}
	
	public int getUploadQueueSize()
	{
		return uploadQueue.size();
	}
	
	public int getAlterationQueueSize()
	{
		return alterationQueue.size();
	}
	
	public GL2 attachDebugger(GL2 gl)
	{
		if (isDebugEnabled() && !debugAttached)
		{
			this.debugAttached = true;
			return (GL2) gl.getContext().setGL( 
				GLPipelineFactory.create("javax.media.opengl.Trace", null, gl, 
					new Object[] { new PrintStream(debugStream) }));
		}
		else
		{
			return gl;
		}
	}
	
	
	private void drawReplacementShape(Box bounds, OpenGLContext c)
	{
		if (bounds != null)
		{
			c.gl().glEnable(GL2.GL_DEPTH_TEST);
			
			FloatMatrix color = FloatMatrix.vector(0.5f, 0.8f, 0.7f, 1.0f);
			WireBox box = new WireBox("replacement", bounds, color);
			box.draw(c);
		}
	}
	
	public void drawReplacement(IDrawable drawable, OpenGLContext c)
	{
		if (drawable != null)
		{
			drawReplacementShape(drawable.getBounds(), c);
		}
	}
	
	public void display(List<IDrawable> drawables, OpenGLContext c)
	{
		try
		{
			
		
			synchronized (this.getDisplayMutex())
			{
				pushDebug("Displaying", this);
				
				/*
				 * Allow alpha blending
				 */
				c.gl().glEnable(GL.GL_BLEND);
				c.gl().glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
		
				
				while (this.getAlterationQueueSize() > 0)
				{
					if (!alterationQueue.isEmpty())
					{
						IAlteration alteration = alterationQueue.remove();
						pushDebug("Processing alteration", alteration);
						
						alteration.alter();
						
						popDebug();
					}
				}		
				
				for (int i = 0; i < this.dequeueSize && getUploadQueueSize() > 0; i++)
				{
					if (!uploadQueue.isEmpty())
					{
						IUploadable uploadable = uploadQueue.remove();
						pushDebug("Uploading uploadable", uploadable);
						
						uploadable.upload(c);
						
						popDebug();
					}
				}
				
				if (drawables != null)
				{
					for (IDrawable drawable : drawables)
					{
						pushDebug("Rendering drawable", drawable);
						if (drawable.isVisible())
						{
							if (drawable.isReady())
							{
								drawable.draw(c);
							}
							else
							{
								drawReplacement(drawable, c);
							}
						}
						
						logDebugScreens(this, c);
						
						popDebug();
					}
				}		
				
				OpenGLStack.validateStack();		
				
				popDebug();
				
				IncidentManager.cutover();
			}
		
		}
		catch (Throwable e)
		{
			IncidentManager.getInstance().notifyIncident(Incident.newError("Failed to display", e));
			throw e;
		}
	}
	
	public void release(List<IDrawable> drawables, OpenGLContext c)
	{
		pushDebug("Releasing drawables", this);
		for (IDrawable drawable : drawables)
		{
			pushDebug("Releasing", drawable);
			drawable.release(c);
			popDebug();
		}
		popDebug();
		
		IncidentManager.cutover();
	}
	
	public String getGlslVersion(GL2 gl)
	{
		return gl.glGetString(GL2.GL_SHADING_LANGUAGE_VERSION);
	}
	
	public String getGlVersion(GL2 gl)
	{
		return gl.glGetString(GL2.GL_VERSION);
	}
	
	protected StackTraceElement[] getDebugCallStack(CallStack stack)
	{
		List<StackTraceElement> items = new ArrayList<StackTraceElement>();
		for (StackTraceElement element : stack.stackTrace)
		{
			String elementLine = element.toString();
			if (!elementLine.contains("orion.sdk.graphics.util.OpenGLManager.addCallStack") &&
				 !elementLine.contains("orion.sdk.graphics.util.OpenGLContext.gl") &&
				 !elementLine.contains("orion.sdk.graphics.util.OpenGLManager.getDebugCallStack") &&
				 !elementLine.contains("orion.sdk.graphics.util.OpenGLManager.logDebug")
				 )
			{
				items.add(element);
			}
			if (elementLine.contains("orion.sdk.graphics.util.OpenGLManager.render"))
			{
				break;
			}
		}
		return items.toArray(new StackTraceElement[items.size()]);
	}
	
	public StackTraceElement[] getDebugCallStack()
	{
		return this.getDebugCallStack(CallStack.getTrace("Debug stack"));
	}
		
	public void pushDebug(String message, INamed object, Object... attachments)
	{
		if (isDebugEnabled())
		{
			String msg = message;
			if (object != null)
			{
				msg += ": " + getShortDescription(object);				
			}
			
			List attachmentsList = new LinkedList(Arrays.asList(attachments)); 
			attachmentsList.add(this.getDebugCallStack());
			
			Incident incident = Incident.newDebug(msg, attachmentsList.toArray());
			IncidentManager.notifyIncident(incident);
			IncidentManager.pushLevel();
		}		
	}		

	public void popDebug()
	{
		if (isDebugEnabled())
		{
			IncidentManager.popLevel();
		}		
	}
	
	public void logDebugIncident(Incident incident)
	{
		if (isDebugEnabled())
		{
			IncidentManager.notifyIncident(incident);
		}
	}
	
	public void logDebugScreens(INamed object, OpenGLContext c)
	{
		if (isDebugEnabled())
		{
			this.pushDebug("Taking screenshots", object);
			
			BufferedImage colorComponent = OpenGLBuffers.getColorBufferImage(c);
			BufferedImage depthComponent = OpenGLBuffers.getDepthBufferImage(c);
			
			this.popDebug();
			
			Incident debugInfo = Incident.newInformation(
				"Debug screens: " + getShortDescription(object),
				new Object[] { new BufferedImage[] {colorComponent, depthComponent} });
			IncidentManager.notifyIncident(debugInfo);
		}		
	}
	
	@Override
	public String getName()
	{
		return "OpenGLManager";
	}
	
	protected class OpenGlDebugStream extends OutputStream
	{
		public static final int BUFFER_SIZE = 1024;
		public static final char NL = '\n';
		public static final char CR = '\r';
		
		protected byte[] buffer = new byte[BUFFER_SIZE];
		protected int pos = 0;
		
		
		@Override
		public void write(int b) throws IOException
		{
			char c = (char) b; 
			buffer[pos++] = (byte) b;
			if (pos > BUFFER_SIZE - 1)
			{
				cutover();
			}
			
			if (c == NL || c == CR)
			{
				cutover();
			}			
		}
		
		protected String replaceConstants(String line)
		{
			String current = line;
			Map<String, String> openGLConstants = getGLConstants();
			for (String hex : openGLConstants.keySet())
			{
				String constant = openGLConstants.get(hex);
				while (current.contains(hex + ","))
				{
					current = current.replaceAll(hex + ",", hex + " ." + constant + "." + ",");
				}
				
				while (current.contains(hex + ")"))
				{
					current = current.replaceAll(hex + "\\)", hex + " ." + constant + "." + ")");
				}
			}
			return current;
		}
		
		protected void cutover()
		{
			/*
			 * Read the next line from the debug stream
			 */
			byte[] glBytes = new byte[pos];
			System.arraycopy(buffer, 0, glBytes, 0, pos);
			pos = 0;			
			String glLine = new String(glBytes).trim();
			glLine = replaceConstants(glLine);
			
			/*
			 * Log a OpenGL incident and attach the current stack. Don't log
			 * glGetError calls and exclude the trace lines used for the OpenGL
			 * debug framework.
			 */
			if (glLine.length() > 0 && !glLine.contains("glGetError"))
			{
				List<CallStack> stacks = OpenGLManager.this.debugStack;
				List<Object> traces = new ArrayList<Object>();

				if (stacks.size() > 0)
				{					
					for (int i = 0; i < stacks.size(); i++)
					{
						CallStack stack = stacks.get(i);
						StackTraceElement[] elements = getDebugCallStack(stack);
						boolean skip = false;
						for (StackTraceElement element : elements)
						{
							String elementLine = element.toString();
							if (elementLine.contains("checkError"))
							{
								skip = true;
								break;
							}
						}
						if (skip)
						{
							continue;
						}
						
						traces.add(elements);
					}
				}
				
				stacks.clear();
				
				Incident incident = new Incident(Incident.Type.OPENGL, glLine, traces.toArray(new Object[traces.size()]));				
				IncidentManager.notifyIncident(incident);
			}
		}
	
	}	
}
