package orion.sdk.node;

import javax.media.opengl.GLException;

import orion.sdk.data.cubes.FloatCube;
import orion.sdk.data.cubes.ObjectCube;
import orion.sdk.graphics.shading.glsl.GenericShader;
import orion.sdk.graphics.shading.texturing.Texture;
import orion.sdk.graphics.util.OpenGLContext;
import orion.sdk.math.FloatMatrix;
import orion.sdk.math.geometry.Box;
import orion.sdk.node.drawables.ShadedNode;

public abstract class AVolumeTreeNode extends ShadedNode
{
	protected Texture dataMap = null;
	protected BrickNode tree = null;
	protected FloatCube data = null; 
	
	public AVolumeTreeNode(String name, GenericShader shader, Box bounds, int depth, int dataCapacity)
	{
		super(name, null, shader, null);
	
		this.tree = BrickNode.constructTree(depth);
		this.data = new FloatCube(dataCapacity, dataCapacity, dataCapacity, 4);
		this.dataMap = new Texture(name + " - data cube", Texture.EFormat.RGBA, Texture.ETarget.TEXTURE_3D, this.data);
	}
	
	@Override
	public void draw(OpenGLContext c) throws GLException
	{		
		super.draw(c);
	}
	
	public Texture getDataMap()
	{
		return dataMap;
	}
	
	public abstract void prepareBrick(int brickX, int brickY, int brickZ);
	
	protected static class BrickNode
	{
		protected static int N = 2;
		
		protected ObjectCube<BrickNode> children = null;
		protected BrickNode parent = null;
		protected int brickX = -1;
		protected int brickY = -1;
		protected int brickZ = -1;
		
		public BrickNode(BrickNode parent)
		{
			this.children = new ObjectCube<>(N, N, N);
			this.parent = parent;
		}
		
		public BrickNode getChild(int x, int y, int z)
		{
			return children.getEntry(x, y, z);
		}
		
		public void setChild(int x, int y, int z, BrickNode child)
		{
			children.setEntry(x, y, z, child);
		}
		
		public int getLevel()
		{
			if (parent == null)
			{
				return 0;
			}
			else
			{
				return parent.getLevel() + 1;
			}
		}
		
		public int getBrickX()
		{
			return brickX;
		}
		
		public int getBrickY()
		{
			return brickY;
		}
		
		public int getBrickZ()
		{
			return brickZ;
		}
		
		public void setBrickX(int brickX)
		{
			this.brickX = brickX;
		}
		
		public void setBrickY(int brickY)
		{
			this.brickY = brickY;
		}
		
		public void setBrickZ(int brickZ)
		{
			this.brickZ = brickZ;
		}
		
		public BrickNode getNodeAt(FloatMatrix p, int level, Box bounds) throws Exception
		{
			if (!bounds.containsPoint(p))
			{
				return null;
			}
			else
			{
				if (this.getLevel() == level)
				{
					return this;
				}
				else
				{				
					float
						cellWidth = bounds.getWidth()	/ N,
						cellHeight = bounds.getHeight()	/ N,
						cellDepth = bounds.getDepth()	/ N;
					
					int
						x = Math.round( (N - 1) * (p.getX() - bounds.getLeft())		/ cellWidth),
						y = Math.round( (N - 1) * (p.getY() - bounds.getBottom())	/ cellHeight),
						z = Math.round( (N - 1) * (p.getZ() - bounds.getBack())		/ cellDepth);
					
					BrickNode child = children.getEntry(x, y, z); 
					if (child != null)
					{
						Box subBounds = bounds.subDivide(x, y, z, N, N, N);
						return child.getNodeAt(p, level, subBounds);
					}
					else
					{
						return this;
					}
				}
			}
		}
		
		public static BrickNode constructTree(int depth)
		{
			return constructTree(depth, null);
		}
		
		private static BrickNode constructTree(int depth, BrickNode parent)
		{
			BrickNode node = new BrickNode(parent);
			if (depth > 0)
			{
				depth--;
				for (int x = 0; x < N; x++)
				{
					for (int y = 0; y < N; y++)
					{
						for (int z = 0; z < N; z++)
						{
							BrickNode child = constructTree(depth, node);
							node.setChild(x, y, z, child);
						}
					}
				}
			}
			return node;
		}		
	}
}
