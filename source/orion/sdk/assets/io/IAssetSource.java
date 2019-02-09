package orion.sdk.assets.io;

import java.io.InputStream;

public interface IAssetSource
{
	public InputStream getInputStream() throws Exception;
	
	public void open() throws Exception;
	
	public void close() throws Exception;
}
