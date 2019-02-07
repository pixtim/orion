package orion.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

public class CamelCase
{
	public static void processFile(File file) throws IOException
	{
		if (!file.toString().contains("CamelCase"))
		{
			if (file.isDirectory())
			{
				File[] children = file.listFiles();
				for (File child : children)
				{
					processFile(child);
				}
			}
			else
			{
				BufferedReader reader = new BufferedReader(new FileReader(file));
				String line = reader.readLine();
				List<String> lines = new LinkedList<String>();
				while (line != null)
				{
					lines.add(line);
					line = reader.readLine();				
				}
				reader.close();
				
				PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file)));
				for (String next : lines)
				{
					writer.println(processLine(next));
				}
				writer.flush();
				writer.close();
			}
		}
	}
	
	public static boolean isLower(String string)
	{
		return string.equals(string.toLowerCase());
	}
	
	public static String toCamelCase(String word)
	{
		StringBuilder result = new StringBuilder();
		boolean nextCaps = false;
		char[] chars = word.toCharArray();
		for (char next : chars)
		{
			if (next != '_')
			{
				if (nextCaps)
				{
					result.append(Character.toUpperCase(next));
				}
				else
				{
					result.append(next);
				}
				
				nextCaps = false;
			}
			else
			{
				nextCaps = true;
			}
		}
		return result.toString();
	}
	
	public static List<String> split(String string, char[] seperators)
	{
		List<String> result = new LinkedList<String>();
		char[] chars = string.toCharArray();
		
		StringBuilder word = new StringBuilder();
		for (char next : chars)
		{
			boolean skip = false;
			for (char seperator : seperators)
			{
				if (next == seperator)
				{
					result.add(word.toString());
					result.add(Character.toString(next));
					word = new StringBuilder();
					skip = true;
					break;
				}
			}
			if (skip)
			{
				continue;
			}
			else
			{
				word.append(next);
			}
		}
		if (word.length() > 0)
		{
			result.add(word.toString());
		}
		return result;
	}
	
	public static String replaceAll(String string, String target, String replacement)
	{
		StringBuilder result = new StringBuilder();
		
		int consumed = 0;
		int last = 0;
		
		for (int i = 0; i <= string.length() - target.length(); i++)
		{
			String leading = string.substring(last, i);
			String current = string.substring(i, i + target.length());
			if (current.equals(target))
			{				
				result.append(leading);
				result.append(replacement);
				last = i + target.length();
				i = last;
				consumed += leading.length() + replacement.length();
			}
		}
		
		String remainder = string.substring(consumed + 1);
		result.append(remainder);
		return result.toString();
	};
	
	public static String processLine(String line)
	{		
		List<String> words = split(line, new char[] {' ', '.', '(', ')', '[', ']', '*', '+', '-', '/'});
		StringBuilder newLine = new StringBuilder();
		for (int i = 0; i < words.size(); i++)
		{
			String word = words.get(i);
			if (isLower(word) && word.contains("_") && 
				word.length() > 0)
			{
				newLine.append(toCamelCase(word));
			}
			else
			{
				newLine.append(word);
			}
		}
		return newLine.toString();
	}
	
	public static void main(String[] args) throws IOException
	{
		processFile(new File("source"));
		processFile(new File("tests"));
	}

}
