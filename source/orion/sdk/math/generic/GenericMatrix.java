package orion.sdk.math.generic;

import java.util.ArrayList;
import java.util.List;

import orion.sdk.math.FloatMatrix;
import orion.sdk.math.IFloatMatrix;
import orion.sdk.monitoring.incidents.Incident;
import orion.sdk.monitoring.incidents.IncidentManager;

/**
 * 
 * @author Tim
 *
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class GenericMatrix<T extends IGenericPrimitive>
{
	protected List<T> entries = null;
	protected int rows = 0;
	protected int columns = 0;
	protected IGenericPrimitiveFactory<T> primitiveFactory = null;
	
	private T ZERO = null;
	private T HALF = null;
	private T ONE = null;
	private T TWO = null;

	protected GenericMatrix(IGenericPrimitiveFactory<T> primitiveFactory, int rows, int columns) throws Exception
	{
		this.primitiveFactory = primitiveFactory;
		this.ZERO = this.primitiveFactory.create();
		this.HALF = this.primitiveFactory.create(0.5f);
		this.ONE = this.primitiveFactory.create(1);
		this.TWO = this.primitiveFactory.create(2);
		
		this.rows = rows;
		this.columns = columns;
		this.entries = new ArrayList<T>(rows * columns);
		
		for (int i = 0; i < this.getLength(); i++)
		{
			this.entries.add(primitiveFactory.create());
		}		
		
	}
	
	public GenericMatrix(IGenericPrimitiveFactory<T> primitiveFactory, T... entries) throws Exception
	{
		this(primitiveFactory, entries.length, 1);
		
		for (int i = 0; i < entries.length; i++)
		{
			this.set(i, entries[i]);
		}
	}

	public GenericMatrix(IGenericPrimitiveFactory<T> primitiveFactory, int rows, int columns, T... entries) throws Exception
	{
		this(primitiveFactory, entries);
		
		this.rows = rows;
		this.columns = columns;
	}
	
	public int getLength()
	{
		return getRowCount() * getColumnCount();
	}
	
	public T get(int row, int column)
	{
		return this.get(row * columns + column);
	}

	public void set(int row, int column, T value)
	{
		this.set(row * columns + column, value);
	}


	public T get(int i)
	{
		return entries.get(i);
	}

	public void set(int i, T value)
	{
		entries.set(i, value);
	}
	
	public T getX()
	{
		return this.get(0);
	}
	
	public T getY()
	{
		return this.get(1);
	}
	
	public T getZ()
	{
		return this.get(2);
	}
	
	public T getU()
	{
		return this.get(3);
	}
	
	public void setX(T value)
	{
		this.set(0, value);
	}
	
	public void setY(T value)
	{
		this.set(1, value);
	}
	
	public void setZ(T value)
	{
		this.set(2, value);
	}
	
	public void setU(T value)
	{
		this.set(3, value);
	}
	
	public int getRowCount()
	{
		return rows;
	}

	public int getColumnCount()
	{
		return columns;
	}	

	public T norm() throws Exception
	{
		return norm(entries.size());
	}

	public T norm(int dimensions) throws Exception
	{
		if (rows > 1 && columns > 1) throw new Exception("Matrix is not a row nor column vector");
		
		T sum = this.primitiveFactory.create();
		
		for (int i = 0; i < dimensions; i++)
		{
			sum = (T) sum.add(this.get(i).power(this.TWO));
		}
		
		return (T) sum.power(this.HALF);
	}
	
	public void normalize() throws Exception
	{
		T norm = norm();
		
		if (norm.compare(this.ZERO) > 0)
		{
			for (int i = 0; i < entries.size(); i++)
			{
				T normalizedValue = (T) this.entries.get(i).devide(norm); 
				this.entries.set(i, normalizedValue);
			}
		}
	}

	public T dot(GenericMatrix<T> v) throws Exception
	{
		this.assertVectors(v);
		
		T result = ZERO;
		
		for (int i = 0; i < v.getLength(); i++)
		{
			result = (T) result.add(this.get(i).multiply(v.get(i)));
		}

		return result;
	}
	
	public GenericMatrix<T> cross(GenericMatrix<T> v) throws Exception
	{
		this.assertVectors(v);
		
		if (v.getLength() == 4)
		{
			return new GenericMatrix<T>(
					this.primitiveFactory,
					(T) this.get(1).multiply(v.get(2)).subtract(this.get(2).multiply(v.get(1))),
					(T) this.get(2).multiply(v.get(0)).subtract(this.get(0).multiply(v.get(2))),
					(T) this.get(0).multiply(v.get(1)).subtract(this.get(1).multiply(v.get(0))),
					ZERO);
		}
		else if (v.getLength() == 3)
		{
			return new GenericMatrix<T>(
					this.primitiveFactory,
					(T) this.get(1).multiply(v.get(2)).subtract(this.get(2).multiply(v.get(1))),
					(T) this.get(2).multiply(v.get(0)).subtract(this.get(0).multiply(v.get(2))),
					(T) this.get(0).multiply(v.get(1)).subtract(this.get(1).multiply(v.get(0))));					
		}
		else if (v.getLength() == 2)
		{
			return new GenericMatrix<T>(
					this.primitiveFactory,
					ZERO,
					ZERO,
					(T) this.get(0).multiply(v.get(1)).subtract(this.get(1).multiply(v.get(0))));					
		}
		else
		{
			throw new Exception("Only 2, 3 or 4 dimensional vectors supported for the cross product");
		}
	}	
	
	public void clear()
	{
		for (int i = 0; i < this.getLength(); i++)
		{
			this.set(i, ZERO);
		}
	}
	
	private void assertVector() throws Exception
	{
		if (this.getRowCount() > 1 && this.getColumnCount() > 1)
			throw new Exception("Matrix is not a vector");
	}
	
	private void assertVectors(GenericMatrix<T> v) throws Exception
	{
		if (this.getRowCount() > 1 && this.getColumnCount() > 1 || v.getRowCount() > 1
				&& v.getColumnCount() > 1)
			throw new Exception("Matrices are not  vectors");
		
		if (this.getLength() != v.getLength()) throw new Exception("Vector dimensions do not match");
	}

	public GenericMatrix<T> column(int j) throws Exception
	{
		GenericMatrix<T> result = new GenericMatrix<T>(this.primitiveFactory, this.getRowCount(), 1);
		
		for (int i = 0; i < this.getRowCount(); i++)
		{
			result.set(i, this.get(i, j));
		}
		
		return result;
	}
	
	public GenericMatrix<T> row(int i) throws Exception
	{
		GenericMatrix<T> result = new GenericMatrix<T>(this.primitiveFactory, 1, this.getColumnCount());		

		for (int j = 0; j < this.getColumnCount(); j++)
		{
			result.set(j, this.get(i, j));
		}
		
		return result;
	}
	
	public GenericMatrix<T> transpose() throws Exception
	{
		GenericMatrix<T> result = new GenericMatrix<T>(this.primitiveFactory, this.getColumnCount(), this.getRowCount());
		
		for (int row = 0; row < this.getRowCount(); row++)
		{
			for (int column = 0; column < this.getColumnCount(); column++)
			{
				result.set(column, row, this.get(row, column));
			}
		}
		
		return result;
	}
	
	public GenericMatrix<T> add(GenericMatrix<T> v) throws Exception
	{
		GenericMatrix<T> matrix = new GenericMatrix<T>(this.primitiveFactory, this.getRowCount(), this.getColumnCount());
		
		try
		{
			for (int i = 0; i < matrix.getLength(); i++)
			{
				matrix.set(i, (T) this.get(i).add(v.get(i)));
			}
		}
		catch (IndexOutOfBoundsException e)
		{
			throw new Exception("Dimensions do not match", e);
		}
		
		return matrix;
	}

	public GenericMatrix<T> subtract(GenericMatrix<T> v) throws Exception
	{
		GenericMatrix<T> matrix = new GenericMatrix<T>(this.primitiveFactory, this.getRowCount(), this.getColumnCount());
		
		try
		{
			for (int i = 0; i < matrix.getLength(); i++)
			{
				matrix.set(i, (T) this.get(i).subtract(v.get(i)));
			}
		}
		catch (IndexOutOfBoundsException e)
		{
			throw new Exception("Dimensions do not match", e);
		}
		
		return matrix;
	}
	
	public GenericMatrix<T> product(GenericMatrix<T> v) throws Exception
	{
		if (this.getColumnCount() != v.getRowCount()) throw new Exception("Inner dimensions do not match");
		
		GenericMatrix<T> result = new GenericMatrix<T>(this.primitiveFactory, this.getRowCount(), this.getColumnCount());
		
		for (int row = 0; row < this.getRowCount(); row++)
		{
			for (int column = 0; column < v.getColumnCount(); column++)
			{
				T sum = ZERO;
				
				for (int i = 0; i < this.getColumnCount(); i++)
				{
					sum = (T) sum.add(this.get(row, i).multiply(v.get(i, column)));
				}
				
				result.set(row, column, sum);
			}
		}
		
		return result;
	}
	
	public GenericMatrix<T> scalarProduct(T c) throws Exception
	{
		GenericMatrix<T> result = new GenericMatrix<T>(this.primitiveFactory, this.getRowCount(), this.getColumnCount());

		for (int row = 0; row < this.getRowCount(); row++)
		{
			for (int column = 0; column < this.getColumnCount(); column++)
			{
				result.set(row, column, (T) c.multiply(this.get(row, column)));
			}
		}

		return result;
	}
	
	public GenericMatrix<T> subMatrix(int row, int column, int rowCount, int columnCount) throws Exception
	{
		GenericMatrix<T> result = new GenericMatrix<T>(this.primitiveFactory, rowCount, columnCount);
		
		for (int i = 0; i < rowCount; i++)
		{
			for (int j = 0; j < columnCount; j++)
			{
				result.set(i, j, this.get(row + i, column + j));
			}
		}
		
		return result;
	}
	
	public void swapRows(int rowI, int rowJ)
	{
		for (int j = 0; j < this.getColumnCount(); j++)
		{
			T temp = this.get(rowI, j);
			
			this.set(rowI, j, this.get(rowJ, j));
			this.set(rowJ, j, temp);
		}
	}

	public void swapColumns(int columnI, int columnJ)
	{
		for (int i = 0; i < this.getRowCount(); i++)
		{
			T temp = this.get(i, columnI);
			
			this.set(i, columnI, this.get(i, columnJ));
			this.set(i, columnJ, temp);
		}
	}
	
	public GenericMatrix<T> clone()
	{
		try
		{
			GenericMatrix<T> matrix = new GenericMatrix<T>(this.primitiveFactory, this.getRowCount(), this.getColumnCount());
			
			for (int i = 0; i < this.getLength(); i++)
			{
				matrix.set(i, this.get(i));
			}
			
			return matrix;		
		}
		catch (Exception e)
		{
			throw new RuntimeException("Failed to clone generic matrix.", e);
		}
	}

	public GenericMatrix<T> gaussianElimination() throws Exception
	{
		GenericMatrix<T> A = this.clone();
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
				if (A.get(k,j).absolute().compare(A.get(maxi,j).absolute()) > 0)
				{
					maxi = k;
				}
			}

			if (A.get(maxi,j).compare(ZERO) != 0) {
				/*
				 * swap rows i and maxi, but do not change the value of i
				 */
				A.swapRows(i, maxi);
				
				/*
				 * Now A[i,j] will contain the old value of A[maxi,j].
				 * Divide each entry in row i by A[i,j]
				 */
				T a = A.get(i, j);
				
				for (int p = 0; p < n; p++)
				{
					A.set(i, p, (T) A.get(i, p).devide(a));
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
						T b = A.get(u, j);
						
						for (int p = 0; p < n; p++)
						{
							T product = (T) b.multiply(A.get(i, p));
							A.set(u, p, (T) A.get(u, p).subtract(product));
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


	private GenericMatrix<T> concatinateHorizontal(GenericMatrix<T> A, GenericMatrix<T> B) throws Exception
	{
		if (A.getRowCount() != B.getRowCount()) throw new Exception("A and B do not have equal row counts");
		
		GenericMatrix<T> result = new GenericMatrix<T>(this.primitiveFactory, A.getRowCount(), A.getColumnCount() + B.getColumnCount());
		
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

	private GenericMatrix<T> concatinateVertical(GenericMatrix<T> A, GenericMatrix<T> B) throws Exception
	{
		if (A.getColumnCount() != B.getColumnCount()) throw new Exception("A and B do not have equal column counts");
		
		GenericMatrix<T> result = new GenericMatrix<T>(this.primitiveFactory, A.getRowCount() + B.getRowCount(), A.getColumnCount());
		
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
	
	public GenericMatrix<T> removeColumn(int column) throws Exception
	{
		GenericMatrix<T> left = 
			column > 0 ? this.subMatrix(0, 0, this.getRowCount(), column) : null;
		GenericMatrix<T> right = 
			column < this.getColumnCount() - 1 ? this.subMatrix(0, column + 1, this.getRowCount(), this.getColumnCount() - column - 1) : null;
		
		if (left == null && right == null)
		{
			throw new Exception("Cannot remove last column of matrix");
		}
		if (right != null)
		{
			if (left != null)
			{
				return this.concatinateHorizontal(left, right);
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

	public GenericMatrix<T> removeRow(int row) throws Exception
	{
		GenericMatrix<T> up = row > 0 ? this.subMatrix(0, 0, row, this.getColumnCount()) : null;
		
		GenericMatrix<T> down = row < this.getRowCount() - 1 ? this.subMatrix(row + 1, 0, this.getRowCount() - row - 1, this.getColumnCount()) : null;
		
		if (up == null && down == null)
		{
			throw new Exception("Cannot remove last row of matrix");
		}
		
		if (down != null)
		{
			if (up != null)
			{
				return this.concatinateVertical(up, down);
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
	
	public T determinant() throws Exception
	{
		if (this.getRowCount() != this.getColumnCount()) 
		{
			throw new Exception("Matrix is not square");
		}
		
		if (this.getColumnCount() == 2 && this.getRowCount() == 2)
		{
			return (T) this.get(0, 0).multiply(this.get(1, 1)).subtract(this.get(0, 1).multiply(this.get(1, 0)));
		}
		
		T det = ZERO;
		
		for (int column = 0; column < this.getColumnCount(); column++)
		{
			GenericMatrix<T> minor = this.removeRow(0).removeColumn(column);
			
			T sign = primitiveFactory.create(Math.pow(-1, column));
			
			det = (T) det.add(sign.multiply(this.get(0, column)).multiply(minor.determinant()));
		}
		
		return det;
	}

	public boolean equals(GenericMatrix<T> obj, T error) throws Exception
	{
		if (obj instanceof GenericMatrix)
		{
			GenericMatrix<T> other = (GenericMatrix<T>) obj;

			if (other.getRowCount() != this.getRowCount()
					|| other.getColumnCount() != this.getColumnCount())
			{
				return false;
			}

			for (int i = 0; i < this.getLength(); i++)
			{
				T delta = (T) other.get(i).subtract(this.get(i)).absolute();
				
				if (delta.compare(error) > 0)
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
		if (obj instanceof GenericMatrix)
		{
			GenericMatrix<T> other = (GenericMatrix<T>) obj;
			
			try
			{
				return this.equals(other, ZERO);
			}
			catch (Exception e)
			{
				IncidentManager.notifyIncident(Incident.newError("Matrix equals check failure.", e));
				
				return false;
			}
		}
		else
		{
			return false;
		}
	}

	public boolean singular() throws Exception
	{
		T det = this.determinant();
		return det.equals(ZERO); 
	}
	
	public T entrywiseNorm(T p) throws Exception
	{
		T sum = ZERO;
		
		for (int i = 0; i < this.getRowCount(); i++)
		{
			for (int j = 0; j < this.getColumnCount(); j++)
			{
				sum = (T) sum.add(get(i, j).absolute().power(p));
			}
		}
		
		return (T) sum.power(ONE.devide(p));
	}
	
	public List<T> rowMajor()
	{
		List<T> result = new ArrayList<T>();
		
		for (int row = 0; row < this.getRowCount(); row++)
		{
			for (int column = 0; column < this.getColumnCount(); column++)
			{
				result.add(this.get(row, column));
			}
		}
		
		return result;
	}

	public List<T> columnMajor()
	{
		List<T> result = new ArrayList<T>();
		
		for (int column = 0; column < this.getColumnCount(); column++) 
		{
			for (int row = 0; row < this.getRowCount(); row++)
			{
				result.add(this.get(row, column));
			}
		}
		
		return result;
	}

	public T trace() throws Exception
	{
		if (this.getRowCount() != this.getColumnCount()) throw new Exception("Matrix is not square");
		
		T result = ZERO;
		
		for (int i = 0; i < this.getRowCount(); i++ )
		{
			result = (T) result.add(this.get(i, i));
		}
		
		return result;
	}

	public GenericMatrix<T> projectToVector(GenericMatrix<T> v) throws Exception
	{
		T scalar = (T) this.dot(v).devide(v.norm());
		
		return v.scalarProduct(scalar); 
	}
		
	public void setToIdentity()
	{		
		for (int column = 0; column < this.getColumnCount(); column++) 
		{
			for (int row = 0; row < this.getRowCount(); row++)
			{
				if (column == row)
				{
					this.set(row, column, ONE);
				}
				else
				{
					this.set(row, column, ZERO);
				}
				
			}
		}
	}	
	
	public GenericMatrix<T> invert() throws Exception
	{
		if (this.getRowCount() != this.getColumnCount()) throw new Exception("Matrix is not square");
		
		int size = this.getRowCount();
		
		GenericMatrix<T> I = new GenericMatrix<>(this.primitiveFactory, size, size);
		I.setToIdentity();
		
		GenericMatrix<T> A = this.concatinateHorizontal(this, I);
		A = A.gaussianElimination();
		
		return A.subMatrix(0, size, size, size);
	}	
 }

