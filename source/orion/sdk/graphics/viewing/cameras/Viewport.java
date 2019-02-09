package orion.sdk.graphics.viewing.cameras;

import java.util.ArrayList;
import java.util.List;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLException;

import orion.sdk.graphics.drawables.IDrawable;
import orion.sdk.graphics.scenes.Scene;
import orion.sdk.graphics.util.IStackable;
import orion.sdk.graphics.util.OpenGLContext;
import orion.sdk.graphics.util.OpenGLManager;
import orion.sdk.graphics.util.OpenGLStack;
import orion.sdk.math.FloatMatrix;
import orion.sdk.math.geometry.Box;

public class Viewport implements IDrawable, IStackable
{
	private float left = 0;
	private float top = 0;
	private float width = 400;
	private float height = 300;
	private List<Scene> scenes= null;
	private boolean visible = true;
	private String name = "unknown";
	
	public boolean clear = true;
	public FloatMatrix clearColor = FloatMatrix.vector(0f, 0f, 0f, 0f);
	
	public Viewport(String name)
	{
		this.name = name;
		this.scenes = new ArrayList<Scene>();
	}
	
	@Override
	public void draw(OpenGLContext c)
	{
		OpenGLManager.getInstance().pushDebug("Drawing", this);
		
		/*
		 *Push the viewport
		 */
		OpenGLStack.push(Viewport.class, this, c);
		
		/*
		 * Clear the viewport
		 */
		if (this.clear)
		{
			c.gl().glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);			
		}
		
		/*
		 * Render the scene
		 */
		List<Scene> scenes = this.getScenes();
		for (Scene scene : scenes)
		{
			if (scene != null && scene.isVisible() && scene.isReady())
			{
				OpenGLContext subC = c.copy();
				subC.viewport = new float[] {c.viewport[0] + this.getLeft(), c.viewport[1] + this.getTop(), this.getWidth(), this.getHeight()};
				scene.draw(subC);
			}
		}
		
		/*
		 * Pop the viewport
		 */
		OpenGLStack.pop(Viewport.class, this, c);
		
		OpenGLManager.getInstance().popDebug();
	}

	@Override
	public void upload(OpenGLContext c) throws GLException
	{
		OpenGLManager.getInstance().pushDebug("Uploading", this);
		List<Scene> scenes = this.getScenes();
		for (Scene scene : scenes)
		{
			if (scene != null)
			{
				scene.upload(c);
			}
		}
		OpenGLManager.getInstance().popDebug();
	}

	@Override
	public void release(OpenGLContext c) throws GLException
	{
		OpenGLManager.getInstance().pushDebug("Releasing", this);
		List<Scene> scenes = this.getScenes();
		for (Scene scene : scenes)
		{
			if (scene != null)
			{
				scene.release(c);
			}
		}
		OpenGLManager.getInstance().popDebug();
	}

	@Override
	public int getFaceCount()
	{
		int count = 0;
		List<Scene> scenes = this.getScenes();
		for (Scene scene : scenes)
		{
			if (scene != null)
			{
				count += scene.getFaceCount();
			}
		}
		
		return count;
	}

	@Override
	public String getName()
	{
		return name;
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
		return visible;
	}

	@Override
	public void apply(OpenGLContext c)
	{
		/*
		 * Set the viewport boundries
		 */
		c.gl().glViewport((int) getLeft(), (int) getTop(), (int) getWidth(),  (int) getHeight());

		/*
		 * Clear the required OpenGL buffers
		 */
		if (clear)
		{
			c.gl().glClearColor(
				clearColor.getX(),
				clearColor.getY(),
				clearColor.getZ(),
				clearColor.getU());
		}			
	}

	@Override
	public void clear(OpenGLContext c) throws GLException
	{
	}
	
	@Override
	public void push(OpenGLContext c) throws GLException
	{
		
	}
	
	@Override
	public void pop(OpenGLContext c) throws GLException
	{
		
	}

	public float getLeft()
	{
		return left;
	}

	public void setLeft(float left)
	{
		this.left = left;
	}

	public float getTop()
	{
		return top;
	}

	public void setTop(float top)
	{
		this.top = top;
	}

	public float getWidth()
	{
		return width;
	}

	public void setWidth(float width)
	{
		this.width = width;
	}

	public float getHeight()
	{
		return height;
	}

	public void setHeight(float height)
	{
		this.height = height;
	}

	public void setVisible(boolean visible)
	{
		this.visible = visible;
	}	
	
	public List<Scene> getScenes()
	{
		return scenes;
	}
	
	public void addScene(Scene scene)
	{
		this.scenes.add(scene);
	}
	
	public Scene getScene(int index)
	{
		return this.scenes.get(index);
	}
}
