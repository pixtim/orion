package orion.sdk.events;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import orion.sdk.monitoring.incidents.Incident;
import orion.sdk.monitoring.incidents.IncidentManager;
import orion.sdk.util.TpsCounter;


public class Processor implements Runnable
{
	public List<IUpdatable> updatables = new LinkedList<IUpdatable>();
	protected Map<IUpdatable, Float> updateTimers = new HashMap<IUpdatable, Float>();
	public float minimumDt = 0.005f;
	
	protected boolean running = false;
	protected boolean blocking = false;
	protected Thread thread = null;
	protected long lastTick = 0;	
	protected TpsCounter counter = new TpsCounter(0.2f, 10);
	
	public Processor(String name)
	{
		thread = new Thread(this, name);	
	}
	
	/**
	 * Starts the loop
	 */
	public void start()
	{
		running = true;
		lastTick = System.currentTimeMillis();
		thread.start();
	}
	
	/**
	 * Stops the loop at blocks until it's last update is completed if {@code block} is {@code true}.
	 */
	public void stop(boolean block) throws InterruptedException
	{
		if (running)
		{
			running = false;
			if (block)
			{
				updatables.wait();
				blocking = true;
			}
		}
	}
	
	public float getUpdatesPerSecond()
	{
		return counter.getTps();
	}
	
	/**
	 * Stops the loop at blocks until it's last update is completed.
	 */
	public void stop() throws InterruptedException
	{
		stop(true);
	}

	@Override
	public void run()
	{
		while (running)
		{	
			float dt = (System.currentTimeMillis() - lastTick) / 1000f;
			if (dt > minimumDt)
			{
				/*
				 * Call each update method if it's update period has elapsed.
				 */
				synchronized (updatables)
				{
					for (IUpdatable nextUpdatable : updatables)
					{
						float updateTimer = 0;
						if (updateTimers.containsKey(nextUpdatable))
						{
							updateTimer = updateTimers.get(nextUpdatable);
						}
						updateTimer = updateTimer + dt;
						updateTimers.put(nextUpdatable, updateTimer);
						try
						{
							if (updateTimer >= nextUpdatable.getUpdatePeriod())
							{
								nextUpdatable.update(updateTimer);
								updateTimers.put(nextUpdatable, 0f);
							}
						} 
						catch (Exception e)
						{
							IncidentManager.notifyIncident(Incident.newError("Update error", e));
						}						
					}
					counter.tick();
				}
				lastTick = System.currentTimeMillis();
				if (blocking)
				{
					updatables.notifyAll();
				}
				
				/*
				 * Clean up updateTimers
				 */
				Set<IUpdatable> candidates = new HashSet<IUpdatable>(updateTimers.keySet());
				for (IUpdatable updatable : candidates)
				{
					if (!updateTimers.containsKey(updatable)){
						updateTimers.remove(updatable);
					}
				}
			}
			else
			{
				try
				{
					float sleepTime = (minimumDt - dt) * 1000f;
					Thread.sleep((long) sleepTime);
				} 
				catch (InterruptedException e)
				{
					IncidentManager.notifyIncident(Incident.newError("Throttling error", e));
				}
			}
		}
	}
}
