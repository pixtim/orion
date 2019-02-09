package orion.sdk.data.cubes;


public class BooleanCube extends ADataCube
{
	protected boolean[] entries = null;
	
	public BooleanCube(int width, int height, int depth, int stride)
	{
		super(width, height, depth, stride);
		
		int size = width * height * depth * stride;
		this.entries = new boolean[size];
	}
	
	public boolean getEntry(int x, int y, int z, int channel)
	{
		int index = getIndex(x, y, z, channel);
		return entries[index];
	}
	
	public void setEntry(int x, int y, int z, int channel, boolean value)
	{
		int index = getIndex(x, y, z, channel);
		entries[index] = value;
	}
	
	public void setEntry(int x, int y, int z, boolean[] tuple)
	{
		for (int i = 0; i < tuple.length; i++)
		{
			setEntry(x, y, z, i, tuple[i]);
		}
	}

	public boolean[] getEntries()
	{
		return entries;
	}
}
