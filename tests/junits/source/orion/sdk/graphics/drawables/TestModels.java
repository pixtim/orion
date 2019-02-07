package orion.sdk.graphics.drawables;

import java.util.Map;

import org.junit.Test;

import orion.sdk.assets.AssetManager;
import orion.sdk.assets.EAssetType;
import orion.sdk.assets.SceneBuilder;
import orion.sdk.assets.io.FileAssetSourceFactory;
import orion.sdk.assets.io.FileSource;
import orion.sdk.assets.loaders.TextureMapGenerator;
import orion.sdk.graphics.buffers.MeshFormatter;
import orion.sdk.graphics.drawables.surfaces.Mesh;
import orion.sdk.graphics.scenes.Scene;
import orion.sdk.graphics.shading.glsl.GenericShader;
import orion.sdk.graphics.shading.texturing.Texture;
import orion.sdk.graphics.shading.texturing.Texture.ETextureType;
import orion.sdk.graphics.util.BasicOpenGLTestCase;
import orion.sdk.graphics.util.OpenGLManager;
import orion.sdk.graphics.viewing.cameras.Viewport;
import orion.sdk.math.Bounds;
import orion.sdk.node.drawables.ShadedNode;

public class TestModels extends BasicOpenGLTestCase
{	
	public enum EScenario {CUBE_AMBIENT, CUBE_DIFFUSE, CUBE_DIFFUSE_AMBIENT, MONKEY_DIFFUSE_AMBIENT}
	
	@Test
	public void testCube_Diffuse() throws Exception
	{		
		Viewport viewport = createTestViewport();
		this.getDrawables().add(viewport);

		SceneBuilder sceneBuilder = new TestSceneBuilder(this, EScenario.CUBE_DIFFUSE, viewport.getScene(0));
		AssetManager.requestAsset(
				TestSceneBuilder.PATH_CUBE,
				EAssetType.MESH_OBJ,
				new FileSource(TestSceneBuilder.PATH_CUBE), 
				sceneBuilder);
		
		this.beginAssetLoad();
		
		this.render();
		
		this.assertLastFrameExpected();
	}
	
	@Test
	public void testCube_Ambient() throws Exception
	{		
		Viewport viewport = createTestViewport();
		this.getDrawables().add(viewport);

		SceneBuilder sceneBuilder = new TestSceneBuilder(this, EScenario.CUBE_AMBIENT, viewport.getScene(0));
		AssetManager.requestAsset(
				TestSceneBuilder.PATH_CUBE,
				EAssetType.MESH_OBJ,
				new FileSource(TestSceneBuilder.PATH_CUBE), 
				sceneBuilder);
		
		this.beginAssetLoad();
		
		this.render();
		
		this.assertLastFrameExpected();
	}
	
	@Test
	public void testCube_DiffuseAmbient() throws Exception
	{		
		Viewport viewport = createTestViewport();
		this.getDrawables().add(viewport);

		SceneBuilder sceneBuilder = new TestSceneBuilder(this, EScenario.CUBE_DIFFUSE_AMBIENT, viewport.getScene(0));
		AssetManager.requestAsset(
				TestSceneBuilder.PATH_CUBE,
				EAssetType.MESH_OBJ,
				new FileSource(TestSceneBuilder.PATH_CUBE), 
				sceneBuilder);
		
		this.beginAssetLoad();
		
		this.render();
		
		this.assertLastFrameExpected();
	}
	
	@Test
	public void testMonkey_DiffuseAmbient() throws Exception
	{		
		Viewport viewport = createTestViewport();
		this.getDrawables().add(viewport);

		SceneBuilder sceneBuilder = new TestSceneBuilder(this, EScenario.MONKEY_DIFFUSE_AMBIENT, viewport.getScene(0));
		AssetManager.requestAsset(
				TestSceneBuilder.PATH_MONKEY,
				EAssetType.MESH_OBJ,
				new FileSource(TestSceneBuilder.PATH_MONKEY), 
				sceneBuilder);
		
		this.beginAssetLoad();
		
		this.render();
		
		this.assertLastFrameExpected();
	}
	
	public static class TestSceneBuilder extends BasicOpenGLTestCase.TestSceneBuilder<EScenario>
	{
		public static final String PATH_MONKEY = ASSETS + "monkey.obj";
		public static final String PATH_CUBE = ASSETS + "cube.obj";
		public static final String PATH_DIFFUSE = ASSETS + "diffuse.png";
		public static final String PATH_AMBIENT = ASSETS + "ambient.png";
		public static final String PATH_DIFFUSE_AMBIENT = VIRTUAL + "diffuse_ambient.map";
		
		public TestSceneBuilder(BasicOpenGLTestCase testcase, EScenario scenario, Scene scene)
		{
			super(testcase, scenario, scene);
		}
		
		@Override
		public void onMeshAvailable(String path, Mesh mesh) throws Exception
		{
			GenericShader.Capabilities capabilities = new GenericShader.Capabilities();
			
			if (this.getScenario() == EScenario.CUBE_AMBIENT)
			{
				capabilities.mapAmbient = true;
			}
			else if (this.getScenario() == EScenario.CUBE_DIFFUSE)
			{
				capabilities.mapDiffuse = true;	
			}
			else if (this.getScenario() == EScenario.CUBE_DIFFUSE_AMBIENT)
			{
				capabilities.mapDiffuse = true;
				capabilities.mapAmbient = true;
			}
			else if (this.getScenario() == EScenario.MONKEY_DIFFUSE_AMBIENT)
			{
				capabilities.mapDiffuse = true;
				capabilities.mapAmbient = true;
			}
			
			if (path.equals(PATH_CUBE) || path.equals(PATH_MONKEY))
			{
				GenericShader shader = new GenericShader("modelShader",	capabilities);			
				ShadedNode model = new ShadedNode("model", null, shader, null);
									
				mesh.setMeshFormatter(new MeshFormatter("model mesh formatter", shader));
				model.setDrawable(mesh);
				model.setBounds(new Bounds(-0.5f, -0.5f, -0.5f, 1, 1, 1));
			
				this.getRoot().addChild(model);
				
				OpenGLManager.getInstance().queueUpload(model);
			}
			
			if (this.getScenario() == EScenario.CUBE_AMBIENT)
			{
				AssetManager.requestAsset(
					PATH_AMBIENT,
					EAssetType.TEXTURE_SINGLE, 
					new FileSource(PATH_AMBIENT),
					this);			
			}
			else if (this.getScenario() == EScenario.CUBE_DIFFUSE)
			{
				this.addLights();
				AssetManager.requestAsset(
						PATH_DIFFUSE,
					EAssetType.TEXTURE_SINGLE, 
					new FileSource(PATH_DIFFUSE),
					this);			
			}
			else if (
				this.getScenario() == EScenario.CUBE_DIFFUSE_AMBIENT || 
				this.getScenario() == EScenario.MONKEY_DIFFUSE_AMBIENT)
			{
				this.addLights();
				AssetManager.requestCompoundAsset(
						PATH_DIFFUSE_AMBIENT,
						new String[] {
							PATH_DIFFUSE, 
							PATH_AMBIENT},
						EAssetType.TEXTURE_SINGLE,
						new FileAssetSourceFactory(),
						EAssetType.TEXTURE_MAP,
						new TextureMapGenerator(),
						this);		
			}			
			
		}
		
		@Override
		public void onTextureAvailable(String path, Texture texture) throws Exception
		{
			ShadedNode model = (ShadedNode) this.getRoot().getChild("model");					
			if (this.getScenario() == EScenario.CUBE_AMBIENT)
			{
				model.getMaterial().setTexture(ETextureType.AMBIENT, texture);
				
				OpenGLManager.getInstance().queueUpload(texture);										
				
				this.getTestcase().endAssetLoad();
			}
			else if (this.getScenario() == EScenario.CUBE_DIFFUSE)
			{
				model.getMaterial().setTexture(ETextureType.DIFFUSE, texture);
				
				OpenGLManager.getInstance().queueUpload(texture);										
				
				this.getTestcase().endAssetLoad();
			}	
		}
		
		@Override
		public void onTextureMapAvailable(String path, Map<String, Texture> map) throws Exception
		{
			ShadedNode model = (ShadedNode) this.getRoot().getChild("model");					
			if (this.getScenario() == EScenario.CUBE_DIFFUSE_AMBIENT ||
				this.getScenario() == EScenario.MONKEY_DIFFUSE_AMBIENT)
			{
				Texture diffuse = map.get(PATH_DIFFUSE);
				Texture ambient = map.get(PATH_AMBIENT);
				model.getMaterial().setTexture(ETextureType.AMBIENT, ambient);
				model.getMaterial().setTexture(ETextureType.DIFFUSE, diffuse);
				
				OpenGLManager.getInstance().queueUpload(ambient);
				OpenGLManager.getInstance().queueUpload(diffuse);					
				
				this.getTestcase().endAssetLoad();
			}
		}
	}
}
