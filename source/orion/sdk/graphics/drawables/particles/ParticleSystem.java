package orion.sdk.graphics.drawables.particles;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import javax.media.opengl.GLException;

import orion.sdk.events.IUpdatable;
import orion.sdk.graphics.drawables.surfaces.Mesh;
import orion.sdk.graphics.shading.texturing.Sprite;
import orion.sdk.graphics.util.OpenGLContext;
import orion.sdk.graphics.util.OpenGLManager;
import orion.sdk.graphics.viewing.cameras.ACamera;
import orion.sdk.math.FloatMatrix;
import orion.sdk.math.IFloatMatrix;
import orion.sdk.math.geometry.Box;
import orion.sdk.monitoring.incidents.Incident;
import orion.sdk.monitoring.incidents.IncidentManager;

public class ParticleSystem extends Mesh implements IUpdatable
{
	public static float UPDATE_ANGLE = (float) (Math.PI * 1.0 / 180.0);
	public static float UPDATE_DISTANCE = 0.1f;
	public Particle[] particles = null;
	public ACamera camera = null;
	protected FloatMatrix lastCameraVector = null;
	protected boolean firstUpload = true;
	protected boolean shouldUpload = false;
	protected ACamera cameraSnapshot = null;
	protected ParticleSorter particleSorter = new ParticleSorter();
	public Sprite sprite = null;
			
	public ParticleSystem(int maxParticleCount, ACamera camera, Box bounds) throws Exception
	{
		super(maxParticleCount, Mesh.FaceType.QUAD, bounds);
		this.particles = new Particle[maxParticleCount];
		this.camera = camera;
		this.cameraSnapshot = camera.clone();
	}
	
	@Override
	public void upload(OpenGLContext c) throws GLException
	{
		/*
		 * Upload the mesh
		 */
		synchronized (particles)
		{
			super.upload(c);
			shouldUpload = false;
		}
	}
	
	public void sortFaces() throws Exception
	{
		synchronized (particles)
		{
			particleSorter.updateDistances();
			Arrays.sort(particles, particleSorter);
		}
	}
	
	public void setFaces() throws Exception
	{
		synchronized (particles)
		{
			/*
			 * Update faces
			 */
			for (int i = 0; i < particles.length; i++)
			{
				int index = i * 4; 
				Particle particle = particles[i];
				if (particle != null)
				{
					IFloatMatrix uv00, uv10, uv11, uv01;
					if (sprite != null)
					{
						Sprite.Tile tile = sprite.tiles.get(particle.spriteTile); 
						uv00 = tile.uv00;
						uv10 = tile.uv10;
						uv11 = tile.uv11;
						uv01 = tile.uv01;
					}
					else
					{
						uv00 = FloatMatrix.vector(0, 0);
						uv10 = FloatMatrix.vector(1, 0);
						uv11 = FloatMatrix.vector(1, 1);
						uv01 = FloatMatrix.vector(0, 1);
					}

					float
						halfW = particle.size.getX() / 2,
						halfH = particle.size.getY() / 2;
					IFloatMatrix 
						v0 = FloatMatrix.vector(-1 * halfW, -1 * halfH, 0, 1),
						v1 = FloatMatrix.vector( 1 * halfW, -1 * halfH, 0, 1),
						v2 = FloatMatrix.vector( 1 * halfW,  1 * halfH, 0, 1),
						v3 = FloatMatrix.vector(-1 * halfW,  1 * halfH, 0, 1),
						normal = FloatMatrix.vector(0, 0, 1),
						color = particle.color;
					setVertex(index + 0, v0, normal, uv00, color, particle.position);
					setVertex(index + 1, v1, normal, uv10, color, particle.position);
					setVertex(index + 2, v2, normal, uv11, color, particle.position);
					setVertex(index + 3, v3, normal, uv01, color, particle.position);					
				}
				else
				{
					setVertex(index + 0, null, null, null, null);
					setVertex(index + 1, null, null, null, null);
					setVertex(index + 2, null, null, null, null);
					setVertex(index + 3, null, null, null, null);					
				}
				setFace(i, index + 0, index + 1, index + 2, index + 3);
			}
		}
		
		OpenGLManager.getInstance().queueUpload(this);
	}

	@Override
	public void update(float dt) throws Exception
	{
		cameraSnapshot = camera.clone();

		FloatMatrix cameraVector = FloatMatrix.vector(cameraSnapshot.getObserver().subtract(cameraSnapshot.getTarget()), 3);
		if ((firstUpload
			|| lastCameraVector == null
			|| lastCameraVector.angleBetween(cameraVector) > UPDATE_ANGLE
			|| Math.abs(lastCameraVector.norm() - cameraVector.norm()) > UPDATE_DISTANCE
			))
		{

			lastCameraVector = cameraVector;
			shouldUpload = true;
			
			sortFaces();		
			setFaces();
			firstUpload = false;						
		}
	}

	@Override
	public float getUpdatePeriod()
	{
		return 0.01f;
	}
	
	protected class ParticleSorter implements Comparator<Particle>
	{
		protected Map<Particle, Float> particleDistances = new HashMap<Particle, Float>();
		
		public ParticleSorter()
		{
			
		}
		
		public void updateDistances() throws Exception
		{
			synchronized (particles)
			{
				IFloatMatrix observer = cameraSnapshot.getObserver();
				IFloatMatrix direction = observer.subtract(cameraSnapshot.getTarget()); 
				for (Particle particle : particles)
				{
					IFloatMatrix projection = observer.subtract(particle.position).projectToVector(direction);
					float distance = projection.norm();
					particleDistances.put(particle, distance);
				}
			}
		}

		@Override
		public int compare(Particle a, Particle b)
		{
			try
			{
				float distanceA = particleDistances.get(a);
				float distanceB = particleDistances.get(b);
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
			catch (Exception e)
			{
				IncidentManager.notifyIncident(Incident.newError("Compare error", e));
				return 0;
			}
		}
	}

	@Override
	public boolean shouldUpdate()
	{
		return true;
	}
}
