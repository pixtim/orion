package orion.sdk.graphics.viewing.projection;

import orion.sdk.math.IFloatMatrix;

public interface IUnprojectListener
{
	public void unprojectEvent(AUnprojectRequest request, IFloatMatrix position);
}
