package orion.sdk.math;


public class FloatTransformation
{
	private IFloatMatrix position = FloatMatrix.vector(0f, 0f, 0f, 1f);
	private IFloatMatrix scale = FloatMatrix.vector(1f, 1f, 1f, 0f);
	private FloatQuaternion rotation = FloatQuaternion.identity();

	public FloatTransformation()
	{
	}

	public FloatTransformation(FloatMatrix position, FloatMatrix scale, FloatQuaternion rotation)
	{
		this.position = position;
		this.scale = scale;
		this.rotation = rotation;
	}

	public IFloatMatrix getMatrix() throws Exception
	{
		IFloatMatrix matrix = FloatMatrix.identity(4);
		
		matrix = matrix.product(FloatMatrix.translate(getPosition()));
		matrix = matrix.product(getRotation().getMatrix());
		matrix = matrix.product(FloatMatrix.scale(getScale()));
		
		return matrix;
	}
	
	public IFloatMatrix getPosition()
	{
		return position;
	}

	public void setPosition(IFloatMatrix position)
	{
		this.position = position;
	}

	public IFloatMatrix getScale()
	{
		return scale;
	}

	public void setScale(IFloatMatrix scale)
	{
		this.scale = scale;
	}

	public FloatQuaternion getRotation()
	{
		return rotation;
	}

	public void setRotation(FloatQuaternion rotation)
	{
		this.rotation = rotation;
	}

}
