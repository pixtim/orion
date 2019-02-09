package orion.sdk.graphics.panels;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLException;

import orion.sdk.graphics.util.OpenGLContext;
import orion.sdk.graphics.util.OpenGLManager;
import orion.sdk.math.FloatMatrix;
import orion.sdk.math.geometry.Box;
import orion.sdk.math.geometry.Vertex;

public class ScenePanel extends APanel
{
	protected Vertex[] vertices = new Vertex[4];

	public ScenePanel(String name)
	{
		this(
			name,
			FloatMatrix.vector(-1, -1, 0, 1),
			FloatMatrix.vector(1, -1, 0, 1),
			FloatMatrix.vector(1, 1, 0, 1),
			FloatMatrix.vector(-1, 1, 0, 1));
	}
	
	public ScenePanel(String name, FloatMatrix v0, FloatMatrix v1, FloatMatrix v2, FloatMatrix v3)
	{
		super(name);
		
		this.setVertex(0, v0, FloatMatrix.vector(0, 0));
		this.setVertex(1, v1, FloatMatrix.vector(1, 0));
		this.setVertex(2, v2, FloatMatrix.vector(1, 1));
		this.setVertex(3, v3, FloatMatrix.vector(0, 1));
	}
	
	public void setVertex(int index, FloatMatrix position, FloatMatrix texture)
	{
		Vertex vertex = new Vertex();
		vertex.setPosition(position);
		vertex.setColor(FloatMatrix.vector(1, 1, 1, 1));
		vertex.setTexture(texture);
		
		this.vertices[index] = vertex;
	}
	
	@Override
	public void draw(OpenGLContext c) throws GLException
	{
		OpenGLManager.getInstance().pushDebug("Drawing", this);
		
		c.gl().glBegin(GL2.GL_QUADS);
		
			for (Vertex vertex : this.vertices)
			{
				vertex.apply(c);
			}
			
		c.gl().glEnd();
		
		OpenGLManager.getInstance().popDebug();
		
		super.draw(c);
	}

	@Override
	public int getFaceCount()
	{
		return 1;
	}

	@Override
	public Box getBounds()
	{
		return null;
	}
}
