package orion.sdk.assets;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import orion.sdk.assets.io.AAssetSourceFactory;
import orion.sdk.assets.io.IAssetSource;
import orion.sdk.assets.loaders.AAssetLoader;
import orion.sdk.assets.loaders.ICompoundAssetProcessor;
import orion.sdk.assets.loaders.ObjMeshLoader;
import orion.sdk.assets.loaders.TextureLoader;
import orion.sdk.events.IUpdatable;
import orion.sdk.events.Processor;
import orion.sdk.graphics.drawables.surfaces.Mesh;
import orion.sdk.graphics.shading.texturing.Sprite;
import orion.sdk.graphics.shading.texturing.Texture;
import orion.sdk.graphics.util.INamed;
import orion.sdk.graphics.util.OpenGLManager;

public class AssetManager implements INamed
{
	protected static AssetManager instance = null;
	
	protected Map<String, Object> cache = new HashMap<String, Object>();
	protected Queue<AssetRequest> requests = new LinkedList<AssetRequest>();
	protected Map<EAssetType, AAssetLoader> loaders = new HashMap<EAssetType, AAssetLoader>();
	
	protected Processor requestProcessor = new Processor("asset request processor");
	
	protected AssetManager()
	{
		requestProcessor.updatables.add(new RequestProcessor());
		initiateLoaders();
		requestProcessor.start();
	}
	
	protected static AssetManager getInstance()
	{
		if (instance == null)
		{
			instance	= new AssetManager();
		}
		return instance;
	}
	
	protected void initiateLoaders()
	{
		loaders.put(EAssetType.MESH_OBJ, new ObjMeshLoader());
		loaders.put(EAssetType.TEXTURE_SINGLE, new TextureLoader());
	}
	
	public static void storeAsset(String assetPath, Object asset)
	{
		AssetManager assetManager = getInstance();
		assetManager.cache.put(assetPath, asset);
	}
	
	public static void deleteAsset(String assetPath)
	{
		AssetManager assetManager = getInstance();
		if (assetManager.cache.containsKey(assetPath))
		{
			assetManager.cache.remove(assetPath);
		}
	}

	public static void requestAsset(
			String assetPath, 
			EAssetType assetType, 
			IAssetSource source, 
			IAssetListener... requesters)
	{
		AssetManager assetManager = getInstance();
		AssetRequest request = new AssetRequest(assetPath, assetType, source, requesters);
		if (assetManager.cache.containsKey(assetPath))
		{
			OpenGLManager.getInstance().pushDebug("Asset manager cache hit", request);
			request.notifyAvailable();		
			OpenGLManager.getInstance().popDebug();
		}
		else
		{
			OpenGLManager.getInstance().pushDebug("Asset manager cache miss, queueing", request);
			assetManager.requests.add(request);
			OpenGLManager.getInstance().popDebug();
		}
	}
	
	public static void requestCompoundAsset(
			String compoundPath,
			String[] partPaths,
			EAssetType[] partTypes,
			AAssetSourceFactory sourceFactory,
			EAssetType compoundAssetType,
			ICompoundAssetProcessor compoundProcessor,
			IAssetListener... requesters)
	{
		CompoundAssetRequest request = new CompoundAssetRequest(
				compoundPath,
				partPaths, 
				partTypes,
				sourceFactory,
				compoundAssetType, 
				compoundProcessor, 
				requesters);
		request.queueParts();
	}
	
	
	public static void requestCompoundAsset(
			String compoundPath,
			String[] partPaths,
			EAssetType partCommonType,
			AAssetSourceFactory sourceFactory,
			EAssetType compoundAssetType,
			ICompoundAssetProcessor compoundProcessor,
			IAssetListener... requesters)
	{
		EAssetType[] partTypes = new EAssetType[partPaths.length];
		for (int i = 0; i < partPaths.length; i++)
		{
			partTypes[i] = partCommonType;
		}
		requestCompoundAsset(
			compoundPath,
			partPaths, 
			partTypes, 
			sourceFactory,
			compoundAssetType,
			compoundProcessor,
			requesters);
	}
	
	public static void clearCache()
	{
		OpenGLManager.getInstance().pushDebug("Clearing cache", AssetManager.getInstance());
		getInstance().cache.clear();
		OpenGLManager.getInstance().popDebug();
	}

	public void release() throws Exception
	{
		requestProcessor.stop();
		cache.clear();
		requests.clear();
		instance = null;
	}
	
	public static Object getAsset(String path) throws Exception
	{
		checkAsset(path);
		return getInstance().cache.get(path);
	}
	
	public static Mesh getMesh(String path) throws Exception
	{		
		return (Mesh) getAsset(path);
	}
	
	public static Sprite getSprite(String path) throws Exception
	{
		return getSprite(path, 0, true);
	}
	
	public static Sprite getSprite(String path, int layer, boolean linear) throws Exception
	{
		Sprite sprite = (Sprite) getAsset(path);
		sprite.linear = linear;
		sprite.name = path;
		return sprite;
	}
	
	public static Texture getTexture(String path, int layer, boolean linear) throws Exception
	{		
		Texture texture = (Texture) getAsset(path);
		texture.linear = linear;
		texture.name = path;
		return texture;
	}
	
	
	public static Map<String, Texture> getTextureMap(String path) throws Exception
	{		
		Map<String, Texture> textureMap = (Map<String, Texture>) getAsset(path);
		return textureMap;
	}

	public static Texture getTexture(String path) throws Exception
	{
		return getTexture(path, 0, true);
	}
	
	protected static void checkAsset(String path) throws Exception
	{
		if (!getInstance().cache.containsKey(path))
		{
			throw new Exception("Asset '" + path + "' not loaded.");
		}
	}

	protected class RequestProcessor implements IUpdatable
	{

		@Override
		public void update(float dt) throws Exception
		{
			if (shouldUpdate())
			{
				synchronized (requests)
				{
					if (!requests.isEmpty())
					{
						AssetRequest assetRequest = requests.remove();
						if (!(assetRequest instanceof CompoundAssetRequest))
						{
							try
							{
								EAssetType assetType = assetRequest.getAssetType();
								if (loaders.containsKey(assetType))
								{
									AAssetLoader loader = loaders.get(assetType);
									Object asset = loader.load(assetRequest.getSource(), assetRequest.getAssetPath());
									cache.put(assetRequest.getAssetPath(), asset);
								}
								assetRequest.notifyAvailable();
							}
							catch (Exception e)
							{
								assetRequest.notifyNotAvailable(e);
							}
						}
					}
				}
			}
		}

		@Override
		public float getUpdatePeriod()
		{
			return 0;
		}

		@Override
		public boolean shouldUpdate()
		{
			return true;
		}		
	}

	@Override
	public String getName()
	{
		return "Asset manager";
	}
}
