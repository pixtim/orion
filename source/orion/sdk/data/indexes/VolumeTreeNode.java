package orion.sdk.data.indexes;

import orion.sdk.data.cubes.ObjectCube;

public class VolumeTreeNode<T>
{
	protected int size = 0;
	protected ObjectCube<VolumeTreeNode<T>> children = null;
	protected T payload = null;
	
	public VolumeTreeNode(int size)
	{
		this.size = size;
		this.children = new ObjectCube<VolumeTreeNode<T>>(size, size, size);
	}
	
	public int getSize()
	{
		return size;
	}
	
	public T getPayload()
	{
		return payload;
	}
	
	public void setPayload(T payload)
	{
		this.payload = payload;
	}
	
	public VolumeTreeNode<T> getChild(int x, int y, int z)
	{
		return this.children.getEntry(x, y, z);
	}
	
	public void setChild(int x, int y, int z, VolumeTreeNode<T> child)
	{
		this.children.setEntry(x, y, z, child);
	}
}
