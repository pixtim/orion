package orion.sdk.monitoring.performance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import orion.sdk.events.IUpdatable;
import orion.sdk.monitoring.incidents.Incident;
import orion.sdk.monitoring.incidents.IncidentManager;

public class StackMonitor implements IUpdatable
{
	protected static StackMonitor instance = new StackMonitor();
	
	protected Map<Long, CallStack> traces = new HashMap<Long, CallStack>();
	protected float maximumCallDuration = 10f; 
	
	protected StackMonitor()
	{
		
	}
	
	public static StackMonitor getInstance()
	{
		return instance;
	}
	
	public static CallStack beginMethod(String name)
	{
		StackMonitor monitor = getInstance();
		synchronized (monitor.traces)
		{
			CallStack trace = CallStack.getTrace(name);
			monitor.traces.put(trace.traceId, trace);
			return trace;
		}
	}
	
	public static void endMethod(CallStack trace)
	{
		StackMonitor monitor = getInstance();
		synchronized (monitor.traces)
		{
			if (monitor.traces.containsKey(trace.traceId))
			{
				monitor.traces.remove(trace.traceId);
			}
		}
	}

	@Override
	public void update(float dt) throws Exception
	{
		if (shouldUpdate())
		{
			synchronized (traces)
			{
				List<CallStack> toRemove = new ArrayList<CallStack>();
				for (CallStack stack : traces.values())
				{
					float age = stack.getAge();
					if (age > maximumCallDuration)
					{
						IncidentManager.notifyIncident(
							Incident.newWarning(
								"The maximum call duration of '" +  stack.name + "' was exceded. The call duration was " + age
										+ "ms at the time of recording. The maximum call duration is "
										+ maximumCallDuration
										+ "ms. See the stack trace below for more information.",
								new Object[] {stack.stackTrace}));
						toRemove.add(stack);					
					}
				}			
				for (CallStack stack : toRemove)
				{
					endMethod(stack);
				}
			}
		}
	}

	@Override
	public float getUpdatePeriod()
	{
		return 0.05f;
	}

	@Override
	public boolean shouldUpdate()
	{
		return true;
	}

}
