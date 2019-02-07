package orion.sdk.math.geometry;

import java.util.ArrayList;
import java.util.List;

import orion.sdk.math.IFloatMatrix;

public class Face
{
	private List<Vertex> vertices = new ArrayList<Vertex>();
	
	public Face(Vertex... vertices)
	{
		for (Vertex vertex : vertices)
		{
			this.vertices.add(vertex);
		}
	}
	
	public List<Vertex> getVertices()
	{
		return this.vertices;
	}
	
	public Plane getPlane() throws Exception
	{
		if (this.vertices.size() < 3)
		{
			throw new Exception("Not enough vertices to construct plane.");
		}
		
		IFloatMatrix position = this.vertices.get(1).getPosition();
		IFloatMatrix leftPos = this.vertices.get(0).getPosition();
		IFloatMatrix rightPos = this.vertices.get(2).getPosition();

		IFloatMatrix left = leftPos.subtract(position);
		IFloatMatrix right = rightPos.subtract(position);
		IFloatMatrix normal = right.cross(left);
		normal.normalize();
		
		return new Plane(position, normal);
	}
}
