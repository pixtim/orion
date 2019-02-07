package orion.sdk.math;

import org.junit.Assert;
import org.junit.Test;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public abstract class ATestFloatMatrix
{
	public final float ERROR = 0.00001f;
	
	public abstract IFloatMatrix matrix(
			float a11, float a12, float a13,
			float a21, float a22, float a23,
			float a31, float a32, float a33
			) throws Exception;
	
	public abstract IFloatMatrix matrix(
			float a11, float a12, float a13, float a14,
			float a21, float a22, float a23, float a24,
			float a31, float a32, float a33, float a34,
			float a41, float a42, float a43, float a44
			) throws Exception;
	
	public abstract IFloatMatrix vector(float x, float y) throws Exception;
	
	public abstract IFloatMatrix vector(float x, float y, float z) throws Exception;
	
	public abstract IFloatMatrix vector(float x, float y, float z, float u) throws Exception;
	
	@Test
	public void test_get_VECTOR() throws Exception
	{
		IFloatMatrix v = this.vector(1, 2, 3, 4);
		
		Assert.assertEquals(1, v.get(0), ERROR);
		Assert.assertEquals(2, v.get(1), ERROR);
		Assert.assertEquals(3, v.get(2), ERROR);
		Assert.assertEquals(4, v.get(3), ERROR);
	}
	
	@Test
	public void test_get_MATRIX() throws Exception
	{
		IFloatMatrix m = this.matrix(
			11, 12, 13, 14,
			21, 22, 23, 24,
			31, 32, 33, 34,
			41, 42, 43, 44);
		
		Assert.assertEquals(11, m.get(0, 0), ERROR);
		Assert.assertEquals(12, m.get(0, 1), ERROR);
		Assert.assertEquals(13, m.get(0, 2), ERROR);
		Assert.assertEquals(14, m.get(0, 3), ERROR);
		
		Assert.assertEquals(21, m.get(1, 0), ERROR);
		Assert.assertEquals(22, m.get(1, 1), ERROR);
		Assert.assertEquals(23, m.get(1, 2), ERROR);
		Assert.assertEquals(24, m.get(1, 3), ERROR);
		
		Assert.assertEquals(31, m.get(2, 0), ERROR);
		Assert.assertEquals(32, m.get(2, 1), ERROR);
		Assert.assertEquals(33, m.get(2, 2), ERROR);
		Assert.assertEquals(34, m.get(2, 3), ERROR);
		
		Assert.assertEquals(41, m.get(3, 0), ERROR);
		Assert.assertEquals(42, m.get(3, 1), ERROR);
		Assert.assertEquals(43, m.get(3, 2), ERROR);
		Assert.assertEquals(44, m.get(3, 3), ERROR);
	}

	@Test
	public void test_set_VECTOR() throws Exception
	{
		IFloatMatrix v = this.vector(0, 0, 0, 0);
		
		v.set(0, 1);
		v.set(1, 2);
		v.set(2, 3);
		v.set(3, 4);
		
		Assert.assertEquals(1, v.get(0), ERROR);
		Assert.assertEquals(2, v.get(1), ERROR);
		Assert.assertEquals(3, v.get(2), ERROR);
		Assert.assertEquals(4, v.get(3), ERROR);
	}
	
	@Test
	public void test_set_MATRIX() throws Exception
	{
		IFloatMatrix m = this.matrix(
			0, 0, 0, 0,
			0, 0, 0, 0,
			0, 0, 0, 0,
			0, 0, 0, 0);
		
		m.set(0, 0, 11);
		m.set(0, 1, 12);
		m.set(0, 2, 13);
		m.set(0, 3, 14);
		
		m.set(1, 0, 21);
		m.set(1, 1, 22);
		m.set(1, 2, 23);
		m.set(1, 3, 24);
		
		m.set(2, 0, 31);
		m.set(2, 1, 32);
		m.set(2, 2, 33);
		m.set(2, 3, 34);
		
		m.set(3, 0, 41);
		m.set(3, 1, 42);
		m.set(3, 2, 43);
		m.set(3, 3, 44);
		
		Assert.assertEquals(11, m.get(0, 0), ERROR);
		Assert.assertEquals(12, m.get(0, 1), ERROR);
		Assert.assertEquals(13, m.get(0, 2), ERROR);
		Assert.assertEquals(14, m.get(0, 3), ERROR);
		
		Assert.assertEquals(21, m.get(1, 0), ERROR);
		Assert.assertEquals(22, m.get(1, 1), ERROR);
		Assert.assertEquals(23, m.get(1, 2), ERROR);
		Assert.assertEquals(24, m.get(1, 3), ERROR);
		
		Assert.assertEquals(31, m.get(2, 0), ERROR);
		Assert.assertEquals(32, m.get(2, 1), ERROR);
		Assert.assertEquals(33, m.get(2, 2), ERROR);
		Assert.assertEquals(34, m.get(2, 3), ERROR);
		
		Assert.assertEquals(41, m.get(3, 0), ERROR);
		Assert.assertEquals(42, m.get(3, 1), ERROR);
		Assert.assertEquals(43, m.get(3, 2), ERROR);
		Assert.assertEquals(44, m.get(3, 3), ERROR);
	}

	@Test
	public void test_normalize() throws Exception
	{
		IFloatMatrix v = this.vector(1, 2, 3, 4);
		
		v.normalize();
		
		Assert.assertEquals(0.99999994f, v.norm(), ERROR);	
	}

	@Test
	public void test_angleBetween() throws Exception
	{
		IFloatMatrix u = this.vector(1, 0, 0);
		IFloatMatrix v = this.vector(0, 1, 0);
		
		float angle = u.angleBetween(v);
		Assert.assertEquals(Math.PI / 2, angle, ERROR);	
	}

	@Test
	public void test_clear() throws Exception
	{
		IFloatMatrix v = this.vector(1, 2, 3, 4);
		
		v.clear();
		
		Assert.assertEquals(0, v.get(0), ERROR);
		Assert.assertEquals(0, v.get(1), ERROR);
		Assert.assertEquals(0, v.get(2), ERROR);
		Assert.assertEquals(0, v.get(3), ERROR);
	}

	@Test
	public void test_dot() throws Exception
	{
		IFloatMatrix u = this.vector(1, 2, 3);
		IFloatMatrix v = this.vector(3, 2, 1);
		
		float dot = u.dot(v);
		
		Assert.assertEquals(10, dot, ERROR);
	}

	@Test
	public void test_norm() throws Exception
	{
		IFloatMatrix v = this.vector(3, 4);
		
		float norm = v.norm();
		
		Assert.assertEquals(5, norm, ERROR);		
	}

	@Test
	public void test_cross_2D() throws Exception
	{
		IFloatMatrix u = this.vector(1, 0);
		IFloatMatrix v = this.vector(0, 1);
		
		IFloatMatrix cross = u.cross(v);
		
		Assert.assertEquals(0, cross.get(0), ERROR);
		Assert.assertEquals(0, cross.get(1), ERROR);
		Assert.assertEquals(1, cross.get(2), ERROR);
	}

	@Test
	public void test_cross_3D() throws Exception
	{
		IFloatMatrix u = this.vector(0, 0, 1);
		IFloatMatrix v = this.vector(1, 0, 0);
		
		IFloatMatrix cross = u.cross(v);
		
		Assert.assertEquals(0, cross.get(0), ERROR);
		Assert.assertEquals(1, cross.get(1), ERROR);
		Assert.assertEquals(0, cross.get(2), ERROR);
	}
	
	@Test
	public void test_getLength_VECTOR() throws Exception
	{
		IFloatMatrix v = this.vector(1, 2, 3, 4);
		
		Assert.assertEquals(4, v.getLength());
	}
	
	@Test
	public void test_getLength_MATRIX() throws Exception
	{
		IFloatMatrix m = this.matrix(
				11, 12, 13, 14,
				21, 22, 23, 24,
				31, 32, 33, 34,
				41, 42, 43, 44);
		
		Assert.assertEquals(16, m.getLength());
	}
	
	@Test
	public void test_column() throws Exception
	{
		IFloatMatrix m = this.matrix(
				11, 12, 13, 14,
				21, 22, 23, 24,
				31, 32, 33, 34,
				41, 42, 43, 44);
		
		IFloatMatrix column = m.column(1);
		
		Assert.assertEquals(12, column.get(0), ERROR);
		Assert.assertEquals(22, column.get(1), ERROR);
		Assert.assertEquals(32, column.get(2), ERROR);		
		Assert.assertEquals(42, column.get(3), ERROR);		
	}

	@Test
	public void test_row() throws Exception
	{
		IFloatMatrix m = this.matrix(
				11, 12, 13, 14,
				21, 22, 23, 24,
				31, 32, 33, 34,
				41, 42, 43, 44);
		
		IFloatMatrix row = m.row(1);
		
		Assert.assertEquals(21, row.get(0), ERROR);
		Assert.assertEquals(22, row.get(1), ERROR);
		Assert.assertEquals(23, row.get(2), ERROR);		
		Assert.assertEquals(24, row.get(3), ERROR);		
	}

	@Test
	public void test_transpose() throws Exception
	{
		IFloatMatrix m = this.matrix(
				11, 12, 13, 14,
				21, 22, 23, 24,
				31, 32, 33, 34,
				41, 42, 43, 44);
		
		IFloatMatrix t = m.transpose();
		
		Assert.assertEquals(11, t.get(0, 0), ERROR);
		Assert.assertEquals(21, t.get(0, 1), ERROR);
		Assert.assertEquals(31, t.get(0, 2), ERROR);		
		Assert.assertEquals(41, t.get(0, 3), ERROR);				
		
		Assert.assertEquals(12, t.get(1, 0), ERROR);
		Assert.assertEquals(22, t.get(1, 1), ERROR);
		Assert.assertEquals(32, t.get(1, 2), ERROR);		
		Assert.assertEquals(42, t.get(1, 3), ERROR);				
		
		Assert.assertEquals(13, t.get(2, 0), ERROR);
		Assert.assertEquals(23, t.get(2, 1), ERROR);
		Assert.assertEquals(33, t.get(2, 2), ERROR);		
		Assert.assertEquals(43, t.get(2, 3), ERROR);				
		
		Assert.assertEquals(14, t.get(3, 0), ERROR);
		Assert.assertEquals(24, t.get(3, 1), ERROR);
		Assert.assertEquals(34, t.get(3, 2), ERROR);		
		Assert.assertEquals(44, t.get(3, 3), ERROR);				
	}

	@Test
	public void test_equals_TRUE() throws Exception
	{
		IFloatMatrix u = this.vector(0, 1);
		IFloatMatrix v = this.vector(0, 1);
		
		Assert.assertTrue(u.equals(v));
	}

	@Test
	public void test_equals_FALSE() throws Exception
	{
		IFloatMatrix u = this.vector(0, 1);
		IFloatMatrix v = this.vector(1, 0);
		
		Assert.assertFalse(u.equals(v));
	}

	@Test
	public void test_add() throws Exception
	{
		IFloatMatrix v1 = this.vector(0, 0, 1);
		IFloatMatrix v2 = this.vector(1, 0, 0);
		IFloatMatrix v3 = this.vector(0, 1, 0);
		
		IFloatMatrix result = this.vector(0, 0, 0);
		result = result.add(v1);
		result = result.add(v2);
		result = result.add(v3);
		
		Assert.assertEquals(1, result.get(0), ERROR);
		Assert.assertEquals(1, result.get(1), ERROR);
		Assert.assertEquals(1, result.get(2), ERROR);
	}

	@Test
	public void test_subtract() throws Exception
	{
		IFloatMatrix v1 = this.vector(0, 0, 1);
		IFloatMatrix v2 = this.vector(1, 0, 0);
		
		IFloatMatrix result = v1.subtract(v2);
		
		Assert.assertEquals(-1, result.get(0), ERROR);
		Assert.assertEquals(0, result.get(1), ERROR);
		Assert.assertEquals(1, result.get(2), ERROR);
	}

	@Test
	public void test_product_PAIR() throws Exception
	{
		IFloatMatrix m1 = this.matrix(
				111, 112, 113, 114,
				121, 122, 123, 124,
				131, 132, 133, 134,
				141, 142, 143, 144);

		IFloatMatrix m2 = this.matrix(
				211, 212, 213, 214,
				221, 222, 223, 224,
				231, 232, 233, 234,
				241, 242, 243, 244);
		
		IFloatMatrix result = m1.product(m2);
		
		Assert.assertEquals(101750, result.get(0, 0), ERROR);
		Assert.assertEquals(102200, result.get(0, 1), ERROR);
		Assert.assertEquals(102650, result.get(0, 2), ERROR);
		Assert.assertEquals(103100, result.get(0, 3), ERROR);
				
		Assert.assertEquals(110790, result.get(1, 0), ERROR);
		Assert.assertEquals(111280, result.get(1, 1), ERROR);
		Assert.assertEquals(111770, result.get(1, 2), ERROR);
		Assert.assertEquals(112260, result.get(1, 3), ERROR);
		
		Assert.assertEquals(119830, result.get(2, 0), ERROR);
		Assert.assertEquals(120360, result.get(2, 1), ERROR);
		Assert.assertEquals(120890, result.get(2, 2), ERROR);
		Assert.assertEquals(121420, result.get(2, 3), ERROR);
		
		Assert.assertEquals(128870, result.get(3, 0), ERROR);
		Assert.assertEquals(129440, result.get(3, 1), ERROR);
		Assert.assertEquals(130010, result.get(3, 2), ERROR);
		Assert.assertEquals(130580, result.get(3, 3), ERROR);
	}

	@Test
	public void test_product_MUTIPLE() throws Exception
	{
		IFloatMatrix m1 = this.matrix(
				111, 112, 113, 114,
				121, 122, 123, 124,
				131, 132, 133, 134,
				141, 142, 143, 144);

		IFloatMatrix m2 = this.matrix(
				211, 212, 213, 214,
				221, 222, 223, 224,
				231, 232, 233, 234,
				241, 242, 243, 244);

		IFloatMatrix m3 = this.matrix(
				311, 312, 313, 314,
				321, 322, 323, 324,
				331, 332, 333, 334,
				341, 342, 343, 344);
		
		IFloatMatrix result = m1;		
		result = result.product(m2);
		result = result.product(m3);
	 
		Assert.assertEquals(1.33584704E8, result.get(0, 0), ERROR);
		Assert.assertEquals(1.339944E8, result.get(0, 1), ERROR);
		Assert.assertEquals(1.34404096E8, result.get(0, 2), ERROR);
		Assert.assertEquals(1.34813792E8, result.get(0, 3), ERROR);
				
		Assert.assertEquals(1.45453104E8, result.get(1, 0), ERROR);
		Assert.assertEquals(1.458992E8, result.get(1, 1), ERROR);
		Assert.assertEquals(1.46345296E8, result.get(1, 2), ERROR);
		Assert.assertEquals(1.46791408E8, result.get(1, 3), ERROR);
		
		Assert.assertEquals(1.57321504E8, result.get(2, 0), ERROR);
		Assert.assertEquals(1.57804E8, result.get(2, 1), ERROR);
		Assert.assertEquals(1.58286496E8, result.get(2, 2), ERROR);
		Assert.assertEquals(1.58769008E8, result.get(2, 3), ERROR);
		
		Assert.assertEquals(1.69189904E8, result.get(3, 0), ERROR);
		Assert.assertEquals(1.697088E8, result.get(3, 1), ERROR);
		Assert.assertEquals(1.70227696E8, result.get(3, 2), ERROR);
		Assert.assertEquals(1.70746608E8, result.get(3, 3), ERROR);
	}
	@Test
	public void test_scalarProduct() throws Exception
	{
		IFloatMatrix v = this.vector(1, 2, 3);
		
		IFloatMatrix result = v.scalarProduct(2);
		
		Assert.assertEquals(2, result.get(0), ERROR);
		Assert.assertEquals(4, result.get(1), ERROR);
		Assert.assertEquals(6, result.get(2), ERROR);
	}

	@Test
	public void test_getX() throws Exception
	{
		IFloatMatrix v = this.vector(1, 2, 3, 4);
		
		float result = v.getX();
		
		Assert.assertEquals(1, result, ERROR);
	}

	@Test
	public void test_getY() throws Exception
	{
		IFloatMatrix v = this.vector(1, 2, 3, 4);
		
		float result = v.getY();
		
		Assert.assertEquals(2, result, ERROR);
	}

	@Test
	public void test_getZ() throws Exception
	{
		IFloatMatrix v = this.vector(1, 2, 3, 4);
		
		float result = v.getZ();
		
		Assert.assertEquals(3, result, ERROR);
	}

	@Test
	public void test_getU() throws Exception
	{
		IFloatMatrix v = this.vector(1, 2, 3, 4);
		
		float result = v.getU();
		
		Assert.assertEquals(4, result, ERROR);
	}

	@Test
	public void test_setX() throws Exception
	{
		IFloatMatrix v = this.vector(0, 0, 0, 0);
		
		v.setX(1);
		
		float result = v.getX();
		
		Assert.assertEquals(1, result, ERROR);
	}

	@Test
	public void test_setY() throws Exception
	{
		IFloatMatrix v = this.vector(0, 0, 0, 0);
		
		v.setY(2);
		
		float result = v.getY();
		
		Assert.assertEquals(2, result, ERROR);	}

	@Test
	public void test_setZ() throws Exception
	{
		IFloatMatrix v = this.vector(0, 0, 0, 0);
		
		v.setZ(3);
		
		float result = v.getZ();
		
		Assert.assertEquals(3, result, ERROR);	}

	@Test
	public void test_setU() throws Exception
	{
		IFloatMatrix v = this.vector(0, 0, 0, 0);
		
		v.setU(4);
		
		float result = v.getU();
		
		Assert.assertEquals(4, result, ERROR);	}

	@Test
	public void test_getRowCount() throws Exception
	{
		IFloatMatrix m = this.matrix(
				11, 12, 13, 14,
				21, 22, 23, 24,
				31, 32, 33, 34,
				41, 42, 43, 44);
		
		Assert.assertEquals(4, m.getRowCount());
	}

	@Test
	public void test_getColumnCount() throws Exception
	{
		IFloatMatrix m = this.matrix(
				11, 12, 13,
				21, 22, 23,
				31, 32, 33);
		
		Assert.assertEquals(3, m.getRowCount());
	}

	@Test
	public void test_subMatrix() throws Exception
	{
		IFloatMatrix m1 = this.matrix(
				11, 12, 13, 14,
				21, 22, 23, 24,
				31, 32, 33, 34,
				41, 42, 43, 44);
		
		IFloatMatrix m2 = m1.subMatrix(1, 1, 2, 2);
		
		Assert.assertEquals(22, m2.get(0, 0), ERROR);
		Assert.assertEquals(23, m2.get(0, 1), ERROR);
		Assert.assertEquals(32, m2.get(1, 0), ERROR);
		Assert.assertEquals(33, m2.get(1, 1), ERROR);
	}

	@Test
	public void test_swapRows() throws Exception
	{
		IFloatMatrix m = this.matrix(
				11, 12, 13, 14,
				21, 22, 23, 24,
				31, 32, 33, 34,
				41, 42, 43, 44);
		
		m.swapRows(0, 1);
		
		Assert.assertEquals(21, m.get(0, 0), ERROR);
		Assert.assertEquals(22, m.get(0, 1), ERROR);
		Assert.assertEquals(23, m.get(0, 2), ERROR);
		Assert.assertEquals(24, m.get(0, 3), ERROR);
		
		Assert.assertEquals(11, m.get(1, 0), ERROR);
		Assert.assertEquals(12, m.get(1, 1), ERROR);
		Assert.assertEquals(13, m.get(1, 2), ERROR);
		Assert.assertEquals(14, m.get(1, 3), ERROR);
	}

	@Test
	public void test_swapColumns() throws Exception
	{
		IFloatMatrix m = this.matrix(
				11, 12, 13, 14,
				21, 22, 23, 24,
				31, 32, 33, 34,
				41, 42, 43, 44);
		
		m.swapColumns(0, 1);
		
		Assert.assertEquals(12, m.get(0, 0), ERROR);
		Assert.assertEquals(22, m.get(1, 0), ERROR);
		Assert.assertEquals(32, m.get(2, 0), ERROR);
		Assert.assertEquals(42, m.get(3, 0), ERROR);
		
		Assert.assertEquals(11, m.get(0, 1), ERROR);
		Assert.assertEquals(21, m.get(1, 1), ERROR);
		Assert.assertEquals(31, m.get(2, 1), ERROR);
		Assert.assertEquals(41, m.get(3, 1), ERROR);
	}
	
	@Test
	public void test_gaussianElimination() throws Exception
	{
		IFloatMatrix m = this.matrix(
				11, 12, 13, 14,
				21, 22, 23, 24,
				31, 32, 33, 34,
				41, 42, 43, 44);
		
		IFloatMatrix result = m.gaussianElimination();
		
		Assert.assertEquals(1, result.get(0, 0), ERROR);
		Assert.assertEquals(0, result.get(0, 1), ERROR);
		Assert.assertEquals(0, result.get(0, 2), ERROR);
		Assert.assertEquals(0, result.get(0, 3), ERROR);
				
		Assert.assertEquals(0, result.get(1, 0), ERROR);
		Assert.assertEquals(1, result.get(1, 1), ERROR);
		Assert.assertEquals(0, result.get(1, 2), ERROR);
		Assert.assertEquals(0, result.get(1, 3), ERROR);
		
		Assert.assertEquals(0, result.get(2, 0), ERROR);
		Assert.assertEquals(0, result.get(2, 1), ERROR);
		Assert.assertEquals(1, result.get(2, 2), ERROR);
		Assert.assertEquals(0, result.get(2, 3), ERROR);
		
		Assert.assertEquals(0, result.get(3, 0), ERROR);
		Assert.assertEquals(0, result.get(3, 1), ERROR);
		Assert.assertEquals(0, result.get(3, 2), ERROR);
		Assert.assertEquals(1, result.get(3, 3), ERROR);
	}

	@Test
	public void test_singular() throws Exception
	{
		IFloatMatrix m = this.matrix(
				11, 12, 13, 14,
				21, 22, 23, 24,
				31, 32, 33, 34,
				41, 42, 43, 44);
		
		Assert.assertTrue(m.singular());
	}

	@Test
	public void test_invert() throws Exception
	{
		float ERROR_MULTIPLE = ERROR * 10000;
		
		IFloatMatrix m = this.matrix(
				11, 12, 13, 14,
				21, 22, 23, 24,
				31, 32, 33, 34,
				41, 42, 43, 44);
		
		IFloatMatrix result = m.invert();	
		
		Assert.assertEquals(-272800.75,  result.get(0, 0), ERROR_MULTIPLE);
		Assert.assertEquals(524288.0, result.get(0, 1), ERROR_MULTIPLE);
		Assert.assertEquals(-230175.046875, result.get(0, 2), ERROR_MULTIPLE);
		Assert.assertEquals(-21312.48046875, result.get(0, 3), ERROR_MULTIPLE);
				
		Assert.assertEquals(591067.0625, result.get(1, 0), ERROR_MULTIPLE);
		Assert.assertEquals(-1048576.0, result.get(1, 1), ERROR_MULTIPLE);
		Assert.assertEquals(323950.125, result.get(1, 2), ERROR_MULTIPLE);
		Assert.assertEquals(133558.625, result.get(1, 3), ERROR_MULTIPLE);
		
		Assert.assertEquals(-363733.3125, result.get(2, 0), ERROR_MULTIPLE);
		Assert.assertEquals(524288.0, result.get(2, 1), ERROR_MULTIPLE);
		Assert.assertEquals(42625.0390625, result.get(2, 2), ERROR_MULTIPLE);
		Assert.assertEquals(-203179.453125, result.get(2, 3), ERROR_MULTIPLE);
		
		Assert.assertEquals(45467.01171875, result.get(3, 0), ERROR_MULTIPLE);
		Assert.assertEquals(0.0, result.get(3, 1), ERROR_MULTIPLE);
		Assert.assertEquals(-136400.125, result.get(3, 2), ERROR_MULTIPLE);
		Assert.assertEquals(90933.34375, result.get(3, 3), ERROR_MULTIPLE);
	}

	@Test
	public void test_removeColumn() throws Exception
	{
		IFloatMatrix m = this.matrix(
				11, 12, 13, 14,
				21, 22, 23, 24,
				31, 32, 33, 34,
				41, 42, 43, 44);
		
		IFloatMatrix result = m.removeColumn(1);
		
		Assert.assertEquals(11, result.get(0, 0), ERROR);
		Assert.assertEquals(13, result.get(0, 1), ERROR);
		Assert.assertEquals(14, result.get(0, 2), ERROR);
		
		Assert.assertEquals(21, result.get(1, 0), ERROR);
		Assert.assertEquals(23, result.get(1, 1), ERROR);
		Assert.assertEquals(24, result.get(1, 2), ERROR);
		
		Assert.assertEquals(31, result.get(2, 0), ERROR);
		Assert.assertEquals(33, result.get(2, 1), ERROR);
		Assert.assertEquals(34, result.get(2, 2), ERROR);
		
		Assert.assertEquals(41, result.get(3, 0), ERROR);
		Assert.assertEquals(43, result.get(3, 1), ERROR);
		Assert.assertEquals(44, result.get(3, 2), ERROR);		
	}

	@Test
	public void test_removeRow() throws Exception
	{
		IFloatMatrix m = this.matrix(
				11, 12, 13, 14,
				21, 22, 23, 24,
				31, 32, 33, 34,
				41, 42, 43, 44);
		
		IFloatMatrix result = m.removeRow(1);
		
		Assert.assertEquals(11, result.get(0, 0), ERROR);
		Assert.assertEquals(12, result.get(0, 1), ERROR);
		Assert.assertEquals(13, result.get(0, 2), ERROR);
		Assert.assertEquals(14, result.get(0, 3), ERROR);
		
		Assert.assertEquals(31, result.get(1, 0), ERROR);
		Assert.assertEquals(32, result.get(1, 1), ERROR);
		Assert.assertEquals(33, result.get(1, 2), ERROR);
		Assert.assertEquals(34, result.get(1, 3), ERROR);
		
		Assert.assertEquals(41, result.get(2, 0), ERROR);
		Assert.assertEquals(42, result.get(2, 1), ERROR);
		Assert.assertEquals(43, result.get(2, 2), ERROR);
		Assert.assertEquals(44, result.get(2, 3), ERROR);
	}

	@Test
	public void test_determinant() throws Exception
	{
		IFloatMatrix m = this.matrix(
				11, 12, 13, 14,
				21, 22, 23, 24,
				31, 32, 33, 34,
				41, 42, 43, 44);
		
		float result = m.determinant();
				
		Assert.assertEquals(0, result, ERROR);
	}

	@Test
	public void test_entrywiseNorm() throws Exception
	{
		IFloatMatrix m = this.matrix(
				2, 2, 2, 2,
				2, 2, 2, 2,
				2, 2, 2, 2,
				2, 2, 2, 2);
		
		float result = m.entrywiseNorm(2);
				
		Assert.assertEquals(8, result, ERROR);
	}

	@Test
	public void test_rowMajor() throws Exception
	{
		IFloatMatrix m = this.matrix(
				1, 2, 3, 4,
				5, 6, 7, 8,
				9, 10, 11, 12,
				13, 14, 15, 16);
		
		float[] result = m.rowMajor();
		
		for (int i = 0; i < m.getLength(); i++)
		{
			Assert.assertEquals(i + 1, result[i], ERROR);
		}
	}

	@Test
	public void test_columnMajor() throws Exception
	{
		IFloatMatrix m = this.matrix(
				1, 5, 9, 13,
				2, 6, 10, 14,
				3, 7, 11, 15,
				4, 8, 12, 16);
		
		float[] result = m.columnMajor();
		
		for (int i = 0; i < m.getLength(); i++)
		{
			Assert.assertEquals(i + 1, result[i], ERROR);
		}
	}

	@Test
	public void test_trace() throws Exception
	{
		IFloatMatrix m = this.matrix(
				1, 2, 2, 2,
				2, 2, 2, 2,
				2, 2, 3, 2,
				2, 2, 2, 4);
		
		float result = m.trace();
		
		Assert.assertEquals(10, result, ERROR);
	}

	@Test
	public void test_projectToVector() throws Exception
	{
		IFloatMatrix u = this.vector(1, 2, 3);
		
		IFloatMatrix v = this.vector(4, 5, 6);
		
		IFloatMatrix result = u.projectToVector(v);
		
		Assert.assertEquals(14.586954, result.get(0), ERROR);
		Assert.assertEquals(18.233692, result.get(1), ERROR);
		Assert.assertEquals(21.880432, result.get(2), ERROR);
	}

	@Test
	public void test_clone() throws Exception
	{
		IFloatMatrix u = this.vector(1, 2, 3);
		
		IFloatMatrix result = u.clone();
		
		Assert.assertEquals(1, result.get(0), ERROR);
		Assert.assertEquals(2, result.get(1), ERROR);
		Assert.assertEquals(3, result.get(2), ERROR);
	}
}
