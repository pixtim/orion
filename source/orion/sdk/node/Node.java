package orion.sdk.node;

import java.util.ArrayList;
import java.util.List;

import orion.sdk.events.IUpdatable;
import orion.sdk.graphics.shading.lighting.Light;
import orion.sdk.graphics.util.IStackable;
import orion.sdk.graphics.util.OpenGLContext;
import orion.sdk.graphics.util.OpenGLStack;
import orion.sdk.node.drawables.LightNode;
import orion.sdk.util.IPersistable;

public abstract class Node implements IPersistable, IUpdatable, IStackable
{
	protected Node parent = null;
	
	public String name = "unnamed";
	
	public Node()
	{
		
	}
	
	public Node(String name)
	{
		this.name = name;
	}
	
	public Node getRoot()
	{
		if (getParent() == null)
		{
			return this;
		}
		else
		{
			return getParent().getRoot();
		}
	}
	
	public List<Node> findNodes(Class<?> nodeClass) throws Exception
	{
		List<Node> nodes = new ArrayList<Node>();
		
		if (nodeClass.isInstance(this))
		{
			nodes.add(this);
		}
		
		return nodes;
	}

	public Node getParent()
	{
		return parent;
	}

	public void setParent(Node parent)
	{
		this.parent = parent;
	}	
	
	@Override
	public String toString()
	{
		String typeName = getClass().toString();
		int last = typeName.lastIndexOf(".");
		return "'" + name + "' (" + typeName.substring(last + 1) + ")";
	}
	
	@Override
	public void push(OpenGLContext c)
	{
	}
	
	@Override
	public void pop(OpenGLContext c)
	{
	}
	
	@Override
	public void apply(OpenGLContext c)
	{
	}
	
	@Override
	public void clear(OpenGLContext c)
	{
	}
	
	protected void pushNode(OpenGLContext c)
	{
		OpenGLStack.push(Node.class, this, c);
	}
	
	protected void popNode(OpenGLContext c)
	{
		OpenGLStack.pop(Node.class, this, c);
	}
}
