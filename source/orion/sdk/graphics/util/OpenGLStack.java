package orion.sdk.graphics.util;

import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;

import com.jogamp.opengl.GLException;

import orion.sdk.monitoring.incidents.Incident;

public class OpenGLStack
{
	private static Map<String, Stack<IStackable>> states = new TreeMap<String, Stack<IStackable>>();
	
	protected OpenGLStack()
	{
	}
	
	private static String getKey(Class<?> stackClass, Object[] stackContext)
	{
		StringBuilder key = new StringBuilder();
		key.append(stackClass.getName());
		
		for (int i = 0; i < stackContext.length; i++)
		{
			key.append(":");
			key.append(stackContext[i].toString());
		}
		
		return key.toString();
	}

	private static Stack<IStackable> getStateStack(String key)
	{
		Stack<IStackable> stack = null;
		
		if (states.containsKey(key))
		{
			stack = states.get(key);
		}
		else
		{
			stack = new Stack<IStackable>();
			states.put(key, stack);
		}
		
		return stack;
	}
	
	public static void apply(IStackable object, OpenGLContext c)
	{
		OpenGLManager.getInstance().pushDebug("Applying", object);
		object.apply(c);
		OpenGLManager.getInstance().popDebug();
	}
	
	public static void clear(IStackable object, OpenGLContext c)
	{
		OpenGLManager.getInstance().pushDebug("Clearing", object);
		object.clear(c);
		OpenGLManager.getInstance().popDebug();
	}
	
	public static void push(Class<?> stackClass, IStackable object, OpenGLContext c, Object... stackContext)
	{
		if (object == null)
		{
			throw new GLException("Stack violation. Can't push NULL onto the stack.");
		}
		
		String key = getKey(stackClass, stackContext);
		Stack<IStackable> stack = getStateStack(key);
		
		OpenGLManager.getInstance().pushDebug("Pushing onto stack '" + key + "'", object);

		if (!stack.isEmpty())
		{
			IStackable peek = stack.peek();
			OpenGLStack.clear(peek, c);
		}		
		
		stack.push(object);
		object.push(c);
		OpenGLStack.apply(object, c);
		
		OpenGLManager.getInstance().logDebugIncident(Incident.newInformation("Stack '" + key + "'", stack.clone()));
		
		OpenGLManager.getInstance().popDebug();
	}

	public static void pop(Class<?> stackClass, Object expected, OpenGLContext c, Object... stackContext)
	{
		if (expected == null)
		{
			throw new GLException("Stack violation. Can't expect NULL from the stack.");
		}
		
		String key = getKey(stackClass, stackContext);
		Stack<IStackable> stack = getStateStack(key);
				
		if (!stack.isEmpty())
		{
			IStackable actual = stack.pop();
			
			OpenGLManager.getInstance().pushDebug("Popping from stack '" + key + "'", actual);	
			
			if (actual != expected)
			{
				String expectedName = OpenGLManager.getInstance().getShortDescription(expected);
				String actualName = OpenGLManager.getInstance().getShortDescription(actual);
				throw new GLException("Stack violation. Unexpected object popped. Expected '" + expectedName + "' but got '" + actualName + "'.");
			}
			
			actual.pop(c);
			OpenGLStack.clear(actual, c);

			if (!stack.isEmpty())
			{
				IStackable peek = stack.peek();
				OpenGLStack.apply(peek, c);
			}
			
			OpenGLManager.getInstance().logDebugIncident(Incident.newInformation("Stack", stack.clone()));
			
			OpenGLManager.getInstance().popDebug();
		}
		else
		{
			throw new GLException("Stack violation. Cannot pop empty stack.");
		}
	}
	
	public static IStackable peek(Class<?> stackClass, Object... stackContext)
	{
		String key = getKey(stackClass, stackContext);
		Stack<IStackable> stack = getStateStack(key);
		
		if (stack.isEmpty())
		{
			return null;
		}
		else
		{
			return stack.peek();			
		}
	}
	
	public static void validateStack()
	{
		Set<String> keys = OpenGLStack.states.keySet();
		for (String key : keys)
		{
			Stack stack = states.get(key);
			if (!stack.isEmpty())
			{
				throw new GLException("Stack violation. Leak detected on stack '" + key + "'. Stack had " + stack.size()
						+ " elements left at the end of the pass completed.");
			}
		}		
	}
}
