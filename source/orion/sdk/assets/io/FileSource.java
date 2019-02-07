package orion.sdk.assets.io;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class FileSource implements IAssetSource
{
	public static String ROOT = "";
	public String filename = null;
	public InputStream inputStream = null;
	
	public FileSource(String filename)
	{
		this.filename = filename;
	}
	
	@Override
	public InputStream getSource() throws Exception
	{
		return inputStream;
	}
	
	protected String getFilename()
	{
		File file = new File(ROOT + filename);
		return file.getAbsolutePath();
	}

	@Override
	public void open() throws Exception
	{
		inputStream = new BufferedInputStream(new FileInputStream(getFilename()));
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
		return "[File source: " + getFilename() + "]";
	}
}
