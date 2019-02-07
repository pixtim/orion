package orion.sdk.graphics.drawables;

import org.junit.Test;

import orion.sdk.graphics.drawables.primitives.Axis;
import orion.sdk.graphics.drawables.primitives.Box;
import orion.sdk.graphics.drawables.primitives.Grid;
import orion.sdk.graphics.util.BasicOpenGLTestCase;
import orion.sdk.graphics.viewing.cameras.Viewport;
import orion.sdk.math.Bounds;
import orion.sdk.math.FloatMatrix;

public class TestDrawables extends BasicOpenGLTestCase
{
	@Test
	public void testDrawable_Axis() throws Exception
	{		
		Viewport viewport = this.createTestViewport(false);						
		viewport.getScene(0).getDrawables().add(new Axis());
		this.getDrawables().add(viewport);
		
		render();
		
		this.assertLastFrameExpected();
	}
	
	@Test
	public void testDrawable_Grid() throws Exception
	{		
		Viewport viewport = this.createTestViewport(false);						
		viewport.getScene(0).getDrawables().add(new Grid(10f, 10, 3f));
		this.getDrawables().add(viewport);				
		
		render();
		
		this.assertLastFrameExpected();
	}
	
	@Test
	public void testDrawable_Box() throws Exception
	{		
		Viewport viewport = this.createTestViewport();						
		viewport.getScene(0).getDrawables().add(
			new Box("box", new Bounds(1, 1, 1), FloatMatrix.vector(1, 0, 0, 1)));
		this.getDrawables().add(viewport);				
		
		render();
		
		this.assertLastFrameExpected();
	}
}
