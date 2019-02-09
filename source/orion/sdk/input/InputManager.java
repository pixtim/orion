package orion.sdk.input;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Image;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.MemoryImageSource;
import java.util.ArrayList;
import java.util.List;

import orion.sdk.graphics.viewing.projection.IUnprojectListener;
import orion.sdk.monitoring.incidents.Incident;
import orion.sdk.monitoring.incidents.IncidentManager;

public class InputManager
{
	protected Component component = null;
	protected static InputManager inputManager = null;	
	protected List<IInputListener> inputListeners = new ArrayList<IInputListener>();
	
	public int previousMouseX = 0;
	public int previousMouseY = 0;
	public int mouseX = 0;
	public int mouseY = 0;
	public int mouseButton = 0;
	public int mouseDelta = 0;
	protected int resetX = 0;
	protected int resetY = 0;
	protected boolean mouseDragging = false;
	public boolean hideCursor = false;
	
	protected InputManager(Component component)
	{
		this.component = component;
		component.addMouseListener(new MouseListener()
		{
			
			@Override
			public void mouseReleased(MouseEvent mouseEvent)
			{
				mouseX = mouseEvent.getX();
				mouseY = mouseEvent.getY();
				mouseButton = getButton(mouseEvent);
				for (IInputListener inputListener : inputListeners)
				{
					inputListener.mouseUp(Mouse.newMouse());
				}
				mouseButton = Mouse.NONE;
				mouseDragging = false;
				updateCursor();
			}
			
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				mouseX = mouseEvent.getX();
				mouseY = mouseEvent.getY();
				resetX = mouseX;
				resetY = mouseY;
				mouseButton = getButton(mouseEvent);
				for (IInputListener inputListener : inputListeners)
				{
					inputListener.mouseDown(Mouse.newMouse());
				}
				updateCursor();
			}
			
			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
			}
			
			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
			}
			
			@Override
			public void mouseClicked(MouseEvent mouseEvent)
			{
			}
		});
		
		component.addMouseWheelListener(new MouseWheelListener()
		{
			
			@Override
			public void mouseWheelMoved(MouseWheelEvent mouseWheelEvent)
			{
				mouseDelta = mouseWheelEvent.getWheelRotation();
				for (IInputListener inputListener : inputListeners)
				{
					inputListener.mouseWheel(Mouse.newMouse());
				}
			}
		});
		
		component.addMouseMotionListener(new MouseMotionListener()
		{
			
			@Override
			public void mouseMoved(MouseEvent mouseEvent)
			{
				previousMouseX = mouseX;
				previousMouseY = mouseY;
				mouseX = mouseEvent.getX();
				mouseY = mouseEvent.getY();
				for (IInputListener inputListener : inputListeners)
				{
					inputListener.mouseMove(Mouse.newMouse());
				}
				updateCursor();
			}
			
			@Override
			public void mouseDragged(MouseEvent mouseEvent)
			{
				previousMouseX = mouseX;
				previousMouseY = mouseY;
				mouseX = mouseEvent.getX();
				mouseY = mouseEvent.getY();
				for (IInputListener inputListener : inputListeners)
				{
					inputListener.mouseMove(Mouse.newMouse());
				}
				mouseDragging = true;
				updateCursor();
			}
		});
		
		/*
		 * TODO Add keyboard
		 */
	}
	
	protected void updateCursor()
	{
		if (hideCursor)
		{
			Image image = Toolkit.getDefaultToolkit().createImage(
			        new MemoryImageSource(16, 16, new int[16 * 16], 0, 16));
			Cursor transparentCursor =
			        Toolkit.getDefaultToolkit().createCustomCursor
			             (image, new Point(0, 0), "invisibleCursor");
			component.setCursor(transparentCursor);
		}
		else
		{
			component.setCursor (Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
	}

	protected int getButton(MouseEvent mouseEvent)
	{
		switch (mouseEvent.getButton())
		{
			case MouseEvent.BUTTON1:
				return Mouse.BUTTON_LEFT;
			case MouseEvent.BUTTON2:
				return Mouse.BUTTON_MIDDLE;
			case MouseEvent.BUTTON3:
				return Mouse.BUTTON_RIGHT;
			default:
				return Mouse.NONE;				
		}
	}
	
	public static void register(Component component)
	{
		inputManager = new InputManager(component);
	}
	
	public static InputManager getInstance()
	{
		return inputManager;
	}
	
	/**
	 * Moves the mouse pointer to the center of the conponent.
	 */
	public void resetMouse()
	{
		try
		{
			Robot robot = new Robot();
			Point location = component.getLocationOnScreen();
			previousMouseX = resetX;
			previousMouseY = resetY;
			mouseX = resetX;
			mouseY = resetY;
			robot.mouseMove(location.x + resetX, location.y + resetY);
		}
		catch (Exception e)
		{
			IncidentManager.notifyIncident(Incident.newError("Robot error", e));
		}
	}
	
	public void registerInputListener(IInputListener listener)
	{
		synchronized (inputListeners)
		{
			inputListeners.add(listener);
		}
	}
	
	public void deregisterInputListener(IInputListener listener)
	{
		synchronized (inputListeners)
		{
			inputListeners.remove(listener);
		}
	}	
}
