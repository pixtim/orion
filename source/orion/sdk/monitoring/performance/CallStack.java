package orion.sdk.monitoring.performance;


public class CallStack
{
	protected static long lastTraceId = 0;
	public StackTraceElement[] stackTrace = new StackTraceElement[0];
	public long traceId = -1;
	public long timestamp = 0;
	public String name = "unknown";
	
	protected static long generateTraceId()
	{
		return ++lastTraceId;
	}
	
	public CallStack(String name, long timestamp, StackTraceElement[] stackTrace)
	{
		this.name = name;
		this.traceId = generateTraceId();
		this.timestamp = timestamp;
		this.stackTrace = new StackTraceElement[stackTrace.length - 1];
		for (int i = 1; i < stackTrace.length; i++)
		{
			this.stackTrace[i - 1] = stackTrace[i];
		}
	}
	
	public float getAge()
	{
		float time = (System.currentTimeMillis() - timestamp) / 1000f;
		return time;
	}
	
	public static CallStack getTrace(String name)
	{
		try
		{
			throw new Exception();
		}
		catch (Exception e)
		{			
			return new CallStack(name, System.currentTimeMillis(), e.getStackTrace());
		}		
	}
}	
