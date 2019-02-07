package orion.sdk.graphics.scenes;

import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GLException;

import orion.sdk.events.IUpdatable;
import orion.sdk.graphics.drawables.IDrawable;
import orion.sdk.graphics.shading.glsl.AShader;
import orion.sdk.graphics.shading.glsl.GenericShader;
import orion.sdk.graphics.shading.glsl.GenericShader.Capabilities;
import orion.sdk.graphics.shading.lighting.Material;
import orion.sdk.graphics.util.OpenGLContext;
import orion.sdk.graphics.util.OpenGLManager;
import orion.sdk.graphics.util.OpenGLStack;
import orion.sdk.graphics.viewing.cameras.ACamera;
import orion.sdk.math.FloatMatrix;
import orion.sdk.math.geometry.Box;

public class Scene implements IDrawable, IUpdatable
{
	private List<IDrawable> drawables = new ArrayList<IDrawable>();
	private ACamera camera = null;
	protected boolean ready = true;
	protected String name = null;
	
	protected AShader baseShader = null;
	protected Material baseMaterial = null;

	public Scene(String name, ACamera camera)
	{
		this.setCamera(camera);
		this.name = name;
		
		Capabilities capabilities = new Capabilities();
		capabilities.mapAmbient = true;
		capabilities.mapDiffuse = true;
		capabilities.mapSpecular = true;
		capabilities.mapEnvironment = false;
		capabilities.mapNormal = false;
		this.baseShader = new GenericShader(name + ".baseShader", null, capabilities);
		
		this.baseMaterial = new Material(name + ".baseMaterial");
		this.baseMaterial.ambient = FloatMatrix.vector(1, 1, 1, 1);
	}
	
	public AShader getBaseShader()
	{
		return baseShader;
	}
	
	public Material getBaseMaterial()
	{
		return baseMaterial;
	}
	
	public IDrawable getDrawable(String name)
	{
		synchronized (getDrawables())
		{
			for (int i = 0; i < getDrawables().size(); i++)
			{
				IDrawable drawable = getDrawables().get(i);
				if (drawable.getName().equals(name))
				{
					return drawable;
				}
			}
		}
		
		return null;
	}
	
	@Override
	public void update(float dt) throws Exception
	{
		if (shouldUpdate())
		{
			synchronized (getDrawables())
			{
				for (IDrawable drawable : getDrawables())
				{
					if (drawable instanceof IUpdatable)
					{
						IUpdatable updatable = (IUpdatable) drawable;
						updatable.update(dt);
					}
				}
			}
		}
	}

	@Override
	public void draw(OpenGLContext c) throws GLException
	{
		OpenGLManager.getInstance().pushDebug("Drawing", this);
		
		/*
		 * Push the material to be used for the whole scene
		 */
		OpenGLStack.push(Material.class, this.getBaseMaterial(), c);
		
		/*
		 * Push the shader to be used for the whole scene
		 */
		OpenGLStack.push(AShader.class, this.getBaseShader(), c);
		
		/*
		 * Push the camera
		 */
		OpenGLStack.push(ACamera.class, this.getCamera(), c);		

		/*
		 * Render each drawable if there are any
		 */
		if (this.getDrawables() != null)
		{
			for (int i = 0; i < this.getDrawables().size(); i++)
			{
				IDrawable drawable = this.getDrawables().get(i);
				if (drawable.isVisible() && drawable.isReady())
				{
					drawable.draw(c);
					
					OpenGLManager.getInstance().logDebugScreens(this, c);
				}
			}
		}
				
		/*
		 * Pop the camera
		 */
		OpenGLStack.pop(ACamera.class, this.getCamera(), c);		

		/*
		 * Pop the shader used for the whole scene
		 */
		OpenGLStack.pop(AShader.class, this.getBaseShader(), c);
		
		/*
		 * Pop the material used for the whole scene
		 */
		OpenGLStack.pop(Material.class, this.getBaseMaterial(), c);
		
		OpenGLManager.getInstance().popDebug();
	}

	@Override
	public void upload(OpenGLContext c) throws GLException
	{
		OpenGLManager.getInstance().pushDebug("Uploading", this);
		if (getDrawables() != null)
		{
			for (int i = 0; i < getDrawables().size(); i++)
			{
				IDrawable drawable = getDrawables().get(i);
				drawable.upload(c);
			}
		}
		OpenGLManager.getInstance().popDebug();
	}

	@Override
	public float getUpdatePeriod()
	{
		return 0;
	}

	@Override
	public void release(OpenGLContext c) throws GLException
	{		
		OpenGLManager.getInstance().pushDebug("Releasing", this);
		if (getDrawables() != null)
		{
			for (int i = 0; i < getDrawables().size(); i++)
			{				
				IDrawable drawable = getDrawables().get(i);
				drawable.release(c);				
			}
		}
		OpenGLManager.getInstance().popDebug();
	}

	@Override
	public int getFaceCount()
	{
		int faces = 0;
		synchronized (getDrawables())
		{
			for (IDrawable drawable : getDrawables())
			{
				faces += drawable.getFaceCount();
			}
		}
		return faces;
	}

	@Override
	public String getName()
	{
		return this.name;
	}

	@Override
	public boolean shouldUpdate()
	{
		return true;
	}

	@Override
	public boolean isReady()
	{
		return true;
	}

	@Override
	public Box getBounds()
	{
		return null;
	}

	@Override
	public boolean isVisible()
	{
		return ready;
	}

	public ACamera getCamera()
	{
		return camera;
	}

	public void setCamera(ACamera camera)
	{
		this.camera = camera;
	}

	public List<IDrawable> getDrawables()
	{
		return drawables;
	}

	public void setDrawables(List<IDrawable> drawables)
	{
		this.drawables = drawables;
	}
}
