package orion.sdk.node.containers;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import orion.sdk.graphics.viewing.cameras.ACamera;
import orion.sdk.math.FloatMatrix;
import orion.sdk.math.IFloatMatrix;
import orion.sdk.math.geometry.Box;
import orion.sdk.monitoring.incidents.Incident;
import orion.sdk.monitoring.incidents.IncidentManager;
import orion.sdk.node.Node;
import orion.sdk.node.drawables.EntityNode;

public class OrderedSpatialNode extends SpatialNode
{
	public static float UPDATE_ANGLE = (float) (Math.PI * 1.0 / 180.0);
	public static float UPDATE_DISTANCE = 0.1f;

	public ACamera camera = null;
	public ACamera cameraSnapshot = null;
	public IFloatMatrix lastCameraVector = null;
	public NodeSorter nodeSorter = new NodeSorter();
	
	public OrderedSpatialNode(String name, int cellsCountX, int cellsCountY, int cellsCountZ,
			Box bounds, ACamera camera)
	{
		super(name, cellsCountX, cellsCountY, cellsCountZ, bounds);
		this.camera = camera;
	}

	@Override
	public void update(float dt) throws Exception
	{
		cameraSnapshot = camera.clone();
		
		IFloatMatrix cameraVector = FloatMatrix.vector(cameraSnapshot.getObserver().subtract(cameraSnapshot.getTarget()), 3);
		
		if (lastCameraVector == null
			|| lastCameraVector.angleBetween(cameraVector) > UPDATE_ANGLE
			|| Math.abs(lastCameraVector.norm() - cameraVector.norm()) > UPDATE_DISTANCE
			)
		{

			lastCameraVector = cameraVector;
			
			sortNodes();		
		}
		
		super.update(dt);
	}
	
	public void sortNodes() throws Exception
	{
		synchronized (children)
		{
			Node[] childArray = children.toArray(new Node[children.size()]);
			nodeSorter.updateDistances();
			Arrays.sort(childArray, nodeSorter);
			children.clear();
			for (Node child : childArray)
			{
				children.add(child);
			}
		}
	}	
	
	protected class NodeSorter implements Comparator<Node>
	{
		protected Map<Node, Float> nodeDistances = new HashMap<Node, Float>();
		
		public NodeSorter()
		{
			
		}
		
		public void updateDistances() throws Exception
		{
			synchronized (children)
			{
				IFloatMatrix observer = cameraSnapshot.getObserver();
				IFloatMatrix direction = observer.subtract(cameraSnapshot.getTarget()); 
				for (Node node : children)
				{
					if (node instanceof EntityNode)
					{
						EntityNode entityNode = (EntityNode) node;
						IFloatMatrix projection = observer
								.subtract(entityNode.getTransformation().getPosition())
								.projectToVector(direction);
						
						float distance = projection.norm();
						nodeDistances.put(node, distance);
					}
				}
			}
		}

		@Override
		public int compare(Node a, Node b)
		{
			try
			{
				if (nodeDistances.containsKey(a) && nodeDistances.containsKey(b))
				{
					float distanceA = nodeDistances.get(a);
					float distanceB = nodeDistances.get(b);
					if (distanceA > distanceB)
					{
						return -1;
					} 
					else if (distanceA < distanceB)
					{
						return 1;					
					}
					else
					{
						return 0;
					}
				}
				else
				{
					return 0;
				}
			}
			catch (Exception e)
			{
				IncidentManager.notifyIncident(Incident.newError("Compare error", e));
				return 0;
			}
		}
	}	

}
