package orion.sdk.graphics.drawables.voxels;

import orion.sdk.graphics.shading.texturing.Sprite;
import orion.sdk.math.FloatMatrix;
import orion.sdk.math.IFloatMatrix;

public class Voxel
{	
	public int spriteTile		= 0;	
	public boolean sideLeft	= true;
	public boolean sideRight	= true;
	public boolean sideFront	= true;
	public boolean sideBack	= true;
	public boolean sideTop		= true;
	public boolean sideBottom	= true;
	public FloatMatrix color			= FloatMatrix.vector(1, 1, 1, 1);
	
	public Voxel()
	{
	}
	
	public int getFaceCount()
	{
		int count = 0;
		count += (sideLeft		? 1 : 0);
		count += (sideRight		? 1 : 0);
		count += (sideFront		? 1 : 0);
		count += (sideBack		? 1 : 0);
		count += (sideTop		? 1 : 0);
		count += (sideBottom	? 1 : 0);
		return count;
	}
	
	public void setFaces(VoxelChunk voxelSystem, FloatMatrix position, FloatMatrix size) throws Exception
	{
		int faceCursor = voxelSystem.getCursor();

		FloatMatrix I = FloatMatrix.identity(4);
		
		/*
		 * Positions
		 */
		IFloatMatrix scale = FloatMatrix.scale(size);
		IFloatMatrix translation = FloatMatrix.translate(position);
		IFloatMatrix transform = translation.product(scale);
		
		IFloatMatrix p1 = transform.product(FloatMatrix.vector(0, 0, 1, 1));
		IFloatMatrix p2 = transform.product(FloatMatrix.vector(1, 0, 1, 1));
		IFloatMatrix p3 = transform.product(FloatMatrix.vector(1, 1, 1, 1));
		IFloatMatrix p4 = transform.product(FloatMatrix.vector(0, 1, 1, 1));
		
		IFloatMatrix p5 = transform.product(FloatMatrix.vector(0, 0, 0, 1));
		IFloatMatrix p6 = transform.product(FloatMatrix.vector(1, 0, 0, 1));
		IFloatMatrix p7 = transform.product(FloatMatrix.vector(1, 1, 0, 1));
		IFloatMatrix p8 = transform.product(FloatMatrix.vector(0, 1, 0, 1));
		
		/*
		 * Normals
		 */
		IFloatMatrix n1 = FloatMatrix.vector(-1,  0,  0);
		IFloatMatrix n2 = FloatMatrix.vector( 1,  0,  0);
		IFloatMatrix n3 = FloatMatrix.vector( 0, -1,  0);
		IFloatMatrix n4 = FloatMatrix.vector( 0,  1,  0);
		IFloatMatrix n5 = FloatMatrix.vector( 0,  0, -1);
		IFloatMatrix n6 = FloatMatrix.vector( 0,  0,  1);
		
		/*
		 * Textures
		 */
		IFloatMatrix uv00, uv10, uv11, uv01;
		if (voxelSystem.sprite != null)
		{
			Sprite.Tile tile = voxelSystem.sprite.tiles.get(spriteTile);
			uv00 = tile.uv00;
			uv10 = tile.uv10;
			uv11 = tile.uv11;
			uv01 = tile.uv01;
		}
		else
		{
			uv00 = FloatMatrix.vector(0, 0);
			uv10 = FloatMatrix.vector(1, 0);
			uv11 = FloatMatrix.vector(1, 1);
			uv01 = FloatMatrix.vector(0, 1);
		}

		/*
		 * Set the faces
		 */
		int
			i = faceCursor * 4,
			j = faceCursor,
			v1 = 0, v2 = 0, v3 = 0, v4 = 0;
			
		
		if (sideFront)
		{
			voxelSystem.setVertex(v1 = i++ , p1, n6, uv00, color); voxelSystem.setVertex(v2 = i++ , p2, n6, uv10, color);
			voxelSystem.setVertex(v3 = i++ , p3, n6, uv11, color); voxelSystem.setVertex(v4 = i++ , p4, n6, uv01, color);
			voxelSystem.setFace(j++, v1, v2, v3, v4);
		}
		if (sideBack)
		{
			voxelSystem.setVertex(v1 = i++ , p5, n5, uv00, color);	voxelSystem.setVertex(v2 = i++, p6, n5, uv10, color);
			voxelSystem.setVertex(v3 = i++ , p7, n5, uv11, color);	voxelSystem.setVertex(v4 = i++, p8, n5, uv01, color);
			voxelSystem.setFace(j++, v1, v2, v3, v4);
		}
		if (sideLeft)
		{
			voxelSystem.setVertex(v1 = i++, p1, n1, uv00, color);	voxelSystem.setVertex(v2 = i++, p5, n1, uv10, color);
			voxelSystem.setVertex(v3 = i++, p8, n1, uv11, color);	voxelSystem.setVertex(v4 = i++, p4, n1, uv01, color);
			voxelSystem.setFace(j++, v1, v2, v3, v4);
		}
		if (sideRight)
		{
			voxelSystem.setVertex(v1 = i++, p2, n2, uv00, color);	voxelSystem.setVertex(v2 = i++, p6, n2, uv10, color);
			voxelSystem.setVertex(v3 = i++, p7, n2, uv11, color);	voxelSystem.setVertex(v4 = i++, p3, n2, uv01, color);
			voxelSystem.setFace(j++, v1, v2, v3, v4);
		}
		if (sideTop)
		{
			voxelSystem.setVertex(v1 = i++, p4, n4, uv00, color);	voxelSystem.setVertex(v2 = i++, p3, n4, uv10, color);
			voxelSystem.setVertex(v3 = i++, p7, n4, uv11, color);	voxelSystem.setVertex(v4 = i++, p8, n4, uv01, color);
			voxelSystem.setFace(j++, v1, v2, v3, v4);
		}
		if (sideBottom)
		{
			voxelSystem.setVertex(v1 = i++, p1, n3, uv00, color);	voxelSystem.setVertex(v2 = i++, p2, n3, uv10, color);
			voxelSystem.setVertex(v3 = i++, p6, n3, uv11, color);	voxelSystem.setVertex(v4 = i++, p5, n3, uv01, color);
			voxelSystem.setFace(j++, v1, v2, v3, v4);
		}
	}
}
