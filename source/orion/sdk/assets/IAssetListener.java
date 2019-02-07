package orion.sdk.assets;

import orion.sdk.assets.AssetRequest;
import orion.sdk.assets.io.IAssetSource;

public interface IAssetListener
{
	public void notifyAssetAvailable(IAssetSource source, AssetRequest request);
	
	public void notifyAssetNotAvailable(IAssetSource source, AssetRequest request, Exception e);
}
