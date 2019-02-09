package orion.sdk.graphics.shading.glsl;

import java.util.List;

import com.jogamp.opengl.GLException;

import orion.sdk.graphics.drawables.volumes.VolumeChunk;
import orion.sdk.graphics.shading.lighting.Light;
import orion.sdk.graphics.shading.lighting.Material;
import orion.sdk.graphics.shading.texturing.Texture.ETextureType;
import orion.sdk.graphics.util.OpenGLContext;
import orion.sdk.graphics.util.OpenGLManager;
import orion.sdk.graphics.util.OpenGLStack;
import orion.sdk.graphics.viewing.cameras.ACamera;
import orion.sdk.math.FloatMatrix;
import orion.sdk.math.IFloatMatrix;
import orion.sdk.math.geometry.Box;
import orion.sdk.node.Node;
import orion.sdk.node.drawables.EntityNode;
import orion.sdk.node.drawables.LightNode;
import orion.sdk.node.drawables.VolumeNode;

/**
 * Provides a generic shader implementation with customizable capabilities.
 * 
 * @author Tim
 *
 */
public class GenericShader extends AShader
{
	public static final int MAX_LIGHTS = 10;
	public static final int MAX_VOLUME_CHUNKS = 64;
	
	protected Capabilities capabilities = null;
	protected Node rootNode = null;
	
	/**
	 * Constructs a generic shader.
	 */
	public GenericShader(String name, Node rootNode, Capabilities capabilities)
	{
		super(name);
		this.capabilities = capabilities;
		this.rootNode = rootNode;
	}

	protected Light[] getLights() throws Exception
	{
		Light[] lights = new Light[MAX_LIGHTS];		
		
		if (this.rootNode != null)
		{
			List<Node> lightNodes = this.rootNode.findNodes(LightNode.class);			
			int index = 0;
			
			for (Node node : lightNodes)
			{
				LightNode lightNode = (LightNode) node;
				lights[index] = lightNode.getLight();
				index++;
			}
		}
		
		return lights;		
	}

	protected VolumeChunk[] getVolumeChunks() throws Exception
	{
		VolumeChunk[] volumeChunks = new VolumeChunk[MAX_VOLUME_CHUNKS];		
		
		if (this.rootNode != null)
		{
			List<Node> volumeNodes = this.rootNode.findNodes(VolumeNode.class);
			int index = 0;
			
			for (Node node : volumeNodes)
			{
				VolumeNode volumeNode = (VolumeNode) node;
				List<VolumeChunk> volumeChunkList = volumeNode.getChunks();
				
				for (VolumeChunk volumeChunk : volumeChunkList)
				{
					volumeChunks[index] = volumeChunk;
					index++;
				}
			}
		}
		
		return volumeChunks;		
	}
	
	public static int getPositionLocation()
	{
		return 0;
	}
	
	public static int getNormalLocation()
	{
		return 1;
	}
	
	public static int getTextureLocation()
	{
		return 2;
	}
	
	public static int getColorLocation()
	{
		return 3;
	}
	
	public static int getCenterLocation()
	{
		return 4;
	}
	
	@Override
	protected void bindAttributes(OpenGLContext c) throws GLException
	{
		OpenGLManager.getInstance().checkError(c, this);
		if (hasPosition())
		{
			c.gl().glBindAttribLocation(shaderProgram, getPositionLocation(), "position");
		}		
		if (hasNormal())
		{
			c.gl().glBindAttribLocation(shaderProgram, getNormalLocation(), "normal");
		}		
		if (hasTexture())
		{
			c.gl().glBindAttribLocation(shaderProgram, getTextureLocation(), "texture");
		}		
		if (hasColor())
		{
			c.gl().glBindAttribLocation(shaderProgram, getColorLocation(), "color");
		}		
		if (hasCenter())
		{
			c.gl().glBindAttribLocation(shaderProgram, getCenterLocation(), "center");
		}		
		OpenGLManager.getInstance().checkError(c, this);		
	}
	
	protected Material getMaterial()
	{
		Material material = (Material) OpenGLStack.peek(Material.class);
		
		return material;
	}
	
	@Override
	protected void uploadUniforms(OpenGLContext c) throws GLException
	{
		try
		{
			/*
			 * Upload material and textures
			 */
			Material material = this.getMaterial();

			if (material != null)
			{
				if (capabilities.mapAlpha && material.getTexture(ETextureType.ALPHA) != null)
				{				
					setUniform(c, "alphaMap", material.getTexture(ETextureType.ALPHA).getLayer(), true);
				}
				
				if (capabilities.mapAmbient && material.getTexture(ETextureType.AMBIENT) != null)
				{				
					setUniform(c, "ambientMap", material.getTexture(ETextureType.AMBIENT).getLayer(), true);
				}
				
				if (capabilities.mapDiffuse && material.getTexture(ETextureType.DIFFUSE) != null)
				{
					setUniform(c, "diffuseMap", material.getTexture(ETextureType.DIFFUSE).getLayer(), true);
				}
				
				if (capabilities.mapSpecular && material.getTexture(ETextureType.SPECULAR) != null)
				{
					setUniform(c, "specularMap", material.getTexture(ETextureType.SPECULAR).getLayer(), true);
				}
				
				if (capabilities.mapNormal && material.getTexture(ETextureType.NORMAL) != null)
				{
					setUniform(c, "normalMap", material.getTexture(ETextureType.NORMAL).getLayer(), true);
				}
			}
			
			/*
			 * Upload lights
			 */
			Light[] lights = this.getLights();
			
			for (int i = 0; i < MAX_LIGHTS; i++)
			{
				Light light = lights[i];
				
				if (light != null)
				{
					setUniform(c, "lights[" + i + "].position", light.position, true);
					setUniform(c, "lights[" + i + "].diffuse", light.diffuse, true);
					setUniform(c, "lights[" + i + "].specular", light.specular, true);
					setUniform(c, "lights[" + i + "].enabled", 1.0f, true);
				}
				else
				{
					setUniform(c, "lights[" + i + "].enabled", 0.0f, true);
				}				
			}
			
			/*
			 * Upload volume chunks
			 */
			if (capabilities.flagVolume)
			{
				VolumeChunk[] volumeChunks = this.getVolumeChunks();
				
				for (int i = 0; i < MAX_VOLUME_CHUNKS; i++)
				{
					VolumeChunk volumeChunk = i < volumeChunks.length ? volumeChunks[i] : null;
					
					if (volumeChunk != null)
					{
						Box posBounds = volumeChunk.getPositionBounds();
						Box texBounds = volumeChunk.getTextureBounds();
						setUniform(c, "volumeChunks[" + i + "].posStart", posBounds.getStart(), true);
						setUniform(c, "volumeChunks[" + i + "].posEnd", posBounds.getEnd(), true);
						setUniform(c, "volumeChunks[" + i + "].texStart", texBounds.getStart(), true);
						setUniform(c, "volumeChunks[" + i + "].texEnd", texBounds.getEnd(), true);
						setUniform(c, "volumeChunks[" + i + "].enabled", 1.0f, true);			
					}
					else
					{
						setUniform(c, "volumeChunks[" + i + "].enabled", 0.0f, true);
					}				
				}
			}
			
			/*
			 * Upload vertex, normal and texture transformations
			 */
			Node node = (Node) OpenGLStack.peek(Node.class);			
			if (node != null)
			{
				if (node instanceof EntityNode)
				{		
					EntityNode entityNode = (EntityNode) node;
					
					IFloatMatrix vertexTransform = entityNode.getVertexTransformation();
					setUniform(c, "vertexTransform", vertexTransform, false);
					
					IFloatMatrix normalTransform = entityNode.getNormalTransformation();
					setUniform(c, "normalTransform", normalTransform, false);
				}

			}
			
			/*
			 * Upload the camera position and frustum
			 */
			ACamera camera = (ACamera) OpenGLStack.peek(ACamera.class);
			if (camera != null)
			{
				IFloatMatrix cam = camera.getObserver();
				setUniform(c, "cam", cam, true);
				
				if (capabilities.flagVolume)
				{
					IFloatMatrix[] frustum = camera.getFrustum(c);
					
					for (int i = 0; i < 8; i++)
					{
						setUniform(c, "frustum" + i, frustum[i], true);
					}
				}
			}
		}
		catch (Exception e)
		{
			throw new GLException("Failed to upload shader data", e);
		}		
	}
	
	public boolean hasPosition()
	{
		return true;
	}

	public boolean hasNormal()
	{
		return true;
	}

	public boolean hasTexture()
	{
		return true;
	}

	public boolean hasColor()
	{
		return true;
	}

	public boolean hasCenter()
	{
		return capabilities.attribCenter;
	}	
	
	@Override
	public String getVertexSource()
	{
		return 
			TB0 + "#version 120" + NL +
			TB0 + NL +
			
			TB0 + "uniform mat4 vertexTransform;" + NL +
			TB0 + "uniform mat3 normalTransform;" + NL +
			TB0 + NL +
			
			TB0 + "attribute vec4 position;" + NL +
			TB0 + "attribute vec3 normal;" + NL +
			TB0 + NL +			
			TB0 + "attribute " + Snippet.MapDim.getGLSL(capabilities) + " texture;" + NL +
			TB0 + "attribute vec4 color;" + NL +
			TB0 + "attribute vec4 center;" + NL +
			TB0 + NL +
			
			TB0 + "varying vec4 pos;" + NL +
			TB0 + "varying vec3 nor;" + NL +
			TB0 + "varying vec2 uv;" + NL +
			TB0 + "varying vec4 col;" + NL +
			TB0 + NL +

			(capabilities.flagVolume
				?
					TB0 + "varying vec4 nextPos;" + NL +
					TB0 + "uniform vec4 frustum0;" + NL +
					TB0 + "uniform vec4 frustum1;" + NL +
					TB0 + "uniform vec4 frustum2;" + NL +
					TB0 + "uniform vec4 frustum3;" + NL +
					TB0 + "uniform vec4 frustum4;" + NL +
					TB0 + "uniform vec4 frustum5;" + NL +
					TB0 + "uniform vec4 frustum6;" + NL +
					TB0 + "uniform vec4 frustum7;" + NL					
				:
					""
			) + NL +
			
			Snippet.Random.getGLSL() +
			TB0 + NL +
			
			TB0 + "void main()" + NL + 
			TB0 + "{" + NL +
				
				(capabilities.attribCenter ?
					TB1 + "mat3 inv = transpose(mat3(gl_ModelViewMatrix));" + NL +
					TB1 + "vec3 corner = position.xyz;" + NL +
					TB1 + "vec3 mid = center.xyz;" + NL +
					TB1 + "corner = inv * corner;" + NL +
					TB1 + "corner = corner + mid;" + NL +						
					TB1 + "pos = vec4(corner.x, corner.y, corner.z, 1);" + NL 
				:
					TB1 + "pos = position;" + NL
				) +
				
				(capabilities.flagVolume ?
						
						TB1 + "vec4 nearTop = (1 - pos.x) * frustum0 + pos.x * frustum1;" + NL +
						TB1 + "vec4 nearBottom = (1 - pos.x) * frustum3 + pos.x * frustum2;" + NL +
						
						TB1 + "vec4 farTop = (1 - pos.x) * frustum4 + pos.x * frustum5;" + NL +
						TB1 + "vec4 farBottom = (1 - pos.x) * frustum7 + pos.x * frustum6;" + NL +	
						
						TB1 + "vec4 pNear = (1 - pos.y) * nearTop + pos.y * nearBottom;" + NL +
						TB1 + "vec4 pFar = (1 - pos.y) * farTop + pos.y * farBottom;" + NL +
						
						TB1 + "float farness = pos.z;" + NL +
						TB1 + "float nextFarness = min(pos.z + " + (1f / (float) capabilities.slabCount) + ", 1.0);" + NL +
						
						TB1 + "pos = (1 - farness) * pNear + (farness) * pFar;" + NL + 	
						TB1 + "nextPos = (1 - nextFarness) * pNear + (nextFarness) * pFar;" + NL 	
					:
						""
				) +
				
				TB1 + "gl_Position = gl_ProjectionMatrix * gl_ModelViewMatrix * pos;" + NL +				
				TB1 + "nor = normalize(normal);" + NL +
				TB1 + "col = color;" + NL +
				TB1 + "uv = texture.xy;" + NL +
			TB0 + "}" + NL;				
	}
	
	@Override
	public String getFragmentSource()
	{
		return
			TB0 + "struct Light" + NL +
			TB0 + "{" + NL +
				TB1 + "vec4 position;" + NL +
				TB1 + "vec4 diffuse;" + NL +
				TB1 + "vec4 specular;" + NL +
				TB1 + "float enabled;" + NL +
			TB0 + "};" + NL +
			TB0 + NL +
			
			(capabilities.flagVolume
				?
					TB0 + "struct VolumeChunk" + NL +
					TB0 + "{" + NL +
						TB1 + "vec3 posStart;" + NL +
						TB1 + "vec3 posEnd;" + NL +
						TB1 + "vec3 texStart;" + NL +
						TB1 + "vec3 texEnd;" + NL +
						TB1 + "float enabled;" + NL +
					TB0 + "};"
				:
					""
			) + NL +
				
			Snippet.Maps.getGLSL(capabilities) +
			TB0 + NL +
			
			(capabilities.flagVolume
				?
					TB0 + "varying vec4 nextPos;" + NL +
					TB0 + "uniform VolumeChunk volumeChunks[" + MAX_VOLUME_CHUNKS + "];" + NL
				:
					""
			) + NL +
			
			TB0 + "uniform Light lights[" + MAX_LIGHTS + "];" + NL +			
			TB0 + "uniform vec4 cam;" + NL +
			TB0 + NL +
			
			TB0 + "varying vec4 pos;" + NL +
			TB0 + "varying vec3 nor;" + NL +
			TB0 + "varying vec2 uv;" + NL +
			TB0 + "varying vec4 col;" + NL +
			TB0 + NL +
			
			Snippet.RemapUV.getGLSL() +
			TB0 + NL +
			
			Snippet.Random.getGLSL() +
			TB0 + NL +
			
			Snippet.PlaneMult.getGLSL() +
			TB0 + NL +
			
			Snippet.Vec4ToFloat.getGLSL() +
			TB0 + NL +
			
			Snippet.ApplyLight.getGLSL() +
			TB0 + NL +
			
			Snippet.LineAaBbIntersect.getGLSL() +
			TB0 + NL +
			
			Snippet.Shade.getGLSL(capabilities) +
			TB0 + NL +
			
			TB0 + "void main()" + NL +
			TB0 + "{" + NL +
				TB1 + "float alpha = 0;" + NL +
				TB1 + "vec3 color = vec3(0, 0, 0);" + NL +
				
				(capabilities.flagVolume
				? 
					TB1 + "for (int i = 0; i < " + MAX_VOLUME_CHUNKS + "; ++i)" + NL +
					TB1 + "{" + NL +
						TB2 + "if (volumeChunks[i].enabled > 0.0)" + NL +					
						TB2 + "{" + NL +			
						
							TB3 + "vec4 calcPos = pos;" + NL +
							TB3 + "vec3 posS = volumeChunks[i].posStart.xyz;" + NL +
							TB3 + "vec3 posE = volumeChunks[i].posEnd.xyz;" + NL +
							TB3 + "vec3 texS = volumeChunks[i].texStart.xyz;" + NL +
							TB3 + "vec3 texE = volumeChunks[i].texEnd.xyz;" + NL +
		
							TB3 + "vec3 texRatio = (calcPos.xyz - posS) / (posE - posS);" + NL +
							
							TB3 + "vec3 sampleTex = texS + texRatio * (texE - texS);" + NL + 
							TB3 + "sampleTex = remapUV(alphaMap, sampleTex);" + NL +
							
							TB3 + "float boundTest = 1;" + NL +
		
							
							TB3 + "boundTest = boundTest * planeMult(calcPos, vec3(posS.x - 1,0,0) , vec3(1,0,0));" + NL +
							TB3 + "boundTest = boundTest * planeMult(calcPos, vec3(posE.x + 1,0,0) , vec3(-1,0,0));" + NL +
		
							TB3 + "boundTest = boundTest * planeMult(calcPos, vec3(0,posS.y - 1,0) , vec3(0,1,0));" + NL +
							TB3 + "boundTest = boundTest * planeMult(calcPos, vec3(0,posE.y + 1,0) , vec3(0,-1,0));" + NL +
		
							TB3 + "boundTest = boundTest * planeMult(calcPos, vec3(0,0,posS.z - 1) , vec3(0,0,1));" + NL +
							TB3 + "boundTest = boundTest * planeMult(calcPos, vec3(0,0,posE.z + 1) , vec3(0,0,-1));" + NL +							
							
							
							TB3 + "if (boundTest > 0)" + NL +
							TB3 + "{" + NL +
								TB4 + "vec4 shaded = shade(calcPos, nor, color, 0f, sampleTex);" + NL +
								TB4 + "color.xyz = shaded.xyz;" + NL +
								TB4 + "alpha = min(alpha + shaded.w, 1.0);" + NL +
		
								
								(capabilities.subSampleSlabs ?
									//TB4 + "if (alpha > " + capabilities.minAlpha + ")" + NL +
									//TB4 + "{" + NL +
										TB5 + "for (float subStep = 0; subStep < 1; subStep += " + (1f / (float) capabilities.slabSamples) + ")" + NL +
										TB5 + "{" + NL +
											TB6 + "calcPos = (1 - subStep) * nextPos + subStep * pos;" + NL +
											
											//TB6 + "calcPos.x = max(calcPos.x, posS.x);" + NL +
											//TB6 + "calcPos.x = min(calcPos.x, posE.x);" + NL +
											
											//TB6 + "calcPos.y = max(calcPos.y, posS.y);" + NL +
											//TB6 + "calcPos.y = min(calcPos.y, posE.y);" + NL +
											
											//TB6 + "calcPos.z = max(calcPos.z, posS.z);" + NL +
											//TB6 + "calcPos.z = min(calcPos.z, posE.z);" + NL +
											
											TB6 + "texRatio = (calcPos.xyz - posS) / (posE - posS);" + NL +
											TB6 + "sampleTex = texS + texRatio * (texE - texS);" + NL + 
											TB6 + "sampleTex = remapUV(alphaMap, sampleTex);" + NL +							
											TB6 + "vec4 shaded = shade(calcPos, nor, color, alpha, sampleTex);" + NL +
											//TB6 + "shaded.w = shaded.w / " + (float) capabilities.slabSamples + ";" + NL +
											TB6 + "if (shaded.w > " + capabilities.minAlpha + ")" + NL +
											TB6 + "{" + NL +
												TB7 + "color.xyz = shaded.xyz;" + NL +
												TB7 + "alpha = min(alpha + shaded.w, 1.0);" + NL +
											TB6 + "}" + NL +
										TB5 + "}" + NL 
									//TB4 + "}" + NL
								: "") +
								
							TB3 + "}" + NL +
						TB2 + "}" + NL +
					TB1 + "}"
				: 
					TB1 + "vec4 shaded = shade(pos, nor, color, 1f, uv);" + NL +
					TB1 + "color = shaded.xyz;" + NL+						
					TB1 + "alpha = shaded.w;"
				) + NL +
				
				//TB1 + "if (alpha < 0.01)" + NL +
				//TB1 + "{" + NL +
				//	TB2 + "discard;" + NL +
				//TB1 + "}" + NL +
				
				TB1 + "gl_FragColor.xyz = color.xyz * col.xyz;" + NL +				
				TB1 + "gl_FragColor.w = alpha * col.w;" + NL +
			TB0 + "}" + NL;
	}

	/**
	 * Implements static functions for executing the following functions (and
	 * generating their GLSL code):
	 * <ul>
	 * <li><code>float vec4ToFloat(vec4 v)</code>: Converts a 4D vector
	 * '<code>v</code>' into a floating point number. It is assumed that the
	 * components of the vector are assumed to be within the range [0,1].</li>
	 * 
	 * <ul>
	 */
	protected static class Snippet extends ProgramBuilder
	{
		private static int B1 = 255;
		private static int B2 = B1 * B1;
		private static int B3 = B2 * B1;
		private static int H3 = B3 / 2;
		private static int H1 = B1 / 2;
		
		private Snippet() {}	
		
		public final static class Random
		{
			public static String getGLSL()
			{
				return
					TB0 + "float random(vec2 coordinate, float seed)" + NL +
					TB0 + "{" + NL +
						TB1 + "float PHI = 1.61803398874989484820459 * 00000.1;" + NL +
						TB1 + "float PI  = 3.14159265358979323846264 * 00000.1;" + NL +
						TB1 + "float SRT = 1.41421356237309504880169 * 10000.0;" + NL +
						TB1 + "return fract(sin(dot(coordinate * seed, vec2(PHI, PI))) * SRT);" + NL +
					TB0 + "}" + NL;
			}
		}		

		public final static class PlaneMult
		{
			public static String getGLSL()
			{
				return
					TB0 + "float planeMult(vec4 inPos, vec3 planePosition, vec3 planeNormal)" + NL +
					TB0 + "{" + NL +
						TB1 + "return max(sign(dot((inPos.xyz - planePosition), planeNormal)), 0);" + NL +
					TB0 + "}" + NL;
			}
		}		
		
		public final static class Shade
		{
			public static String getGLSL(Capabilities capabilities)
			{
				String mapDim = MapDim.getGLSL(capabilities);
				return
					TB0 + "vec4 shade(vec4 inPos, vec3 inNor, vec3 inColor, float inAlpha, " + mapDim + " uv)" + NL +
					TB0 + "{" + NL +
						TB1 + "vec3 color = vec3(0.0, 0.0, 0.0);" + NL +
						TB1 + "float alpha = 1;" + NL +						
						
						(capabilities.mapAmbient
						? TB1 + "color = texture(ambientMap, uv).xyz;" + NL
						: "") +
						
						(capabilities.mapAlpha
						? TB1 + "alpha = texture(alphaMap, uv).w;" + NL
						: "") +
						
						TB1 + "vec3 diffuse = vec3(1.0, 1.0, 1.0);" + NL +
						TB1 + "vec3 specular = vec3(1.0, 1.0, 1.0);" + NL +
						
						TB1 + "vec4 calcPos = inPos;" + NL +
						TB1 + "vec3 calcNor = inNor;" + NL +
						
						(capabilities.mapDiffuse
						? TB1 + "diffuse = texture(diffuseMap, uv).xyz;" + NL
						: "") +
						
						(capabilities.mapSpecular
						? TB1 + "specular = texture(specularMap, uv).xyz;" + NL
						: "") +
						
						(capabilities.mapNormal
						? TB1 + "calcNor = texture(normalMap, uv).xyz * 2.0 - vec3(1.0, 1.0, 1.0);" + NL
						: "") +
						
						TB1 + "color.xyz = color.xyz + gl_FrontMaterial.ambient.xyz;" + NL +
						TB1 + "for (int i = 0; i < " + MAX_LIGHTS + "; ++i)" + NL +
						TB1 + "{" + NL +
							TB2 + "if (lights[i].enabled > 0.0)" + NL +
							TB2 + "{" + NL +
								TB3 + "color.xyz = color.xyz + applyLight(lights[i], calcNor, cam, calcPos, diffuse, specular).xyz;" + NL +
							TB2 + "}" + NL +
						TB1 + "}" + NL +
						
						TB1 + "vec3 outColor = (1.0 - alpha) * inAlpha * inColor + min(alpha + (1 - inAlpha), 1) * color;" + NL +
						
						TB1 + "return vec4(outColor, alpha);" + NL +
						//(capabilities.flagVolume
						//? TB1 + "return vec4(0.001 * outColor + calcNor, alpha);" + NL
						//: TB1 + "return vec4(outColor, alpha);" + NL) +
					TB0 + "}" + NL;
			}
		}
		
		public final static class ApplyLight
		{
			public static String getGLSL()
			{
				return
					TB0 + "vec3 applyLight(Light light, vec3 nor, vec4 cam, vec4 pos, vec3 diffuse, vec3 specular)" + NL +
					TB0 + "{" + NL +
						TB1 + "vec4 lightPos = light.position;" + NL +
						TB1 + "vec3 lightDir = normalize(lightPos.xyz - pos.xyz);" + NL +
						TB1 + "float diffDot = max(0.0, dot(nor, lightDir));" + NL +
						TB1 + "vec4 V = normalize(cam - pos);" + NL +
						TB1 + "vec4 L = normalize(lightPos - pos);" + NL +
						TB1 + "float specDot = diffDot * pow(max(0.0, dot(V.xyz, reflect(-1.0 * L.xyz, nor))), gl_FrontMaterial.shininess);" + NL +
						TB1 + "vec3 result = " + NL +
							TB2 + "diffuse.xyz * gl_FrontMaterial.diffuse.xyz * diffDot * light.diffuse.xyz +" + NL +
							TB2 + "specular.xyz * gl_FrontMaterial.specular.xyz * specDot * light.specular.xyz;" + NL +
						//TB1 + "result.xyz = result.xyz * 0.0001 + nor.xyz;" + NL +
						TB1 + "return result;" + NL +
					TB0 + "}" + NL;
			}
		}
	
		/**
		 * GLSL snippet for the maps required a generic shader
		 */
		public final static class Maps
		{
			public static String getGLSL(Capabilities capabilities)
			{
				String sampler = capabilities.flagVolume ? "sampler3D" : "sampler2D";
				
				return
					TB0 + "uniform " + sampler + " alphaMap;" + NL +
					TB0 + "uniform " + sampler + " ambientMap;" + NL +
					TB0 + "uniform " + sampler + " diffuseMap;" + NL +
					TB0 + "uniform " + sampler + " specularMap;" + NL +				
					TB0 + "uniform " + sampler + " normalMap;" + NL;				
			}
		}
		
		/**
		 * GLSL snippet for map dimensionality.
		 */
		public final static class MapDim
		{
			public static String getGLSL(Capabilities capabilities)
			{
				return capabilities.flagVolume ? "vec3" : "vec2";
			}
		}
			
		/**
		 * GLSL function for converting from a vec4 to a float 
		 */
		public final static class Vec4ToFloat
		{
			public static String getGLSL()
			{
				return
						TB0 + "float vec4ToFloat(vec4 v)" + NL +
						TB0 + "{" + NL +
							TB1 + "float intPart = (" + B3 + " * v.r) + (" + B2 + " * v.g) + (" + B1 + " * v.b) - " + H3 + ";" + NL +
							TB1 + "float expPart = (" + B1 + " * v.a) - " + H1 + ";" + NL +
							TB1 + "return (intPart / " + B3 + " * 10.0) * pow(2.0, expPart);" + NL +
						TB0 + "}" + NL;
			}
			
			public static float execute(FloatMatrix v)
			{
				float intPart = ((B3 * v.getX()) + (B2 * v.getY()) + (B1 * v.getZ()) - H3);
				float expPart = (B1 * v.getU()) - H1;
				return (intPart / B3 * 10) * (float) Math.pow(2.0, expPart);
			}
		}
		
		/**
		 * GLSL function for converting from a float to a vec4
		 */
		public final static class FloatToVec4
		{
			public static String getGLSL()
			{
				return
						TB0 + "vec4 floatToVec4(float n)" + NL +
						TB0 + "{" + NL +
							TB1 + "float expPart = getExponent(n);" + NL +
							TB1 + "float intPart = pow(2.0, -1.0 * expPart) * n * 0.1 * " + B3 + ";" + NL +
							TB1 + NL +
							TB1 + "float x = intPart + " + H3 + ";" + NL +
							TB1 + "float r = floor(x / " + B2 + ") / " + B1 + ";" + NL +
							TB1 + "x = x - " + B2 + " * (" + B1 + " * r);" + NL +
							TB1 + "float g = floor(x / " + B1 + ") / " + B1 + ";" + NL +
							TB1 + "x = x - " + B1 + " * (" + B1 + " * g);" + NL +
							TB1 + "float b = floor(x) / " + B1 + ";" + NL +
							TB1 + NL +
							TB1 + "float a = (expPart + " + H1 + ") / " + B1 + ";" + NL +
							TB1 + NL +
							TB1 + "return vec4(r, g, b, a);" + NL +
							
						TB0 + "}" + NL;
			}
			
			public static FloatMatrix execute(float n)
			{
				float expPart = GetExponent.execute(n);
				float intPart = (float) Math.pow(2.0, -1.0 * expPart) * n * 0.1f * B3;
				
				float x = intPart + H3;			
				float r = (float) ( Math.floor(x / B2	)) / B1;
				x = x - B2 * (B1 * r);			
				float g = (float) ( Math.floor(x / B1	)) / B1;
				x = x - B1 * (B1 * g);			
				float b = (float) ( Math.floor(x			)) / B1;			
				
				float a = (expPart + H1) / (float) B1;
				
				return FloatMatrix.vector(r, g, b, a);
			}
		}
		
		/**
		 * GLSL function for finding an (integer) float {@code m} from a float {@code n} such that {@code n < 2^m} 
		 */
		public final static class GetExponent
		{
			public static String getGLSL()
			{
				return
						TB0 + "float getExponent(float n)" + NL +
						TB0 + "{" + NL +
							TB1 + "float x = log(n) / log(2.0);" + NL +
							TB1 + "return ceil(x);" + NL +
						TB0 + "}" + NL;
			}
		
			public static float execute(float n)
			{
				float x = (float) (Math.log(n) / Math.log(2d));
				return (float) Math.ceil(x);
			}
		}
		
		/**
		 * Remaps UV coordinates to the middle of each texture pixel. 
		 */
		public final static class RemapUV
		{
			public static String getGLSL()
			{
				return
						TB0 + "vec3 remapUV(sampler3D texture, vec3 uv)" + NL +
						TB0 + "{" + NL +
							TB1 + "vec3 texSize = textureSize3D(texture, 0).xyz;" + NL +
							TB1 + "vec3 uvPix = vec3(1.0 / texSize.x, 1.0 / texSize.y, 1.0 / texSize.z);" + NL +
							TB1 + "vec3 uvStart = 0.5 * uvPix;" + NL +
							TB1 + "vec3 uvEnd = vec3(1.0, 1.0, 1.0) - 0.5 * uvPix;" + NL +
							TB1 + "return uvStart + uv * (uvEnd - uvStart);" + NL +
						TB0 + "}" + NL;
			}
		}

		
		/**
		 * Remaps UV coordinates to the middle of each texture pixel. 
		 */
		public final static class LineAaBbIntersect
		{
			public static String getGLSL()
			{
				final float EPSILON = 0.001f;
				
				return
						TB0 + "float lineAaBbIntersect(vec3 p1, vec3 p2, vec3 min, vec3 max)" + NL +
						TB0 + "{" + NL +

						    TB1 + "vec3 d = (p2 - p1) * 0.5f;" + NL +
						    TB1 + "vec3 e = (max - min) * 0.5f;" + NL +
						    TB1 + "vec3 c = p1 + d - (min + max) * 0.5f;" + NL +
						    TB1 + "vec3 ad = abs(d);" + NL +
				
						    TB1 + "if (abs(c.x) > e.x + ad.x)" + NL +
						        TB2 + "return 0.0;" + NL +
						    TB1 + "if (abs(c.y) > e.y + ad.y)" + NL +
						        TB2 + "return 0.0;" + NL +
						    TB1 + "if (abs(c.z) > e.z + ad.z)" + NL +
						        TB2 + "return 0.0;" + NL +
						  
						    TB1 + "if (abs(d.y * c.z - d.z * c.y) > e.y * ad.z + e.z * ad.y + " + EPSILON + ")" + NL +
						        TB2 + "return 0.0;" + NL +
						    TB1 + "if (abs(d.z * c.x - d.x * c.z) > e.z * ad.x + e.x * ad.y + " + EPSILON + ")" + NL +
						        TB2 + "return 0.0;" + NL +
						    TB1 + "if (abs(d.x * c.y - d.y * c.x) > e.x * ad.y + e.y * ad.x + " + EPSILON + ")" + NL +
						        TB2 + "return 0.0;" + NL +
						            
						    TB1 + "return 1.0;" + NL +
						
						TB0 + "}" + NL;
			}
		}		
		
		
		/*
		bool Intersection::Intersect(const Vector3& p1, const Vector3& p2, const Vector3& min, const Vector3& max)
		{
		    Vector3 d = (p2 - p1) * 0.5f;
		    Vector3 e = (max - min) * 0.5f;
		    Vector3 c = p1 + d - (min + max) * 0.5f;
		    Vector3 ad = d.Absolute(); // Returns same vector with all components positive

		    if (fabsf(c[0]) > e[0] + ad[0])
		        return false;
		    if (fabsf(c[1]) > e[1] + ad[1])
		        return false;
		    if (fabsf(c[2]) > e[2] + ad[2])
		        return false;
		  
		    if (fabsf(d[1] * c[2] - d[2] * c[1]) > e[1] * ad[2] + e[2] * ad[1] + EPSILON)
		        return false;
		    if (fabsf(d[2] * c[0] - d[0] * c[2]) > e[2] * ad[0] + e[0] * ad[2] + EPSILON)
		        return false;
		    if (fabsf(d[0] * c[1] - d[1] * c[0]) > e[0] * ad[1] + e[1] * ad[0] + EPSILON)
		        return false;
		            
		    return true;
		}
		
		*/
		
		
	}
	
	/**
	 * Defines the capabilities of a {@link GenericShader} 
	 */
	public static class Capabilities
	{
		/*
		 * Maps
		 */
		public boolean mapAmbient = false;
		public boolean mapDiffuse = false;
		public boolean mapSpecular = false;
		public boolean mapAlpha = false;
		public boolean mapNormal = false;
		public boolean mapEnvironment = false;
		public boolean mapData = false;
		
		/*
		 * Vertex attributes
		 */
		public boolean attribCenter = false;
		
		/*
		 * Shader flags
		 */
		public boolean flagVolume = false;				
		
		/*
		 * Data dimensions
		 */
		public int dataWidth = 64;
		public int dataHeight = 64;
		public int dataDepth = 64;
		
		/*
		 * Volume parameters
		 */
		public int slabCount = 16;
		public int slabSamples = 32;
		public boolean subSampleSlabs = true;
		
		/*
		 * General
		 */
		public float minAlpha = 0.05f;
	}

	@Override
	public void setVertexPosition(IFloatMatrix position, OpenGLContext c)
	{
		if (position != null)
		{
			this.setAttribute("position", position, c);
		}
	}

	@Override
	public void setVertexColor(IFloatMatrix color, OpenGLContext c)
	{
		if (color != null)
		{
			this.setAttribute("color", color, c);
		}
	}

	@Override
	public void setVertexTexture(IFloatMatrix texture, OpenGLContext c)
	{
		if (texture != null)
		{
			this.setAttribute("texture", texture, c);
		}
	}

	@Override
	public void setVertexNormal(IFloatMatrix normal, OpenGLContext c)
	{
		if (normal != null)
		{
			this.setAttribute("normal", normal, c);
		}
	}	
}
