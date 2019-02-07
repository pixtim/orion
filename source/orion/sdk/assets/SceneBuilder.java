package orion.sdk.assets;

import java.util.Map;

import orion.sdk.assets.io.IAssetSource;
import orion.sdk.graphics.drawables.surfaces.Mesh;
import orion.sdk.graphics.scenes.Scene;
import orion.sdk.graphics.shading.texturing.Sprite;
import orion.sdk.graphics.shading.texturing.Texture;
import orion.sdk.monitoring.incidents.Incident;
import orion.sdk.monitoring.incidents.IncidentManager;

public class SceneBuilder implements IAssetListener
{
	private Scene scene = null;

	public SceneBuilder(Scene scene)
	{
		this.scene = scene;
	}

	public Scene getScene()
	{
		return scene;
	}

	public void setScene(Scene scene)
	{
		this.scene = scene;
	}

	public void onTextureAvailable(String path, Texture texture) throws Exception
	{
	}

	public void onMeshAvailable(String path, Mesh mesh) throws Exception
	{
	}

	public void onTextureMapAvailable(String path, Map<String, Texture> map) throws Exception
	{
	}

	public void onSpriteAvailable(String path, Sprite sprite) throws Exception
	{
	}

	@Override
	public void notifyAssetAvailable(IAssetSource source, AssetRequest request)
	{
		try
		{
			EAssetType assetType = request.getAssetType();
			String path = request.getAssetPath();
			switch (assetType)
			{
			case NONE:
				break;
			case TEXTURE_SINGLE:
				onTextureAvailable(path, AssetManager.getTexture(path));
				break;
			case MESH_OBJ:
				onMeshAvailable(path, AssetManager.getMesh(path));
				break;
			case TEXTURE_MAP:
				onTextureMapAvailable(path, AssetManager.getTextureMap(path));
				break;
			case TEXTURE_SPRITE:
				onSpriteAvailable(path, AssetManager.getSprite(path));
				break;
			default:
				break;
			}
		}
		catch (Exception e)
		{
			IncidentManager.notifyIncident(Incident.newError(
					"Failed to notify scene builder of new asset", e));
		}
	}

	@Override
	public void notifyAssetNotAvailable(IAssetSource source, AssetRequest request, Exception e)
	{
		EAssetType assetType = request.getAssetType();
		String path = request.getAssetPath();
		
		IncidentManager.notifyIncident(Incident.newError("Asset not available: " + path + " (" + assetType.toString() + ")" , e));

	}

}
