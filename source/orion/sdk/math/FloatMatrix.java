package orion.sdk.math;

import orion.sdk.data.MultiMap;
import orion.sdk.util.StructuredBinary;
import orion.sdk.util.IPersistable;

/**
 * 
 * @author Tim
 *
 */
public class FloatMatrix implements IPersistable, IFloatMatrix
{
	/**
	* An array of floating point values storing the entries of this matrix. The matrix is stored in row-major order.
	*/
	protected float[] entries = null;

	/**
	* The number of rows of this matrix.
	*/
	protected int rows = 0;

	/**
	* The number of columns of this matrix.
	*/
	protected int columns = 0;

	protected FloatMatrix(int rows, int columns)
	{
		this.rows = rows;
		this.columns = columns;
		this.entries = new float[rows * columns];
	}	

	@Override
	public float get(int i, int j)
	{
		return entries[i * columns + j];
	}

	@Override
	public void set(int i, int j, float value)
	{
		entries[i * columns + j] = value;
	}

	@Override
	public void normalize() throws Exception
	{
		float norm = norm();
		if (norm > 0)
			for (int i = 0; i < entries.length; i++)
				entries[i] /= norm;
	}

	@Override
	public float angleBetween(IFloatMatrix v) throws Exception
	{
		float denominator = this.norm() * v.norm();
		float dot = this.dot(v);
		if (denominator > 0)
			return (float) Math.acos(dot / denominator);
		else
			return 0;
	}

	/**
	 * Sets all the entries of this matrix to zero.
	 */
	@Override
	public void clear()
	{
		for (int i = 0; i < entries.length; i++)
		{
			entries[i] = 0;
		}
	}
	
	private void assertVector() throws Exception
	{
		if (this.getRowCount() > 1 && this.getColumnCount() > 1)
			throw new Exception("Matrix is not a vector");
	}
	
	private void assertVectors(IFloatMatrix v) throws Exception
	{
		if (this.getRowCount() > 1 && this.getColumnCount() > 1 || v.getRowCount() > 1
				&& v.getColumnCount() > 1)
			throw new Exception("Matrices are not  vectors");
		
		if (this.getLength() != v.getLength()) throw new Exception("Vector dimensions do not match");
	}

	@Override
	public float dot(IFloatMatrix v) throws Exception
	{
		this.assertVectors(v);
		
		float result = 0;
		
		for (int i = 0; i < this.entries.length; i++)
			result += (this.get(i) * v.get(i));
		
		return result;
	}

	@Override
	public float norm() throws Exception
	{
		return norm(entries.length);
	}

	@Override
	public float norm(int dimension) throws Exception
	{
		this.assertVector();
		
		float sum = 0;
		for (int i = 0; i < dimension; i++)
			sum += (float)(Math.pow(this.get(i), 2));
		return (float)(Math.sqrt(sum));
	}
	
	@Override
	public FloatMatrix cross(IFloatMatrix v) throws Exception
	{
		this.assertVectors(v);
		
		if (this.getLength() == 4)
			return vector(
				this.get(1) * v.get(2) - this.get(2) * v.get(1),
				this.get(2) * v.get(0) - this.get(0) * v.get(2),
				this.get(0) * v.get(1) - this.get(1) * v.get(0),
				0
			);
		else if (this.getLength() == 3)
			return vector(
				this.get(1) * v.get(2) - this.get(2) * v.get(1),
				this.get(2) * v.get(0) - this.get(0) * v.get(2),
				this.get(0) * v.get(1) - this.get(1) * v.get(0)
			);
		else if (this.getLength() == 2)
		{
			return vector(
				0,
				0,
				this.get(0) * v.get(1) - this.get(1) * v.get(0)
			);
		}
			throw new Exception("Only 2, 3 or 4 dimensional vectors supported for the cross product");
	}

	@Override
	public IFloatMatrix clone()
	{
		FloatMatrix matrix = zeros(rows, columns);
		
		for (int i = 0; i < this.getLength(); i++)
		{
			matrix.set(i, this.get(i));
		}
		
		return matrix;
	}
	
	@Override
	public int getLength()
	{
		return getRowCount() * getColumnCount();
	}

	/**
	 * Retuns the j-th column of this matrix.
	 */
	@Override
	public IFloatMatrix column(int j)
	{
		FloatMatrix result = zeros(rows, 1);
		for (int i = 0; i < rows; i++)
			result.set(i, get(i, j));
		return result;
	}

	/**
	 * Returns the i-th row of this matrix.
	 */
	@Override
	public IFloatMatrix row(int i)
	{
		FloatMatrix result = zeros(1, columns);
		for (int j = 0; j < columns; j++)
			result.set(j, get(i, j));
		return result;
	}

	@Override
	public IFloatMatrix transpose()
	{
		FloatMatrix result = zeros(columns, rows);
		
		for (int row = 0; row < rows; row++)
			for (int column = 0; column < columns; column++)
				result.set(column, row, get(row, column));
		
		return result;
	}

	@Override
	public String toString()
	{
		return toString(rows != 1 && columns != 1, 8);
	}

	@Override
	public String toString(boolean multiline, int digits)
	{
		String result = "\n[";
		for (int row = 0; row < rows; row++)
		{
			String line = multiline && row > 0 ? " " : "";
			for (int column = 0; column < columns; column++)
			{
				float value = get(row, column);
				if (value == 0)
				{
					value = 0;
				}
				line += String.format("%1$" + digits + "s", Float.toString(value)) + (column < columns - 1 ? ", " : "");
			}
			result += line;
			if (row < rows - 1) result += !multiline ? "; " : "\n";
		}
		return result + "]\n";
	}

	@Override
	public boolean equals(IFloatMatrix obj, float error)
	{
		if (obj instanceof IFloatMatrix)
		{
			IFloatMatrix other = (IFloatMatrix) obj;

			if (other.getRowCount() != this.getRowCount()
					|| other.getColumnCount() != this.getColumnCount())
			{
				return false;
			}

			for (int i = 0; i < entries.length; i++)
			{
				if (Math.abs(other.get(i) - this.get(i)) > error)
				{
					return false;
				}
			}

			return true;
		}
		else
		{
			return false;
		}
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof IFloatMatrix)
		{
			IFloatMatrix other = (IFloatMatrix) obj;
			
			return this.equals(other, 0f);
		}
		else
		{
			return false;
		}
	}
	
	@Override
	public IFloatMatrix add(IFloatMatrix v) throws Exception
	{
		IFloatMatrix matrix = new FloatMatrix(this.getRowCount(), this.getColumnCount());
		
		try
		{
			for (int i = 0; i < matrix.getLength(); i++)
			{
				matrix.set(i, this.get(i) + v.get(i));
			}
		}
		catch (IndexOutOfBoundsException e)
		{
			throw new Exception("Dimensions do not match", e);
		}
		
		return matrix;
	}

	@Override
	public IFloatMatrix subtract(IFloatMatrix v) throws Exception
	{
		FloatMatrix matrix = new FloatMatrix(this.getRowCount(), this.getColumnCount());
		
		try
		{
			for (int i = 0; i < matrix.getLength(); i++)
			{
				matrix.set(i, this.get(i) - v.get(i));
			}
		}
		catch (IndexOutOfBoundsException e)
		{
			throw new Exception("Dimensions do not match", e);
		}
		
		return matrix;
	}
	
	@Override	
	public IFloatMatrix product(IFloatMatrix v) throws Exception
	{
		if (this.getColumnCount() != v.getRowCount()) throw new Exception("Inner dimensions do not match");
		
		FloatMatrix result = new FloatMatrix(this.getRowCount(), v.getColumnCount());
		
		for (int row = 0; row < this.getRowCount(); row++)
		{
			for (int column = 0; column < v.getColumnCount(); column++)
			{
				float sum = 0;
				
				for (int i = 0; i < this.getColumnCount(); i++)
				{
					sum += this.get(row, i) * v.get(i, column);
				}
				
				result.set(row, column, sum);
			}
		}
		
		return result;
	}

	@Override
	public IFloatMatrix scalarProduct(float c)
	{
		IFloatMatrix result = new FloatMatrix(this.getRowCount(), this.getColumnCount());

		for (int row = 0; row < this.getRowCount(); row++)
		{
			for (int column = 0; column < this.getColumnCount(); column++)
			{
				result.set(row, column, c * this.get(row, column));
			}
		}

		return result;
	}

	@Override
	public float get(int i)
	{
		return this.entries[i];
	}

	@Override
	public void set(int i, float value)
	{
		this.entries[i] = value;
	}
	
	@Override
	public float getX()
	{
		return this.get(0);
	}

	@Override
	public float getY()
	{
		return this.get(1);
	}
	
	@Override
	public float getZ()
	{
		return this.get(2);
	}
	
	@Override
	public float getU()
	{
		return this.get(3);
	}
	
	@Override
	public void setX(float value)
	{
		this.set(0, value);
	}
	
	@Override
	public void setY(float value)
	{
		this.set(1, value);
	}
	
	@Override
	public void setZ(float value)
	{
		this.set(2, value);
	}
	
	@Override
	public void setU(float value)
	{
		this.set(3, value);
	}
	
	@Override
	public int getRowCount()
	{
		return this.rows;
	}

	@Override
	public int getColumnCount()
	{
		return this.columns;
	}

	/**
	 * Retuns the sub-matrix at [row, column] with the specified number of rows and columns.
	 */
	@Override
	public IFloatMatrix subMatrix(int row, int column, int rowCount, int columnCount)
	{
		IFloatMatrix result = new FloatMatrix(rowCount, columnCount);
		
		for (int i = 0; i < rowCount; i++)
		{
			for (int j = 0; j < columnCount; j++)
			{
				result.set(i, j, this.get(row + i, column + j));
			}
		}
		
		return result;
	}

	@Override
	public void swapRows(int rowI, int rowJ)
	{
		for (int j = 0; j < this.getColumnCount(); j++)
		{
			float temp = this.get(rowI, j);
			
			this.set(rowI, j, this.get(rowJ, j));
			this.set(rowJ, j, temp);
		}
	}

	@Override
	public void swapColumns(int columnI, int columnJ)
	{
		for (int i = 0; i < this.getRowCount(); i++)
		{
			float temp = this.get(i, columnI);
			
			this.set(i, columnI, this.get(i, columnJ));
			this.set(i, columnJ, temp);
		}
	}
	
	/**
	 * Performs Gaussian elimination on this matrix and returns the result without changing this matrix.
	 */
	@Override
	public IFloatMatrix gaussianElimination() throws Exception
	{
		IFloatMatrix A = this.clone();
		int i = 0, j = 0;
		int m = A.getRowCount(), n = A.getColumnCount();
		
		while (i < m && j < n)
		{
			/*
			 * Find pivot in column j, starting in row i:
			 */
			int maxi = i;
			
			for (int k = i + 1; k < m; k++)
			{
				if (Math.abs(A.get(k,j)) > Math.abs(A.get(maxi,j)))
				{
					maxi = k;
				}
			}

			if (A.get(maxi,j) != 0) {
				/*
				 * swap rows i and maxi, but do not change the value of i
				 */
				A.swapRows(i, maxi);
				
				/*
				 * Now A[i,j] will contain the old value of A[maxi,j].
				 * Divide each entry in row i by A[i,j]
				 */
				float a = A.get(i, j);
				
				for (int p = 0; p < n; p++)
				{
					A.set(i, p, A.get(i, p) / a);
				}
				
				/*
				 * Now A[i,j] will have the value 1.
				 */
				for (int u = 0; u < m; u++)
				{
					if (u != i)
					{
						/*
						 * subtract A[u,j] * row i from row u
						 */
						float b = A.get(u, j);
						
						for (int p = 0; p < n; p++)
						{
							A.set(u, p, A.get(u, p) - b * A.get(i, p));
						}
						
						/*
						 * Now A[u,j] will be 0, since A[u,j] - A[i,j] * A[u,j] = A[u,j] - 1 * A[u,j] = 0.
						 */
					}
				}
				
				i = i + 1;
			}
			
			j = j + 1;
		}

		return A;
	}

	@Override
	public boolean singular() throws Exception
	{
		return this.determinant() == 0;
	}

	/**
	 * Returns the inverse of this matrix. This matrix is assumed to be square and invertable.
	 */
	@Override
	public IFloatMatrix invert() throws Exception
	{
		if (this.getRowCount() != this.getColumnCount()) throw new Exception("Matrix is not square");
		
		int size = this.getRowCount();
		
		IFloatMatrix A = FloatMatrix.concatinateHorizontal(this, FloatMatrix.identity(size));
		A = A.gaussianElimination();
		
		return A.subMatrix(0, size, size, size);
	}

	/**
	 * Removes a column from this matrix and returns the result, not affecting this matrix.
	 */
	@Override
	public IFloatMatrix removeColumn(int column) throws Exception
	{
		IFloatMatrix left = 
				column > 0 ? this.subMatrix(0, 0, this.getRowCount(), column) : null;
		IFloatMatrix right = 
				column < this.getColumnCount() - 1 ? this.subMatrix(0, column + 1, this.getRowCount(), this.getColumnCount() - column - 1) : null;
		
		if (left == null && right == null)
		{
			throw new Exception("Cannot remove last column of matrix");
		}
		if (right != null)
		{
			if (left != null)
			{
				return FloatMatrix.concatinateHorizontal(left, right);
			}
			else
			{
				return right;
			}
		}
		else
		{
			return left;
		}
	}

	/**
	 *  Removes a row from this matrix and returns the result, not affecting this matrix.
	 */
	@Override
	public IFloatMatrix removeRow(int row) throws Exception
	{
		IFloatMatrix up = row > 0 ? this.subMatrix(0, 0, row, this.getColumnCount()) : null;
		
		IFloatMatrix down = row < this.getRowCount() - 1 ? this.subMatrix(row + 1, 0, this.getRowCount() - row - 1, this.getColumnCount()) : null;
		
		if (up == null && down == null)
		{
			throw new Exception("Cannot remove last row of matrix");
		}
		
		if (down != null)
		{
			if (up != null)
			{
				return FloatMatrix.concatinateVertical(up, down);
			}
			else
			{
				return down;
			}
		}
		else
		{
			return up;
		}
	}

	/**
	 * Returns the determinant of this matrix.
	 */
	@Override
	public float determinant() throws Exception
	{
		if (this.getRowCount() != this.getColumnCount()) 
		{
			throw new Exception("Matrix is not square");
		}
		
		if (this.getColumnCount() == 2 && this.getRowCount() == 2)
		{
			return this.get(0, 0) * this.get(1, 1) - this.get(0, 1) * this.get(1, 0);
		}
		
		float det = 0;
		
		for (int column = 0; column < this.getColumnCount(); column++)
		{
			IFloatMatrix minor = this.removeRow(0).removeColumn(column);
			
			det += (float) Math.pow(-1, column) * this.get(0, column) * minor.determinant();
		}
		
		return det;
	}

	/**
	 * Returns the entry wise p-norm of this matrix.
	 */
	@Override
	public float entrywiseNorm(float p)
	{
		float sum = 0;
		
		for (int i = 0; i < this.getRowCount(); i++)
		{
			for (int j = 0; j < this.getColumnCount(); j++)
			{
				sum += (float)Math.pow(Math.abs(get(i, j)), p);
			}
		}
		
		return (float) Math.pow(sum, 1f / p);
	}

	/**
	 * Returns a 1D array of this matrix using the row major representation.
	 */
	@Override
	public float[] rowMajor()
	{
		int index = 0;		
		float[] result = new float[this.getRowCount() * this.getColumnCount()];
		
		for (int row = 0; row < this.getRowCount(); row++)
		{
			for (int column = 0; column < this.getColumnCount(); column++)
			{
				result[index++] = this.get(row, column);
			}
		}
		
		return result;
	}

	/**
	 * Returns a 1D array of this matrix using the column major representation.
	 */
	@Override
	public float[] columnMajor()
	{
		int index = 0;
		float[] result = new float[this.getRowCount() * this.getColumnCount()];
		
		for (int column = 0; column < this.getColumnCount(); column++) 
		{
			for (int row = 0; row < this.getRowCount(); row++)
			{
				result[index++] = this.get(row, column);
			}
		}
		
		return result;
	}

	@Override
	public float trace() throws Exception
	{
		if (this.getRowCount() != this.getColumnCount()) throw new Exception("Matrix is not square");
		
		float T = 0;
		
		for (int i = 0; i < this.getRowCount(); i++ )
		{
			T += this.get(i, i);
		}
		
		return T;
	}

	@Override
	public IFloatMatrix projectToVector(IFloatMatrix v) throws Exception
	{
		float scalar = this.dot(v) / v.norm();
		
		return v.scalarProduct(scalar); 
	}

	@Override
	public void read(StructuredBinary binary) throws Exception
	{
		rows = binary.getInt(0);
		entries = binary.getFloatArray(1);
		columns = entries.length / rows;		
	}

	@Override
	public StructuredBinary write() throws Exception
	{
		StructuredBinary binary = new StructuredBinary();
		binary.add(rows);
		binary.add(entries);
		return binary;
	}

	/**
	 * Concatinates two matrices with equal row counts horizontally.
	 */
	private static IFloatMatrix concatinateHorizontal(IFloatMatrix A, IFloatMatrix B) throws Exception
	{
		if (A.getRowCount() != B.getRowCount()) throw new Exception("A and B do not have equal row counts");
		
		FloatMatrix result = new FloatMatrix(A.getRowCount(), A.getColumnCount() + B.getColumnCount());
		
		for (int row = 0; row < A.getRowCount(); row++)
		{
			for (int column = 0; column < A.getColumnCount(); column++)
			{
				result.set(row, column, A.get(row, column));
			}
			
			for (int column = 0; column < B.getColumnCount(); column++)
			{
				result.set(row, A.getColumnCount() + column, B.get(row, column));
			}
		}
		
		return result;
	}

	/**
	 * Concatinates two matrices with equal column counts vertically.
	 */
	private static IFloatMatrix concatinateVertical(IFloatMatrix A, IFloatMatrix B) throws Exception
	{
		if (A.getColumnCount() != B.getColumnCount()) throw new Exception("A and B do not have equal column counts");
		
		FloatMatrix result = new FloatMatrix(A.getRowCount() + B.getRowCount(), A.getColumnCount());
		
		for (int column = 0; column < A.getColumnCount(); column++)
		{
			for (int row = 0; row < A.getRowCount(); row++)
			{
				result.set(row, column, A.get(row, column));
			}
			for (int row = 0; row < B.getRowCount(); row++)
			{
				result.set(A.getRowCount() + row, column, B.get(row, column));
			}
		}
		
		return result;
	}
	
	/**
	 * Returns a matrix parsed from a string containing the matrix string
	 * representation.
	 * <p>
	 * The string should be format as follows. The matrix should be contained
	 * within matching square braces "[" and "]". The rows of the matrix should
	 * be delimited by semicolons ";". The elements of each row should be
	 * delimited by commas ",". The parsing can be set to ensure the matrix or
	 * vector is homogeneous.
	 */
	public static FloatMatrix parse(String input, boolean makeHomogeneous) throws Exception
	{
		FloatMatrix matrix = null;
		input = input.trim();
		input = input.replaceAll("[", "");
		input = input.replaceAll("]", "");
		String[] rows = input.split(";");
		if (rows.length < 1)
			throw new Exception("Cannot parse empty matrix");
		boolean first = true;
		for (int i = 0; i < rows.length; i++)
		{
			String[] row = rows[i].split(",");
			if (first)
			{
				if (row.length < 1)
					throw new Exception("Cannot parse empty row");
				first = false;
				int
					rowExtra = (makeHomogeneous ? 1 : 0),
					columnExtra = (makeHomogeneous && row.length != 1 ? 1 : 0);
				matrix = new FloatMatrix(rows.length + rowExtra, row.length + columnExtra);
			}
			for (int j = 0; j < row.length; j++)
				matrix.set(i, j, Float.parseFloat(row[j].trim()));
		}
		if (makeHomogeneous)
			matrix.set(matrix.rows - 1, matrix.columns - 1, 1);
		return matrix;
	}
	
	public static FloatMatrix identity(int size)
	{
		FloatMatrix matrix = new FloatMatrix(size, size);
		matrix.clear();
		for (int i = 0; i < size; i++)
		{
			matrix.set(i, i, 1);
		}
		return matrix;
	}
	
	public static FloatMatrix vector(float x, float y)
	{
		FloatMatrix matrix = new FloatMatrix(2, 1);
		matrix.entries[0] = x;
		matrix.entries[1] = y;
		return matrix;
	}
	
	public static FloatMatrix vector(float x, float y, float z)
	{
		FloatMatrix matrix = new FloatMatrix(3, 1);
		matrix.entries[0] = x;
		matrix.entries[1] = y;
		matrix.entries[2] = z;
		return matrix;
	}
	
	public static FloatMatrix vector(float x, float y, float z, float u)
	{
		FloatMatrix matrix = new FloatMatrix(4, 1);
		matrix.entries[0] = x;
		matrix.entries[1] = y;
		matrix.entries[2] = z;
		matrix.entries[3] = u;
		return matrix;
	}

	public static FloatMatrix vector(IFloatMatrix v, int dimensions) throws Exception
	{
		if (v.getRowCount() > 1 && v.getColumnCount() > 1)
		{
			throw new Exception("Cannot create a vector from a non-vector matrix.");
		}
		
		FloatMatrix matrix = new FloatMatrix(dimensions, 1);
		
		for (int i = 0; i < v.getLength() && i < dimensions; i++)
		{
			matrix.set(i, v.get(i));
		}
		
		return matrix;
	}

	public static FloatMatrix vector(float[] entries)
	{
		FloatMatrix m = new FloatMatrix(entries.length, 1);
		m.entries = entries;
		return m;
	}

	public static FloatMatrix zeros(int rows, int columns)
	{
		FloatMatrix matrix = new FloatMatrix(rows, columns);
		matrix.clear();
		return matrix;
	}

	public static FloatMatrix zeros(int size)
	{
		return zeros(size, 1);
	}

	public static FloatMatrix matrix(
		float a11, float a12, float a13,
		float a21, float a22, float a23,
		float a31, float a32, float a33
		)
	{
		FloatMatrix matrix = new FloatMatrix(3, 3);
		matrix.entries[0] = a11;
		matrix.entries[1] = a12;
		matrix.entries[2] = a13;
		matrix.entries[3] = a21;
		matrix.entries[4] = a22;
		matrix.entries[5] = a23;
		matrix.entries[6] = a31;
		matrix.entries[7] = a32;
		matrix.entries[8] = a33;
		return matrix;
	}

	public static FloatMatrix matrix(
		float a11, float a12, float a13, float a14,
		float a21, float a22, float a23, float a24,
		float a31, float a32, float a33, float a34,
		float a41, float a42, float a43, float a44
		)
	{
		FloatMatrix matrix = new FloatMatrix(4, 4);
		matrix.entries[0] = a11;
		matrix.entries[1] = a12;
		matrix.entries[2] = a13;
		matrix.entries[3] = a14;
		matrix.entries[4] = a21;
		matrix.entries[5] = a22;
		matrix.entries[6] = a23;
		matrix.entries[7] = a24;
		matrix.entries[8] = a31;
		matrix.entries[9] = a32;
		matrix.entries[10] = a33;
		matrix.entries[11] = a34;
		matrix.entries[12] = a41;
		matrix.entries[13] = a42;
		matrix.entries[14] = a43;
		matrix.entries[15] = a44;
		return matrix;
	}

	public static FloatMatrix matrix(float[] values)
	{
		int
			length = values.length,
			rows = (int) Math.sqrt(length),
			columns = rows;
		FloatMatrix matrix = new FloatMatrix(rows, columns);
		int row = 0, column = 0;
		for (int i = 0; i < length; i++)
		{
			matrix.set(row, column, values[i]);
			row++;
			if (row > rows - 1)
			{
				row = 0;
				column++;
			}
		}
		return matrix;
	}

	public static FloatMatrix matrix(FloatMatrix original, int size)
	{
		FloatMatrix matrix = identity(size);
		for (int row = 0; row < original.getRowCount(); row++)
			for (int column = 0; column < original.getColumnCount(); column++)
				matrix.set(row, column, original.get(row, column));
		return matrix;
	}
	
	public static IFloatMatrix translate(float dx, float dy, float dz)
	{
		return matrix(
				1, 0, 0, dx, 
				0, 1, 0, dy, 
				0, 0, 1, dz, 
				0, 0, 0, 1);
	}
	
	public static IFloatMatrix translate(IFloatMatrix dv)
	{
		return translate(dv.getX(), dv.getY(), dv.getZ());
	}
	
	public static IFloatMatrix scale(float sx, float sy, float sz)
	{
		return matrix(
				sx, 0, 0, 0, 
				0, sy, 0, 0, 
				0, 0, sz, 0, 
				0, 0, 0, 1);
	}
	
	public static IFloatMatrix scale(IFloatMatrix ds)
	{
		return scale(ds.getX(), ds.getY(), ds.getZ());
	}	
 }

