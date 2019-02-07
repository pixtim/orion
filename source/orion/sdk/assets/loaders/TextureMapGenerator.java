package orion.sdk.assets.loaders;

import java.util.Map;
import java.util.TreeMap;

import orion.sdk.assets.AssetManager;
import orion.sdk.graphics.shading.texturing.Texture;

/**
 * Generates a {@link Map} of {@link Texture} instances from the {@link AssetManager} using the
 * provided list of part paths.
 * 
 * @author Tim
 *
 */
public class TextureMapGenerator implements ICompoundAssetProcessor
{
	protected int layer = 0;
	protected boolean linear = false;
	
	public TextureMapGenerator(int layer, boolean linear)
	{
		this.layer = layer;
		this.linear = linear;
	}
	
	public TextureMapGenerator()
	{
		this(0, true);
	}
	
	@Override
	public Object process(String[] partPaths) throws Exception
	{
		/*
		 * Collect all the texture parts and put them into a map which we'll return.
		 */
		Map<String, Texture> textureMap = new TreeMap<String, Texture>();
		for (String path : partPaths)
		{
			Texture part = AssetManager.getTexture(path, layer, linear);
			textureMap.put(path, part);
		}

		return textureMap;
	}
}
