package orion.client.gui;

import java.awt.Dimension;
import java.awt.Point;
import java.util.List;

import javax.swing.JFrame;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.gl2.GLUgl2;
import com.jogamp.opengl.util.FPSAnimator;

import orion.sdk.Version;
import orion.sdk.graphics.buffers.VertexBuffer;
import orion.sdk.graphics.drawables.IDrawable;
import orion.sdk.graphics.util.OpenGLContext;
import orion.sdk.graphics.util.OpenGLManager;
import orion.sdk.input.InputManager;
import orion.sdk.monitoring.incidents.Incident;
import orion.sdk.monitoring.incidents.IncidentManager;
import orion.sdk.monitoring.performance.PerformanceManager;
import orion.sdk.util.TpsCounter;

@SuppressWarnings("serial")
public class OpenGLFrame extends JFrame implements GLEventListener
{
	protected static int TARGET_FPS = 60;
	
	protected GLCanvas glCanvas = null;
	protected long lastDisplay = System.currentTimeMillis();
	protected float dt = 0;
	protected TpsCounter fpsCounter = new TpsCounter(0.1f, 10);
	
	public OpenGLFrame()
	{		
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setPreferredSize(new Dimension(1000, 1000));
		setLocation(new Point(100, 0));
		glCanvas = new GLCanvas(OpenGLFrame.getGlCapabilities());
		glCanvas.addGLEventListener(this);
		add(glCanvas);
		pack();
		
		FPSAnimator animator = new FPSAnimator(glCanvas, TARGET_FPS);
		animator.start();
		
		InputManager.register(glCanvas);
	}

	public static void main(String[] args) throws Exception
	{
		(new OpenGLFrame()).setVisible(true);
		Client client = Client.getInstance(); 
		client.initialize();
		client.start();
	}

	public static GLCapabilities getGlCapabilities()
	{
		GLProfile glProfile = GLProfile.getDefault();
		return new GLCapabilities(glProfile);
	}

	static
	{
		GLProfile.initSingleton();
	}

	@Override
	public void init(GLAutoDrawable drawable)
	{
		GL2 gl = drawable.getGL().getGL2();
		String glslVersion = OpenGLManager.getInstance().getGlslVersion(gl);
		String glVersion = OpenGLManager.getInstance().getGlVersion(gl);
		IncidentManager.notifyIncident(Incident.newInformation("GLSL version: '" + glslVersion + "'"));
		IncidentManager.notifyIncident(Incident.newInformation("GL version: '" + glVersion + "'"));
	}

	@Override
	public void dispose(GLAutoDrawable drawable)
	{
		Client game = Client.getInstance();
		game.dispose(getOpenGLContext(drawable));
	}
	
	protected OpenGLContext getOpenGLContext(GLAutoDrawable drawable)
	{
		GL2 gl = drawable.getGL().getGL2();
		GLUgl2 glu = new GLUgl2();

		gl = OpenGLManager.getInstance().attachDebugger(gl);
		
		OpenGLContext c = new OpenGLContext(
			gl, 
			glu, 
			dt, 
			new float[] {0, 0, glCanvas.getWidth(), glCanvas.getHeight()});
		
		return c;
	}

	@Override
	public void display(GLAutoDrawable drawable)
	{
		/*
		 * Initialize and draw each drawable
		 */
		try
		{
			List<IDrawable> drawables = Client.getInstance().drawables;
			
			OpenGLManager.getInstance().display(drawables, getOpenGLContext(drawable));
		}
		catch (Exception e)
		{
			IncidentManager.notifyIncident(Incident.newError(e.getMessage(), e));
			dispose();
			System.exit(-1);
		}
		
		dt = (System.currentTimeMillis() - lastDisplay) / 1000f;
		lastDisplay = System.currentTimeMillis();
		fpsCounter.tick();
		
		/*
		 * Set title with fps counter
		 */
		setTitle(Version.TITLE + " v" + Version.getVersion() + " - " + 
			(int) fpsCounter.getTps() + " fps - " +	
			(int) Client.getInstance().guiLoop.getUpdatesPerSecond() + " ups - " + 
			VertexBuffer.getBufferCount() + " vbuffers - " + 
			String.format("%,d", Client.getInstance().getFaceCount()) + " faces - " + 
			String.format("%.2f", PerformanceManager.getMemoryUsage()) + "% memory used - " +
			OpenGLManager.getInstance().getUploadQueueSize() + " uploads queued");
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
	{
		Client.getInstance().screenWidth = width;
		Client.getInstance().screenHeight = height;
	}
}
