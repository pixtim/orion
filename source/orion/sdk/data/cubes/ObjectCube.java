package orion.sdk.data.cubes;

import java.util.Iterator;

public class ObjectCube<T> extends ADataCube implements Iterable<T>
{
	protected Object[] entries = null;
	
	public ObjectCube(int width, int height, int depth)
	{
		this(width, height, depth, 1);
	}
	
	public ObjectCube(int width, int height, int depth, int stride)
	{
		super(width, height, depth, stride);
		
		int size = width * height * depth * stride;
		this.entries = new Object[size];
	}
	
	@SuppressWarnings("unchecked")
	public T getEntry(int x, int y, int z, int channel)
	{
		int index = getIndex(x, y, z, channel);
		return (T) entries[index];
	}	
	
	public T getEntry(int x, int y, int z)
	{
		return getEntry(x, y, z, 0);
	}	

	public void setEntry(int x, int y, int z, int channel, T value)
	{
		int index = getIndex(x, y, z, channel);
		entries[index] = value;
	}
	
	public void setEntry(int x, int y, int z, T value)
	{
		setEntry(x, y, z, 0, value);
	}

	public Iterator<T> iterator()
	{
		return new DataCubeIterator<T>();
	}
	
	private class DataCubeIterator<K> implements Iterator<K>
	{
		private int index;
		private int x = 0;
		private int y = 0;
		private int z = 0;
		private int channel = 0;
		private int size = getWidth() * getHeight() * getDepth();
		
		@Override
		public boolean hasNext()
		{			
			return index < size;
		}

		@SuppressWarnings("unchecked")
		@Override
		public K next()
		{			
			K entry = (K) getEntry(x, y, z, channel);			
			index++;
			
			channel++;
			if (channel > stride - 1)
			{
				channel = 0;
				x++;
				if (x > width - 1)
				{
					x = 0;
					y++;
					if (y > height - 1)
					{
						y = 0;
						z++;
					}
				}
			}
			return entry;
		}

		@Override
		public void remove()
		{	
		}
		
	}	
}
