package orion.sdk.assets.loaders;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import orion.sdk.assets.io.IAssetSource;
import orion.sdk.graphics.drawables.surfaces.Mesh;
import orion.sdk.math.FloatMatrix;

public class ObjMeshLoader extends AAssetLoader
{
	private static int[] parseIndexes(String[] indexes)
	{
		int[] values = new int[indexes.length];
		for (int i = 0; i < indexes.length; i++)
		{
			try
			{
				values[i] = Integer.parseInt(indexes[i]) - 1;
			}
			catch (NumberFormatException e)
			{
				values[i] = -1;
			}
		}
		return values;
	}

	@Override
	public Object load(IAssetSource source, String assetPath) throws Exception
	{
		
		BufferedReader reader = null;
		try
		{
			List<FloatMatrix> positions = new ArrayList<FloatMatrix>();
			List<FloatMatrix> textures = new ArrayList<FloatMatrix>();
			List<FloatMatrix> normals = new ArrayList<FloatMatrix>();
			List<List<int[]>> faces = new ArrayList<List<int[]>>();
			
			source.open();
			InputStream inputStream = source.getInputStream();
			
			if (inputStream == null)
			{
				throw new Exception("Asset not found: " + assetPath);
			}
					
			reader = new BufferedReader(new InputStreamReader(inputStream));
			String line = null;
			int faceType = -1;
			do
			{
				line = reader.readLine();
				if (line != null)
				{
					line = line.trim();
					if (line.startsWith("#") || line.length() == 0)
					{
						// Skip comment lines
						continue;
					}
					else
					{
						String[] args = line.split(" ");
						if (args[0].equalsIgnoreCase("v"))
						{
							positions.add(FloatMatrix.vector(
								Float.parseFloat(args[1]),
								Float.parseFloat(args[2]),
								Float.parseFloat(args[3]),
								1));
						}
						else if (args[0].equalsIgnoreCase("vt"))
						{
							textures.add(FloatMatrix.vector(
								Float.parseFloat(args[1]),
								Float.parseFloat(args[2])));
						}
						else if (args[0].equalsIgnoreCase("vn"))
						{
							normals.add(FloatMatrix.vector(
								Float.parseFloat(args[1]),
								Float.parseFloat(args[2]),
								Float.parseFloat(args[3])));
						}
						else if (args[0].equalsIgnoreCase("f"))
						{
							if (faceType == -1)
							{
								switch (args.length - 1)
								{
									case 3:
										faceType = Mesh.FaceType.TRIANGLE;
										break;
									case 4:
										faceType = Mesh.FaceType.QUAD;
										break;
								}
							}
							List<int[]> face = new ArrayList<int[]>();
							for (int i = 1; i < args.length; i++)
							{
								int[] indexes = parseIndexes(args[i].split("/"));
								face.add(indexes);
							}
							faces.add(face);
						}
					}
				}
			} while (line != null);
			
			Mesh mesh = new Mesh(faces.size(), faceType, null);
			for (int i = 0; i < faces.size(); i++)
			{
				List<int[]> face = faces.get(i);
				int start = i * face.size();
				for (int vertex = 0; vertex < face.size(); vertex++)
				{					
					int[] data = face.get(vertex);
					int 
						position	= data[0],
						texture	= data[1],
						normal	= data[2];
					if (texture != -1)
					{
						mesh.setVertex(
								start + vertex,
								positions.get(position),
								normals.get(normal),
								textures.get(texture));
					}
					else if (normal != -1)
					{
						mesh.setVertex(
								start + vertex,
								positions.get(position),
								normals.get(normal));
					}
					else
					{
						mesh.setVertex(
								start + vertex,
								positions.get(position));
					}
				}
				if (faceType == Mesh.FaceType.TRIANGLE)
				{
					mesh.setFace(i, new int[] {start + 0, start + 1, start + 2});
				}
				else if (faceType == Mesh.FaceType.QUAD)
				{
					mesh.setFace(i, new int[] {start + 0, start + 1, start + 2, start + 3});
				}
			}
			return mesh;
		} 
		finally
		{
			if (reader != null)
			{
				reader.close();				
			}
			if (source != null)
			{
				source.close();
			}
		}
		
	}
}
