package orion.sdk.graphics.viewing.cameras;

import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingQueue;

import javax.media.opengl.GL2;
import javax.media.opengl.GLException;

import orion.sdk.graphics.util.IStackable;
import orion.sdk.graphics.util.OpenGLContext;
import orion.sdk.graphics.util.OpenGLManager;
import orion.sdk.graphics.viewing.projection.AUnprojectRequest;
import orion.sdk.math.FloatMatrix;
import orion.sdk.math.FloatQuaternion;
import orion.sdk.math.IFloatMatrix;
import orion.sdk.monitoring.incidents.Incident;
import orion.sdk.monitoring.incidents.IncidentManager;

public abstract class ACamera implements IStackable
{
	protected static Stack<ACamera> cameras = new Stack<ACamera>();
	
	protected IFloatMatrix observer = FloatMatrix.vector(0, 0, 10f, 1);
	protected IFloatMatrix target = FloatMatrix.vector(0, 0, 0, 1);
	protected IFloatMatrix up = FloatMatrix.vector(0, 1, 0, 0);
	protected String name = "unknown camera";
	private Queue<AUnprojectRequest> unprojectRequests = new LinkedBlockingQueue<AUnprojectRequest>();
	
	
	public ACamera(String name)
	{
		this.name = name;
	}
	
	public void queueUnprojectRequest(AUnprojectRequest request)
	{
		synchronized (unprojectRequests)
		{
			unprojectRequests.add(request);
		}
	}
	
	@Override
	public String getName()
	{
		return this.name;
	}
	
	public void rotateThirdPerson(FloatQuaternion rotation) throws Exception
	{
		synchronized (OpenGLManager.getInstance().getDisplayMutex()) 
		{	
			IFloatMatrix direction = target.subtract(observer);		
	      direction = FloatQuaternion.matrixProduct(rotation, direction);
	      direction = direction.scalarProduct(-1f);
	      observer = target.add(direction); 
	      up = FloatQuaternion.matrixProduct(rotation, up);     
		}
	}
	
	public void rotateFirstPerson(FloatQuaternion rotation) throws Exception
	{
		synchronized (OpenGLManager.getInstance().getDisplayMutex()) 
		{
	      IFloatMatrix direction = target.subtract(observer);
	      direction = FloatQuaternion.matrixProduct(rotation, direction);
	      target = observer.add(direction);		
	      up = FloatQuaternion.matrixProduct(rotation, up);
		}
	}

	public void thirdPerson(FloatQuaternion rotation, FloatMatrix target)
			throws Exception
	{
		synchronized (OpenGLManager.getInstance().getDisplayMutex()) 
		{		
			IFloatMatrix offset = FloatQuaternion.matrixProduct(rotation,
					FloatMatrix.vector(0, 0, getObserverDistance(), 0));
			IFloatMatrix up = FloatQuaternion.matrixProduct(rotation, FloatMatrix.vector(0, 1, 0, 0));
	
			this.target = FloatMatrix.vector(target, 4);
			this.target.setU(1);
			
			this.observer = target.add(offset);
			this.observer.setU(1);
	
			this.up = FloatMatrix.vector(up, 4);
			this.up.setU(0);
		}
	}
	
	public float getObserverDistance() throws Exception
	{
		return observer.subtract(target).norm();
	}
	
	public void setObserverDistance(float distance) throws Exception
	{
		synchronized (OpenGLManager.getInstance().getDisplayMutex()) 
		{
			IFloatMatrix direction = observer.subtract(target);
			direction.normalize();
			float observerDistance = (distance > 0 ? distance : 0.01f);
			observer = target.add(direction.scalarProduct(observerDistance));
		}
	}
	
	public void firstPerson(FloatQuaternion rotation, FloatMatrix observer)
			throws Exception
	{
		synchronized (OpenGLManager.getInstance().getDisplayMutex()) 
		{
			IFloatMatrix offset = 
					FloatQuaternion.matrixProduct(rotation, FloatMatrix.vector(0, 0, -1 * getObserverDistance(), 0));
			IFloatMatrix up = FloatQuaternion.matrixProduct(rotation, FloatMatrix.vector(0, 1, 0, 0));
	
			this.observer = FloatMatrix.vector(observer, 4);
			this.observer.setU(1);
			
			this.target = observer.add(offset);
			this.target.setU(1);
	
			this.up = FloatMatrix.vector(up, 4);
			this.up.setU(0);
		}
	}
	
	public IFloatMatrix getTarget()
	{
		return target.clone();
	}
	
	public IFloatMatrix getObserver()
	{
		return observer.clone();
	}
	
	public IFloatMatrix getUp()
	{
		return up.clone();
	}
	
	public IFloatMatrix getModelViewRotation() throws Exception
	{
		IFloatMatrix forward =
			FloatMatrix.vector(
				observer.getX() - target.getX(),
				observer.getY() - target.getY(),
				observer.getZ() - target.getZ());
		forward.normalize();
		
		IFloatMatrix up =	FloatMatrix.vector(this.getUp(), 3);
		up.normalize();
		
		IFloatMatrix side = up.cross(forward);
		side.normalize();
		
		IFloatMatrix rotation = FloatMatrix.matrix(
				side.getX(), up.getX(), forward.getX(), 0f,	
				side.getY(), up.getY(), forward.getY(), 0f,
				side.getZ(), up.getZ(), forward.getZ(), 0f,
				0f, 0f, 0f, 1f);
		
		return rotation;
	}

	
	public IFloatMatrix getModelViewTranslation() throws Exception
	{
		IFloatMatrix translation = FloatMatrix.translate(
			-1f * observer.getX(),
			-1f * observer.getY(),
			-1f * observer.getZ());
		
		return translation;
	}
	
	public IFloatMatrix getModelView() throws Exception
	{
		IFloatMatrix rotation = getModelViewRotation();
		IFloatMatrix translation = getModelViewTranslation();
		IFloatMatrix modelView = rotation.product(translation);
		return modelView;
	}
	
	public void clear(OpenGLContext c)
	{
		synchronized (OpenGLManager.getInstance().getDisplayMutex()) 
		{
			c.gl().glMatrixMode(GL2.GL_MODELVIEW);
			c.gl().glLoadIdentity();
			c.gl().glMatrixMode(GL2.GL_PROJECTION);
			c.gl().glLoadIdentity();
		}
	}	
	
	@Override
	public void push(OpenGLContext c)
	{
		
	}
	
	@Override
	public void pop(OpenGLContext c)
	{
	}
	
	@Override
	public void apply(OpenGLContext c) throws GLException
	{
		processUnprojectRequests(c);
	}
	
	protected void processUnprojectRequests(OpenGLContext c)
	{
		synchronized (unprojectRequests)
		{
			while (!unprojectRequests.isEmpty())
			{
				AUnprojectRequest request = unprojectRequests.remove();
				try
				{
					request.unproject(this, c);
				} catch (Exception e)
				{
					IncidentManager.notifyIncident(Incident.newError("Failed to unproject '" + request.getName() + "'", e));
				}
			}
		}
	}
	
	@Override
	public abstract ACamera clone() throws CloneNotSupportedException;
	
	public IFloatMatrix unproject(OpenGLContext c, IFloatMatrix point)
	{
		float[] M = new float[16];
		float[] P = new float[16];
		float[] viewport = c.viewport;
		c.gl().glGetFloatv(GL2.GL_MODELVIEW_MATRIX, M, 0);
		c.gl().glGetFloatv(GL2.GL_PROJECTION_MATRIX, P, 0);		
		
		float[] entries = new float[4];
		c.glu().gluUnProject(
			point.getX(),
			point.getY(),
			point.getZ(),
			M, 0,
			P, 0,
			new int[] {(int) viewport[0], (int) viewport[1], (int) viewport[2], (int) viewport[3]}, 0,
			entries, 0);
		entries[3] = 1;
		return FloatMatrix.vector(entries);
	}
	
	public IFloatMatrix unproject(
		OpenGLContext c,
		IFloatMatrix sourcePoint,
		Viewport sourceViewport,
		Viewport desinationViewport)
	{
		if (sourceViewport == null)
		{
			sourceViewport = desinationViewport;
		}

		IFloatMatrix p = FloatMatrix.vector(
			(sourcePoint.getX() - sourceViewport.getLeft()) / sourceViewport.getWidth() * 
				desinationViewport.getWidth() + desinationViewport.getLeft(),
			(sourceViewport.getHeight() - (sourcePoint.getY() - sourceViewport.getTop())) / sourceViewport.getHeight() * 
				desinationViewport.getHeight() + desinationViewport.getTop(),
			sourcePoint.getZ());
		
		return unproject(c, p);
	}
	
	public IFloatMatrix[] getFrustum(OpenGLContext c)
	{
		float 
			left = c.viewport[0],
			top = c.viewport[1],
			width = c.viewport[2],
			height = c.viewport[3],
			near = 0,
			far = 1;
		
		IFloatMatrix[] corners = new IFloatMatrix[]
			{
				/*
				 * Near plane
				 */
				FloatMatrix.vector(left, top, near),				
				FloatMatrix.vector(left + width, top, near),				
				FloatMatrix.vector(left + width, top + height, near),				
				FloatMatrix.vector(left, top + height, near),
				
				/*
				 * Far plane
				 */
				FloatMatrix.vector(left, top, far),
				FloatMatrix.vector(left + width, top, far),
				FloatMatrix.vector(left + width, top + height, far),
				FloatMatrix.vector(left, top + height, far),
			};
		
		for (int i = 0; i < corners.length; i++)
		{
			corners[i] = unproject(c, corners[i]);
		}
		return corners;
	}
}
