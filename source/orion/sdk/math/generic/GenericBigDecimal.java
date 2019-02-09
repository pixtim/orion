package orion.sdk.math.generic;

import java.math.BigDecimal;

public class GenericBigDecimal implements IGenericPrimitive
{
	private BigDecimal value = new BigDecimal(0);
	
	public GenericBigDecimal(BigDecimal value)
	{
		this.value = value;
	}
	
	public GenericBigDecimal(double value)
	{
		this.value = new BigDecimal(value);
	}
	
	public BigDecimal getValue()
	{
		return value;
	}
	
	public void setValue(BigDecimal value)
	{
		this.value = value;
	}
	
	@Override
	public IGenericPrimitive add(IGenericPrimitive other) throws Exception
	{
		GenericBigDecimal otherBigDecimal = (GenericBigDecimal) other;
		
		return new GenericBigDecimal(this.getValue().add(otherBigDecimal.getValue()));		
	}

	@Override
	public IGenericPrimitive subtract(IGenericPrimitive other) throws Exception
	{
		GenericBigDecimal otherBigDecimal = (GenericBigDecimal) other;
		
		return new GenericBigDecimal(this.getValue().subtract(otherBigDecimal.getValue()));		
	}

	@Override
	public IGenericPrimitive multiply(IGenericPrimitive other) throws Exception
	{
		GenericBigDecimal otherBigDecimal = (GenericBigDecimal) other;
		
		return new GenericBigDecimal(this.getValue().multiply(otherBigDecimal.getValue()));		
	}

	@Override
	public IGenericPrimitive devide(IGenericPrimitive other) throws Exception
	{
		GenericBigDecimal otherBigDecimal = (GenericBigDecimal) other;
		
		return new GenericBigDecimal(this.getValue().divide(otherBigDecimal.getValue(), BigDecimal.ROUND_HALF_EVEN));		
	}

	@Override
	public IGenericPrimitive power(IGenericPrimitive power) throws Exception
	{
		/*
		 * TODO: Implement a true big decimal power operator. This one won't scale.
		 */
		GenericBigDecimal powerBigDecimal = (GenericBigDecimal) power;
		
		double result = Math.pow(this.getValue().doubleValue(), powerBigDecimal.getValue().doubleValue());
		
		return new GenericBigDecimal(new BigDecimal(result));
	}

	@Override
	public IGenericPrimitive absolute() throws Exception
	{
		return new GenericBigDecimal(this.getValue().abs());
	}

	@Override
	public int compare(IGenericPrimitive other) throws Exception
	{
		GenericBigDecimal otherBigDecimal = (GenericBigDecimal) other;
		
		return this.getValue().compareTo(otherBigDecimal.getValue());
	}	
}
