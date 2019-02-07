package orion.sdk.assets.io;

public interface IAssetSource
{
	public Object getSource() throws Exception;
	
	public void open() throws Exception;;
	
	public void close() throws Exception;;
}
