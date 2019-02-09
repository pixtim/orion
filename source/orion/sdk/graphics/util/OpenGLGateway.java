package orion.sdk.graphics.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.jogamp.opengl.GLException;

/**
 * Untested thread gateway utility class.
 * 
 * @author Tim
 *
 */
public class OpenGLGateway implements INamed
{
	public static final long TIMEOUT = 10000;
	
	private static OpenGLGateway instance = new OpenGLGateway();
	private Map<INamed, GatewayQueue> gateways = new HashMap<INamed, GatewayQueue>();
	
	private static OpenGLGateway getInstance()
	{
		if (OpenGLGateway.instance == null)
		{
			OpenGLGateway.instance = new OpenGLGateway();			
		}
		
		return OpenGLGateway.instance;
	}
	
	public static void enter(INamed resource, INamed caller) throws GLException
	{
		OpenGLGateway instance = OpenGLGateway.getInstance();
		instance.enterLocal(resource, caller);
	}
	
	public static void exit(INamed resource, INamed caller) throws GLException
	{
		OpenGLGateway instance = OpenGLGateway.getInstance();
		instance.exitLocal(resource, caller);
	}
	
	private void enterLocal(INamed resource, INamed caller) throws GLException
	{
		String resourceName = OpenGLManager.getInstance().getShortDescription(resource);
		String callerName = OpenGLManager.getInstance().getShortDescription(caller);
		OpenGLManager.getInstance().pushDebug("Entering gateway for " + resourceName + " from " + callerName, this);
		
		GatewayQueue gatewayQueue = null;
		if (this.gateways.containsKey(resource))
		{
			gatewayQueue = this.gateways.get(resource);
		}
		else
		{
			gatewayQueue = new GatewayQueue();
			this.gateways.put(resource, gatewayQueue);
		}
		
		try
		{
			gatewayQueue.enter(caller);
		}
		catch (InterruptedException e)
		{
			throw new GLException("OpenGLGateway state violation. Thread interrupted.", e); 
		}
		
		OpenGLManager.getInstance().popDebug();
	}
	
	private void exitLocal(INamed resource, INamed caller) throws GLException
	{
		String resourceName = OpenGLManager.getInstance().getShortDescription(resource);
		OpenGLManager.getInstance().pushDebug("Exiting gateway for " + resourceName, this);
		
		if (!this.gateways.containsKey(resource))
		{
			throw new GLException("OpenGLGateway state violation. Can't exit a gateway that was never entered.");
		}
		
		GatewayQueue gatewayQueue = this.gateways.get(resource);
		
		gatewayQueue.exit(caller);
		
		if (!gatewayQueue.isLocked())
		{
			this.gateways.remove(resource);
		}
		OpenGLManager.getInstance().popDebug();
	}

	private static class GatewayQueue
	{
		private List<INamed> callers = new LinkedList<INamed>();
		private List<INamed> sleeping = new LinkedList<INamed>();
		
		public GatewayQueue()
		{
		}
		
		public synchronized void enter(INamed caller) throws InterruptedException
		{			
			if (this.isLocked())
			{
				this.queueCaller(caller);
				
				this.suspend(caller);
			}
			else
			{
				this.queueCaller(caller);
			}
		}
		
		public synchronized void exit(INamed caller)
		{			
			if (this.isLocked())
			{
				INamed headCaller = this.callers.get(0);
				if (headCaller != caller)
				{
					throw new RuntimeException("GatewayQueue state violation. Unexpected called encountered.");
				}
				
				INamed nextCaller = this.dequeueCaller();
				
				if (nextCaller != null)
				{
					this.resume(nextCaller);
				}
			}
			else
			{
				throw new RuntimeException("GatewayQueue state violation. Can't exit empty gateway.");
			}
		}
		
		private void suspend(INamed caller) throws InterruptedException
		{
			this.sleeping.add(caller);
			while (this.sleeping.contains(caller))
			{
				caller.wait(TIMEOUT);
			}
		}
		
		private void resume(INamed caller)
		{
			this.sleeping.remove(caller);
			caller.notify();			
		}
		
		private INamed dequeueCaller()
		{
			this.callers.remove(0);

			if (this.isLocked())
			{
				return this.callers.get(0);
			}
			else
			{
				return null;
			}
		}
		
		private void queueCaller(INamed caller)
		{
			this.callers.add(caller);
		}
		
		public boolean isLocked()
		{
			return !callers.isEmpty();
		}
	}
	
	@Override
	public String getName()
	{
		return "OpenGL Gateway";
	}
}
