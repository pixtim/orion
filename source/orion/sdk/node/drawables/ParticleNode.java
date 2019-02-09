package orion.sdk.node.drawables;

import java.util.ArrayList;
import java.util.List;

import orion.sdk.graphics.buffers.VertexFormatter;
import orion.sdk.graphics.drawables.particles.Particle;
import orion.sdk.graphics.drawables.particles.ParticleSystem;
import orion.sdk.graphics.shading.glsl.GenericShader;
import orion.sdk.graphics.util.OpenGLStack;
import orion.sdk.graphics.viewing.cameras.ACamera;
import orion.sdk.math.geometry.Box;

public class ParticleNode extends ShadedNode
{
	private List<Particle> particles = new ArrayList<Particle>();

	public ParticleNode(
		String name, 
		GenericShader shader,
		Box bounds)
	{
		super(name, null, shader, null);
	}
	
	public void clear()
	{
		particles.clear();
	}
	
	public void addParticle(Particle particle)
	{
		particles.add(particle);
	}
	
	public List<Particle> getParticles()
	{
		return particles;
	}
	
	public void generate() throws Exception
	{	
		ACamera camera = (ACamera) OpenGLStack.peek(ACamera.class);
		ParticleSystem particleSystem = new ParticleSystem(particles.size(), camera, null);
		particleSystem.setVertexFormatter(new VertexFormatter("particle mesh formatter", (GenericShader) this.getShader()));
		for (int i = 0; i < particles.size(); i++)
		{
			particleSystem.particles[i] = particles.get(i);
		}
		particleSystem.setFaces();
		setDrawable(particleSystem);
	}
}
