package orion.sdk.monitoring.performance;


public class PerformanceManager
{
	public static long getMaxMemory()
	{
		Runtime runtime = Runtime.getRuntime();
	   return runtime.maxMemory();
	}
	
	public static long getAllocatedMemory()
	{
		Runtime runtime = Runtime.getRuntime();
	   return runtime.totalMemory();
	}
	
	public static long getFreeMemory()
	{
		Runtime runtime = Runtime.getRuntime();
	   return runtime.freeMemory();
	}
	
	public static float getMemoryUsage()
	{
		return (getAllocatedMemory() / (float) getMaxMemory() * 100f);
	}
}
