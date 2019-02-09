package orion.sdk.assets.io;

import java.io.InputStream;

public class ClasspathAssetSource implements IAssetSource
{
	public static String ROOT = "";
	public String name = null;
	public InputStream inputStream = null;
	
	public ClasspathAssetSource(String name)
	{
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public InputStream getInputStream() throws Exception
	{
		return inputStream;
	}

	@Override
	public void open() throws Exception
	{
		this.inputStream = getClass().getClassLoader().getResourceAsStream(name); 
	}

	@Override
	public void close() throws Exception
	{
		if (inputStream != null)
		{
			inputStream.close();
		}		
	}
	
	@Override
	public String toString()
	{
		return "[File source: " + getName() + "]";
	}
}
