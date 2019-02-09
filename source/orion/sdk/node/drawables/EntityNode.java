package orion.sdk.node.drawables;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLException;

import orion.sdk.graphics.drawables.IDrawable;
import orion.sdk.graphics.shading.glsl.AShader;
import orion.sdk.graphics.shading.lighting.Material;
import orion.sdk.graphics.util.OpenGLContext;
import orion.sdk.math.FloatMatrix;
import orion.sdk.math.FloatTransformation;
import orion.sdk.math.IFloatMatrix;
import orion.sdk.util.StructuredBinary;

/**
 * Represents a visible entity in the game world.
 * 
 * @author Tim
 * @since 1.0.00
 */
public class EntityNode extends ShadedNode
{
	private FloatTransformation vertexTransform = new FloatTransformation();
			
	public EntityNode(String name, IDrawable drawable, AShader shader, Material material)
	{
		super(name, drawable, shader, material);
	}
	
	@Override
	public float getUpdatePeriod()
	{
		return 0;
	}
	
	public IFloatMatrix getVertexTransformation() throws Exception
	{
		synchronized (vertexTransform)
		{
			IFloatMatrix parentTransformation = FloatMatrix.identity(4);
			if (getParent() != null && getParent() instanceof EntityNode)
			{
				EntityNode parentNode = (EntityNode) getParent();
				parentTransformation = parentNode.getVertexTransformation();
			}

			return parentTransformation.product(this.getTransformation().getMatrix());			
		}
	}
	
	public IFloatMatrix getNormalTransformation() throws Exception
	{
		IFloatMatrix vertexTransformation = getVertexTransformation();
		IFloatMatrix normalTransformation = vertexTransformation.invert().transpose();
		return normalTransformation.subMatrix(0,  0, 3, 3);
	}

	
	
	@Override
	public void draw(OpenGLContext c) throws GLException
	{
		c.gl().glMatrixMode(GL2.GL_MODELVIEW);
		c.gl().glPushMatrix();
		
		try
		{
			c.gl().glMultMatrixf(getVertexTransformation().columnMajor(), 0);
		} catch (Exception e)
		{
			throw new GLException("Model view transformation failed.", e);
		}

		super.draw(c);
		
		c.gl().glMatrixMode(GL2.GL_MODELVIEW);
		c.gl().glPopMatrix();
	}

	@Override
	public void read(StructuredBinary binary) throws Exception
	{
		
	}

	@Override
	public StructuredBinary write() throws Exception
	{
		throw new Exception("Not implemented yet");
	}
	
	public FloatTransformation getTransformation()
	{
		return vertexTransform;
	}

	
	
}
