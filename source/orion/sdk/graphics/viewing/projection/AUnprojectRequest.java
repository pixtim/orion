package orion.sdk.graphics.viewing.projection;

import java.util.ArrayList;
import java.util.List;

import orion.sdk.graphics.util.OpenGLContext;
import orion.sdk.graphics.viewing.cameras.ACamera;
import orion.sdk.graphics.viewing.cameras.Viewport;
import orion.sdk.math.FloatMatrix;
import orion.sdk.math.IFloatMatrix;

public abstract class AUnprojectRequest
{
	protected String name = "unknown";
	protected List<IUnprojectListener> listeners = new ArrayList<IUnprojectListener>();
	protected FloatMatrix sourcePoint = null;
	protected Viewport sourceViewport = null;
	protected Viewport desinationViewport = null;
	
	public AUnprojectRequest(
		String name,
		FloatMatrix sourcePoint,
		Viewport sourceViewport,
		Viewport desinationViewport)
	{
		this.name = name;
		this.sourcePoint = sourcePoint;
		this.sourceViewport = sourceViewport;
		this.desinationViewport = desinationViewport;
	}
	
	public String getName()
	{
		return name;
	}
	
	public abstract void unproject(ACamera camera, OpenGLContext c) throws Exception;
	
	protected void notifyListeners(IFloatMatrix position)
	{
		synchronized (listeners)
		{
			for (IUnprojectListener listener : listeners)
			{
				listener.unprojectEvent(this, position);
			}		
		}
	}
	
	public void registerUnprojectListener(IUnprojectListener listener)
	{
		synchronized (listeners)
		{
			listeners.add(listener);
		}
	}
	
	public void deregisterUnprojectListener(IUnprojectListener listener)
	{
		synchronized (listeners)
		{
			listeners.remove(listener);
		}
	}
}
