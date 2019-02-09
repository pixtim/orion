package orion.client.gui;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.jogamp.opengl.GLException;

import orion.client.graphics.drawables.volumes.MengerSponge;
import orion.sdk.assets.AssetManager;
import orion.sdk.assets.AssetRequest;
import orion.sdk.assets.EAssetType;
import orion.sdk.assets.IAssetListener;
import orion.sdk.assets.io.ClasspathAssetSource;
import orion.sdk.assets.io.IAssetSource;
import orion.sdk.events.IUpdatable;
import orion.sdk.events.Processor;
import orion.sdk.graphics.buffers.VertexFormatter;
import orion.sdk.graphics.drawables.IDrawable;
import orion.sdk.graphics.drawables.primitives.WireAxis;
import orion.sdk.graphics.drawables.primitives.WireCube;
import orion.sdk.graphics.drawables.primitives.WireGrid;
import orion.sdk.graphics.drawables.surfaces.Mesh;
import orion.sdk.graphics.drawables.volumes.VolumeChunk;
import orion.sdk.graphics.drawables.volumes.VolumeProxyLayeredFrustum;
import orion.sdk.graphics.panels.OrthoPanel;
import orion.sdk.graphics.panels.PanelFrame;
import orion.sdk.graphics.scenes.Scene;
import orion.sdk.graphics.shading.glsl.AShader;
import orion.sdk.graphics.shading.glsl.GenericShader;
import orion.sdk.graphics.shading.glsl.GenericShader.Capabilities;
import orion.sdk.graphics.shading.lighting.Material;
import orion.sdk.graphics.shading.texturing.Texture;
import orion.sdk.graphics.shading.texturing.Texture.ETextureType;
import orion.sdk.graphics.shading.texturing.Texture.EUploadType;
import orion.sdk.graphics.util.IAlteration;
import orion.sdk.graphics.util.OpenGLContext;
import orion.sdk.graphics.util.OpenGLManager;
import orion.sdk.graphics.viewing.cameras.ACamera;
import orion.sdk.graphics.viewing.cameras.OrthographicCamera;
import orion.sdk.graphics.viewing.cameras.PerspectiveCamera;
import orion.sdk.graphics.viewing.cameras.Viewport;
import orion.sdk.graphics.viewing.projection.AUnprojectRequest;
import orion.sdk.graphics.viewing.projection.IUnprojectListener;
import orion.sdk.graphics.viewing.projection.PlaneUnprojectRequest;
import orion.sdk.input.IInputListener;
import orion.sdk.input.InputManager;
import orion.sdk.input.Keyboard;
import orion.sdk.input.Mouse;
import orion.sdk.math.FloatMatrix;
import orion.sdk.math.FloatQuaternion;
import orion.sdk.math.IFloatMatrix;
import orion.sdk.math.geometry.Box;
import orion.sdk.monitoring.incidents.Incident;
import orion.sdk.monitoring.incidents.IncidentManager;
import orion.sdk.monitoring.incidents.IncidentTrace;
import orion.sdk.monitoring.performance.StackMonitor;
import orion.sdk.node.containers.ContainerNode;
import orion.sdk.node.drawables.EntityNode;
import orion.sdk.node.drawables.LightNode;
import orion.sdk.node.drawables.ShadedNode;
import orion.sdk.node.drawables.VolumeNode;

public class Client implements IInputListener, IUnprojectListener
{
	private static Client client = new Client();
	public Processor gameLoop = new Processor("game loop");
	public Processor guiLoop = new Processor("GUI loop");
	public Processor graphicsLoop = new Processor("graphics loop");
	public Processor monitorLoop = new Processor("monitor loop");
	public List<IDrawable> drawables = new LinkedList<IDrawable>();
	
	public float screenWidth = 0;
	public float screenHeight = 0;
	
	public int bufferWidth = 512;
	public int bufferHeight = 512;
	public int volumeRes = 60;
	public boolean subSampleSlabs = true;
	public int slabCount = 128;
	public int slabSamples = 4;
	public int spongeDepth = 3;
	public int spongeCount = 1;
	public float spongeAlpha = 0.25f;
	
	public FloatMatrix cameraOrigin = FloatMatrix.vector(0, 0, 0, 1);
	public float cameraDistance = 4;
	public float cameraSpeed = 1;
	public float rotationX = (float) Math.PI * 0.1f;
	public float rotationY = (float) Math.PI * -0.2f;
	public float lastRotationX = 0;
	public float lastRotationY = 0;

	protected Viewport mainViewport;
	protected Viewport backgroundViewport;
	protected IncidentTrace incidentTrace = new IncidentTrace("trace", "log");
	
	public static float testRotation = 0;
	
	public static String PLANE_UNPROJECT = "planeUnproject";
	
	private ClientMainPanel clientMainPanel = null;
	
	protected Client()
	{
		InputManager.getInstance().registerInputListener(this);
	}

	public void dispose(OpenGLContext c)
	{		
		IncidentManager.notifyIncident(Incident.newInformation("Disposing of client"));

		OpenGLManager.getInstance().release(drawables, c);
		
		try
		{
			this.incidentTrace.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	void setSceneViewport() throws Exception
	{
		/*
		 * Create a viewport
		 */
		this.mainViewport = new Viewport("scene viewport");
		this.mainViewport.clear = true;
		this.mainViewport.clearColor = FloatMatrix.vector(0.9f, 0.9f, 0.9f, 1);
		
		/*
		 * Ensure that the viewport is resized with the window and maintain a 3rd person camera
		 */
		graphicsLoop.updatables.add(new IUpdatable()
		{
			@Override
			public void update(float dt) throws Exception
			{
				if (shouldUpdate())
				{
				
					mainViewport.setWidth(bufferWidth);
					mainViewport.setHeight(bufferHeight);
					ACamera cam = mainViewport.getScene(0).getCamera();
					FloatQuaternion rotation = FloatQuaternion.product(
						FloatQuaternion.rotation(FloatMatrix.vector(0, 1, 0, 0), rotationX),
						FloatQuaternion.rotation(FloatMatrix.vector(1, 0, 0, 0), rotationY));
					cam.thirdPerson(rotation, cameraOrigin);
					cam.setObserverDistance(cameraDistance);
					
				}
			}

			@Override
			public float getUpdatePeriod()
			{
				return 0f;
			}

			@Override
			public boolean shouldUpdate()
			{
				return mainViewport.getScenes().size() > 0;
			}
		});		
	}
	
	void generateSceneContent() throws GLException
	{
		TestScene testScene = new TestScene(this.mainViewport);
		graphicsLoop.updatables.add(testScene.mainRoot);
		
		OpenGLManager.getInstance().queueAlteration(testScene);
	}
	
	public void initialize() throws Exception
	{
		try
		{
			this.incidentTrace.open();
			IncidentManager.registerListener(this.incidentTrace);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		/*
		 * Add a stack monitor.
		 */
		monitorLoop.updatables.add(StackMonitor.getInstance());
		
		/*
		 * Set main viewport that contains the scene.
		 */
		this.setSceneViewport();
		
		/*
		 * Create a panel for the client and a second viewport that will be used to render it.  
		 */		
		OrthographicCamera backgroundCamera = new OrthographicCamera("backgroundCamera");
		this.clientMainPanel = new ClientMainPanel(backgroundCamera);
				
		this.backgroundViewport = new Viewport("backgroundViewport");
		this.backgroundViewport.addScene(new Scene("backgroundScene", backgroundCamera));
		this.backgroundViewport.clearColor = FloatMatrix.vector(1f, 1f, 1f, 1f);
		this.backgroundViewport.clear = true;
		this.drawables.add(this.backgroundViewport);
		
		Capabilities clientMainPanelShaderCapabilties = new Capabilities();
		clientMainPanelShaderCapabilties.mapAmbient = true;
		AShader clientMainPanelShader = new GenericShader("clientMainPanelShader", null, clientMainPanelShaderCapabilties);
		Material clientMainPanelShadedNodeMaterial = new Material("clientMainPanelShadedNodeMaterial");
		ShadedNode clientMainPanelShadedNode = new ShadedNode("clientMainPanelShadedNode", this.clientMainPanel, clientMainPanelShader, clientMainPanelShadedNodeMaterial);			
		this.clientMainPanel.registerTextures(clientMainPanelShadedNodeMaterial);
		this.backgroundViewport.getScene(0).getDrawables().add(clientMainPanelShadedNode);
		
		OpenGLManager.getInstance().queueUpload(clientMainPanelShadedNode);
		
		/*
		 * Populate the scene
		 */
		this.generateSceneContent();
			
		/*
		 * Ensure that the background viewport resizes with the window.
		 */
		guiLoop.updatables.add(new IUpdatable()
		{
			@Override
			public void update(float dt) throws Exception
			{
				if (shouldUpdate())
				{
					backgroundViewport.setWidth(Client.getInstance().screenWidth);
					backgroundViewport.setHeight(Client.getInstance().screenHeight);
				}
			}

			@Override
			public float getUpdatePeriod()
			{
				return 0f;
			}

			@Override
			public boolean shouldUpdate()
			{
				return true;
			}
		});
		
	}
	
	public static Client getInstance()
	{
		return client;
	}
	
	public void start()
	{
		guiLoop.start();
		graphicsLoop.start();
		monitorLoop.start();
	}
	
	public void terminate()
	{
		
	}
	
	public void connect()
	{
		
	}
	
	public void disconnect()
	{
		
	}

	@Override
	public void mouseDown(Mouse mouse)
	{
		
	}

	@Override
	public void mouseUp(Mouse mouse)
	{
		lastRotationX = rotationX;
		lastRotationY = rotationY;
		InputManager.getInstance().hideCursor = false;
		
		if (mouse.button == Mouse.BUTTON_LEFT && !mouse.dragging)
		{
			FloatMatrix sourcePoint = FloatMatrix.vector(mouse.x, mouse.y, 0f, 1f);
			FloatMatrix planePosition = FloatMatrix.vector(0f, 0f, 0f, 1f);
			FloatMatrix planeNormal = FloatMatrix.vector(0f, 1f, 0f, 0f);
			
			PlaneUnprojectRequest unprojectRequest =	new PlaneUnprojectRequest(
				PLANE_UNPROJECT,
				sourcePoint,
				backgroundViewport,
				mainViewport,
				planePosition,
				planeNormal);
			unprojectRequest.registerUnprojectListener(this);
			
			mainViewport.getScene(0).getCamera().queueUnprojectRequest(unprojectRequest);
		}
			
	}

	@Override
	public void mouseMove(Mouse mouse)
	{
		float mouseZoneSize = 1000;
		InputManager inputManager = InputManager.getInstance();
		if (mouse.button == Mouse.BUTTON_LEFT && mouse.dragging)
		{
			InputManager.getInstance().hideCursor = true;
			float
				deltaX = (mouse.x - inputManager.previousMouseX) / mouseZoneSize,
				deltaY = (mouse.y - inputManager.previousMouseY) / mouseZoneSize,
				mouseSpeed = 1f;
			
			lastRotationX = rotationX;
			lastRotationY = rotationY;					
			rotationX = rotationX + deltaX * (float) Math.PI * mouseSpeed * -1;
			rotationY = rotationY + deltaY * (float) Math.PI * mouseSpeed * -1;
			try
			{				
				if (FloatMatrix.vector(deltaX, deltaY).norm() > 0.001)
				{
					InputManager.getInstance().resetMouse();
				}
			} catch (Exception e)
			{
				IncidentManager.notifyIncident(Incident.newError("Matrix error", e));
			}
		}
	}

	@Override
	public void mouseWheel(Mouse mouse)
	{
		cameraDistance += mouse.delta * cameraSpeed;
		cameraSpeed = 0.01f + cameraDistance / 10;
		if (cameraDistance < 0)
		{
			cameraDistance = 0;
		}
	}

	@Override
	public void keyDown(Keyboard keyboard)
	{
		
	}

	@Override
	public void keyUp(Keyboard keyboard)
	{
		
	}
	
	public int getFaceCount()
	{
		int faces = 0;
		synchronized (drawables)
		{
			for (IDrawable drawable : drawables)
			{
				faces += drawable.getFaceCount();
			}
		}
		return faces;		
	}

	@Override
	public void unprojectEvent(AUnprojectRequest request, IFloatMatrix position)
	{
		String name = request.getName();
		if (name.equals(PLANE_UNPROJECT))
		{
			cameraOrigin.setX(position.getX());
			cameraOrigin.setY(position.getY());
			cameraOrigin.setZ(position.getZ());
		}		
	}
	
	class ClientMainPanel extends OrthoPanel
	{
		private Texture colorTexture = null;
		private Texture depthTexture = null;
		
		public ClientMainPanel(OrthographicCamera viewportCamera)
		{
			super("backgroundPanel", viewportCamera);
			
			this.colorTexture = new Texture("colorTexture", bufferWidth, bufferHeight, 1, 1, 1, 1, Texture.EFormat.RGBA, Texture.ETarget.TEXTURE_2D);
			this.colorTexture.setTextureType(Texture.ETextureType.AMBIENT);
			this.colorTexture.uploadType = EUploadType.ALLOCATE_ONLY;			
			this.colorTexture.linear = true;
			
			this.depthTexture = new Texture("depthTexture", bufferWidth, bufferHeight, 1, 1, 1, 1, Texture.EFormat.DEPTH, Texture.ETarget.TEXTURE_2D);
			this.depthTexture.setTextureType(Texture.ETextureType.DEPTH);
			this.depthTexture.uploadType = EUploadType.ALLOCATE_ONLY;
			this.depthTexture.linear = true;
			
			PanelFrame frame = new PanelFrame("panelFrame", this.colorTexture, this.depthTexture);
			frame.getDrawables().add(Client.this.mainViewport);
			
			this.getFrames().add(frame);
		}
		
		public Texture getColorTexture()
		{
			return colorTexture;
		}
		
		public Texture getDepthTexture()
		{
			return depthTexture;
		}
	}
	
	public class TestScene implements IAssetListener, IAlteration
	{
		protected Viewport rootViewport = null;
		protected Scene mainScene = null;
		
		protected ContainerNode mainRoot = new ContainerNode("mainRoot");
		
		protected ACamera mainSceneCamera = null;
		protected final String resources = "";
		protected final String testing = resources + "testing" + File.separator;
		
		protected final String modelPath = testing + "monkey.obj";
		protected final String gridPath = testing + "grid.png";
		protected final String dotPath = testing + "dot.png";
		protected final String starPath = testing + "star.png";
		protected final String spritePath = testing + "sprite.png";
		
		protected final String volumePath = "testVolumeMap";
		protected final String alphaLayer0Path = testing + "alpha0.png";
		protected final String alphaLayer1Path = testing + "alpha1.png";		
		protected final String alphaLayer2Path = testing + "alpha2.png";
		protected final String alphaLayer3Path = testing + "alpha3.png";
		protected final String ambientLayer0Path = testing + "ambient0.png";
		protected final String ambientLayer1Path = testing + "ambient1.png";		
		protected final String ambientLayer2Path = testing + "ambient2.png";
		protected final String ambientLayer3Path = testing + "ambient3.png";
		
		protected final String galacticTextureMapPath = "galacticTextureMap";
		protected final String galacticColorMapPath = resources + "galaxy" + File.separator + "galacticColor.png";
		protected final String galacticDensityMapPath = resources + "galaxy" + File.separator + "galacticDensity.png";		
		
		protected final String layeredTextureMapPath = "layeredTextureMap";
		protected final String layeredAPath = testing + "a.png";
		protected final String layeredBPath = testing + "b.png";		
		
		public TestScene(Viewport viewport)
		{
			this.rootViewport = viewport;
		}
		
		/*
		public void addParticleSystem() throws Exception
		{

			int particleCount = 10000;
			float space = 10;
			
			ParticleSystem particleSystem = new ParticleSystem(particleCount, camera, null);		
			LcgRandom randomX = new LcgRandom(465);
			LcgRandom randomY = new LcgRandom(564658);
			LcgRandom randomZ = new LcgRandom(6544);
			LcgRandom randomC = new LcgRandom(45648);
			LcgRandom randomS = new LcgRandom(648789);
			LcgRandom randomT = new LcgRandom(164);
			for (int i = 0; i < particleCount; i++)
			{
				Particle particle = new Particle();
				particle.position.setX(randomX.nextFloat(-1 * space, space));
				particle.position.setY(randomY.nextFloat(-1 * space, space) * 0.25f);
				particle.position.setZ(randomZ.nextFloat(-1 * space, space));
				float size = randomS.nextFloat(0.2f, 1f) * 0.25f;				
				particle.size.setX(size);
				particle.size.setY(size);
				particle.color.setR(randomC.nextFloat(0.1f, 0.4f));
				particle.color.setG(randomC.nextFloat(0.4f, 0.6f));
				particle.color.setB(randomC.nextFloat(0.6f, 1f));
				particle.spriteTile = randomT.nextInt(0, 2);
				particleSystem.particles[i] = particle;
			}
			GenericShader shader = new GenericShader("basicShader", new GenericShader.Capabilities());
			SolidNode particles = new SolidNode("particles", null, shader);
			particles.material.ambient = Matrix.newMatrix(1, 1, 1, 1);
			particles.material.diffuse = Matrix.newMatrix(0, 0, 0, 1);
			particles.material.specular = Matrix.newMatrix(0, 0, 0, 1);
			particles.setDrawable(particleSystem);
			particles.name = "particles";
			root.addChild(particles);			
					
		}
		*/
		
		public void generate() throws Exception
		{
			/*
			 * Create the scenes that we will add later
			 */
			this.mainSceneCamera = new PerspectiveCamera("mainCamera");
			this.mainScene = new Scene("mainScene", this.mainSceneCamera);

			/*
			 * Add test nodes
			 */
			this.addTestLights();
			this.addTestAxis();
			this.addTestGrid();
			this.addTestModel();
			this.addTestVolume();
			
			/*
			 * Add the content to the scenes
			 */
			this.mainScene.getDrawables().add(this.mainRoot);
			
			/*
			 * Create a viewport and add the scenes
			 */
			this.rootViewport.addScene(mainScene);
		}
		
		private void addTestLights() throws Exception
		{
			float lightAxis = 10;

			boolean color = true;
			
			if (color)
			{
				LightNode rightLight = new LightNode("redLight");
				rightLight.light.diffuse = FloatMatrix.vector(0.8f, 0f, 0f, 1);
				rightLight.light.specular = FloatMatrix.vector(0.2f, 0.0f, 0.0f, 1);
				rightLight.light.position = FloatMatrix.vector(lightAxis, 0, 0, 1);		
				mainRoot.addChild(rightLight);
	
				LightNode topLight = new LightNode("greenLight");
				topLight.light.diffuse = FloatMatrix.vector(0.0f, 0.8f, 0.0f, 1);
				topLight.light.specular = FloatMatrix.vector(0, 0.2f, 0, 1);
				topLight.light.position = FloatMatrix.vector(0, lightAxis, 0, 1);
				mainRoot.addChild(topLight);
				
				LightNode frontLight = new LightNode("blueLight");
				frontLight.light.diffuse = FloatMatrix.vector(0.0f, 0.0f, 0.8f, 1);
				frontLight.light.specular = FloatMatrix.vector(0, 0, 0.2f, 1);
				frontLight.light.position = FloatMatrix.vector(0, 0, lightAxis, 1);
				mainRoot.addChild(frontLight);	
			}
			else
			{
				LightNode mainLight = new LightNode("mainLight");
				mainLight.light.diffuse = FloatMatrix.vector(0.8f, 0.8f, 0.8f, 1);
				mainLight.light.specular = FloatMatrix.vector(0.2f, 0.2f, 0.2f, 1);
				mainLight.light.position = FloatMatrix.vector(lightAxis, lightAxis, lightAxis, 1);
				mainRoot.addChild(mainLight);
				
				LightNode backLight = new LightNode("backLight");
				backLight.light.diffuse = FloatMatrix.vector(0.3f, 0.3f, 0.3f, 1);
				backLight.light.specular = FloatMatrix.vector(0, 0, 0, 1);
				backLight.light.position = FloatMatrix.vector(-1f * lightAxis, -1f * lightAxis, -1f * lightAxis, 1);
				mainRoot.addChild(backLight);
							
			}
		}
		
		private void addTestGrid() throws GLException
		{
			mainRoot.addChild(new EntityNode("grid", new WireGrid(10f, 10, 1f), null, null));
		}
		
		private void addTestAxis() throws GLException
		{
			mainRoot.addChild(new EntityNode("axis", new WireAxis(3f, 6f), null, null));
		}
		
		private void addTestCompoundAsset() throws GLException
		{
			/*
			AssetManager.requestCompoundAsset(
					galacticTextureMapPath,
					new String[] {
						galacticColorMapPath, 
						galacticDensityMapPath},
					EAssetType.TEXTURE_SINGLE,
					new FileAssetSourceFactory(),
					EAssetType.TEXTURE_MAP,
					new TextureMapGenerator(),
					this);
			*/			
		}
		
		private void addTestCube() throws GLException
		{
			WireCube cube = new WireCube();
			mainRoot.addChild(new EntityNode("cube", cube, null, null));			
			OpenGLManager.getInstance().queueUpload(cube);			
		}
		
		private void addTestModel() throws GLException
		{
			/*
			 * Request for a model
			 */
			AssetManager.requestAsset(
					modelPath, 
					EAssetType.MESH_OBJ, 
					new ClasspathAssetSource(modelPath), 
					this);			
		}
		
		private void addTestVolume() throws GLException
		{
			Capabilities volumeShaderCapabilities = new Capabilities();
			volumeShaderCapabilities.flagVolume = true;
			volumeShaderCapabilities.mapAlpha = true;
			volumeShaderCapabilities.mapAmbient = false;
			volumeShaderCapabilities.mapDiffuse = true;
			volumeShaderCapabilities.mapNormal = true;
			volumeShaderCapabilities.slabCount = slabCount;
			volumeShaderCapabilities.slabSamples = slabSamples;
			volumeShaderCapabilities.subSampleSlabs = subSampleSlabs;
			Material volumeMaterial = new Material("volumeMaterial");
			volumeMaterial.diffuse = FloatMatrix.vector(1, 1, 1, 1);
			volumeMaterial.ambient = FloatMatrix.vector(0.1f, 0.1f, 0.1f, 1);
			GenericShader volumeShader = new GenericShader("volumeShader", mainRoot, volumeShaderCapabilities);
			
			VolumeProxyLayeredFrustum proxy = new VolumeProxyLayeredFrustum(volumeShaderCapabilities.slabCount, volumeShader);
			proxy.setVertexFormatter(new VertexFormatter("proxyVertexFormatter", volumeShader));
			VolumeNode volumeNode = new VolumeNode("volumeNode", proxy, volumeShader, volumeMaterial);
			volumeNode.setEnableAlphaBlending(true);
			volumeNode.setEnableDepthTest(true);
			volumeNode.getMaterial().setTexture(Client.this.clientMainPanel.depthTexture);
			
			MengerSponge sponge = new MengerSponge(volumeRes);
			sponge.generate(spongeDepth, spongeAlpha);
			Texture alphaMap = sponge.getAlphaTexture();
			Texture diffuseMap = sponge.getDiffuseTexture();
			Texture normalMap = sponge.getNormalTexture();
			volumeNode.getMaterial().setTexture(ETextureType.ALPHA, alphaMap);
			volumeNode.getMaterial().setTexture(ETextureType.DIFFUSE, diffuseMap);
			volumeNode.getMaterial().setTexture(ETextureType.NORMAL, normalMap);

			Random random = new Random();
			
			for (int i = 0; i < spongeCount; i++)
			{
				/*
				float size = (random.nextFloat() * 0.9f + 0.5f) * 1f;
				float x = (random.nextFloat() * 2f - 1f) * 1f;
				float y = (random.nextFloat() * 2f - 1f) * 1f;
				float z = (random.nextFloat() * 2f - 1f) * 1f;
				*/
				
				float size = 2;
				float x = 2, y = -1f, z = 1f;
				
				VolumeChunk chunk = new VolumeChunk();
				chunk.getPositionBounds().set(x, y, z, size, size, size);
				chunk.getTextureBounds().set(0f, 0f, 0f, 1f, 1f, 1f);
				volumeNode.getChunks().add(chunk);
			}

			OpenGLManager.getInstance().queueUpload(volumeNode);
			OpenGLManager.getInstance().queueUpload(proxy);
			OpenGLManager.getInstance().queueUpload(alphaMap);
			OpenGLManager.getInstance().queueUpload(diffuseMap);
			OpenGLManager.getInstance().queueUpload(normalMap);
			
			mainRoot.addChild(volumeNode);			
		}

		
		@Override
		public void notifyAssetAvailable(IAssetSource source, AssetRequest request)
		{
			try
			{
				String assetPath = request.getAssetPath();
				
				if (assetPath.equals(modelPath))
				{
					/*
					 * Add the model, request a texture and queue the model for upload
					 */
					GenericShader.Capabilities capabilities = new GenericShader.Capabilities();
					//capabilities.mapAmbient = true;
					//capabilities.mapDiffuse = true;
					
					GenericShader shader = new GenericShader("modelShader", mainRoot,	capabilities);
					Material material = new Material("modelMaterial");
					Mesh mesh = AssetManager.getMesh(modelPath);
					mesh.setVertexFormatter(new VertexFormatter("modelMeshFormatter", shader));
					EntityNode modelNode = new EntityNode("model", mesh, shader, material);
					
					//modelNode.setBounds(new Box(-5f, -5f, -5f, 10, 10, 10));
					
					//modelNode.getTransformation().setScale(FloatMatrix.vector(5f, 5f, 5f));

					/*
					model.getTransformation().setPosition(Matrix.newMatrix(0, 0.01f, 0, 1));
					model.getTransformation().setRotation(Quaternion.rotation(0, 0, 1, (float) Math.PI / 4f));					
					*/
					mainRoot.addChild(modelNode);
					
					OpenGLManager.getInstance().queueUpload(modelNode);
					/*		
					AssetManager.requestAsset(
						gridPath,
						EAssetType.TEXTURE_SINGLE, 
						new FileSource(gridPath),
						this);
					*/
					/*
					AssetManager.requestAsset(
							dotPath,
							EAssetType.TEXTURE_SINGLE, 
							new FileSource(dotPath),
							this);
					*/
				}
				else if (assetPath.equals(dotPath))
				{
					/*
					 * Set the model's texture and queue the texture for upload
					 */
					ShadedNode model = (ShadedNode) mainRoot.getChild("model");
					Texture texture = AssetManager.getTexture(dotPath, 1, true);
					model.getMaterial().setTexture(ETextureType.AMBIENT, texture);
					
					OpenGLManager.getInstance().queueUpload(texture);
				}				
				else if (assetPath.equals(gridPath))
				{
					/*
					 * Set the model's texture and queue the texture for upload
					 */
					ShadedNode model = (ShadedNode) mainRoot.getChild("model");					
					Texture texture = AssetManager.getTexture(gridPath, 0, true);					
					model.getMaterial().setTexture(ETextureType.DIFFUSE, texture);					
					
					OpenGLManager.getInstance().queueUpload(texture);										
					
					/*
					backgroundPanel.texture = texture;					
					
					*/
				}
				else if (assetPath.equals(starPath))
				{

				}
				else if (assetPath.equals(spritePath))
				{
					
					/*
					Sprite sprite = AssetManager.getSprite(spritePath, 2, false);
					SolidNode particles = (SolidNode) root.getChild("particles");
					particles.material.alphaMap = sprite;
					particles.material.ambientMap = sprite;
					particles.setEnableAlphaBlending(true);
					ParticleSystem particleSystem = (ParticleSystem) particles.getDrawable();
					particleSystem.sprite = sprite;
					//particles.enableDepthTest = false;
					*/
					/*
					Sprite sprite = AssetManager.getSprite(spritePath, 2, false);
					SolidNode voxels = (SolidNode) root.getChild("voxels");
					VoxelChunk voxelSystem = (VoxelChunk)	voxels.drawable;
					voxelSystem.sprite = sprite;
					voxels.material.diffuseMap = sprite;
					voxels.enableAlphaBlending = true;
					*/
				}
				else if (assetPath.equals(galacticTextureMapPath))
				{			
					Box bounds = new Box();
					bounds.set(-4f, -0.5f, -4f, 8f, 1f, 8f);
					Map<String, Texture> galacticTextureMap = AssetManager.getTextureMap(galacticTextureMapPath);
					Texture colorMap = galacticTextureMap.get(galacticColorMapPath);
					Texture densityMap = galacticTextureMap.get(galacticDensityMapPath);
					


					
					/*					
					AssetManager.requestCompoundAsset(
						spritePath,
						new String[] {
							resources + "testing" + File.separator +  "a.png", 
							resources + "testing" + File.separator +  "b.png",
							resources + "testing" + File.separator +  "c.png"},
						AssetType.TEXTURE_SINGLE,
						new FileAssetSourceFactory(),
						AssetType.TEXTURE_SPRITE,
						new GridSpriteGenerator(512),
						this);
					*/				
				}				
				else if (assetPath.equals(volumePath))
				{			

				}				
			}
			catch (Exception e)
			{
				IncidentManager.notifyIncident(Incident.newError("Failed to process asset", e));
			}			
		}

		@Override
		public void notifyAssetNotAvailable(IAssetSource source, AssetRequest request, Exception e)
		{
			IncidentManager.notifyIncident(Incident.newError(request.getAssetPath() + " not available.", e));
			
		}

		@Override
		public String getName()
		{
			return "Test scene generator";
		}

		@Override
		public void alter() throws GLException
		{
			try 
			{
				this.generate();
			} 
			catch (Exception e) 
			{
				IncidentManager.notifyIncident(Incident.newError("Alter failed", e));
				throw new GLException(e);
			}
		}
	}	
}

