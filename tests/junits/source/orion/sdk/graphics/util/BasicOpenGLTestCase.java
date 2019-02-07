package orion.sdk.graphics.util;

import java.io.File;

import orion.sdk.assets.SceneBuilder;
import orion.sdk.graphics.drawables.primitives.Axis;
import orion.sdk.graphics.drawables.primitives.Grid;
import orion.sdk.graphics.scenes.Scene;
import orion.sdk.graphics.viewing.cameras.ACamera;
import orion.sdk.graphics.viewing.cameras.PerspectiveCamera;
import orion.sdk.graphics.viewing.cameras.Viewport;
import orion.sdk.math.FloatMatrix;
import orion.sdk.math.FloatQuaternion;
import orion.sdk.node.containers.ContainerNode;
import orion.sdk.node.drawables.LightNode;

public class BasicOpenGLTestCase extends OpenGLTestCase
{
	protected Viewport createTestViewport() throws Exception
	{
		return this.createTestViewport(true);
	}
	
	protected Viewport createTestViewport(boolean addCoordinates) throws Exception
	{
		float
			rotationX = (float) (Math.PI * 30f / 180f),
			rotationY = (float) (Math.PI * -30f / 180f);
		
		PerspectiveCamera camera = new PerspectiveCamera("testCamera");
		FloatQuaternion rotation = FloatQuaternion.product(
			FloatQuaternion.rotation(FloatMatrix.vector(0, 1, 0, 0), rotationX),
			FloatQuaternion.rotation(FloatMatrix.vector(1, 0, 0, 0), rotationY));		
		camera.thirdPerson(rotation,  FloatMatrix.vector(0, 0, 0, 1));
		camera.setObserverDistance(2);

		Scene scene = new Scene("testScene", camera);
		
		Viewport viewport = new Viewport("testViewport");
		viewport.getScenes().add(scene);
		viewport.setWidth(getScreenWidth());
		viewport.setHeight(getScreenHeight());
		viewport.clearColor = FloatMatrix.vector(1, 1, 1, 1);
				
		if (addCoordinates)
		{
			viewport.getScene(0).getDrawables().add(new Grid(10f, 10, 1f));
			viewport.getScene(0).getDrawables().add(new Axis());
		}
		
		return viewport;
	}
	
	protected ACamera getCamera()
	{
		return (ACamera) OpenGLStack.peek(ACamera.class);
	}
	
	public static class TestSceneBuilder<T extends Enum<?>> extends SceneBuilder
	{
		public static final String VIRTUAL = "." + File.separator; 
		public static final String ASSETS = 
			"tests" + File.separator + 
			"junits" + File.separator + 
			"resources" + File.separator + 
			"assets" + File.separator;		
		
		private BasicOpenGLTestCase testcase = null;
		private ContainerNode root = null;
		private T scenario = null;
		
		public TestSceneBuilder(BasicOpenGLTestCase testcase, T scenario, Scene scene)
		{
			super(scene);
			this.scenario = scenario;
			this.testcase = testcase;
			this.root = new ContainerNode("root");
			this.getScene().getDrawables().add(this.getRoot());
		}
		
		public T getScenario()
		{
			return scenario;
		}
		
		public BasicOpenGLTestCase getTestcase()
		{
			return testcase;
		}
		
		public ContainerNode getRoot()
		{
			return root;
		}
		
		protected void addLights()
		{
			float lightAxis = 100;
		
			LightNode rightLight = new LightNode("redLight");
			rightLight.light.diffuse = FloatMatrix.vector(0.3f, 0.3f, 0.3f, 1);
			rightLight.light.specular = FloatMatrix.vector(0.0f, 0.0f, 0.0f, 1);
			rightLight.getTransformation().getPosition().setX(lightAxis);		
			this.getRoot().addChild(rightLight);
			
			LightNode topLight = new LightNode("greenLight");
			topLight.light.diffuse = FloatMatrix.vector(0.5f, 0.5f, 0.5f, 1);
			topLight.light.specular = FloatMatrix.vector(0, 0, 0, 1);
			topLight.getTransformation().getPosition().setY(lightAxis);
			this.getRoot().addChild(topLight);
			
			LightNode frontLight = new LightNode("blueLight");
			frontLight.light.diffuse = FloatMatrix.vector(0.8f, 0.8f, 0.8f, 1);
			frontLight.light.specular = FloatMatrix.vector(0, 0, 0, 1);
			frontLight.getTransformation().getPosition().setZ(lightAxis);
			this.getRoot().addChild(frontLight);		
		}		
	}
}
