package orion.sdk.data.cubes;


@SuppressWarnings("rawtypes")
public abstract class ADataCube
{
	protected int width = 0;
	protected int height = 0;
	protected int depth = 0;
	protected int stride = 0;

	public ADataCube(int width, int height, int depth, int stride)
	{
		this.width = width;
		this.height = height;
		this.depth = depth;
		this.stride = stride;				
	}
	
	public int getWidth()
	{
		return width;
	}
	
	public int getHeight()
	{
		return height;
	}
	
	public int getDepth()
	{
		return depth;
	}
	
	public int getStride()
	{
		return stride;
	}
	
	public int getIndex(int x, int y, int z, int channel)
	{
		int bar = getWidth() * getStride();
		int slice = getHeight() * bar;
		int index =
			z * slice +
			y * bar + 
			x * getStride() + 
			channel;
		
		return index;
	}	
	
	
}
