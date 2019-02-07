package orion.sdk.math.geometry;

import orion.sdk.math.FloatMatrix;
import orion.sdk.math.IFloatMatrix;

public class Box implements IBounded<Box>, IRectangular<Box>
{
	protected IFloatMatrix start = FloatMatrix.vector(0, 0, 0);
	protected IFloatMatrix end = FloatMatrix.vector(0, 0, 0);
	
	public float getWidth()
	{
		return this.end.getX() - this.start.getX();
	}
	
	public float getHeight()
	{
		return this.end.getY() - this.start.getY();
	}
	
	public float getDepth()
	{
		return this.end.getZ() - this.start.getZ();
	}
	
	public float getLeft()
	{
		return this.start.getX();
	}
	
	public float getBottom()
	{
		return this.start.getY();
	}
	
	public float getBack()
	{
		return this.start.getZ();
	}
	
	public float getRight()
	{
		return this.end.getX();
	}
	
	public float getTop()
	{
		return this.end.getY();
	}
	
	public float getFront()
	{
		return this.end.getZ();
	}

	public IFloatMatrix getCenter()
	{
		return this.getPoint(0.5f, 0.5f, 0.5f);
	}
	
	public IFloatMatrix getStart()
	{
		return this.start;
	}
	
	public IFloatMatrix getEnd()
	{
		return this.end;
	}
	
	public IFloatMatrix getPoint(float relativeX, float relativeY, float relativeZ)
	{
		return FloatMatrix.vector(
				getLeft() + relativeX * getWidth(),
				getBottom() + relativeY * getHeight(),
				getBack() + relativeZ * getDepth(),
				1.0f);		
	}

	public IFloatMatrix getSize()
	{
		return FloatMatrix.vector(
			getWidth(),
			getHeight(),
			getDepth(),
			0f);
	}
	
	public Box()
	{
		
	}
	
	public Box(float x, float y, float z, float width, float height, float depth)
	{
		set(x, y, z, width, height, depth);
	}
	
	public Box(float width, float height, float depth)
	{
		set(width * -0.5f, height * -0.5f, depth * -0.5f, width, height, depth);
	}

	public void set(float x, float y, float z, float width, float height, float depth)
	{
		this.start.setX(x);
		this.start.setY(y);
		this.start.setZ(z);
		this.end.setX(x + width);
		this.end.setY(y + height);
		this.end.setZ(z + depth);
	}
	
	public Box subDivide(int x, int y, int z, int width, int height, int depth) throws Exception
	{
		FloatMatrix cellSize = 
			FloatMatrix.vector(getWidth() / width, getHeight() / height, getDepth() / depth);
		Box bounds = new Box();
		bounds.set(
			getLeft() + x * cellSize.getX(), 
			getBottom() + y * cellSize.getY(), 
			getBack() + z * cellSize.getZ(), 
			cellSize.getX(),
			cellSize.getY(),
			cellSize.getZ());
		return bounds;
	}
	
	public boolean containsPoint(IFloatMatrix p)
	{
		float
			x = p.getX(),
			y = p.getY(),
			z = p.getZ();
		return 
			x >= getLeft() && 
			x <= getRight() &&
			y >= getBottom() && 
			y <= getTop() &&
			z >= getFront() && 
			z <= getBack();			
	}
}
