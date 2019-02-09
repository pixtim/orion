package orion.sdk.assets;

import java.util.HashMap;
import java.util.Map;

import orion.sdk.assets.io.AAssetSourceFactory;
import orion.sdk.assets.io.IAssetSource;
import orion.sdk.assets.loaders.ICompoundAssetProcessor;
import orion.sdk.monitoring.incidents.Incident;
import orion.sdk.monitoring.incidents.IncidentManager;

public class CompoundAssetRequest extends AssetRequest implements IAssetListener
{
	protected Map<String, Object> parts = new HashMap<String, Object>();
	protected String[] partPaths = new String[0];
	protected ICompoundAssetProcessor compoundAssetProcessor = null;
	protected AAssetSourceFactory sourceFactory = null;
	protected EAssetType[] partTypes = null;

	/**
	 * Constructs a new compound asset request and requests the asset manager for each asset path.
	 * @param partPaths
	 * 	An array of paths - one for each asset.
	 * @param partTypes
	 * 	An array of corresponding asset types.
	 * @param sourceFactory
	 * 	A factory that creates asset sources from the provided asset paths.
	 * @param compoundAssetType
	 * 	The type of compound asset that is being requested.
	 * @param compoundProcessor
	 * 	A compound asset processor that will combine the asset parts into	a single compounded 
	 * 	asset.
	 * @param requesters
	 * 	An array of objects that should be notified when the compound asset is loaded.
	 */
	public CompoundAssetRequest(
		String compoundPath,
		String[] partPaths,
		EAssetType[] partTypes,
		AAssetSourceFactory sourceFactory,
		EAssetType compoundAssetType,
		ICompoundAssetProcessor compoundProcessor,
		IAssetListener... requesters)
	{
		super(compoundPath, compoundAssetType, null, requesters);
		this.partPaths = partPaths;
		this.partTypes = partTypes;
		this.compoundAssetProcessor = compoundProcessor;
		this.sourceFactory = sourceFactory;
	}
	
	public void queueParts()
	{
		for (int i = 0; i < partPaths.length; i++)
		{
			IAssetSource source = sourceFactory.create(partPaths[i]);
			AssetManager.requestAsset(partPaths[i], partTypes[i], source, this);
		}		
	}

	@Override
	public void notifyAssetAvailable(IAssetSource source, AssetRequest request)
	{
		try
		{
			String assetPath = request.getAssetPath();
			Object asset;
			asset = AssetManager.getAsset(assetPath);
			parts.put(assetPath, asset);			
		} 
		catch (Exception e)
		{
			IncidentManager.notifyIncident(Incident.newError("Asset was notified but it is unavailable", e));
		}
			
		/*
		 * Check if all the parts are loaded yet
		 */
		boolean allLoaded = true;
		for (String partPath : partPaths)
		{
			if (!parts.containsKey(partPath))
			{
				allLoaded = false;
				break;
			}
		}
		
		/*
		 * Process the compound asset and store the result in the asset manager's
		 * memory. Notify all asset manager listeners that the compound asset is
		 * available if all the parts have been loaded
		 */
		if (allLoaded)
		{
			try
			{
				Object compoundAsset = compoundAssetProcessor.process(partPaths);
				AssetManager.storeAsset(getAssetPath(), compoundAsset);
				notifyAvailable();
			} 
			catch (Exception e)
			{
				IncidentManager.notifyIncident(Incident.newError("Failled to compound asset parts", e));
				notifyNotAvailable(e);
			}			
		}
	}

	@Override
	public void notifyAssetNotAvailable(IAssetSource source, AssetRequest request, Exception e)
	{
		/*
		 * Notify all asset manager listeners that the compound asset is not
		 * available because one of the parts could not be loaded
		 */
		if (!parts.containsKey(request.getAssetPath()))
		{
			notifyNotAvailable(e);
		}
	}
}
