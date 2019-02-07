package orion.sdk.graphics.scenes;

import orion.sdk.graphics.viewing.cameras.ACamera;
import orion.sdk.math.generic.GenericMatrix;

public class ScalingScene extends Scene
{
	protected int currentLevel = 0;
	protected int maxLevels = 25;
	protected int degree = 10; 
	
	// 1,000,000,000,000,000,000,000 units across

	protected float minCameraThreshold = 0.1f;
	protected float maxCameraThreshold = 0.9f;
	
	public ScalingScene(String name, ACamera camera, int currentLevel, int maxLevels, int degree)
	{
		super(name, camera);
		
		this.currentLevel = currentLevel;
		this.maxLevels = maxLevels;
		this.degree = degree;
	}

	@Override
	public void update(float dt) throws Exception
	{
		super.update(dt);
		
	}
}
