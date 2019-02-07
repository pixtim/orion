package orion.sdk.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Map;
import java.util.Set;

public class Transform
{
	
	public static int bytesToInt(byte... data)
	{
		ByteBuffer buffer = ByteBuffer.wrap(data);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		return buffer.getInt();
	}

	public static byte[] intToBytes(int value)
	{
		ByteBuffer buffer = ByteBuffer.allocateDirect(4);
		buffer.putInt(value);
		return buffer.array();
	}

	public static long bytesToLong(byte... data)
	{
		ByteBuffer buffer = ByteBuffer.wrap(data);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		return buffer.getLong();
	}

	public static byte[] longToBytes(long value)
	{
		ByteBuffer buffer = ByteBuffer.allocateDirect(8);
		buffer.putLong(value);
		return buffer.array();
	}

	public static float bytesToFloat(byte... data)
	{
		ByteBuffer buffer = ByteBuffer.wrap(data);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		return buffer.getFloat();
	}

	public static byte[] floatToBytes(float value)
	{
		ByteBuffer buffer = ByteBuffer.allocateDirect(4);
		buffer.putFloat(value);
		return buffer.array();
	}

	public static byte[] floatArrayToBytes(float... values)
	{
		ByteBuffer buffer = ByteBuffer.allocateDirect(4 + values.length * 4);
		buffer.position(0);
		buffer.putInt(values.length);
		for (int i = 0; i < values.length; i++)
		{
			buffer.position(i + 1);
			buffer.putFloat(values[i]);
		}
		return buffer.array();
	}

	public static float[] bytesToFloatArray(byte... data)
	{
		ByteBuffer buffer = ByteBuffer.wrap(data);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		float[] values = new float[buffer.getInt(0)];				
		for (int i = 0; i < values.length; i++)
		{
			values[i] = buffer.getFloat(4 + i * 4);
		}
		return values;
	}

	public static byte[] longArrayToBytes(long... values)
	{
		ByteBuffer buffer = ByteBuffer.allocateDirect(4 + values.length * 8);
		buffer.position(0);
		buffer.putInt(values.length);
		for (int i = 0; i < values.length; i++)
		{
			buffer.position(i + 1);
			buffer.putLong(values[i]);
		}
		return buffer.array();
	}

	public static long[] bytesToLongArray(byte... data)
	{
		ByteBuffer buffer = ByteBuffer.wrap(data);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		long[] values = new long[buffer.getInt(0)];				
		for (int i = 0; i < values.length; i++)
		{
			values[i] = buffer.getLong(4 + i * 8);
		}
		return values;
	}	
	public static String bytesToString(byte... data)
	{
		return new String(data);
	}

	public static byte[] stringToBytes(String value)
	{
		return value.getBytes();
	}
	
	public static FloatBuffer floatBuffer(float[] values)
	{
		if (values != null)
		{
			ByteBuffer buffer = ByteBuffer.allocateDirect(values.length * 4); 
			buffer.order(ByteOrder.nativeOrder());
			FloatBuffer floatBuffer = buffer.asFloatBuffer();
			floatBuffer.put(values);
			floatBuffer.position(0);
			return floatBuffer;
		}
		else
		{
			return null;
		}
	}
	
	
	public static ByteBuffer byteBuffer(byte[] values)
	{
		ByteBuffer buffer = ByteBuffer.allocateDirect(values.length);
		buffer.order(ByteOrder.nativeOrder());
		buffer.put(values);
		buffer.position(0);
		return buffer;
	}
	
	public static String getString(Map map)
	{
		String result = "{";
		Set keys = map.keySet();
		int count = keys.size();
		int i = 0;
		
		for (Object key : keys)
		{
			result = result + "'" + key + "' = '" + map.get(key) + "'";
			if (i < count - 1)
			{
				result = result + ", ";
			}
			i++;
		}
		return result + "}";
	}
	
	public static String pad(String string, int length, char character, boolean padAtLeftSide)
	{
		StringBuilder builder = new StringBuilder();
		builder.append(string);
		if (string.length() < length)
		{
			while (builder.length() < length)
			{
				if (padAtLeftSide)
				{
					builder.insert(0, character);
				}
				else
				{
					builder.append(character);
				}
			}
			return builder.toString();
		}
		else
		{
			return string.substring(length);
		}
	}
	
	public static String getPadding(int length, char character)
	{
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < length; i++)
		{
			builder.append(character);
		}
		return builder.toString();
	}
}
