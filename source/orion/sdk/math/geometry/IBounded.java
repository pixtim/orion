package orion.sdk.math.geometry;

import orion.sdk.math.IFloatMatrix;

public interface IBounded<T>
{
	public float getWidth();
	
	public float getHeight();
	
	public float getDepth();
	
	public float getLeft();
	
	public float getBottom();
	
	public float getBack();
	
	public float getRight();
	
	public float getTop();
	
	public float getFront();
	
	public IFloatMatrix getStart();
	
	public IFloatMatrix getEnd();

	public IFloatMatrix getCenter();
	
	public IFloatMatrix getPoint(float relativeX, float relativeY, float relativeZ);

	public IFloatMatrix getSize();
	
	public boolean containsPoint(IFloatMatrix p);

}
