package orion.sdk.node.drawables;

import orion.sdk.graphics.shading.lighting.Light;
import orion.sdk.math.FloatMatrix;
import orion.sdk.util.StructuredBinary;

public class LightNode extends EntityNode
{
	public LightNode(String name)
	{
		super(name, null, null, null);
		this.light = new Light();
	}

	public Light light = null;
	
	/**
	 * Returns the light in model view space.
	 */
	public Light getLight() throws Exception
	{
		if (light != null)
		{
			Light copy = light.clone();
			copy.position =
					this.getVertexTransformation().product(light.position);
			copy.direction =
					this.getNormalTransformation().product(light.direction);
			return copy;
		}
		else
		{
			throw new Exception("No light specified");
		}
	}
	
	@Override
	public void read(StructuredBinary binary) throws Exception
	{

	}

	@Override
	public StructuredBinary write() throws Exception
	{
		return null;
	}

	@Override
	public float getUpdatePeriod()
	{
		return 0;
	}

}
