package orion.sdk.graphics.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;

import junit.framework.Assert;
import orion.sdk.assets.AssetManager;
import orion.sdk.graphics.drawables.IDrawable;
import orion.sdk.monitoring.incidents.Incident;
import orion.sdk.monitoring.incidents.IncidentManager;
import orion.sdk.util.Shell;

public abstract class OpenGLTestCase implements INamed
{	
	private OpenGLTestBed testBed = null;

	private Object testMutex = null;
	private Object assetMutex = null;
	
	private boolean assetsLoaded = false;
	
	@BeforeClass
	public static void beforeClass() throws Exception
	{
		OpenGLManager.getInstance().setDebugEnabled(true);
	}
	
	@Rule
	public TestName testName = new TestName();
	
	@Before
	public void beforeTest() throws Exception
	{
		OpenGLManager.getInstance().pushDebug("Running test case", this);
		
		AssetManager.clearCache();
		this.testMutex = new Object();
		this.assetMutex = new Object();
		this.testBed = new OpenGLTestBed(this);
		this.beginTest();
	}
	
	@After
	public void afterTest() throws Exception
	{
		this.endTest();
		this.testBed.dispose();
		
		OpenGLManager.getInstance().popDebug();
	}
	
	@Override
	public String getName()
	{
		return this.testName.getMethodName();
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
	

	private void beginTest() throws InterruptedException
	{
		/*
		 * Wait for test bed to start rendering
		 */
		this.wait(this.testBed.getRenderMutex(), "[Test case] waiting for [test bed] to start drawing");
		
		/*
		 * Notify test bed that testing started
		 */
		this.notify(this.getTestMutex(), "[Test case] notifying [test bed] that testing started");
	}
	
	private void endTest()
	{
		/*
		 * Notify test bed that testing is done.
		 */
		this.notify(this.getTestMutex(), "[Test case] notifying [test bed] that testing ended");
	}
	
	public void beginAssetLoad()
	{
		if (!this.isAssetsLoaded())
		{
			this.wait(this.getAssetMutex(), "[Test case] waiting for [test case] to load assets");
		}
	}
	
	public void endAssetLoad()
	{
		this.assetsLoaded = true;
		this.notify(this.getAssetMutex(), "[Test case] notifying [test case] that assets are loaded");
	}
	
	public boolean isAssetsLoaded()
	{
		return assetsLoaded;
	}
	
	public void render() throws InterruptedException
	{
		/*
		 * Notify test bed that rendering is required
		 */
		this.notify(this.testBed.getRenderMutex(), "[Test case] notifying [test bed] that rendering is required");
		
		/*
		 * Wait for test bed to finish rendering
		 */
		this.wait(this.getTestMutex(), "[Test case] waiting for [test bed] to finnish rendering");
	}

	public Object getTestMutex()
	{
		return this.testMutex;
	}
	
	public Object getAssetMutex()
	{
		return assetMutex;
	}
	
	public List<IDrawable> getDrawables()
	{
		return testBed.getDrawables();
	}
	
	public void setDrawables(List<IDrawable> drawables)
	{
		testBed.setDrawables(drawables);
	}
	
	public float getScreenWidth()
	{
		return testBed.getScreenWidth();
	}
	
	public float getScreenHeight()
	{
		return testBed.getScreenHeight();
	}
	
	protected File getExpectedFile() throws Exception
	{
		File expectedDir = new File(
				"tests" + File.separator +
				"junits" + File.separator +
				"resources" + File.separator +
				"expected"); 
		
		Shell.makeDirectories(expectedDir);
		
		return new File(
				expectedDir + File.separator +
				this.getClass().getSimpleName() + "." + this.getName() +
				".png");
	}
	
	protected File getActualFile() throws Exception
	{
		File actualDir = new File(
				"tests" + File.separator +
				"junits" + File.separator +
				"resources" + File.separator +
				"actual"); 
		
		Shell.makeDirectories(actualDir);

		return new File(
				actualDir + File.separator +
				this.getClass().getSimpleName() + "." + this.getName() +
				".png");
	}
	
	protected BufferedImage getLastFrame()
	{
		return this.testBed.getLastFrame();
	}
	
	protected void saveLastFrameAsExpected() throws Exception
	{
		BufferedImage image = this.getLastFrame();
		if (image != null)
		{
			ImageIO.write(image, "PNG", this.getExpectedFile());
		}
	}		
	
	protected void saveLastFrameAsActual() throws Exception
	{
		BufferedImage image = this.getLastFrame();
		if (image != null)
		{
			ImageIO.write(image, "PNG", this.getActualFile());
		}
	}	

	protected BufferedImage getExpected() throws Exception
	{
		File expectedFile = this.getExpectedFile();
		File actualFile = this.getExpectedFile();
		if (expectedFile.exists())
		{
			BufferedImage image = ImageIO.read(expectedFile);
			return image;
		}
		else
		{
			saveLastFrameAsActual();
			Assert.fail("No expected frame could be found. One was created with the last frame output at '" + actualFile + "'.");
			return null;
		}
	}
	
	protected void assertLastFrameExpected() throws Exception
	{
		BufferedImage expected = this.getExpected();
		BufferedImage actual = this.getLastFrame();
		Assert.assertNotNull("Actual frame should not be null");
		Assert.assertEquals("Actual frame width and expected frame width should be equal", expected.getWidth(), actual.getWidth());
		Assert.assertEquals("Actual frame height and expected frame height should be equal", expected.getHeight(), actual.getHeight());
		
		int 
			width = expected.getWidth(),
			height = actual.getHeight();
		boolean match = true;
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				int expectedPixel = expected.getRGB(x, y);
				int actualPixel = actual.getRGB(x, y);
				match &= (expectedPixel == actualPixel);
				if (!match)
				{
					saveLastFrameAsActual();
					Assert.fail(
						"Actual frame pixel and expected frame pixel should be equal. " +
						"The actual frame has ben saved to '" + this.getActualFile() + "'");
				}
			}
		}
	}
}
