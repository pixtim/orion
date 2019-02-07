package orion.sdk.math.generic;

public interface IGenericPrimitive<T extends IGenericPrimitive>
{
	T add(T other) throws Exception;
	T subtract(T other) throws Exception;
	T multiply(T other) throws Exception;
	T devide(T other) throws Exception;
	T power(T power) throws Exception;
	T absolute() throws Exception;
	int compare(T other) throws Exception;
}
