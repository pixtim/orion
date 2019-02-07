package orion.sdk.util;

public abstract class ARandom
{
	/**
	 * @return A random {@code float} between {@code min} and {@code max}
	 */	
	public final float nextFloat(float min, float max)
	{
		return (float) nextDouble(min, max);
	}
	
	/**
	 * @return A random {@code float} between {@code 0} and {@code 1}
	 */	
	public final float nextFloat()
	{
		return (float) nextDouble();
	}

	/**
	 * @return A random {@code double} between {@code min} and {@code max}
	 */	
	public final double nextDouble(double min, double max)
	{
		double next = (getNext() - getMin()) / (double) (getMax() - getMin());
		next = (next < 0 ? 0 : next);
		next = (next > 1 ? 1 : next);
		return next * (max - min) + min;
	}
	
	/**
	 * @return A random {@code double} between {@code 0} and {@code 1}
	 */	
	public final double nextDouble()
	{
		return nextDouble(0d, 1d);
	}
	
	/**
	 * @return A random {@code long} between {@code 0} and {@code Long.MAX_VALUE}
	 */	
	public final long nextLong()
	{
		return (long) Math.round(nextDouble() * Long.MAX_VALUE);
	}
		
	/**
	 * @return A random {@code long} between {@code min} and {@code max}
	 */	
	public final long nextLong(long min, long max)
	{
		return (long) Math.round(nextDouble(min, max));
	}
	
	/**
	 * @return A random {@code int} between {@code 0} and {@code Integer.MAX_VALUE}
	 */	
	public final int nextInt()
	{
		return (int) Math.round(nextDouble() * Integer.MAX_VALUE);
	}
		
	/**
	 * @return A random {@code int} between {@code min} and {@code max}
	 */	
	public final int nextInt(int min, int max)
	{
		return (int) Math.round(nextDouble(min, max));
	}

	/**
	 * @return The maximum value of the random function 
	 */
	protected abstract long getMax();
	
	/**
	 * @return The minimum value of the random function
	 */
	protected abstract long getMin();
	
	
	/**
	 * @return The next value of the random function
	 */
	protected abstract long getNext();
}
