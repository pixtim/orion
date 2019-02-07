package orion.sdk.monitoring.incidents;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class IncidentManager
{
	protected static IncidentManager instance = new IncidentManager();
	
	protected List<IIncidentListener> listeners = new ArrayList<IIncidentListener>();
	protected List<Incident> incidents = new LinkedList<Incident>();
	
	protected int currentLevel = 0;

	protected Stack<Incident> incidentStack = new Stack<Incident>();
	protected Incident lastIncident = null;
	
	protected IncidentManager()
	{
		
	}
	
	protected int _getCurrentLevel()
	{
		return this.currentLevel;
	}
	
	protected Incident _getCurrentParent()
	{
		if (!this.incidentStack.empty())
		{
			return this.incidentStack.peek();			
		}
		else
		{
			return null;
		}		
	}
	
	protected void _pushLevel()
	{
		this.currentLevel++;
		
		this.incidentStack.push(this.lastIncident);
	}
	
	protected void _popLevel()
	{
		this.currentLevel--;
		
		this.incidentStack.pop();
	}
	
	protected void _registerListener(IIncidentListener listener)
	{
		this.listeners.add(listener);
	}	
	
	protected void _deregisterListener(IIncidentListener listener)
	{
		this.listeners.remove(listener);
	}
	
	protected synchronized void _notifyIncident(Incident incident)
	{
		if (this.currentLevel == 0)
		{
			this.incidents.add(incident);
		}
		
		this.lastIncident = incident;
	}
	
	protected synchronized void _cutover()
	{
		for (IIncidentListener listener : this.listeners)
		{
			try
			{
				listener.processIncidents(this.incidents);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		this.incidents.clear();
	}
	
	public static IncidentManager getInstance()
	{
		return instance;
	}
	
	public static void notifyIncident(Incident incident)
	{
		IncidentManager instance = IncidentManager.getInstance();
		instance._notifyIncident(incident);
	}
	
	public static void registerListener(IIncidentListener listener)
	{
		IncidentManager instance = IncidentManager.getInstance();
		instance._registerListener(listener);
	}
	
	public static void deregisterListener(IIncidentListener listener)
	{
		IncidentManager instance = IncidentManager.getInstance();
		instance._deregisterListener(listener);
	}
	
	public static void popLevel()
	{
		IncidentManager instance = IncidentManager.getInstance();
		instance._popLevel();
	}
	
	public static void pushLevel()
	{
		IncidentManager instance = IncidentManager.getInstance();
		instance._pushLevel();
	}
	
	public static int getCurrentLevel()
	{
		IncidentManager instance = IncidentManager.getInstance();
		return instance._getCurrentLevel();
	}
	
	public static Incident getCurrentParent()
	{
		IncidentManager instance = IncidentManager.getInstance();
		return instance._getCurrentParent();
	}
	
	public static void cutover()
	{
		IncidentManager instance = IncidentManager.getInstance();
		instance._cutover();
	}	
}
