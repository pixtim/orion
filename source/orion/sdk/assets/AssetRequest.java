package orion.sdk.assets;

import orion.sdk.assets.io.IAssetSource;
import orion.sdk.graphics.util.INamed;
import orion.sdk.monitoring.incidents.Incident;
import orion.sdk.monitoring.incidents.IncidentManager;

public class AssetRequest implements INamed
{
	private String assetPath = null;
	private IAssetSource source = null;
	private EAssetType assetType = EAssetType.NONE;
	private IAssetListener[] requesters = null;

	public AssetRequest(
		String assetPath,
		EAssetType assetType,
		IAssetSource assetSource,
		IAssetListener... requesters)
	{
		this.setAssetPath(assetPath);
		this.setAssetType(assetType);
		this.setSource(assetSource);
		this.setRequesters(requesters);
	}
	
	@Override
	public String getName()
	{
		return this.getAssetPath();
	}

	public void notifyAvailable()
	{
		IncidentManager.notifyIncident(Incident.newInformation("Asset available: '" + getAssetPath() + "'"));
		synchronized (getRequesters())
		{
			for (IAssetListener requester : getRequesters())
			{
				requester.notifyAssetAvailable(getSource(), this);
			}
		}
	}

	public void notifyNotAvailable(Exception e)
	{
		synchronized (getRequesters())
		{
			for (IAssetListener requester : getRequesters())
			{
				requester.notifyAssetNotAvailable(getSource(), this, e);
			}
		}
	}

	public String getAssetPath()
	{
		return assetPath;
	}

	public void setAssetPath(String assetPath)
	{
		this.assetPath = assetPath;
	}

	public IAssetSource getSource()
	{
		return source;
	}

	public void setSource(IAssetSource source)
	{
		this.source = source;
	}

	public EAssetType getAssetType()
	{
		return assetType;
	}

	public void setAssetType(EAssetType assetType)
	{
		this.assetType = assetType;
	}

	public IAssetListener[] getRequesters()
	{
		return requesters;
	}

	public void setRequesters(IAssetListener[] requesters)
	{
		this.requesters = requesters;
	}
}
