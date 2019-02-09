package orion.sdk.graphics.drawables.particles;

import orion.sdk.math.FloatMatrix;

public class Particle
{
	public int type = ParticleType.SPHERICAL_BILLBOARD;
	public FloatMatrix position = FloatMatrix.vector(0, 0, 0, 1);
	public FloatMatrix size = FloatMatrix.vector(0.1f, 0.1f);
	public FloatMatrix color = FloatMatrix.vector(1, 1, 1, 1);
	public int spriteTile = 0;
	
	public Particle()
	{
		
	}
	
	public static class ParticleType
	{
		public static final int SPHERICAL_BILLBOARD = 1;
		public static final int AXIAL_BILLBOARD = 1; 
	}
}
