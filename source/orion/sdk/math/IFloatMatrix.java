package orion.sdk.math;

public interface IFloatMatrix
{
	public float get(int i, int j);

	public void set(int i, int j, float value);

	public void normalize() throws Exception;

	public float angleBetween(IFloatMatrix v) throws Exception;

	public void clear();

	public float dot(IFloatMatrix v) throws Exception;

	public float norm() throws Exception;

	public float norm(int dimension) throws Exception;

	public IFloatMatrix cross(IFloatMatrix v) throws Exception;

	public int getLength();

	public IFloatMatrix column(int j);

	public IFloatMatrix row(int i);

	public IFloatMatrix transpose();

	public String toString(boolean multiline, int digits);

	public boolean equals(IFloatMatrix obj, float error);

	public boolean equals(Object obj);

	public IFloatMatrix add(IFloatMatrix v) throws Exception;

	public IFloatMatrix subtract(IFloatMatrix v) throws Exception;

	public IFloatMatrix product(IFloatMatrix v) throws Exception;

	public IFloatMatrix scalarProduct(float c);
	
	public float get(int i);

	public void set(int i, float value);
	
	public float getX();
	
	public float getY();
	
	public float getZ();
	
	public float getU();
	
	public void setX(float value);
	
	public void setY(float value);

	public void setZ(float value);
	
	public void setU(float value);
	
	public int getRowCount();

	public int getColumnCount();

	public IFloatMatrix subMatrix(int row, int column, int rowCount, int columnCount);

	public void swapRows(int rowI, int rowJ);

	public void swapColumns(int columnI, int columnJ);
	
	public IFloatMatrix gaussianElimination() throws Exception;

	public boolean singular() throws Exception;

	public IFloatMatrix invert() throws Exception;

	public IFloatMatrix removeColumn(int column) throws Exception;
	
	public IFloatMatrix removeRow(int row) throws Exception;

	public float determinant() throws Exception;

	public float entrywiseNorm(float p) throws Exception;

	public float[] rowMajor();

	public float[] columnMajor();

	public float trace() throws Exception;

	public IFloatMatrix projectToVector(IFloatMatrix v) throws Exception;

	public IFloatMatrix clone();
}
