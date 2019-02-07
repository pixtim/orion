package orion.sdk.math.generic;

public interface IGenericPrimitiveFactory<T extends IGenericPrimitive>
{
	T create() throws Exception;
	T create(int value) throws Exception;
	T create(long value) throws Exception;
	T create(float value) throws Exception;
	T create(double value) throws Exception;
}
