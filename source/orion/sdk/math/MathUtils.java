package orion.sdk.math;

public class MathUtils
{

	/**
	 * Determines whether and where a ray intersects a enclosed triangle. All vectors are 3D.
	 */
	public static IFloatMatrix lineTriangleIntersect(IFloatMatrix rayOrigin, IFloatMatrix rayDirection, IFloatMatrix v0, IFloatMatrix v1, IFloatMatrix v2) throws Exception
	{
		/*
		 *  Implemetation from "Fast, Minimum Storage Ray/Triangle Intersection" by Tomas Moller and Ben Trumbore
		 */
		float epsilon = 0.000001f;
		
		IFloatMatrix
			edge1 = v1.subtract(v0),
			edge2 = v2.subtract(v0);
	
		IFloatMatrix pVec = rayDirection.cross(edge2);
	
		float det = edge1.dot(pVec);
		if (det < epsilon) return null;
	
		IFloatMatrix tVec = rayOrigin.subtract(v0);
	
		float u = tVec.dot(pVec);
		if (u < 0 || u > det) return null;
	
		IFloatMatrix qVec = tVec.cross(edge1);
	
		float v = rayDirection.dot(qVec);
		if (v < 0 || u + v > det) return null;
	
		float t = edge2.dot(qVec);
	
		float invDet = 1f / det;
		t *= invDet;
		u *= invDet;
		v *= invDet;
	
		return rayOrigin.add(rayDirection.scalarProduct(t));
	}

	public static IFloatMatrix linePlaneIntersect(IFloatMatrix lineStart, IFloatMatrix lineDirection, IFloatMatrix planeStart, IFloatMatrix planeNormal) throws Exception
	{
		IFloatMatrix A = lineStart, c = lineDirection, B = planeStart, n = planeNormal;
		float t = n.dot(B.subtract(A)) / n.dot(c);		
		return A.add(c.scalarProduct(t));
	}
	
	public static IFloatMatrix interpolate(IFloatMatrix start, IFloatMatrix end, float alpha) throws Exception
	{
		return start.add(end.subtract(start).scalarProduct(alpha));
	}

}
