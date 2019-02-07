package orion.sdk.graphics.drawables.voxels;

import javax.media.opengl.GL2;
import javax.media.opengl.GLException;
import javax.media.opengl.glu.gl2.GLUgl2;

import orion.sdk.data.cubes.ObjectCube;
import orion.sdk.graphics.util.OpenGLContext;
import orion.sdk.math.FloatMatrix;
import orion.sdk.math.geometry.Box;

public class StaticVoxelChunk extends VoxelChunk
{
	public StaticVoxelChunk(ObjectCube<Voxel> voxels, Box bounds, int maxFaces)
	{
		super(voxels, bounds, maxFaces);
	}
	
	public void setFaces() throws Exception
	{
		super.setFaces();
		
		resetCursor();
		int
			width = voxels.getWidth(),
			height = voxels.getHeight(),
			depth = voxels.getDepth();
		FloatMatrix size = FloatMatrix.vector(
				bounds.getWidth() / width,
				bounds.getHeight() / height,
				bounds.getDepth() / depth);
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				for (int z = 0; z < depth; z++)
				{
					Voxel voxel = voxels.getEntry(x, y, z);
					if (voxel != null)
					{
						int faceCount = voxel.getFaceCount();
						if (faceCount > 0)
						{
							FloatMatrix position = FloatMatrix.vector(
								x / (float) width * bounds.getWidth() + bounds.getLeft(),
								y / (float) height * bounds.getHeight() + bounds.getBottom(),
								z / (float) depth * bounds.getDepth() + bounds.getBack());
							voxel.setFaces(this, position, size);
						}
						advanceCursor(faceCount);
					}												
				}
			}
		}

	}
	
	@Override
	public void upload(OpenGLContext c) throws GLException
	{
		try
		{
			setFaces();
		}
		catch (Exception e)
		{
			throw new GLException("Could not upload voxel system", e);
		}
		super.upload(c);
	}	
}
