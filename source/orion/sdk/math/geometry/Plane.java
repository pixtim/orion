package orion.sdk.math.geometry;

import orion.sdk.math.IFloatMatrix;

public class Plane
{
	private IFloatMatrix position = null;
	private IFloatMatrix normal = null;
	
	public Plane(IFloatMatrix position, IFloatMatrix normal)
	{
		this.position = position;
		this.normal = normal;
	}
}
