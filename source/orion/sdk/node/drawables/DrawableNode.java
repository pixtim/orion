package orion.sdk.node.drawables;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLException;

import orion.sdk.events.IUpdatable;
import orion.sdk.graphics.drawables.IDrawable;
import orion.sdk.graphics.util.OpenGLContext;
import orion.sdk.graphics.util.OpenGLManager;
import orion.sdk.graphics.util.OpenGLStack;
import orion.sdk.graphics.viewing.ClippingPane;
import orion.sdk.math.FloatMatrix;
import orion.sdk.math.geometry.Box;
import orion.sdk.monitoring.performance.CallStack;
import orion.sdk.monitoring.performance.StackMonitor;
import orion.sdk.node.Node;
import orion.sdk.util.StructuredBinary;

public class DrawableNode extends Node implements IDrawable
{
	private IDrawable drawable = null;
	public boolean visible = true;
	private Box bounds;
	private boolean clipBoundsEnabled = false;
	private ClippingPane[] clippingPanes = new ClippingPane[6];
	
	private boolean enableAlphaBlending = false;
	private boolean enableDepthTest = true;
	private boolean enableFaceCulling = true;
	private boolean ready = false;

	public DrawableNode(String name)
	{
		super(name);
	}

	public DrawableNode(String name, IDrawable drawable)
	{
		super(name);
		
		this.setDrawable(drawable);
	}
	
	public void setDrawable(IDrawable drawable)
	{
		this.drawable = drawable;
		
		if (drawable != null)
		{
			this.setBounds(drawable.getBounds());
		}				
	}

	public boolean shouldEnableAlphaBlending()
	{
		return enableAlphaBlending;
	}

	public void setEnableAlphaBlending(boolean enableAlphaBlending)
	{
		this.enableAlphaBlending = enableAlphaBlending;
	}

	public boolean shouldEnableDepthTest()
	{
		return enableDepthTest;
	}

	public void setEnableDepthTest(boolean enableFaceCulling)
	{
		this.enableFaceCulling  = enableFaceCulling;
	}
	
	public boolean shouldEnableFaceCulling()
	{
		return enableFaceCulling;
	}

	public void setEnableFaceCulling(boolean enableFaceCulling)
	{
		this.enableFaceCulling = enableFaceCulling;
	}
	
	

	@Override
	public boolean shouldUpdate()
	{
		return visible;
	}

	@Override
	public Box getBounds()
	{
		return bounds;
	}
	
	public void setBounds(Box bounds)
	{
		this.bounds = bounds;
	}
	
	@Override
	public boolean isVisible()
	{
		return visible;
	}
	
	
	public void setEnableClippingBounds(boolean clipBounds)
	{
		this.clipBoundsEnabled = clipBounds;
	}
	
	public boolean getEnableClippingBounds()
	{
		return this.clipBoundsEnabled;
	}	
	
	

	@Override
	public void upload(OpenGLContext c) throws GLException
	{
		OpenGLManager.getInstance().pushDebug("Uploading", this);
		CallStack stack = StackMonitor.beginMethod("upload " + name);
		if (!isReady())
		{
			if (getDrawable() != null)
			{
				getDrawable().upload(c);
			}
			
			ready = true;			
		}
		StackMonitor.endMethod(stack);
		OpenGLManager.getInstance().popDebug();
	}

	@Override
	public void release(OpenGLContext c) throws GLException
	{
		OpenGLManager.getInstance().pushDebug("Releasing", this);
		if (getDrawable() != null)
		{
			getDrawable().release(c);
		}
		OpenGLManager.getInstance().popDebug();
	}
	
	@Override
	public boolean isReady()
	{
		return ready;
	}


	@Override
	public int getFaceCount()
	{
		int faces = 0;
		if (getDrawable() != null)
		{
			faces += getDrawable().getFaceCount();			
		}
		
		return faces;
	}

	@Override
	public String getName()
	{
		return name;
	}

	public IDrawable getDrawable()
	{
		return drawable;
	}
	

	@Override
	public void update(float dt) throws Exception
	{
		if (shouldUpdate())
		{
			IDrawable drawable = getDrawable();
			if (drawable != null && drawable instanceof IUpdatable)
			{
				IUpdatable updatable = (IUpdatable) drawable;
				updatable.update(dt);
			}
		}
	}

	@Override
	public void read(StructuredBinary binary) throws Exception
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public StructuredBinary write() throws Exception
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public float getUpdatePeriod()
	{
	return 0;
	}

	@Override
	public void draw(OpenGLContext c) throws GLException
	{
		this.pushNode(c);
		CallStack trace = StackMonitor.beginMethod("draw " + name);

		/*
		 * Enable states
		 */
		if (shouldEnableDepthTest())
		{
			c.gl().glEnable(GL2.GL_DEPTH_TEST);
		}
		
		if (shouldEnableAlphaBlending())
		{
			c.gl().glEnable(GL2.GL_BLEND);
			c.gl().glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
		}
		
		if (shouldEnableFaceCulling())
		{
			c.gl().glEnable(GL2.GL_CULL_FACE);
			c.gl().glCullFace(GL2.GL_BACK);
		}
		
		/*
		 * Draw node
		 */
		IDrawable drawable = getDrawable();
		
		if (drawable == null || drawable.isVisible() && !drawable.isReady())
		{
			/*
			 * The drawable is not ready yet. Draw a replacement.
			 */
			OpenGLManager.getInstance().drawReplacement(this, c);
		}
		else
		{
			/*
			 * Draw the drawable if possible
			 */
			if (drawable != null && drawable.isVisible())
			{
				if (drawable.isReady())
				{	
					/*
					 * Push clipping panes if required.
					 */
					pushClippingBounds(c);
					
					/*
					 * Draw the drawable
					 */
					drawable.draw(c);
					
					/*
					 * Pop clipping panes if required.
					 */
					popClippingBounds(c);
				}				
			}
		}

		
		/*
		 * Disable states
		 */
		if (shouldEnableDepthTest())
		{
			c.gl().glDisable(GL2.GL_DEPTH_TEST);
		}
		
		if (shouldEnableAlphaBlending())
		{
			c.gl().glDisable(GL2.GL_BLEND);
		}
		
		if (shouldEnableFaceCulling())
		{
			c.gl().glDisable(GL2.GL_CULL_FACE);
		}
		
		this.popNode(c);
		StackMonitor.endMethod(trace);
	}
	
	
	public void pushClippingBounds(OpenGLContext c)
	{
		if (getEnableClippingBounds())
		{
			Box bounds = getBounds();
			FloatMatrix[] positions = new FloatMatrix[]
			{
				/*
				 * Left and right sides
				 */
				FloatMatrix.vector(bounds.getLeft(), bounds.getBottom(), bounds.getBack(), 1),
				FloatMatrix.vector(bounds.getRight(), bounds.getBottom(), bounds.getBack(), 1),
				/*
				 * Bottom and top sides
				 */
				FloatMatrix.vector(bounds.getLeft(), bounds.getBottom(), bounds.getBack(), 1),
				FloatMatrix.vector(bounds.getLeft(), bounds.getTop(), bounds.getBack(), 1),
				/*
				 * Back and front sides
				 */
				FloatMatrix.vector(bounds.getRight(), bounds.getTop(), bounds.getBack(), 1),
				FloatMatrix.vector(bounds.getRight(), bounds.getTop(), bounds.getFront(), 1)
			};
			FloatMatrix[] normals = new FloatMatrix[]
			{
					/*
					 * Left and right normals
					 */
					FloatMatrix.vector( 1f,  0f,  0f, 0f),
					FloatMatrix.vector(-1f,  0f,  0f, 0f),
					/*
					 * Bottom and top normals
					 */
					FloatMatrix.vector( 0f,  1f,  0f, 0f),
					FloatMatrix.vector( 0f, -1f,  0f, 0f),
					/*
					 * Back and front normals
					 */
					FloatMatrix.vector( 0f,  0f,  1f, 0f),
					FloatMatrix.vector( 0f,  0f, -1f, 0f),
			};
			for (int i = 0; i < 6; i++)
			{
				ClippingPane pane = new ClippingPane(normals[i], positions[i], i);
				this.clippingPanes[i] = pane;
				OpenGLStack.push(ClippingPane.class, pane, c);
			}
		}
	}
	
	public void popClippingBounds(OpenGLContext c)
	{
		if (getEnableClippingBounds())
		{
			for (int i = 6 - 1; i >= 0; i--)
			{
				ClippingPane pane = this.clippingPanes[i];
				OpenGLStack.pop(ClippingPane.class, pane, c);
			}
		}
	}	
}
