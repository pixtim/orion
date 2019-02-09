package orion.client.graphics.drawables.volumes;

import com.jogamp.opengl.GLException;

import orion.sdk.data.cubes.BooleanCube;
import orion.sdk.data.cubes.FloatCube;
import orion.sdk.graphics.shading.texturing.Texture;
import orion.sdk.graphics.shading.texturing.Texture.EFormat;
import orion.sdk.graphics.shading.texturing.Texture.ETarget;
import orion.sdk.math.FloatMatrix;
import orion.sdk.math.IFloatMatrix;

public class MengerSponge
{
	protected FloatCube alphaCube;
	protected FloatCube diffuseCube;
	protected FloatCube normalCube;
	protected FloatCube gaussianFilter;
	
	protected Texture diffuseTexture;
	protected Texture alphaTexture;
	protected Texture normalTexture;
	
	protected int res;
	
	public MengerSponge(int res)
	{
		this.alphaCube = new FloatCube(res, res, res, 1);
		this.diffuseCube = new FloatCube(res, res, res, 4);
		this.normalCube = new FloatCube(res, res, res, 3);
		this.res = res;
	}
	
	public void generate(int level, float alpha) throws GLException
	{
		this.generate(this.res, level, alpha);
		
		this.alphaTexture = new Texture("alphaMap", EFormat.A, ETarget.TEXTURE_3D, this.alphaCube);
		this.alphaTexture.setLinear(false);
		
		this.diffuseTexture = new Texture("diffuseMap", EFormat.RGBA, ETarget.TEXTURE_3D, this.diffuseCube);
		this.diffuseTexture.setLinear(false);
		
		this.normalTexture = new Texture("normalMap", EFormat.RGB, ETarget.TEXTURE_3D, this.normalCube);
		this.normalTexture.setLinear(false);		
	}
	
	protected void generate(int res, int level, float alpha) throws GLException
	{
		try
		{
			IFloatMatrix center = FloatMatrix.vector(res / 2, res / 2, res / 2);
			for (int x = 0; x < res; x++)
			{
				for (int y = 0; y < res; y++)
				{
					for (int z = 0; z < res; z++)
					{
						alphaCube.setEntry(x, y, z, 0, alpha);
						
						IFloatMatrix current = FloatMatrix.vector(x, y, z);
						IFloatMatrix direction = current.subtract(center);
						direction.normalize();
						
						boolean color = false;
						if (color)
						{
							diffuseCube.setEntry(x, y, z, 0, (direction.getX() + 1f) / 2f);
							diffuseCube.setEntry(x, y, z, 1, (direction.getY() + 1f) / 2f);
							diffuseCube.setEntry(x, y, z, 2, (direction.getZ() + 1f) / 2f);
						}
						else
						{
							diffuseCube.setEntry(x, y, z, 0, 1f);
							diffuseCube.setEntry(x, y, z, 1, 1f);
							diffuseCube.setEntry(x, y, z, 2, 1f);
						}
					}
				}
			}
			
			mengerSponge(0, 0, 0, res, level);
			
			generateGaussianFilter(10, 2);
			calculateNormals();
		}
		catch (Exception e)
		{
			throw new GLException(e);
		}
	}
	
	protected float getAlphaAt(int x, int y, int z)
	{
		if (x < 0 || x > this.res - 1 ||
			y < 0 || y > this.res - 1 ||
			z < 0 || z > this.res - 1)
		{
			return 0;
		}
		else
		{
			return this.alphaCube.getEntry(x, y, z, 0);
		}
	}
	
	protected void generateGaussianFilter(int res, float standardDeviation)
	{
		this.gaussianFilter = new FloatCube(res, res, res, 1);
		float sum = 0;
		float mid = res / 2f;
		for (int x = 0; x < res; x++)
		{
			for (int y = 0; y < res; y++)
			{
				for (int z = 0; z < res; z++)
				{
					float gaussian = this.gaussian(x - mid, y - mid, z - mid, standardDeviation);
					this.gaussianFilter.setEntry(x, y, z, 0, gaussian);
					sum += gaussian;
				}
			}
		}
		
		for (int x = 0; x < res; x++)
		{
			for (int y = 0; y < res; y++)
			{
				for (int z = 0; z < res; z++)
				{
					float gaussian = this.gaussianFilter.getEntry(x, y, z, 0);
					this.gaussianFilter.setEntry(x, y, z, 0, gaussian / sum);
				}
			}
		}
	}
	
	protected float gaussian(float x, float y, float z, float standardDeviation)
	{
		float o = standardDeviation;
		
		return (float) (Math.pow(Math.E, -1f * (x * x + y * y + z * z) / (2 * o * o)) / Math.sqrt(2 * Math.PI * o * o));
	}
	
	protected float getGaussianAlphaAt(int x, int y, int z)
	{
		int sampleSize = this.gaussianFilter.getWidth();
		int radius = sampleSize / 2;
		float alpha = 0;
		
		for (int iX = 0; iX < sampleSize; iX++)
		{
			for (int iY = 0; iY < sampleSize; iY++)
			{
				for (int iZ = 0; iZ < sampleSize; iZ++)
				{
					float sample = this.getAlphaAt(x - radius + iX, y - radius + iY, z - radius + iZ);
					float weight = this.gaussianFilter.getEntry(iX, iY, iZ, 0);
					alpha += weight * sample;
				}
			}
		}
		
		return alpha;
	}
	
	protected void calculateNormals() throws Exception
	{
		for (int x = 0; x < res; x++)
		{
			for (int y = 0; y < res; y++)
			{
				for (int z = 0; z < res; z++)
				{
					
					float dx = getGaussianAlphaAt(x, y, z) - getGaussianAlphaAt(x - 1, y, z);
					float dy = getGaussianAlphaAt(x, y, z) - getGaussianAlphaAt(x, y - 1, z);
					float dz = getGaussianAlphaAt(x, y, z) - getGaussianAlphaAt(x, y, z - 1);
					
					IFloatMatrix direction = FloatMatrix.vector(dx, dy, dz);
					direction = direction.scalarProduct(-1f);
					direction.normalize();
					
					normalCube.setEntry(x, y, z, 0, (1f * direction.getX() + 1f) / 2f);
					normalCube.setEntry(x, y, z, 1, (1f * direction.getY() + 1f) / 2f);
					normalCube.setEntry(x, y, z, 2, (1f * direction.getZ() + 1f) / 2f);				
				}
			}
		}
	}

	protected void mengerSponge(int x, int y, int z, int size, int maxLevel) throws Exception
	{
		if (maxLevel > 0)
		{	
			int[] X = new int[4];
			int[] Y = new int[4];
			int[] Z = new int[4];
			BooleanCube clearedCube = new BooleanCube(3, 3, 3, 1);
			
			for (int i = 0; i <= 3; i++)
			{
				X[i] = x + Math.round(size * (i / 3.0f));
				Y[i] = y + Math.round(size * (i / 3.0f));
				Z[i] = z + Math.round(size * (i / 3.0f));
			}
			
			int subSize = size / 3;
			mengerSpongeClear(X, Y, Z, 0, 1, 1, subSize, clearedCube);
			mengerSpongeClear(X, Y, Z, 2, 1, 1, subSize, clearedCube);
			mengerSpongeClear(X, Y, Z, 1, 0, 1, subSize, clearedCube);
			mengerSpongeClear(X, Y, Z, 1, 2, 1, subSize, clearedCube);
			mengerSpongeClear(X, Y, Z, 1, 1, 0, subSize, clearedCube);
			mengerSpongeClear(X, Y, Z, 1, 1, 2, subSize, clearedCube);
			mengerSpongeClear(X, Y, Z, 1, 1, 1, subSize, clearedCube);
			
			for (int xI = 0; xI < 3; xI++)
			{
				for (int yI = 0; yI < 3; yI++)
				{
					for (int zI = 0; zI < 3; zI++)
					{
						if (!clearedCube.getEntry(xI, yI, zI, 0))
						{
							mengerSponge(X[xI], Y[yI], Z[zI], subSize, maxLevel - 1);
						}
					}
				}
			}
		}
	}
	
	protected void mengerSpongeClear(int[] X, int[] Y, int[] Z, int xI, int yI, int zI, int size, BooleanCube clearedCube) throws Exception
	{
		/*
		 * Clear the block
		 */
		int left = X[xI];
		int top = Y[yI];
		int front = Z[zI];
		
		for (int x = left; x < left + size; x++)
		{
			for (int y = top; y < top + size; y++)
			{
				for (int z = front; z < front + size; z++)
				{
					alphaCube.setEntry(x, y, z, 0, 0f);
				}
			}
		}		
		clearedCube.setEntry(xI,  yI, zI, 0, true);
	
	}
	
	public Texture getDiffuseTexture()
	{
		return diffuseTexture;
	}
	
	public Texture getAlphaTexture()
	{
		return alphaTexture;
	}
	
	public Texture getNormalTexture() {
		return normalTexture;
	}
}
