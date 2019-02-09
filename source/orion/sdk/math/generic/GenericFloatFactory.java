package orion.sdk.math.generic;

public class GenericFloatFactory implements IGenericPrimitiveFactory<GenericFloat>
{
	@Override
	public GenericFloat create() throws Exception
	{
		return new GenericFloat(0);
	}

	@Override
	public GenericFloat create(int value) throws Exception
	{
		return new GenericFloat(value);
	}

	@Override
	public GenericFloat create(long value) throws Exception
	{
		return new GenericFloat(value);
	}

	@Override
	public GenericFloat create(float value) throws Exception
	{
		return new GenericFloat(value);
	}

	@Override
	public GenericFloat create(double value) throws Exception
	{
		return new GenericFloat((float) value);
	}

}
