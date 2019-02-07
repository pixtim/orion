package orion.sdk.graphics.drawables.volumes;

import java.util.HashMap;
import java.util.Map;

import orion.sdk.graphics.shading.texturing.Texture;
import orion.sdk.graphics.shading.texturing.Texture.Brick;
import orion.sdk.math.geometry.Box;

public class VolumeChunk
{
	private Box positionBounds = new Box();
	private Box textureBounds = new Box();
	private Map<Texture.ETextureType, Brick> bricks = new HashMap<Texture.ETextureType, Brick>();
	
	public VolumeChunk()
	{

	}
	
	public void setBrick(Texture.ETextureType type, Brick brick)
	{
		this.bricks.put(type, brick);
	}
	
	public Brick getBrick(Texture.ETextureType type)
	{
		return this.bricks.get(type);
	}

	public Box getPositionBounds()
	{
		return this.positionBounds;
	}
	
	public Box getTextureBounds()
	{
		return this.textureBounds;
	}
}
