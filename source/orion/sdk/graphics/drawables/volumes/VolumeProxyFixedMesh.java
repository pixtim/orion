package orion.sdk.graphics.drawables.volumes;

import java.util.List;

import javax.media.opengl.GL2;
import javax.media.opengl.GLException;

import orion.sdk.graphics.drawables.IDrawable;
import orion.sdk.graphics.drawables.surfaces.Mesh;
import orion.sdk.graphics.util.OpenGLContext;
import orion.sdk.math.FloatMatrix;
import orion.sdk.math.FloatQuaternion;
import orion.sdk.math.IFloatMatrix;
import orion.sdk.math.MathUtils;
import orion.sdk.math.geometry.Box;

public class VolumeProxyFixedMesh implements IDrawable
{
	private Mesh mesh = null;
	private List<VolumeChunk> chunks = null;
	
	public VolumeProxyFixedMesh(List<VolumeChunk> chunks, int sliceCount, int rotationCount)
	{
		this.chunks = chunks;
		//this.mesh = this.buildMesh(sliceCount, rotationCount, sliceCount, rotationCount);
		
	}
	
	private Mesh buildMesh(List<VolumeChunk> chunks, int sliceCount, int rotationCount)
	{
		Mesh mesh = new Mesh(sliceCount * rotationCount * 3, Mesh.FaceType.QUAD, null);
		
		int vertexIndex = 0;
		int faceIndex = 0;
		
		FloatQuaternion[] rotations = new FloatQuaternion[3];		
		rotations[0] = FloatQuaternion.rotation(FloatMatrix.vector(1, 0, 0), (float) (Math.PI) / rotationCount);
		rotations[1] = FloatQuaternion.rotation(FloatMatrix.vector(0, 1, 0), (float) (Math.PI) / rotationCount);
		rotations[2] = FloatQuaternion.rotation(FloatMatrix.vector(0, 0, 1), (float) (Math.PI) / rotationCount);
		
		for (VolumeChunk chunk : chunks)
		{
			Box posBounds = chunk.getPositionBounds();
			Box texBounds = chunk.getTextureBounds();
			
		}
		
		
		
		try
		{
			
		}
		catch (Exception e)
		{
			throw new GLException("Can't initialize proxy geometry", e);
		}			
		
		return mesh;
	}
	
	private void slice(Mesh mesh, FloatMatrix[] posStart, FloatMatrix[] posEnd, FloatMatrix[] texStart, FloatMatrix[] texEnd, int sliceCount, int faceIndex, int vertexIndex) throws Exception
	{
		float delta = 1f / sliceCount;
		IFloatMatrix color = FloatMatrix.vector(1, 1, 1, 1f);				
		
		for (float alpha = 0f; alpha < 1f; alpha += delta)
		{
			for (int i = 0; i < 4; i++)
			{
				IFloatMatrix normal = posEnd[i].subtract(posStart[i]);
				normal.normalize();
				IFloatMatrix pos = MathUtils.interpolate(posStart[i], posEnd[i], alpha);
				IFloatMatrix tex = MathUtils.interpolate(texStart[i], texEnd[i], alpha);
				
				mesh.setVertex(vertexIndex++, pos, normal, tex, color);
			}

			mesh.setFace(faceIndex++, vertexIndex, vertexIndex + 1, vertexIndex + 2, vertexIndex + 3);			
		}			
	}

	@Override
	public void upload(OpenGLContext c) throws GLException
	{
		mesh.upload(c);
		
	}

	@Override
	public void release(OpenGLContext c) throws GLException
	{
		mesh.release(c);
		
	}

	@Override
	public boolean isReady()
	{
		return mesh.isReady();
	}

	@Override
	public String getName()
	{
		return mesh.getName() + "_volumeProxy";
	}

	@Override
	public void draw(OpenGLContext c) throws GLException
	{
		for (int i = 0; i < chunks.size(); i++)
		{
			VolumeChunk chunk = chunks.get(i);
			
			c.gl().glMatrixMode(GL2.GL_MODELVIEW);
			c.gl().glPushMatrix();
			
			IFloatMatrix start = chunk.getPositionBounds().getStart();			
			IFloatMatrix size = chunk.getPositionBounds().getSize();
			
			c.gl().glTranslatef(start.getX(), start.getY(), start.getZ());
			c.gl().glScalef(size.getX(), size.getY(), size.getZ());
			
			mesh.draw(c);
			
			c.gl().glMatrixMode(GL2.GL_MODELVIEW);
			c.gl().glPopMatrix();
			
		}
	}

	@Override
	public int getFaceCount()
	{
		return this.chunks.size() * mesh.getFaceCount();
	}

	@Override
	public Box getBounds()
	{
		return null;
	}

	@Override
	public boolean isVisible()
	{
		return true;
	}

}
