
package orion.sdk.math;

/**
 * This class provides a Quaternion implentation. Quaternions are used to represent and apply rotations about arbitrary axis.
 * 
 * @author Tim
 * @since 1.0.00
 */
public class FloatQuaternion
{
	/**
	 * The real scalar part of this quaternion.
	 */
	protected float w = 0;

	/**
	 * The imaginary vector part of this quaternion.
	 */
	protected IFloatMatrix v = FloatMatrix.vector(0f, 0f, 0f);

	/**
	 * Creates a new quaternion with zero real and imaginary components.
	 */
	public FloatQuaternion()
	{
		
	}

	/**
	 * Creates a new quaternion with the given components.
	 */
	public FloatQuaternion(float x, float y, float z, float w)
	{
		this.w = w;
		this.v = FloatMatrix.vector(x, y, z);
	}

	/**
	 * Creates a new quaternion with the given components.
	 */
	public FloatQuaternion(float w, IFloatMatrix v)
	{
		this.w = w;
		this.v = FloatMatrix.vector(v.get(0), v.get(1), v.get(2));
	}

	public float getW()
	{
		return w;
	}
	
	public void setW(float w)
	{
		this.w = w;
	}
	
	public IFloatMatrix getV()
	{
		return v;
	}
	
	public void setV(IFloatMatrix v)
	{
		this.v = v;
	}

	/**
	 * Gets the conjugate of this quaternion.
	 */
	public FloatQuaternion conjugate()
	{
		return new FloatQuaternion(w, v.scalarProduct(-1));
	}

	/**
	 * Gets the inverse of this quaternion.
	 */
	public FloatQuaternion inverse()
	{
		float denominator = w * w + v.get(0) * v.get(0) + v.get(1) * v.get(1) + v.get(2) * v.get(2);
		return scalarProduct(conjugate(), 1f / denominator);
	}

	/**
	 * Gets the norm of this quaternion.
	 */
	public float norm()
	{
		return (float) Math.sqrt(w * w + v.get(0) * v.get(0) + v.get(1) * v.get(1) + v.get(2) * v.get(2));
	}

	/**
	 * Returns the quaternion multiplicative identidy.
	 */
	public static FloatQuaternion identity()
	{
		return new FloatQuaternion(0, 0, 0, 1);
	}

	/**
	 * Returns a quaternion representing a rotation about an arbitrary axis.
	 */
	public static FloatQuaternion rotation(IFloatMatrix axis, float angle)
	{
		IFloatMatrix u = axis;
		float s = (float) Math.cos(angle / 2);
		IFloatMatrix v = u.scalarProduct((float) Math.sin(angle / 2));
		FloatQuaternion q = new FloatQuaternion(s, v);
		return FloatQuaternion.scalarProduct(q, 1f / q.norm());
	}

	/**
	 * Returns a quaternion representing a rotation about an arbitrary axis.
	 */
	public static FloatQuaternion rotation(float axisX, float axisY, float axisZ, float angle)
	{
		return rotation(FloatMatrix.vector(axisX, axisY, axisZ), angle);
	}
	/**
	 * Returns a quaternion from a general 3x3 or 4x4 rotation matrix.
	 */
	public static FloatQuaternion fromMatrix(IFloatMatrix M)
	{
		/*
		 * From http://www.gamedev.net/community/forums/topic.asp?topicId=502905
		 */
		float trace = 1.0f + M.get(0, 0) + M.get(1, 1) + M.get(2, 2);
		/*
		 * Note a large threshold was chosen to avoid truncation error near the poles
		 */
		if (trace > 0.1f)
		{
			float s = (float)(Math.sqrt(trace) * 2f);
			return new FloatQuaternion(
				(M.get(2, 1) - M.get(1, 2)) / s,
				(M.get(0, 2) - M.get(2, 0)) / s,
				(M.get(1, 0) - M.get(0, 1)) / s,
				s / 4f
			);
		}
		else if (M.get(0, 0) > M.get(1, 1) && M.get(0, 0) > M.get(2, 2))
		{
			float s = (float)(Math.sqrt(1.0f + M.get(0, 0) - M.get(1, 1) - M.get(2, 2)) * 2f);
			return new FloatQuaternion(
				s / 4f,
				(M.get(1, 0) + M.get(0, 1)) / s,
				(M.get(0, 2) + M.get(2, 0)) / s,
				(M.get(2, 1) - M.get(1, 2)) / s
			);
		}
		else if (M.get(1, 1) > M.get(2, 2))
		{
			float s = (float)(Math.sqrt(1.0f + M.get(1, 1) - M.get(0, 0) - M.get(2, 2)) * 2f);
			return new FloatQuaternion(
				(M.get(1, 0) + M.get(0, 1)) / s,
				s / 4f,
				(M.get(2, 1) + M.get(1, 2)) / s,
				(M.get(0, 2) - M.get(2, 0)) / s
			);
		}
		else
		{
			float s = (float)(Math.sqrt(1.0f + M.get(2, 2) - M.get(0, 0) - M.get(1, 1)) * 2f);
			return new FloatQuaternion(
				(M.get(0, 2) + M.get(2, 0)) / s,
				(M.get(2, 1) + M.get(1, 2)) / s,
				s / 4f,
				(M.get(1, 0) - M.get(0, 1)) / s
			);
		}
	}

	/**
	 * Returns a rotation quaternion equivalent to an Euler rotation transformation.
	 * @return
	 */
	public static FloatQuaternion fromEuler(float xRoll, float yRoll, float zRoll) throws Exception
	{
		return
			FloatQuaternion.product(FloatQuaternion.product(
				rotation(FloatMatrix.vector(1, 0, 0), xRoll), 
				rotation(FloatMatrix.vector(0, 1, 0), yRoll)), 
				rotation(FloatMatrix.vector(0, 0, 1), zRoll)); 
	}
	
	/**
	 * Returns the product of two quaternions.
	 */
	public static FloatQuaternion product(FloatQuaternion a, FloatQuaternion b) throws Exception
	{
		float w = a.w * b.w - a.v.dot(b.v);
		
		IFloatMatrix t = b.v.scalarProduct(a.w).add(a.v.scalarProduct(b.w));
		IFloatMatrix v = t.add(a.v.cross(b.v));
		
		return new FloatQuaternion(w, v);
	}

	/**
	 * Returns the product of a quaternion and a 3x1 or 4x1 vector.
	 */
	public static IFloatMatrix matrixProduct(FloatQuaternion q, IFloatMatrix P) throws Exception
	{
		FloatQuaternion p = new FloatQuaternion(0, FloatMatrix.vector(P.get(0), P.get(1), P.get(2)));
		FloatQuaternion qInv = q.inverse();
		FloatQuaternion y = FloatQuaternion.product(FloatQuaternion.product(q, p), qInv);
		return FloatMatrix.vector(y.v.get(0), y.v.get(1), y.v.get(2), 0);
	}

	/**
	 * Performs scalar multiplication of a quaternion.
	 */
	public static FloatQuaternion scalarProduct(FloatQuaternion x, float c)
	{
		float w = x.w * c;
		IFloatMatrix v = x.v.scalarProduct(c);
		return new FloatQuaternion(w, v);
	}

	/**
	 * Performs quaternion addition.
	 */
	public static FloatQuaternion add(FloatQuaternion a, FloatQuaternion b) throws Exception
	{
		return new FloatQuaternion(a.w + b.w, a.v.add(b.v));
	}

	/**
	 * Performs quaternion subtraction.
	 */
	public static FloatQuaternion subtract(FloatQuaternion a, FloatQuaternion b) throws Exception
	{
		return new FloatQuaternion(a.w - b.w, a.v.subtract(b.v));
	}

	/**
	 * Returns the linear interpolation between two quaternions.
	 */
	public static FloatQuaternion interpolate(FloatQuaternion a, FloatQuaternion b, float alpha) throws Exception
	{
		return add(scalarProduct(a, 1 - alpha), scalarProduct(b, alpha));
	}

	/**
	 * Gets the angle of rotation for this quaternion.
	 */
	public float getAngle()
	{
		return (float)(2 * Math.acos(w));
	}

	/**
	 * Set the angle of rotation for this quaternion.
	 */
	public void setAngle(float angle) throws Exception
	{
		w = (float)Math.cos(angle / 2);
		v = getAxis().scalarProduct((float)Math.sin(angle / 2));
	}

	/**
	 * Gets the axis of rotation.
	 */
	public IFloatMatrix getAxis() throws Exception
	{
		float denominator = (float)Math.sin(getAngle() / 2);
		if (denominator == 0)
			throw new Exception("Multiple of 360 degree rotation, cannot find axis of rotation");
		else
			return v.scalarProduct(1f / denominator);
	}

	/**
	 * Sets the axis of rotation. The axis should be non-zero and the sine of the angle of rotation cannot be zero.
	 * @param axis
	 */
	public void setAxis(IFloatMatrix axis) throws Exception
	{
		float norm = axis.norm();
		if (norm > 0)
			v = axis.scalarProduct(1f / norm * (float)Math.sin(getAngle() / 2));
		else
			throw new Exception("Norm of axis is zero");
	}
	
	/**
	 * Gets a 4x4 matrix representing this quaternion. It is assumed that this quaternion is normalized.
	 */
	public IFloatMatrix getMatrix()
	{
			IFloatMatrix M = FloatMatrix.identity(4);
			float x = v.get(0), y = v.get(1), z = v.get(2);

			M.set(0, 0, 1f - 2f * y * y - 2f * z * z);
			M.set(0, 1, 2 * x * y - 2 * w * z);
			M.set(0, 2, 2f * x * z + 2f * w * y);

			M.set(1, 0, 2 * x * y + 2 * w * z);
			M.set(1, 1, 1f - 2f * x * x - 2f * z * z);
			M.set(1, 2, 2f * y * z - 2f * w * x);

			M.set(2, 0, 2 * x * z - 2 * w * y);
			M.set(2, 1, 2f * y * z + 2f * w * x);
			M.set(2, 2, 1f - 2f * x * x - 2f * y * y);

			return M;
	}

	@Override
	public String toString()
	{
		try
		{
			return "w: " + w + ", V:" + v.toString() + ", Axis: " + getAxis().toString() + ", Angle: " + getAngle() / Math.PI * 180;
		}
		catch (Exception e)
		{
			/*
			 * Ignore
			 */
		}
		return "Invalid quaternion";
	}

	/**
	 * Normalizes this quaternion
	 */
	public void normalize()
	{
		FloatQuaternion result = scalarProduct(this, 1 / norm());;
		w = result.w;
		v = result.v;
	}

	@Override
	public FloatQuaternion clone()
	{
		return new FloatQuaternion(w, v.clone());
	}
}

