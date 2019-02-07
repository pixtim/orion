package orion.sdk.assets.loaders;

import orion.sdk.assets.io.FileSource;
import orion.sdk.assets.io.IAssetSource;
import orion.sdk.graphics.shading.texturing.Texture;

public class TextureLoader extends AAssetLoader
{
	@Override
	public Object load(IAssetSource source, String assetPath) throws Exception
	{
		if (source instanceof FileSource)
		{
			FileSource fileSource = (FileSource) source;
			try
			{
				fileSource.open();
				Texture texture = new Texture(assetPath, fileSource.inputStream);
				return texture;
			}
			finally
			{
				if (fileSource != null)
				{
					fileSource.close();
				}
			}
		}
		else
		{
			throw new Exception("Source not supported");
		}
	}

}
