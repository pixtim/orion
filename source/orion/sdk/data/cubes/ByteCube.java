package orion.sdk.data.cubes;

import orion.sdk.math.FloatMatrix;

public class ByteCube extends ADataCube
{
	protected byte[] entries = null;
	
	public ByteCube(int width, int height, int depth, int stride)
	{
		super(width, height, depth, stride);
		
		int size = width * height * depth * stride;
		this.entries = new byte[size];
	}
	
	public byte getEntry(int x, int y, int z, int channel)
	{
		int index = getIndex(x, y, z, channel);
		return entries[index];
	}
	
	public FloatMatrix getEntry(int x, int y, int z)
	{
		float[] values = new float[stride];
		for (int i = 0; i < stride; i++)
		{
			int index = getIndex(x, y, z, i);
			values[i] = entries[index];
		}
		
		return FloatMatrix.vector(values);
	}
	
	public void setEntry(int x, int y, int z, int channel, byte value)
	{
		int index = getIndex(x, y, z, channel);
		entries[index] = value;
	}
	
	public void setEntry(int x, int y, int z, byte[] tuple)
	{
		for (int i = 0; i < tuple.length; i++)
		{
			setEntry(x, y, z, i, tuple[i]);
		}
	}

	public byte[] getEntries()
	{
		return entries;
	}
}
