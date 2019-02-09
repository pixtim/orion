package orion.sdk.graphics.shading.texturing;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;

import javax.imageio.ImageIO;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLException;

import orion.sdk.data.cubes.ADataCube;
import orion.sdk.data.cubes.ByteCube;
import orion.sdk.data.cubes.FloatCube;
import orion.sdk.data.cubes.ObjectCube;
import orion.sdk.graphics.util.IStackable;
import orion.sdk.graphics.util.IUploadable;
import orion.sdk.graphics.util.OpenGLContext;
import orion.sdk.graphics.util.OpenGLManager;
import orion.sdk.graphics.util.OpenGLStack;
import orion.sdk.util.Transform;

/**
 * Provides a generic implementation for OpenGL textures. The following features are supported:
 * <ul>
 * <li>
 * Textures support linear and nearest filtering</li>
 * <li>
 * Textures can be 2D or 3D</li>
 * <li>
 * Textures can be stored in brick format and streamed to the host.</li>
 * <li>
 * Textures can be constructed from images.</li>
 * </ul>
 * 
 * @author Tim
 * 
 */
public class Texture implements IUploadable, IStackable
{
	public String name = "unknown";
	protected boolean ready = false;
	
	public ETarget target = ETarget.TEXTURE_2D;
	public EFormat format = EFormat.RGBA;
	public EDataType dataType = EDataType.FLOAT;
	protected ETextureType textureType = ETextureType.AMBIENT; 
	
	public EUploadType uploadType = EUploadType.ALLOCATE_AND_UPLOAD;	
	public boolean linear = true;
	
	protected int textureId = -1;
	
	protected int width = 0;
	protected int height = 0;
	protected int depth = 0;
	
	protected BufferedImage[] images = null;	
	protected ObjectCube<Brick> bricks = null;	
	
	protected TextureStack textureStack = null;
	
	public Texture(String name, InputStream inputStream) throws IOException
	{
		this(name, EFormat.RGBA, ETarget.TEXTURE_2D, ImageIO.read(inputStream));
	}

	public Texture(String name, EFormat format, ETarget target, BufferedImage... images) throws IOException
	{
		this(name, format, target, 1, 1, 1, images);
	}

	public Texture(
			String name,
			EFormat format, ETarget target, 
			int bricksX, int bricksY, int bricksZ,
			BufferedImage... images)
	{
		this.name = name;
		
		if (images.length > 0)
		{
			this.target = target;
			this.format = format;
			this.images = images;
			
			this.width = images[0].getWidth();
			this.height = images[0].getHeight();
			this.depth = images.length;
			
			this.dataType = EDataType.BYTE;
			
			this.bricks = new ObjectCube<Brick>(bricksX, bricksY, bricksZ);
			for (int x = 0; x < bricksX; x++)
			{
				for (int y = 0; y < bricksY; y++)
				{
					for (int z = 0; z < bricksZ; z++)
					{
						Brick brick = this.generateBrickFromImages(x, y, z, images); 
						this.bricks.setEntry(x, y, z, brick);
					}
				}
			}
		}
	}

	public Texture(
		String name, 
		int width, int height, int depth,
		int bricksX, int bricksY, int bricksZ,
		EFormat format, ETarget target)
	{
		this.name = name;
		
		this.width = width;
		this.height = height;
		this.depth = depth;
		
		this.target = target;
		this.format = format;
		this.images = null;
		
		this.bricks = new ObjectCube<Brick>(bricksX, bricksY, bricksZ);
	}
	
	public Texture(String name, EFormat format, ETarget target, ADataCube cube)
	{
		this(
			name,
			cube.getWidth(), cube.getHeight(), cube.getDepth(),
			1, 1, 1,
			format, target);
		
		Brick brick = new Brick(this, 0, 0, 0);
		brick.setData(cube);
		getBricks().setEntry(0, 0, 0, brick);
	}
	
	public void setTextureStack(TextureStack textureStack)
	{
		this.textureStack = textureStack;
	}
	
	public TextureStack getTextureStack()
	{
		return textureStack;
	}
	
	public ETextureType getTextureType()
	{
		return textureType;
	}
	
	public void setTextureType(ETextureType textureType)
	{
		this.textureType = textureType;
	}
	
	public int getLayer() throws GLException
	{
		if (textureStack != null)
		{
			return textureStack.getUnit(this);
		}
		
		return 0;
	}
	
	public int getWidth()
	{
		return width;
	}
	
	public int getHeight()
	{
		return height;
	}
	
	public int getDepth()
	{
		return depth;
	}
	
	public ObjectCube<Brick> getBricks()
	{
		return bricks;
	}
	
	public BufferedImage[] getImages()
	{
		return images;
	}
	
	protected Brick generateBrickFromImages(
		int brickX, int brickY, int brickZ,
		BufferedImage... images)
	{
		Brick brick = new Brick(this, brickX, brickY, brickZ);
		ByteCube byteCube = brick.constructAsByteCube(getStride());
		
		int
			width = brick.getWidth(),
			height = brick.getHeight(),
			depth = brick.getDepth(),
			left = brick.getLeft(),
			top = brick.getTop(),
			front = brick.getFront();
		
		for (int z = front + depth - 1; z >= front; z--)
		{
			BufferedImage image = images[z];  
			for (int y = top; y < top + height; y++)
			{
				for (int x = left; x < left + width; x++)
				{
					Color pixel = new Color(image.getRGB(x, y), false);
					putTextile(
							byteCube,
						this.format,
						x - left, y - top, z - front,
						(byte) pixel.getRed(),
						(byte) pixel.getGreen(),
						(byte) pixel.getBlue(),
						(byte) pixel.getAlpha());
				}
			}
		}		
		
		return brick;
	}
	
	public BufferedImage getImage()
	{
		return this.getImage(0);
	}
	
	public BufferedImage getImage(int index)
	{
		return this.images[index];
	}
	
	public int getStride()
	{
		switch (this.format)
		{
			case RGBA:
				return 4;
			case RGB:
				return 3;
			case A:
				return 1;
			case DEPTH:
				return 1;
		}
		return -1;
	}
	
	protected static void putTextile(
		ByteCube data,
		EFormat format,
		int x, int y, int z,
		byte red, byte green, byte blue, byte alpha)
	{
		switch (format)
		{
			case RGBA:
				data.setEntry(x, y, z, 0, red);
				data.setEntry(x, y, z, 1, green);
				data.setEntry(x, y, z, 2, blue);
				data.setEntry(x, y, z, 3, alpha);
				break;
			case RGB:
				data.setEntry(x, y, z, 0, red);
				data.setEntry(x, y, z, 1, green);
				data.setEntry(x, y, z, 2, blue);
				break;
			case A:
				byte brightness = (byte) ((red + green + blue) / 3f);
				data.setEntry(x, y, z, 0, brightness);
				break;
		}
	}
	
	public void setLinear(boolean linear)
	{
		this.linear = linear;
	}

	@Override
	public void upload(OpenGLContext c) throws GLException
	{		
		if (!isReady())
		{
			OpenGLManager.getInstance().pushDebug("Uploading", this);
			OpenGLStack.push(Texture.class, this, c, this.getLayer());
			
			/*
			 * Bind texture and set specific parameters
			 */
			
			switch (this.target)
			{
				case TEXTURE_2D:
					c.gl().glPixelStorei(GL2.GL_PACK_ALIGNMENT, 1);
					c.gl().glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_EDGE);  
					c.gl().glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_EDGE);  					
					break;
				case TEXTURE_3D:					
					c.gl().glPixelStorei(GL2.GL_PACK_ALIGNMENT, 1);
					c.gl().glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_REPLACE);
					c.gl().glTexParameteri(GL2.GL_TEXTURE_3D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_BORDER);
					c.gl().glTexParameteri(GL2.GL_TEXTURE_3D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_BORDER);
					c.gl().glTexParameteri(GL2.GL_TEXTURE_3D, GL2.GL_TEXTURE_WRAP_R, GL2.GL_CLAMP_TO_BORDER);					
					
					break;
			}
			
			/*
			 * Set common parameters
			 */
			int glTarget = getGlTarget();
			c.gl().glTexParameteri(glTarget, GL2.GL_TEXTURE_MAG_FILTER, linear ? GL2.GL_LINEAR : GL2.GL_NEAREST);
			c.gl().glTexParameteri(glTarget, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
			c.gl().glTexParameteri(glTarget, GL2.GL_TEXTURE_BASE_LEVEL, 0);
			c.gl().glTexParameteri(glTarget, GL2.GL_TEXTURE_MAX_LEVEL, 0);
			c.gl().glTexParameteri(glTarget, GL2.GL_GENERATE_MIPMAP, GL2.GL_TRUE);
			
			/*
			 * Reserve texture memory
			 */
			switch (this.target)
			{
				case TEXTURE_2D:
					c.gl().glTexImage2D(
							glTarget,
							0,
							this.getGlFormat(),
							this.getWidth(),
							this.getHeight(),
							0,
							this.getGlFormat(),
							this.getGlDataType(),
							null);
					break;
				case TEXTURE_3D:
					c.gl().glTexImage3D(
							glTarget,
							0,
							this.getGlFormat(),
							this.getWidth(),
							this.getHeight(),
							this.getDepth(),
							0,
							this.getGlFormat(),
							this.getGlDataType(),
							null);
					break;
			}
			
			OpenGLStack.pop(Texture.class, this, c, this.getLayer());
			
			OpenGLManager.getInstance().checkError(c);
			
			ready = true;
			
			if (uploadType == EUploadType.ALLOCATE_AND_UPLOAD)
			{
				for (Brick brick : bricks)
				{
					OpenGLManager.getInstance().queueUpload(brick);
				}
			}
			
			OpenGLManager.getInstance().popDebug();
		}
	}
	
	public void queueBrick(int x, int y, int z)
	{
		Brick brick = bricks.getEntry(x, y, z);
		OpenGLManager.getInstance().queueUpload(brick);
	}
	
	public EFormat getFormat()
	{
		return this.format;
	}
	
	public EDataType getDataType()
	{
		return this.dataType;
	}
	
	public int getGlTarget()
	{
		switch (this.target)
		{
			case TEXTURE_1D:
				return GL2.GL_TEXTURE_1D;
			case TEXTURE_2D:
				return GL2.GL_TEXTURE_2D;
			case TEXTURE_3D:
				return GL2.GL_TEXTURE_3D;
		}
		return -1;
	}
	
	public int getGlFormat()
	{
		switch (this.format)
		{
			case RGBA:
				return GL2.GL_RGBA;
			case RGB:
				return GL2.GL_RGB;
			case A:
				return GL2.GL_ALPHA;
			case DEPTH:
				return GL2.GL_DEPTH_COMPONENT;
		}
		return -1;
	}
	
	public int getGlDataType()
	{
		switch (this.dataType)
		{
			case FLOAT:
				return GL2.GL_FLOAT;
			case BYTE:
				return GL2.GL_UNSIGNED_BYTE;
		}
		return -1;
	}

	@Override
	public void release(OpenGLContext c) throws GLException
	{

	}

	@Override
	public boolean isReady()
	{
		return ready;
	}
	
	@Override
	public String getName()
	{
		return name;
	}
	
	public int getTextureID()
	{
		return textureId;
	}

	@Override
	public void apply(OpenGLContext c)
	{
		if (textureId == -1)
		{
			int[] textureIds = new int[1];
			c.gl().glGenTextures(1, textureIds, 0);
			textureId = textureIds[0];				
		}
		
		int glType = getGlTarget();			
		c.gl().glActiveTexture(GL2.GL_TEXTURE0 + getLayer());			
		c.gl().glEnable(glType);		
		c.gl().glBindTexture(glType, textureId);			
		
		OpenGLManager.getInstance().checkError(c, this);
	}

	@Override
	public void clear(OpenGLContext c)
	{
		int glType = getGlTarget(); 
		c.gl().glActiveTexture(GL2.GL_TEXTURE0 + getLayer());			
		c.gl().glBindTexture(glType, 0);			
		c.gl().glDisable(glType);
	}
	
	@Override
	public void push(OpenGLContext c) throws GLException
	{
		
	}
	
	@Override
	public void pop(OpenGLContext c) throws GLException
	{
		
	}
	
	public static class Brick implements IUploadable
	{
		protected int brickX = -1;
		protected int brickY = -1;
		protected int brickZ = -1;
		
		protected ADataCube data = null;
		protected Texture texture = null;
		
		public Brick(Texture texture, int brickX, int brickY, int brickZ)
		{
			this.brickX = brickX;
			this.brickY = brickY;
			this.brickZ = brickZ;
			this.texture = texture;
		}
		
		public ByteCube constructAsByteCube(int stride)
		{
			setData(new ByteCube(getWidth(), getHeight(), getDepth(), stride));
			return (ByteCube) getData();
		}
		
		public FloatCube constructAsFloatCube(int stride)
		{
			setData(new FloatCube(getWidth(), getHeight(), getDepth(), stride));
			return (FloatCube) getData();
		}
		
		public void setData(ADataCube data)
		{
			this.data = data;
		}
		
		public ADataCube getData()
		{
			return data;
		}
		
		public int getWidth()
		{
			return texture.getWidth() / texture.getBricks().getWidth();
		}
		
		public int getHeight()
		{
			return texture.getHeight() / texture.getBricks().getHeight();
		}
		
		public int getDepth()
		{
			return texture.getDepth() / texture.getBricks().getDepth();
		}
		
		public int getLeft()
		{
			return this.brickX * getWidth(); 
		}
		
		public int getTop()
		{
			return this.brickY * getHeight(); 
		}
		
		public int getFront()
		{
			return this.brickZ * getDepth(); 
		}
		
		@Override
		public String getName()
		{
			return texture.getName() + " [" + this.brickX + ", " + this.brickY + ", " + this.brickZ + "]";
		}
		
		@Override
		public String toString()
		{
			return "'" + getName() + "' (Brick)";
		}

		@Override
		public void upload(OpenGLContext c) throws GLException
		{
			OpenGLManager.getInstance().pushDebug("Uploading", this);
			OpenGLStack.push(Texture.class, texture, c, texture.getLayer());
			
			/*
			 * Determine sub-texture coordinates
			 */
			int
				width = getWidth(),
				height = getHeight(),
				depth = getDepth(),
				left = getLeft(),
				top = getTop(),
				front = getFront();
			
			/*
			 * Upload sub-texture
			 */
			Buffer dataBuffer = null;
			if (this.data instanceof FloatCube)
			{
				float[] floats = ((FloatCube) this.data).getEntries();
				dataBuffer = Transform.floatBuffer(floats);
			} 
			else if (this.data instanceof ByteCube)
			{
				byte[] bytes = ((ByteCube) this.data).getEntries();
				dataBuffer = Transform.byteBuffer(bytes);
			} 
			
			switch (texture.target)
			{
				case TEXTURE_1D:
					/*
					 * TODO
					 */
					break;
				case TEXTURE_2D:						
					c.gl().glTexSubImage2D(
							texture.getGlTarget(),
							0,
							left,
							top,
							width,
							height,
							texture.getGlFormat(),
							texture.getGlDataType(),
							dataBuffer);
					break;
				case TEXTURE_3D:
					c.gl().glTexSubImage3D(
							texture.getGlTarget(),
							0,
							left,
							top,
							front,
							width,
							height,
							depth,
							texture.getGlFormat(),
							texture.getGlDataType(),
							dataBuffer);
					break;
			}
			
			OpenGLManager.getInstance().checkError(c, this);
			
			OpenGLStack.pop(Texture.class, texture, c, texture.getLayer());
			
			OpenGLManager.getInstance().checkError(c, this);
			OpenGLManager.getInstance().popDebug();
		}

		@Override
		public void release(OpenGLContext c) throws GLException
		{
			
		}

		@Override
		public boolean isReady()
		{
			return true;
		}
		
	}
	
	public enum ETarget {TEXTURE_1D, TEXTURE_2D, TEXTURE_3D};
	
	public enum EFormat {RGBA, RGB, A, DEPTH}
	
	public enum EDataType {FLOAT, BYTE}
	
	public enum EUploadType {ALLOCATE_ONLY, ALLOCATE_AND_UPLOAD}
	
	public enum ETextureType {AMBIENT, DIFFUSE, SPECULAR, ALPHA, NORMAL, ENVIRONENT, DEPTH, DATA};
}
