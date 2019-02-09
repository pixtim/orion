package orion.sdk.graphics.drawables.voxels;

import com.jogamp.opengl.GLException;

import orion.sdk.data.cubes.ObjectCube;
import orion.sdk.graphics.drawables.surfaces.Mesh;
import orion.sdk.graphics.shading.texturing.Sprite;
import orion.sdk.math.FloatMatrix;
import orion.sdk.math.geometry.Box;

public abstract class VoxelChunk extends Mesh
{
	public ObjectCube<Voxel> voxels;
	public Box bounds = new Box();
	
	protected int voxelCursor = 0;
	public Sprite sprite = null;
	
	public VoxelChunk(ObjectCube<Voxel> voxels, Box bounds, int maxFaces) throws GLException
	{
		super(maxFaces, Mesh.FaceType.QUAD, bounds);
		this.voxels = voxels;
		this.bounds = bounds;
	}
	
	protected void advanceCursor(int faces)
	{
		voxelCursor += faces;
	}
	
	protected void resetCursor()
	{
		voxelCursor = 0;
	}	
	
	protected int getCursor()
	{
		return voxelCursor;
	}
	
	protected Voxel getVoxel(int x, int y, int z)
	{
		return voxels.getEntry(x, y, z);
	}
	
	protected void setVoxel(int x, int y, int z, Voxel voxel)
	{
		voxels.setEntry(x, y, z, voxel);
	}
	
	public FloatMatrix getVoxelPosition(int x, int y, int z)
	{
		return FloatMatrix.vector(
			bounds.getLeft() + bounds.getWidth() / voxels.getWidth() * x,
			bounds.getBottom() + bounds.getHeight() / voxels.getHeight() * y,
			bounds.getBack() + bounds.getDepth() / voxels.getDepth() * z);
			
	}
	
	public FloatMatrix getVoxelSize()
	{
		return FloatMatrix.vector(
			bounds.getWidth() / voxels.getWidth(),
			bounds.getHeight() / voxels.getHeight(),
			bounds.getDepth() / voxels.getDepth());
	}
	
	public void setFaces() throws Exception
	{
	}
	
	/**
	 * Sets the sides of each voxel so that only the surfaces are rendered.
	 * @param voxels
	 * 	The voxels to cull.
	 * @return
	 * 	The number of faces required to render the voxels after culling.
	 */
	public static int cullVoxels(ObjectCube<Voxel> voxels)
	{
		int
			faces = 0,
			width = voxels.getWidth(),
			height = voxels.getHeight(),
			depth = voxels.getDepth();
		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++)
				for (int z = 0; z < depth; z++)
				{
					Voxel voxel = voxels.getEntry(x, y, z);
					if (voxel != null)
					{
						Voxel
							left = x - 1 >= 0 ? voxels.getEntry(x - 1, y, z) : null, 
							right = x + 1 < width ? voxels.getEntry(x + 1, y, z) : null,
							bottom = y - 1 >= 0 ? voxels.getEntry(x, y - 1, z) : null,
							top = y + 1 < height ? voxels.getEntry(x, y + 1, z) : null,
							back = z - 1 >= 0 ? voxels.getEntry(x, y, z - 1) : null,
							front = z + 1 < depth ? voxels.getEntry(x, y, z + 1) : null;
						voxel.sideLeft = left == null;
						voxel.sideRight = right == null;
						voxel.sideTop = top == null;
						voxel.sideBottom = bottom == null;
						voxel.sideFront = front == null;
						voxel.sideBack = back == null;
						faces += voxel.getFaceCount();
					}
				}		
		return faces;
	}
}
