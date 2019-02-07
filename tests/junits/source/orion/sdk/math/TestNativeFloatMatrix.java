package orion.sdk.math;

public class TestNativeFloatMatrix extends ATestFloatMatrix
{
	
	public IFloatMatrix matrix(
			float a11, float a12, float a13,
			float a21, float a22, float a23,
			float a31, float a32, float a33
			)
	{
		return FloatMatrix.matrix(a11, a12, a13, a21, a22, a23, a31, a32, a33);
	}
	
	public IFloatMatrix matrix(
			float a11, float a12, float a13, float a14,
			float a21, float a22, float a23, float a24,
			float a31, float a32, float a33, float a34,
			float a41, float a42, float a43, float a44
			)
	{
		return FloatMatrix.matrix(a11, a12, a13, a14, a21, a22, a23, a24, a31, a32, a33, a34, a41, a42, a43, a44);
	}
	
	public IFloatMatrix vector(float x, float y)
	{
		return FloatMatrix.vector(x, y);
	}
	
	public IFloatMatrix vector(float x, float y, float z)
	{
		return FloatMatrix.vector(x, y, z);
	}
	
	public IFloatMatrix vector(float x, float y, float z, float u)
	{
		return FloatMatrix.vector(x, y, z, u);
	}
}
