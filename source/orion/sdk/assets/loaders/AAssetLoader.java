package orion.sdk.assets.loaders;

import orion.sdk.assets.io.IAssetSource;


public abstract class AAssetLoader
{
	public abstract Object load(IAssetSource source, String assetPath) throws Exception;
}
