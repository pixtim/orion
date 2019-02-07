package orion.sdk.math.generic;

public class GenericBigDecimalFactory implements IGenericPrimitiveFactory<GenericBigDecimal>
{

	@Override
	public GenericBigDecimal create() throws Exception
	{
		return new GenericBigDecimal(0);
	}

	@Override
	public GenericBigDecimal create(int value) throws Exception
	{
		return new GenericBigDecimal(value);
	}

	@Override
	public GenericBigDecimal create(long value) throws Exception
	{
		return new GenericBigDecimal(value);
	}

	@Override
	public GenericBigDecimal create(float value) throws Exception
	{
		return new GenericBigDecimal(value);
	}

	@Override
	public GenericBigDecimal create(double value) throws Exception
	{
		return new GenericBigDecimal(value);
	}

}
