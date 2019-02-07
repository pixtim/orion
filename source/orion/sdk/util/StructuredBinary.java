package orion.sdk.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * This class structures data in a binary tree in memory. The data is packed and
 * unpacked using length indicators for each node. The maximum length of the
 * tree is {@code Integer.MAX_VALUE} and the maximum length of the byte array in
 * each node is {@code Integer.MAX_VALUE}
 * 
 * @author Tim
 * 
 */
public class StructuredBinary
{
	public byte[] data = new byte[0];
	public int type = DataType.BINARY;
	public List<StructuredBinary> children = new ArrayList<StructuredBinary>();
	
	public void add(int type, byte... data)
	{
		StructuredBinary child = new StructuredBinary();
		child.data = data;
		child.type = type;
		children.add(child);
	}
	
	public void add(int value)
	{
		add(DataType.INTEGER, Transform.intToBytes(value));
	}
	
	public void add(float value)
	{
		add(DataType.FLOAT, Transform.floatToBytes(value));
	}
	
	public void add(long value)
	{
		add(DataType.LONG, Transform.longToBytes(value));
	}	
	
	public void add(String value)
	{
		add(DataType.STRING, Transform.stringToBytes(value));
	}	
	
	public void add(float... values)
	{
		add(DataType.FLOAT_ARRAY, Transform.floatArrayToBytes(values));
	}
	
	public void add(long... values)
	{
		add(DataType.LONG_ARRAY, Transform.longArrayToBytes(values));
	}
	
	public int count()
	{
		return children.size();
	}
	
	public byte[] getBytes(int index)
	{
		return children.get(index).data;
	}
	
	public int getInt(int index)
	{
		return Transform.bytesToInt(getBytes(index));
	}
	
	public long getLong(int index)
	{
		return Transform.bytesToLong(getBytes(index));
	}
	
	public float getFloat(int index)
	{
		return Transform.bytesToFloat(getBytes(index));
	}
	
	public String getString(int index)
	{
		return Transform.bytesToString(getBytes(index));
	}
	
	public float[] getFloatArray(int index)
	{
		return Transform.bytesToFloatArray(getBytes(index));
	}	
	
	public long[] getLongArray(int index)
	{
		return Transform.bytesToLongArray(getBytes(index));
	}	
	
	public static StructuredBinary read(InputStream input) throws Exception
	{		
		StructuredBinary node = new StructuredBinary();
		Metadata metadata = Metadata.read(input);		
		boolean isLeaf = metadata.isLeaf();
		node.type = metadata.getType();
		if (isLeaf)
		{
			node.data = new byte[metadata.length];
			input.read(node.data, 0, node.data.length);
		}
		else
		{
			int dataRead = 0;
			while (dataRead < metadata.length)
			{
				StructuredBinary child = read(input);
				dataRead = dataRead + Metadata.META_LENGTH + child.data.length;
				node.children.add(child);
			}
		}
		return node;
	}
	
	public boolean isLeaf()
	{
		return children.isEmpty();
	}


	/**
	 * Returns the number of bytes required to write the specified node.
	 */
	protected static int getLength(StructuredBinary node)
	{
		if (node.isLeaf())
		{
			return Metadata.META_LENGTH + node.data.length;
		}
		else
		{
			int childrenLength = 0;
			for (StructuredBinary nextChild : node.children)
			{
				childrenLength = childrenLength + getLength(nextChild);
			}
			return Metadata.META_LENGTH + childrenLength;
		}
	}	
	
	public void write(OutputStream output) throws IOException
	{
		/*
		 * Write the metadata for this node
		 */
		Metadata metadata = new Metadata();
		metadata.setLeaf(isLeaf());
		metadata.setType(type);
		metadata.length = getLength(this) - Metadata.META_LENGTH;
		metadata.write(output);
		
		/*
		 * Write the leaf data or recur if this node is not a leaf.
		 */
		if (isLeaf())
		{
			output.write(data);
		}
		else
		{
			for (StructuredBinary nextChild : children)
			{
				nextChild.write(output);
			}
		}
	}
	
	public static class DataType
	{
		public static final int BINARY		= 0;
		public static final int INTEGER		= 1;
		public static final int FLOAT			= 2;
		public static final int LONG			= 3;
		public static final int STRING		= 4;		
		public static final int FLOAT_ARRAY	= 5;
		public static final int LONG_ARRAY	= 6;
	}
	
	/**
	 * Represents a metadata binary block. It contains a 1-byte bitmap as well as a 4 byte lengh
	 * indicator.
	 * 
	 * @author Tim
	 * @since 1.0.00
	 */
	public static class Metadata
	{
		private static final byte BIT_LEAF			= 0b00000001;
		private static final byte BIT_TYPE_BIT_1	= 0b00000010;
		private static final byte BIT_TYPE_BIT_2	= 0b00000100;
		private static final byte BIT_TYPE_BIT_3	= 0b00001000;
		private static final int META_LENGTH		= 1 + 4;
		
		public byte bitmap = 0b00000000;
		public int length = 0;
		
		private boolean getBit(byte bit)
		{
			return (bitmap & bit) != 0b00000000;
		}
		
		private void setBit(byte bit, boolean value)
		{
			byte mask = (byte) (bit ^ 0b00000000);
			byte maskedBitmap = (byte) (mask & bitmap);
			byte select = (byte) ((value ? 0b11111111 : 0b00000000) & bit);
			bitmap = (byte) (maskedBitmap | select);
		}
		
		public void setLeaf(boolean leaf)
		{
			setBit(Metadata.BIT_LEAF, leaf);
		}
		
		public boolean isLeaf()
		{
			 return getBit(Metadata.BIT_LEAF);
		}
		
		public int getType()
		{
			boolean
				type1 = getBit(Metadata.BIT_TYPE_BIT_1),
				type2 = getBit(Metadata.BIT_TYPE_BIT_2),
				type3 = getBit(Metadata.BIT_TYPE_BIT_3);
			return
					(type1 ? 1 : 0) +
					(type2 ? 2 : 0) +
					(type3 ? 4 : 0);
		}
		
		public void setType(int type)
		{
			int remainder = type;

			if (remainder / 4 > 0)
			{
				remainder = remainder - 4;
				setBit(Metadata.BIT_TYPE_BIT_3, true);
			}
			else
			{
				setBit(Metadata.BIT_TYPE_BIT_3, false);
			}
			

			if (remainder / 2 > 0)
			{
				remainder = remainder - 2;
				setBit(Metadata.BIT_TYPE_BIT_2, true);
			}
			else
			{
				setBit(Metadata.BIT_TYPE_BIT_2, false);
			}

			if (remainder > 0)
			{
				setBit(Metadata.BIT_TYPE_BIT_1, true);
			}
			else
			{
				setBit(Metadata.BIT_TYPE_BIT_1, false);
			}
		}
		
		/**
		 * Writes this metadata block to an output stream.
		 */
		public void write(OutputStream output) throws IOException
		{
			output.write(bitmap);
			output.write(Transform.intToBytes(length));
		}
		
		/**
		 * Reads the next metadata block and returns {@code null} when stream ends.
		 */
		public static Metadata read(InputStream input) throws IOException
		{
			Metadata metadata = new Metadata();
			int nextByte = input.read();
			if (nextByte != -1){
				metadata.bitmap = (byte) nextByte;
				byte[] lengthData = new byte[4];
				input.read(lengthData, 0, 4);
				metadata.length = Transform.bytesToInt(lengthData);
				return metadata;
			}
			else
			{
				return null;
			}
		}
	}
}
