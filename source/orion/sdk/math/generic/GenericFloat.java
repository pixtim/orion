package orion.sdk.math.generic;

@SuppressWarnings("rawtypes")
public class GenericFloat implements IGenericPrimitive
{
	private float value = 0;
	
	public GenericFloat(float value)
	{
		this.value = value;
	}
	
	@Override
	public IGenericPrimitive add(IGenericPrimitive other)
	{
		GenericFloat otherFloat = (GenericFloat) other;
		
		return new GenericFloat(this.getValue() + otherFloat.getValue());
	}

	@Override
	public IGenericPrimitive subtract(IGenericPrimitive other)
	{
		GenericFloat otherFloat = (GenericFloat) other;
		
		return new GenericFloat(this.getValue() - otherFloat.getValue());
	}

	@Override
	public IGenericPrimitive multiply(IGenericPrimitive other)
	{
		GenericFloat otherFloat = (GenericFloat) other;
		
		return new GenericFloat(this.getValue() * otherFloat.getValue());
	}

	@Override
	public IGenericPrimitive devide(IGenericPrimitive other)
	{
		GenericFloat otherFloat = (GenericFloat) other;
		
		return new GenericFloat(this.getValue() / otherFloat.getValue());
	}

	@Override
	public IGenericPrimitive power(IGenericPrimitive power) throws Exception
	{
		GenericFloat otherFloat = (GenericFloat) power;
		
		return new GenericFloat((float) Math.pow(this.getValue(), otherFloat.getValue()));
	}
	
	@Override
	public IGenericPrimitive absolute() throws Exception
	{
		return new GenericFloat((float) Math.abs(this.getValue()));
	}

	@Override
	public int compare(IGenericPrimitive other) throws Exception
	{
		GenericFloat otherFloat = (GenericFloat) other;
		
		return Float.compare(this.getValue(), otherFloat.getValue());
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof GenericFloat)
		{
			GenericFloat other = (GenericFloat) obj;
			return this.getValue() == other.getValue();
		}
		else
		{
			return false;
		}
	}

	public float getValue()
	{
		return value;
	}
	
	public void setValue(float value)
	{
		this.value = value;
	}
	
	@Override
	public String toString()
	{
		return Float.toString(this.getValue());
	}
}
