package orion.sdk.graphics.shading.texturing;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import orion.sdk.math.IFloatMatrix;
import orion.sdk.monitoring.incidents.Incident;
import orion.sdk.monitoring.incidents.IncidentManager;

public class Sprite extends Texture
{
	public List<Tile> tiles = new ArrayList<Tile>();
	
	public Sprite(String name, InputStream inputStream) throws IOException
	{
		super(name, inputStream);
	}	

	public Sprite(String name, BufferedImage image) throws IOException		
	{
		super(name, EFormat.RGBA, ETarget.TEXTURE_2D, image);
	}
	
	public static class Tile
	{
		public float x;
		public float y;
		public float w;
		public float h;
		public IFloatMatrix uv00;
		public IFloatMatrix uv10;
		public IFloatMatrix uv11;
		public IFloatMatrix uv01;
		
		public Tile(
				float x, float y, float w, float h,
				IFloatMatrix uv00, IFloatMatrix uv10, IFloatMatrix uv11, IFloatMatrix uv01
				)
		{
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
			this.uv00 = uv00;
			this.uv10 = uv10;
			this.uv11 = uv11;
			this.uv01 = uv01;
		}
		
		@Override
		public Tile clone() throws CloneNotSupportedException
		{
			return new Tile(x, y, w, h, uv00.clone(), uv10.clone(), uv11.clone(), uv01.clone());
		}
	}
	
	@Override
	public Sprite clone() throws CloneNotSupportedException
	{
		Sprite clone;
		try
		{
			clone = new Sprite(getName(), images[0]);
			for (Tile tile : tiles)
			{
				clone.tiles.add(tile.clone());
			}
			return clone;
		} catch (IOException e)
		{
			IncidentManager.notifyIncident(Incident.newError("Failed to clone sprite", e.getStackTrace()));
			return this;
		}
	}	
}
