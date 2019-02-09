package orion.sdk.math;

import org.junit.Assert;
import org.junit.Test;

public class TestMathUtils
{	
	public final float ERROR = 0.00001f;
	
	@Test
	public void test_linePlaneIntersect() throws Exception
	{
		FloatMatrix lineStart = FloatMatrix.vector(0, 0, 1);
		FloatMatrix lineDirection = FloatMatrix.vector(0, 0, -1);
		FloatMatrix planeStart = FloatMatrix.vector(0, 0, 0);
		FloatMatrix planeNormal = FloatMatrix.vector(0, 0, 1);
		
		IFloatMatrix result = MathUtils.linePlaneIntersect(lineStart, lineDirection, planeStart, planeNormal);
		
		Assert.assertEquals(0, result.get(0), ERROR);
		Assert.assertEquals(0, result.get(1), ERROR);
		Assert.assertEquals(0, result.get(2), ERROR);
	}
}
