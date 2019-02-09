package orion.sdk.util;

import java.io.File;
import java.util.Stack;

public class Shell
{
	public static void makeDirectories(File file) throws Exception
	{
		
		Stack<File> directories = new Stack<File>();
		File nextDirectory = file;
		do
		{
			directories.push(nextDirectory);
			nextDirectory = nextDirectory.getParentFile(); 
		} while (nextDirectory != null);
		
		while (!directories.isEmpty())
		{
			File directory = directories.pop();
			
			int tries = 10;
			while (!directory.exists())
			{
				if (tries <= 0)
				{
					throw new Exception("Failed to make directories for '" + file + "'");
				}

				directory.mkdir();
				Thread.sleep(200);
				tries--;
			}
		}
	}
}
