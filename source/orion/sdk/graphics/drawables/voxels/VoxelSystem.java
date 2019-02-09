package orion.sdk.graphics.drawables.voxels;

import com.jogamp.opengl.GLException;

import orion.sdk.data.cubes.ObjectCube;
import orion.sdk.graphics.drawables.IDrawable;
import orion.sdk.graphics.util.OpenGLContext;
import orion.sdk.graphics.util.OpenGLManager;
import orion.sdk.math.geometry.Box;

public abstract class VoxelSystem implements IDrawable
{
	protected ObjectCube<VoxelChunk> chunks;
	public int chunkWidth = 0;
	public int chunkHeight = 0;
	public int chunkDepth = 0;
	public Box bounds;
	private boolean ready = false;
	public boolean visible = true;
	
	public VoxelSystem(
			Box bounds, 
			int width, 
			int height, 
			int depth,
			int xChunkCount,
			int yChunkCount,
			int zChunkCount)
	{
		this.bounds = bounds;
		this.chunkWidth = width / xChunkCount;
		this.chunkHeight = height / yChunkCount;
		this.chunkDepth = depth / zChunkCount;
		this.chunks = new ObjectCube<VoxelChunk>(xChunkCount, yChunkCount, zChunkCount);
	}
	
	public abstract VoxelChunk generateChunk(int chunkX, int chunkY, int chunkZ, Box chunkBounds) throws Exception;
	
	public void generateVoxelSystem() throws Exception
	{
		int
			xChunkCount = chunks.getWidth(),
			yChunkCount = chunks.getHeight(),
			zChunkCount = chunks.getDepth();
		for (int x = 0; x < xChunkCount; x++)
		{
			for (int y = 0; y < yChunkCount; y++)
			{
				for (int z = 0; z < zChunkCount; z++)
				{
					Box chunkBounds = bounds.subDivide(x, y, z, xChunkCount, yChunkCount, zChunkCount);
					VoxelChunk chunk = generateChunk(x, y, z, chunkBounds);
					chunks.setEntry(x, y, z, chunk);
				}
			}
		}
	}

	@Override
	public void upload(OpenGLContext c) throws GLException
	{
		OpenGLManager.getInstance().pushDebug("Uploading", this);
		for (VoxelChunk chunk : chunks)
		{
			chunk.upload(c);
		}
		ready = true;
		OpenGLManager.getInstance().popDebug();
	}

	@Override
	public void release(OpenGLContext c) throws GLException
	{
		OpenGLManager.getInstance().pushDebug("Releasing", this);
		for (VoxelChunk chunk : chunks)
		{
			chunk.release(c);
		}
		OpenGLManager.getInstance().popDebug();
	}

	@Override
	public void draw(OpenGLContext c) throws GLException
	{
		for (VoxelChunk chunk : chunks)
		{
			if (chunk.isVisible() && chunk.isReady())
			{
				chunk.draw(c);
			}
		}
	}
	
	@Override
	public int getFaceCount()
	{
		int faces = 0;
		for (VoxelChunk chunk : chunks)
		{
			faces += chunk.getFaceCount();
		}
		return faces;
	}

	@Override
	public boolean isReady()
	{
		return ready;
	}
	
	@Override
	public boolean isVisible()
	{
		return visible;
	}
}
