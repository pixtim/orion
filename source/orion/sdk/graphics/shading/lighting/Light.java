package orion.sdk.graphics.shading.lighting;

import orion.sdk.math.FloatMatrix;
import orion.sdk.math.IFloatMatrix;

public class Light
{
	public IFloatMatrix diffuse = FloatMatrix.vector(1, 1, 1, 1f);
	public IFloatMatrix specular = FloatMatrix.vector(0.3f, 0.3f, 0.3f, 1f);
	
	public IFloatMatrix position = FloatMatrix.vector(0f, 0f, 0.0f, 1f);
	public IFloatMatrix direction = FloatMatrix.vector(0f, 0f, -1f);
	
	public int shape = LightShape.SPHERICAL;
	public float range = -1;
	
	public Light()
	{
		
	}
	
	public static class LightShape
	{
		public static final int SPHERICAL	= 0;
		public static final int DIRECTIONAL = 1;
		public static final int CONIC			= 2;
	}
	
	@Override
	public Light clone() throws CloneNotSupportedException
	{
		Light light = new Light();
		light.diffuse = diffuse.clone();
		light.specular = specular.clone();
		light.position = position.clone();
		light.direction = direction.clone();
		light.range = range;
		light.shape = shape;
		return light;
	}
}
