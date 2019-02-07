package orion.sdk.assets.loaders;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import orion.sdk.assets.AssetManager;
import orion.sdk.graphics.shading.texturing.Sprite;
import orion.sdk.graphics.shading.texturing.Texture;
import orion.sdk.math.FloatMatrix;

public class GridSpriteGenerator implements ICompoundAssetProcessor
{
	protected int size = 256;
	
	public GridSpriteGenerator(int size)
	{
		this.size = size;
	}
	
	/**
	 * Constructs a grid structured texture sprite
	 */
	@Override
	public Object process(String[] partPaths) throws Exception
	{
		/*
		 * Collect all the texture parts
		 */
		List<Texture> parts = new ArrayList<Texture>();
		for (String path : partPaths)
		{
			if (path != null)
			{
				Object asset = AssetManager.getAsset(path);
				if (!(asset instanceof Texture))
				{
					throw new Exception("Texture parts expected");
				}
				Texture part = (Texture) asset;
				parts.add(part);
			}
			else
			{
				parts.add(null);
			}
		}
		
		/*
		 * Calculate the sprite layout
		 */
		int stride = (int) Math.ceil(Math.sqrt(parts.size())); 
		float tileSize = size / stride;
		float x = 0;
		float y = 0;
		float uvSize = tileSize / size; 
		List<Sprite.Tile> tiles = new ArrayList<Sprite.Tile>();
		for (int tileY = 0; tileY < stride; tileY++)
		{			
			for (int tileX = 0; tileX < stride; tileX++)
			{
				float
					xUv = x / size,
					yUv = y / size;
				Sprite.Tile tile = new Sprite.Tile(
						x, y, tileSize, tileSize,
						FloatMatrix.vector(xUv, yUv),
						FloatMatrix.vector(xUv + uvSize, yUv),
						FloatMatrix.vector(xUv + uvSize, yUv + uvSize),
						FloatMatrix.vector(xUv, yUv + uvSize));
				tiles.add(tile);
				x = x + tileSize;				
			}
			x = 0;
			y = y + tileSize;
		}
		
		/*
		 * Create the texture image and draw the parts on it
		 */
		BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);		
		Graphics g = image.createGraphics();
		for (int i = 0; i < parts.size(); i++)
		{
			Texture part = parts.get(i);
			if (part != null)
			{
				Sprite.Tile tile = tiles.get(i);
				g.drawImage(part.getImages()[0], (int) tile.x, (int) tile.y, (int) tile.w, (int) tile.h, null);
			}
		}

		/*
		 * Create the sprite
		 */
		Sprite sprite = new Sprite("grid sprite", image);
		sprite.tiles = tiles;
		return sprite;
	}
}
