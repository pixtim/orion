package orion.sdk.node.containers;

import orion.sdk.data.cubes.ObjectCube;
import orion.sdk.math.FloatMatrix;
import orion.sdk.math.IFloatMatrix;
import orion.sdk.math.geometry.Box;
import orion.sdk.node.Node;
import orion.sdk.node.drawables.EntityNode;

public class SpatialNode extends ContainerNode
{
	public ObjectCube<Node> cells;
	public Box bounds;
	
	public SpatialNode(String name, int cellsCountX, int cellsCountY, int cellsCountZ, Box bounds)
	{
		super(name);
		this.cells = new ObjectCube<Node>(cellsCountX, cellsCountY, cellsCountZ);
		this.bounds = bounds;
	}
	
	private FloatMatrix getIndexes(EntityNode entityNode)
	{
		IFloatMatrix location = entityNode.getTransformation().getPosition();
		IFloatMatrix ratios = FloatMatrix.vector(
			(location.getX() - bounds.getLeft()) / bounds.getWidth(),
			(location.getY() - bounds.getBottom()) / bounds.getHeight(),
			(location.getZ() - bounds.getBack()) / bounds.getDepth());
		return FloatMatrix.vector( 
			Math.round(ratios.getX() * (cells.getWidth() - 1)),
			Math.round(ratios.getY() * (cells.getHeight() - 1)),
			Math.round(ratios.getZ() * (cells.getDepth() - 1)));
	}
	
	@Override
	public void addChild(Node child)
	{
		super.addChild(child);
		if (child instanceof EntityNode)
		{
			FloatMatrix indexes = getIndexes((EntityNode) child);
			cells.setEntry(
				(int) indexes.getX(),
				(int) indexes.getY(),
				(int) indexes.getZ(),
				child);
		}
		else
		{
			cells.setEntry(0, 0, 0, child);
		}
	}
	
	@Override
	public void removeChild(String name)
	{
		super.removeChild(name);
		Node child = getChild(name);
		FloatMatrix indexes = getIndexes((EntityNode) child);
		cells.setEntry(
			(int) indexes.getX(),
			(int) indexes.getY(),
			(int) indexes.getZ(),
			null);
	}
}
