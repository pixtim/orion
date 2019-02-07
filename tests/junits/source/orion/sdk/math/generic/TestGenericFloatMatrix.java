package orion.sdk.math.generic;

import orion.sdk.math.ATestFloatMatrix;
import orion.sdk.math.IFloatMatrix;

public class TestGenericFloatMatrix extends ATestFloatMatrix
{
	private static GenericFloatFactory FLOAT_FACTORY = GenericFloatMatrixAdapter.FLOAT_FACTORY;
	
	@Override
	public IFloatMatrix matrix(
			float a11, float a12, float a13, 
			float a21, float a22, float a23, 
			float a31, float a32, float a33) throws Exception 
	{
		GenericFloatMatrixAdapter adapter = new GenericFloatMatrixAdapter(3, 3);
		GenericMatrix<GenericFloat> matrix = adapter.getMatrix();
		
		matrix.set(0, 0, FLOAT_FACTORY.create(a11));
		matrix.set(0, 1, FLOAT_FACTORY.create(a12));
		matrix.set(0, 2, FLOAT_FACTORY.create(a13));
		
		matrix.set(1, 0, FLOAT_FACTORY.create(a21));
		matrix.set(1, 1, FLOAT_FACTORY.create(a22));
		matrix.set(1, 2, FLOAT_FACTORY.create(a23));
		
		matrix.set(2, 0, FLOAT_FACTORY.create(a31));
		matrix.set(2, 1, FLOAT_FACTORY.create(a32));
		matrix.set(2, 2, FLOAT_FACTORY.create(a33));
		
		return new GenericFloatMatrixAdapter(matrix);
	}

	@Override
	public IFloatMatrix matrix(
			float a11, float a12, float a13, float a14, 
			float a21, float a22, float a23, float a24,
			float a31, float a32, float a33, float a34, 
			float a41, float a42, float a43, float a44) throws Exception 
	{
		GenericFloatMatrixAdapter adapter = new GenericFloatMatrixAdapter(4, 4);
		GenericMatrix<GenericFloat> matrix = adapter.getMatrix();
		
		matrix.set(0, 0, FLOAT_FACTORY.create(a11));
		matrix.set(0, 1, FLOAT_FACTORY.create(a12));
		matrix.set(0, 2, FLOAT_FACTORY.create(a13));
		matrix.set(0, 3, FLOAT_FACTORY.create(a14));
		
		matrix.set(1, 0, FLOAT_FACTORY.create(a21));
		matrix.set(1, 1, FLOAT_FACTORY.create(a22));
		matrix.set(1, 2, FLOAT_FACTORY.create(a23));
		matrix.set(1, 3, FLOAT_FACTORY.create(a24));
		
		matrix.set(2, 0, FLOAT_FACTORY.create(a31));
		matrix.set(2, 1, FLOAT_FACTORY.create(a32));
		matrix.set(2, 2, FLOAT_FACTORY.create(a33));
		matrix.set(2, 3, FLOAT_FACTORY.create(a34));
		
		matrix.set(3, 0, FLOAT_FACTORY.create(a41));
		matrix.set(3, 1, FLOAT_FACTORY.create(a42));
		matrix.set(3, 2, FLOAT_FACTORY.create(a43));
		matrix.set(3, 3, FLOAT_FACTORY.create(a44));
		
		return new GenericFloatMatrixAdapter(matrix);
	}

	@Override
	public IFloatMatrix vector(float x, float y) throws Exception
	{		
		return new GenericFloatMatrixAdapter(x, y);
	}

	@Override
	public IFloatMatrix vector(float x, float y, float z) throws Exception
	{
		return new GenericFloatMatrixAdapter(x, y, z);
	}

	@Override
	public IFloatMatrix vector(float x, float y, float z, float u) throws Exception
	{
		return new GenericFloatMatrixAdapter(x, y, z, u);
	}
	
	@Override
	public void test_angleBetween() throws Exception
	{
		/*
		 * Not supported.
		 */
	}
}
