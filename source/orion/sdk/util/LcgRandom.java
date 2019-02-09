package orion.sdk.util;

/**
 * This pseudo random number generator is a Linear Congruential Generator (LCG). 
 * 
 * @author Tim
 * @since 1.0.00
 */
public class LcgRandom extends ARandom
{
	public long number 		= 0;
	public long multiplier	= 23;
	public long increment	= 65435;
	public long modulus		= 543423532;

	/**
	 * Constructs a LCG with the system time as a seed.
	 */
	public LcgRandom()
	{
		number = System.currentTimeMillis();
	}
	
	/**
	 * Constructs a LCG with the given seed
	 */
	public LcgRandom(long seed)
	{
		number = seed;
	}

	@Override
	protected long getMax()
	{
		return 543384604;
	}

	@Override
	protected long getMin()
	{
		return 26131;
	}

	@Override
	protected long getNext()
	{
		number = (multiplier * number + increment) % modulus;
		return number;
	}
}
