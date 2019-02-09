package orion.sdk.util;


public class TpsCounter
{
	protected float[] history = null;
	protected long last = System.currentTimeMillis();
	protected int frames = 0;
	protected int samples = 0;
	protected float sampleRate = 0.5f; 
	
	public TpsCounter(float sampleRate, int historyCount)
	{
		history = new float[historyCount];
	}
	
	public void tick()
	{
		frames++;
		float time = (System.currentTimeMillis() - last) / 1000f;
		if (time > sampleRate)
		{
			for (int i = history.length - 1; i > 0; i--)
			{
				history[i] = history[i - 1];
			}
			
			history[0] = time != 0 ? frames / time : 0;
			frames = 0;
			last = System.currentTimeMillis();
			samples++;
		}
	}
	
	public float getTps()
	{
		float min = Float.MAX_VALUE;
		for (float next : history)
		{
			min = Math.min(min, next);
		}
		return min;
		/*
		if (samples >= history.length){
			float sum = 0;
			for (float next : history)
			{
				sum += next;
			}
			return sum / history.length;
		}
		else
		{
			return 0;
		}
		*/
	}
	
}
