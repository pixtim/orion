package orion.sdk.assets.io;


public class FileAssetSourceFactory extends AAssetSourceFactory
{
	@Override
	public IAssetSource create(String path)
	{
		return new FileAssetSource(path);
	}

}
