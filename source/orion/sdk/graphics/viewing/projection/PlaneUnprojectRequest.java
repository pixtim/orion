package orion.sdk.graphics.viewing.projection;

import orion.sdk.graphics.util.OpenGLContext;
import orion.sdk.graphics.viewing.cameras.ACamera;
import orion.sdk.graphics.viewing.cameras.Viewport;
import orion.sdk.math.FloatMatrix;
import orion.sdk.math.IFloatMatrix;
import orion.sdk.math.MathUtils;

public class PlaneUnprojectRequest extends AUnprojectRequest
{
	protected IFloatMatrix planePosition;
	protected IFloatMatrix planeNormal;

	public PlaneUnprojectRequest(
		String name,
		FloatMatrix sourcePoint,
		Viewport sourceViewport,
		Viewport desinationViewport,
		IFloatMatrix planePosition,
		IFloatMatrix planeNormal)
	{
		super(name, sourcePoint, sourceViewport, desinationViewport);
		this.planePosition = planePosition;
		this.planeNormal = planeNormal;
	}

	@Override
	public void unproject(ACamera camera, OpenGLContext c) throws Exception
	{
		IFloatMatrix aScreen = FloatMatrix.vector(sourcePoint.getX(), sourcePoint.getY(), 1f, 1f);
		IFloatMatrix bScreen = FloatMatrix.vector(sourcePoint.getX(), sourcePoint.getY(), 0f, 1f);
				
		IFloatMatrix aWorld = camera.unproject(c, aScreen, sourceViewport, desinationViewport);
		IFloatMatrix bWorld = camera.unproject(c, bScreen, sourceViewport, desinationViewport);
		IFloatMatrix direction = bWorld.subtract(aWorld);
		
		IFloatMatrix intersection = MathUtils.linePlaneIntersect(aWorld, direction, planePosition, planeNormal);
		notifyListeners(intersection);
	}
}
