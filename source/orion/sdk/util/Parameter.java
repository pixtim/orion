package orion.sdk.util;

public class Parameter
{
	public static void nullcheck(Object parameter, String parameterName) throws Exception
	{
		if (parameter == null)
		{
			throw new NullPointerException("'" + parameterName + "' cannot be null");
		}
	}
}
