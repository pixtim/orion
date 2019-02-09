package orion.sdk.node.containers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.jogamp.opengl.GLException;

import orion.sdk.graphics.drawables.IDrawable;
import orion.sdk.graphics.util.OpenGLContext;
import orion.sdk.graphics.util.OpenGLManager;
import orion.sdk.math.geometry.Box;
import orion.sdk.node.Node;
import orion.sdk.util.StructuredBinary;

public class ContainerNode extends Node implements IDrawable
{
	protected Map<String, Node> lookup = new HashMap<String, Node>();
	protected List<Node> children = new LinkedList<Node>();	
	protected boolean visible = true;
	
	public ContainerNode(String name)
	{
		this.name = name;
	}
	
	public void addChild(Node child)
	{
		synchronized (children)
		{
			synchronized (lookup)
			{
				OpenGLManager.getInstance().pushDebug("Adding " + OpenGLManager.getInstance().getShortDescription(child) + " to", this);
				children.add(child);
				lookup.put(child.name, child);
				child.setParent(this);
				OpenGLManager.getInstance().popDebug();
			}
		}
	}	

	public Node getChild(String name)
	{
		if (lookup.containsKey(name))
		{
			return lookup.get(name);
		}
		else
		{
			return null;
		}
	}
	
	public void removeChild(String name)
	{
		if (lookup.containsKey(name))
		{
			synchronized (lookup)
			{
				Node child = lookup.get(name);
				synchronized (children)
				{
					children.remove(child);
					lookup.remove(name);
				}
			}
		}
	}
	
	public boolean hasChild(String name)
	{
		return lookup.containsKey(name); 
	}

	@Override
	public void read(StructuredBinary binary) throws Exception
	{
		
	}

	@Override
	public StructuredBinary write() throws Exception
	{
		return null;
	}

	@Override
	public void update(float dt) throws Exception
	{
		synchronized (children)
		{				
			for (Node child : children)
			{
				child.update(dt);
			}
		}		
	}

	@Override
	public float getUpdatePeriod()
	{
		return 0;
	}

	@Override
	public boolean shouldUpdate()
	{
		return true;
	}

	@Override
	public void upload(OpenGLContext c) throws GLException
	{
		OpenGLManager.getInstance().pushDebug("Uploading", this);
		synchronized (children)
		{				
			for (Node child : children)
			{
				if (child instanceof IDrawable)
				{
					IDrawable drawableChild = (IDrawable) child;
					OpenGLManager.getInstance().queueUpload(drawableChild);
				}
			}
		}
		OpenGLManager.getInstance().popDebug();
	}

	@Override
	public void release(OpenGLContext c) throws GLException
	{
		OpenGLManager.getInstance().pushDebug("Releasing", this);
		synchronized (children)
		{				
			for (Node child : children)
			{
				if (child instanceof IDrawable)
				{
					IDrawable drawableChild = (IDrawable) child;
					drawableChild.release(c);
				}
			}
		}		
		OpenGLManager.getInstance().popDebug();
	}

	@Override
	public boolean isReady()
	{
		return true;
	}

	@Override
	public void draw(OpenGLContext c) throws GLException
	{
		OpenGLManager.getInstance().pushDebug("Drawing", this);
		
		synchronized (children)
		{				
			for (Node child : children)
			{
				if (child instanceof IDrawable)
				{					
					IDrawable drawableChild = (IDrawable) child;
					
					drawableChild.draw(c);
					
					OpenGLManager.getInstance().logDebugScreens(this, c);
				}
			}
		}
		
		OpenGLManager.getInstance().popDebug();
	}

	@Override
	public int getFaceCount()
	{
		int sum = 0;
		synchronized (children)
		{				
			for (Node child : children)
			{
				if (child instanceof IDrawable)
				{
					IDrawable drawableChild = (IDrawable) child;
					if (drawableChild.isVisible() && drawableChild.isReady())
					{
						sum += drawableChild.getFaceCount();
					}
				}
			}
		}
		return sum;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public Box getBounds()
	{
		return null;
	}

	@Override
	public boolean isVisible()
	{
		return this.visible;
	}

	protected void setVisible(boolean visible)
	{
		this.visible = visible;
	}
	
	@Override
	public List<Node> findNodes(Class<?> nodeClass) throws Exception
	{
		List<Node> nodes = new ArrayList<Node>();
		
		synchronized (this.children)
		{				
			for (Node child : children)
			{
				if (nodeClass.isInstance(child))
				{
					nodes.add(child);
				}
			}
		}
		
		nodes.addAll(super.findNodes(nodeClass));
		
		return nodes;
	}
}

