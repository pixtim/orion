package orion.sdk.math.generic;

import java.util.List;

import orion.sdk.math.FloatMatrix;
import orion.sdk.math.IFloatMatrix;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class GenericFloatMatrixAdapter implements IFloatMatrix
{
	public static GenericFloatFactory FLOAT_FACTORY = new GenericFloatFactory();
	public static String ERROR_MSG = "A generic float matrix failure has been encountered.";
	
	private GenericMatrix<GenericFloat> matrix = null;
	
	public GenericFloatMatrixAdapter(GenericMatrix<GenericFloat> matrix) throws Exception
	{
		this.matrix = matrix;
	}
	
	public GenericFloatMatrixAdapter(int rows, int columns) throws Exception
	{
		this.matrix = new GenericMatrix<GenericFloat>(FLOAT_FACTORY, rows, columns);
	}
	
	public GenericFloatMatrixAdapter(float... entries) throws Exception
	{
		this(entries.length, 1);
		
		for (int i = 0; i < entries.length; i++)
		{
			this.getMatrix().get(i).setValue(entries[i]);
		}
	}
	
	public GenericFloatMatrixAdapter(IFloatMatrix floatMatrix) throws Exception
	{
		this(floatMatrix.getRowCount(), floatMatrix.getColumnCount());
		
		for (int i = 0; i < floatMatrix.getLength(); i++)
		{
			this.getMatrix().set(i, FLOAT_FACTORY.create(floatMatrix.get(i)));
		}
	}

	public IFloatMatrix toFloatMatrix()
	{
		IFloatMatrix floatMatrix = FloatMatrix.zeros(this.getRowCount(), this.getColumnCount());
		
		for (int i = 0; i < floatMatrix.getLength(); i++)
		{
			floatMatrix.set(i, this.getMatrix().get(i).getValue());
		}
		
		return floatMatrix;
	}
	
	public GenericMatrix<GenericFloat> getMatrix()
	{
		return matrix;
	}

	@Override
	public float get(int i, int j)
	{
		return this.getMatrix().get(i, j).getValue();
	}

	@Override
	public void set(int i, int j, float value)
	{
		try
		{
			this.getMatrix().set(i, j, FLOAT_FACTORY.create(value));
		}
		catch (Exception e) 
		{
			throw new RuntimeException(ERROR_MSG, e);
		}
	}

	@Override
	public void normalize() throws Exception
	{
		this.getMatrix().normalize();
	}

	@Override
	public float angleBetween(IFloatMatrix v) throws Exception
	{
		/*
		 * Not supported.
		 */
		throw new NotImplementedException();
	}

	@Override
	public void clear()
	{
		this.getMatrix().clear();
	}

	@Override
	public float dot(IFloatMatrix v) throws Exception
	{
		GenericFloatMatrixAdapter adapterV = new GenericFloatMatrixAdapter(v);
		return this.getMatrix().dot(adapterV.getMatrix()).getValue();
	}

	@Override
	public float norm() throws Exception
	{
		return this.getMatrix().norm().getValue();
	}

	@Override
	public float norm(int dimension) throws Exception
	{
		return this.getMatrix().norm(dimension).getValue();
	}

	@Override
	public IFloatMatrix cross(IFloatMatrix v) throws Exception
	{
		GenericFloatMatrixAdapter adapterV = new GenericFloatMatrixAdapter(v);
		GenericMatrix<GenericFloat> genericResult = this.getMatrix().cross(adapterV.getMatrix());		
		return new GenericFloatMatrixAdapter(genericResult);
	}

	@Override
	public int getLength()
	{
		return this.getMatrix().getLength();
	}

	@Override
	public IFloatMatrix column(int j)
	{
		try
		{
			GenericMatrix<GenericFloat> genericResult = this.getMatrix().column(j);		
			return new GenericFloatMatrixAdapter(genericResult);
		}
		catch (Throwable e)
		{
			throw new RuntimeException(ERROR_MSG, e);
		}
	}

	@Override
	public IFloatMatrix row(int i)
	{
		try
		{
			GenericMatrix<GenericFloat> genericResult = this.getMatrix().row(i);
			return new GenericFloatMatrixAdapter(genericResult);
		}
		catch (Throwable e)
		{
			throw new RuntimeException(ERROR_MSG, e);
		}
	}

	@Override
	public IFloatMatrix transpose()
	{
		try
		{
			GenericMatrix<GenericFloat> genericResult = this.getMatrix().transpose();
			return new GenericFloatMatrixAdapter(genericResult);
		}
		catch (Throwable e)
		{
			throw new RuntimeException(ERROR_MSG, e);
		}
	}

	@Override
	public String toString(boolean multiline, int digits)
	{
		throw new NotImplementedException();
	}
	
	@Override
	public boolean equals(Object obj)
	{
		try
		{
			if (obj instanceof IFloatMatrix)
			{
				GenericFloatMatrixAdapter other = new GenericFloatMatrixAdapter((IFloatMatrix) obj);
				return this.matrix.equals(other.matrix);
			}
			else
			{
				return false;
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException(ERROR_MSG, e); 
		}
	}

	@Override
	public boolean equals(IFloatMatrix obj, float error)
	{
		try
		{
			if (obj instanceof IFloatMatrix)
			{
				GenericFloatMatrixAdapter other = new GenericFloatMatrixAdapter((IFloatMatrix) obj);
				return this.matrix.equals(other.matrix, FLOAT_FACTORY.create(error));
			}
			else
			{
				return false;
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException(ERROR_MSG, e); 
		}
	}

	@Override
	public IFloatMatrix add(IFloatMatrix v) throws Exception
	{
		GenericFloatMatrixAdapter adapterV = new GenericFloatMatrixAdapter(v);
		GenericMatrix<GenericFloat> genericResult = this.getMatrix().add(adapterV.getMatrix());
		return new GenericFloatMatrixAdapter(genericResult);
	}

	@Override
	public IFloatMatrix subtract(IFloatMatrix v) throws Exception
	{
		GenericFloatMatrixAdapter adapterV = new GenericFloatMatrixAdapter(v);
		GenericMatrix<GenericFloat> genericResult = this.getMatrix().subtract(adapterV.getMatrix());
		return new GenericFloatMatrixAdapter(genericResult);
	}

	@Override
	public IFloatMatrix product(IFloatMatrix v) throws Exception
	{
		GenericFloatMatrixAdapter adapterV = new GenericFloatMatrixAdapter(v);
		GenericMatrix<GenericFloat> genericResult = this.getMatrix().product(adapterV.getMatrix());
		return new GenericFloatMatrixAdapter(genericResult);
	}

	@Override
	public IFloatMatrix scalarProduct(float c)
	{
		try
		{
			GenericMatrix<GenericFloat> genericResult = this.getMatrix().scalarProduct(FLOAT_FACTORY.create(c));
			return new GenericFloatMatrixAdapter(genericResult);
		}
		catch (Exception e)
		{
			throw new RuntimeException(ERROR_MSG, e);
		} 
	}

	@Override
	public float get(int i)
	{
		return this.getMatrix().get(i).getValue();
	}

	@Override
	public void set(int i, float value)
	{
		try
		{
			this.getMatrix().set(i, FLOAT_FACTORY.create(value));
		}
		catch (Exception e)
		{
			throw new RuntimeException(ERROR_MSG, e);
		} 
	}

	@Override
	public float getX()
	{
		try
		{
			return this.getMatrix().getX().getValue();
		}
		catch (Exception e)
		{
			throw new RuntimeException(ERROR_MSG, e);
		} 		
	}

	@Override
	public float getY()
	{
		try
		{
			return this.getMatrix().getY().getValue();
		}
		catch (Exception e)
		{
			throw new RuntimeException(ERROR_MSG, e);
		}
	}

	@Override
	public float getZ()
	{
		try
		{
			return this.getMatrix().getZ().getValue();
		}
		catch (Exception e)
		{
			throw new RuntimeException(ERROR_MSG, e);
		}
	}

	@Override
	public float getU()
	{
		try
		{
			return this.getMatrix().getU().getValue();
		}
		catch (Exception e)
		{
			throw new RuntimeException(ERROR_MSG, e);
		}
	}

	@Override
	public void setX(float value)
	{
		try
		{
			this.getMatrix().setX(FLOAT_FACTORY.create(value));
		}
		catch (Exception e)
		{
			throw new RuntimeException(ERROR_MSG, e);
		}
	}

	@Override
	public void setY(float value)
	{
		try
		{
			this.getMatrix().setY(FLOAT_FACTORY.create(value));
		}
		catch (Exception e)
		{
			throw new RuntimeException(ERROR_MSG, e);
		}
	}

	@Override
	public void setZ(float value)
	{
		try
		{
			this.getMatrix().setZ(FLOAT_FACTORY.create(value));
		}
		catch (Exception e)
		{
			throw new RuntimeException(ERROR_MSG, e);
		}
	}

	@Override
	public void setU(float value)
	{
		try
		{
			this.getMatrix().setU(FLOAT_FACTORY.create(value));
		}
		catch (Exception e)
		{
			throw new RuntimeException(ERROR_MSG, e);
		}
	}

	@Override
	public int getRowCount()
	{
		return this.getMatrix().getRowCount();
	}

	@Override
	public int getColumnCount()
	{
		return this.getMatrix().getColumnCount();
	}

	@Override
	public IFloatMatrix subMatrix(int row, int column, int rowCount, int columnCount)
	{
		try
		{
			GenericMatrix<GenericFloat> genericResult = this.getMatrix().subMatrix(row, column, rowCount, columnCount);
			return new GenericFloatMatrixAdapter(genericResult);
		}
		catch (Exception e)
		{
			throw new RuntimeException(ERROR_MSG, e);
		}		
	}

	@Override
	public void swapRows(int rowI, int rowJ)
	{
		this.getMatrix().swapRows(rowI, rowJ);
	}

	@Override
	public void swapColumns(int columnI, int columnJ)
	{
		this.getMatrix().swapColumns(columnI, columnJ);
	}

	@Override
	public IFloatMatrix gaussianElimination()
	{
		try
		{
			GenericMatrix<GenericFloat> genericResult = this.getMatrix().gaussianElimination();
			return new GenericFloatMatrixAdapter(genericResult);
		}
		catch (Exception e)
		{
			throw new RuntimeException(ERROR_MSG, e);
		}		
	}

	@Override
	public boolean singular() throws Exception
	{		
		return this.matrix.singular();
	}

	@Override
	public IFloatMatrix invert() throws Exception
	{
		GenericMatrix<GenericFloat> genericResult = this.getMatrix().invert();
		return new GenericFloatMatrixAdapter(genericResult);
	}

	@Override
	public IFloatMatrix removeColumn(int column) throws Exception
	{
		GenericMatrix<GenericFloat> genericResult = this.getMatrix().removeColumn(column);
		return new GenericFloatMatrixAdapter(genericResult);
	}

	@Override
	public IFloatMatrix removeRow(int row) throws Exception
	{
		GenericMatrix<GenericFloat> genericResult = this.getMatrix().removeRow(row);
		return new GenericFloatMatrixAdapter(genericResult);
	}

	@Override
	public float determinant() throws Exception
	{
		GenericFloat genericResult = this.getMatrix().determinant();
		return genericResult.getValue();
	}

	@Override
	public float entrywiseNorm(float p) throws Exception
	{
		return this.matrix.entrywiseNorm(FLOAT_FACTORY.create(p)).getValue();
	}

	@Override
	public float[] rowMajor()
	{
		List<GenericFloat> genericResult = this.getMatrix().rowMajor();
		float[] result = new float[genericResult.size()];
		
		for (int i = 0; i < genericResult.size(); i++)
		{
			result[i] = genericResult.get(i).getValue();
		}	
		
		return result;
	}

	@Override
	public float[] columnMajor()
	{
		List<GenericFloat> genericResult = this.getMatrix().columnMajor();
		float[] result = new float[genericResult.size()];
		
		for (int i = 0; i < genericResult.size(); i++)
		{
			result[i] = genericResult.get(i).getValue();
		}	
		
		return result;
	}

	@Override
	public float trace() throws Exception
	{
		GenericFloat genericResult = this.getMatrix().trace();
		return genericResult.getValue();
	}

	@Override
	public IFloatMatrix projectToVector(IFloatMatrix v) throws Exception
	{
		GenericFloatMatrixAdapter adapterV = new GenericFloatMatrixAdapter(v);
		GenericMatrix<GenericFloat> genericResult = this.getMatrix().projectToVector(adapterV.getMatrix());
		return new GenericFloatMatrixAdapter(genericResult);
	}

	@Override
	public IFloatMatrix clone()
	{
		try
		{
			GenericMatrix<GenericFloat> genericResult = this.getMatrix().clone();
			return new GenericFloatMatrixAdapter(genericResult);
		}
		catch (Exception e)
		{
			throw new RuntimeException(ERROR_MSG, e);
		}		
	}
}
