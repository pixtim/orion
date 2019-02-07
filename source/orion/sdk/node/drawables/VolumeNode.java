package orion.sdk.node.drawables;

import java.util.LinkedList;
import java.util.List;

import orion.sdk.graphics.drawables.IDrawable;
import orion.sdk.graphics.drawables.volumes.VolumeChunk;
import orion.sdk.graphics.shading.glsl.AShader;
import orion.sdk.graphics.shading.lighting.Material;

public class VolumeNode extends ShadedNode
{
	public VolumeNode(
		String name,
		IDrawable drawable,
		AShader shader,
		Material material)
	{
		super(name, drawable, shader, material);
	}

	protected List<VolumeChunk> chunks = new LinkedList<VolumeChunk>();

	public List<VolumeChunk> getChunks()
	{
		return this.chunks;
	}
}
