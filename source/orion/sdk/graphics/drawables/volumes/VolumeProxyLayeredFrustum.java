package orion.sdk.graphics.drawables.volumes;

import com.jogamp.opengl.GLException;

import orion.sdk.graphics.drawables.surfaces.Mesh;
import orion.sdk.graphics.shading.glsl.GenericShader;
import orion.sdk.math.FloatMatrix;
import orion.sdk.math.geometry.Box;

public class VolumeProxyLayeredFrustum extends Mesh
{
	private int sliceCount = 100;

	public VolumeProxyLayeredFrustum(int raySamples, GenericShader shader)
	{
		super(raySamples, Mesh.FaceType.QUAD, new Box(0, 0, 0, 1, 1, 1));
		
		this.sliceCount = raySamples;
		
		this.initialize();
	}
	
	public void initialize()
	{
		try
		{
			float near = 0f;
			float far = 1f;
			
			float delta = (far - near) / sliceCount;
			int step = 0;
			
			FloatMatrix color = FloatMatrix.vector(1, 1, 1, 1f);
			
			for (float z = far; z > near; z -= delta)
			{
				int faceIndex = step;
				int vertexIndex = faceIndex * 4;
					
				setVertex(vertexIndex + 0, FloatMatrix.vector(0, 0, z, 1), null, null, color);
				setVertex(vertexIndex + 1, FloatMatrix.vector(1, 0, z, 1), null, null, color);
				setVertex(vertexIndex + 2, FloatMatrix.vector(1, 1, z, 1), null, null, color);
				setVertex(vertexIndex + 3, FloatMatrix.vector(0, 1, z, 1), null, null, color);

				setFace(faceIndex, vertexIndex, vertexIndex + 1, vertexIndex + 2, vertexIndex + 3);			
				step++;
			}
		}
		catch (Exception e)
		{
			throw new GLException("Can't initialize proxy geometry", e);
		}		
	}
	
	private float remap(float w)
	{
		return (float) (1 - Math.pow(w, 3));
	}
}