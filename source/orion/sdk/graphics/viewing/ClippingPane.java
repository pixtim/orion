package orion.sdk.graphics.viewing;

import javax.media.opengl.GL2;
import javax.media.opengl.GLException;

import orion.sdk.graphics.util.IStackable;
import orion.sdk.graphics.util.OpenGLContext;
import orion.sdk.math.FloatMatrix;
import orion.sdk.monitoring.incidents.Incident;
import orion.sdk.monitoring.incidents.IncidentManager;

public class ClippingPane implements IStackable
{
	public FloatMatrix normal = FloatMatrix.vector(0f, 0f, 1f, 0f);
	public FloatMatrix position = FloatMatrix.vector(0f, 0f,0f, 1f);
	public int index = 0;
	
	public ClippingPane()
	{
		
	}
	
	public ClippingPane(FloatMatrix normal, FloatMatrix position, int index)
	{
		this.normal = normal;
		this.position = position;
		this.index = index;
	}
	
	protected double[] getEquation() throws Exception
	{
		double[] equation = new double[4];
		FloatMatrix N  = FloatMatrix.vector(normal, 3);
		N.normalize();			
		
		equation[0] = N.getX();
		equation[1] = N.getY();
		equation[2] = N.getZ();
		
		FloatMatrix P = FloatMatrix.vector(position, 3);
		float D = -1f * N.dot(P); 
		equation[3] = D;
		
		return equation;
	}

	@Override
	public void apply(OpenGLContext c)
	{
		try
		{
			int pane = GL2.GL_CLIP_PLANE0 + this.index;
			c.gl().glEnable(pane);
			double[] equation;
			equation = getEquation();
			c.gl().glClipPlane(pane, equation, 0 );
		}
		catch (Exception e)
		{
			IncidentManager.notifyIncident(Incident.newError("Failed to push clipping pane", e));
		}
	}

	@Override
	public void clear(OpenGLContext c)
	{
		int pane = GL2.GL_CLIP_PLANE0 + this.index;
		c.gl().glDisable(pane);		
	}
	
	@Override
	public void push(OpenGLContext c) throws GLException
	{
		
	}
	
	@Override
	public void pop(OpenGLContext c) throws GLException
	{
		
	}

	@Override
	public String getName()
	{
		return "Clipping pane " + this.index;
	}
}
