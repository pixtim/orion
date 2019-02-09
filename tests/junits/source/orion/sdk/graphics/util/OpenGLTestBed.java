package orion.sdk.graphics.util;

import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLException;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.gl2.GLUgl2;
import javax.swing.JFrame;

import orion.sdk.graphics.drawables.IDrawable;
import orion.sdk.input.InputManager;
import orion.sdk.monitoring.incidents.Incident;
import orion.sdk.monitoring.incidents.IncidentManager;

import com.jogamp.opengl.util.Animator;

/**
 * Provides an environment for running OpenGL enabled test cases via JUnit
 * extensions of {@link OpenGLTestCase}
 * <p>
 * Note: Multipass rendering is not supported yet.
 * 
 * @author Tim
 * 
 */
@SuppressWarnings("serial")
public class OpenGLTestBed extends JFrame implements GLEventListener, INamed
{
	public static final long TIMEOUT = 5000;
	
	protected List<IDrawable> drawables = new LinkedList<IDrawable>();
	
	protected GLCanvas glCanvas = null;	
	protected long lastDisplay = System.currentTimeMillis();
	protected float dt = 0;	
	protected float screenWidth = 0;
	protected float screenHeight = 0;
	protected BufferedImage lastRender = null;
	
	private Object renderMutex = new Object();
	private OpenGLTestCase testCase = null;
	
	public OpenGLTestBed(OpenGLTestCase testCase) throws Exception
	{
		this.testCase = testCase;
		OpenGLManager.getInstance().invalidateDebugger();		
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setBounds(100, 100, 400, 300);
		setUndecorated(true);
		glCanvas = new GLCanvas(OpenGLTestBed.getGlCapabilities());
		glCanvas.addGLEventListener(this);
		add(glCanvas);

		Animator animator = new Animator(glCanvas);
		animator.setRunAsFastAsPossible(true);
		animator.start();
		
		InputManager.register(glCanvas);
		
		show();
		setVisible(false);
	}
	
	@Override
	public String getName()
	{
		return "Test bed";
	}
	
	public Object getRenderMutex()
	{
		return renderMutex;
	}
	
	public List<IDrawable> getDrawables()
	{
		return drawables;
	}
	
	public void setDrawables(List<IDrawable> drawables)
	{
		this.drawables = drawables;
	}
	
	public float getScreenWidth()
	{
		return screenWidth;
	}
	
	public float getScreenHeight()
	{
		return screenHeight;
	}

	private static GLCapabilities getGlCapabilities()
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
		IncidentManager.notifyIncident(Incident.newInformation("Initializing test bed"));
		
		GL2 gl = drawable.getGL().getGL2();
		String glslVersion = OpenGLManager.getInstance().getGlslVersion(gl);
		IncidentManager.notifyIncident(Incident.newInformation("GLSL version: '" + glslVersion + "'"));
	}

	@Override
	public void dispose(GLAutoDrawable drawable)
	{
		IncidentManager.notifyIncident(Incident.newInformation("Disposing of test bed"));

		OpenGLContext c = getOpenGLContext(drawable);
		OpenGLManager.getInstance().release(drawables, c);
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
	
	public BufferedImage getLastFrame()
	{
		return lastRender;
	}
	
	private void wait(Object mutex, String debugMessage)
	{
		OpenGLManager.getInstance().pushDebug(debugMessage, this);
		try
		{
			synchronized (mutex)
			{
				mutex.wait(OpenGLTestBed.TIMEOUT);
			}
		}
		catch (Exception e)
		{
			IncidentManager.notifyIncident(Incident.newError(e.getMessage(), e));
			dispose();
		}
		OpenGLManager.getInstance().popDebug();
	}
	
	private void notify(Object mutex, String debugMessage)
	{
		OpenGLManager.getInstance().pushDebug(debugMessage, this);
		synchronized (mutex)
		{
			mutex.notify();
		}
		OpenGLManager.getInstance().popDebug();
	}

	@Override
	public void display(GLAutoDrawable drawable)
	{
		/*
		 * Notify test case that we can start rendering
		 */
		notify(this.getRenderMutex(), "[Test bed] notifying [test case] that drawing has started");
		
		/*
		 * Wait for test case to start before continuing
		 */
		wait(this.testCase.getTestMutex(), "[Test bed] waiting for [test case] to start before rendering");
		
		/*
		 * Wait for test case to require rendering
		 */
		wait(this.getRenderMutex(), "[Test bed] waiting for [test case] to request render");

		/*
		 * Render a frame
		 */
		OpenGLContext c = getOpenGLContext(drawable);
		try
		{
			List<IDrawable> drawables = getDrawables();
			
			OpenGLManager.getInstance().display(drawables, c);
		}
		catch (Exception e)
		{
			IncidentManager.notifyIncident(Incident.newError(e.getMessage(), e));
			dispose();
		}
		
		dt = (System.currentTimeMillis() - lastDisplay) / 1000f;
		lastDisplay = System.currentTimeMillis();
		
		/*
		 * Read the last frame so that tests can inspect it
		 */
		lastRender = OpenGLBuffers.getColorBufferImage(c);
		
		/*
		 * Notify tests that the rendering is done
		 */
		notify(this.testCase.getTestMutex(), "[Test bed] notifying [test case] that rendering is done");
		
		/*
		 * Wait for tests to finish asserting before continuing
		 */
		wait(this.testCase.getTestMutex(), "[Test bed] waiting for [test case] to finnish (or render again) before next continuing render");
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
	{
		this.screenWidth = width;
		this.screenHeight = height;
	}
}
