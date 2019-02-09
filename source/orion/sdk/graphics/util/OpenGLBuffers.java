package orion.sdk.graphics.util;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLException;

import orion.sdk.graphics.shading.texturing.Texture;
import orion.sdk.graphics.shading.texturing.Texture.EDataType;
import orion.sdk.graphics.shading.texturing.Texture.EFormat;


public class OpenGLBuffers
{

	private OpenGLBuffers() {}
	
	private static int readComponent(ByteBuffer buffer, Texture.EDataType dataType) throws GLException
	{
		int value = 0;
		
		switch (dataType)
		{
			case BYTE:
				byte byteValue = buffer.get();
				value = (int) (byteValue / 127f * 255);
				break;
			case FLOAT:
				float floatValue = buffer.getFloat();
				value = (int) (floatValue);
				break;
			default:
				throw new GLException("Unsupported image data type");
		}
		
		value = Math.max(value, 0);
		value = Math.min(value, 255);
		
		return value;
	}
	
	private static Color readColor(ByteBuffer buffer, int x, int y, Texture.EFormat format, Texture.EDataType dataType)
	{
		int red = 0;
		int green = 0;
		int blue = 0;
		float alpha = 0;
		
		switch (format)
		{
			case RGBA:
				red = readComponent(buffer, dataType);
				green = readComponent(buffer, dataType);
				blue = readComponent(buffer, dataType);
				alpha = readComponent(buffer, dataType) / 255f;				
				break;
			case A:
			case DEPTH:
				int alphaValue = 255 - readComponent(buffer, dataType); 
				red = alphaValue;
				green = alphaValue;
				blue = alphaValue;
				alpha = 1f;				
				break;
			default:
				throw new GLException("Unsupported image format");
		}
		
		int grid = 200;
		int step = 25;
		if (y % step == 0)
		{
			if (x % step == 0)
			{
				grid = 255;
			}
		}
		else
		{
			if (x % step != 0)
			{
				grid = 255;
			}
		}
		
		red = (int) (alpha * red + (1f - alpha) * grid);
		green = (int) (alpha * green + (1f - alpha) * grid);
		blue = (int) (alpha * blue + (1f - alpha) * grid);
		
		return new Color(red, green, blue, 255);
	}
	
	private static BufferedImage rasterize(ByteBuffer buffer, int width, int height, Texture.EFormat format, Texture.EDataType dataType) throws GLException
	{
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		
		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				Color color = readColor(buffer, x, y, format, dataType);
				
				image.setRGB(x, (height - 1) - y, color.getRGB());
			}
		}
		
		return image;		
	}
	
	private static int getComponentCount(Texture.EFormat format)
	{
		if (format == EFormat.RGBA)
		{
			return 4;
		}
		else if (format == EFormat.A)
		{
			return 1;
		}
		else if (format == EFormat.DEPTH)
		{
			return 1;
		}
		
		throw new GLException("Unsupported image format");
	}	
	
	private static int getComponentSize(Texture.EDataType dataType)
	{
		if (dataType == EDataType.FLOAT)
		{
			return 4;
		}
		else if (dataType == EDataType.BYTE)
		{
			return 1;
		}
		
		throw new GLException("Unsupported image data type");
	}
	
	private static ByteBuffer allocate(int width, int height, Texture.EFormat format, Texture.EDataType dataType)
	{
		int componentCount = getComponentCount(format);
		int componentSize = getComponentSize(dataType);						
		int stride = componentCount * componentSize;
		int capacity = width * height * stride;		
		ByteBuffer buffer = ByteBuffer.allocate(capacity);
		return buffer;
	}
	
	public static BufferedImage getColorBufferImage(OpenGLContext c)
	{
		int
			left = (int) c.viewport[0],
			top = (int) c.viewport[1],
			width = (int) c.viewport[2],
			height = (int) c.viewport[3];
	
		ByteBuffer buffer = allocate(width, height, EFormat.RGBA, EDataType.BYTE);
		
		c.gl().glPixelStorei(GL2.GL_PACK_ALIGNMENT, 1);
		c.gl().glReadPixels(
			left,
			top,
			width,
			height,			
			GL2.GL_RGBA,
			GL2.GL_BYTE,
			buffer);
		OpenGLManager.getInstance().checkError(c);
		
		BufferedImage image = rasterize(buffer, width, height, Texture.EFormat.RGBA, Texture.EDataType.BYTE);
		
		return image;
	}
	
	public static BufferedImage getDepthBufferImage(OpenGLContext c)
	{
		int
			left = (int) c.viewport[0],
			top = (int) c.viewport[1],
			width = (int) c.viewport[2],
			height = (int) c.viewport[3];
	
		ByteBuffer buffer = allocate(width, height, EFormat.A, EDataType.BYTE);
		
		c.gl().glPixelStorei(GL2.GL_PACK_ALIGNMENT, 1);
		c.gl().glReadPixels(
			left,
			top,
			width,
			height,			
			GL2.GL_DEPTH_COMPONENT,
			GL2.GL_BYTE,
			buffer);
		OpenGLManager.getInstance().checkError(c);
		
		BufferedImage image = rasterize(buffer, width, height, Texture.EFormat.A, Texture.EDataType.BYTE);
		
		return image;
	}
	
	public static BufferedImage getTextureImage(Texture texture, OpenGLContext c)
	{
		/*
		 * TODO BUG: OpenGL doesn't populate the buffer as expected when using
		 * glGetTexImage. Works for glReadPixels.
		 */
		OpenGLStack.push(Texture.class, texture, c, texture.getLayer());
		
		int width = texture.getWidth();
		int height = texture.getHeight();
		
		EFormat format = texture.getFormat();
		EDataType dataType = texture.getDataType();
		
		ByteBuffer buffer = allocate(width, height, format, dataType);

		c.gl().glPixelStorei(GL2.GL_PACK_ALIGNMENT, 1);
		
		int glTarget = texture.getGlTarget();
		int glFormat = texture.getGlFormat();
		int glDataType = texture.getGlDataType();
		
		c.gl().glGetTexImage(glTarget, 0, glFormat, glDataType, buffer);
		OpenGLManager.getInstance().checkError(c);

		BufferedImage image = rasterize(buffer, width, height, format, dataType);

		OpenGLStack.pop(Texture.class, texture, c, texture.getLayer());
		
		return image;
	}
}
