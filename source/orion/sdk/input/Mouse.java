package orion.sdk.input;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

public class Mouse
{	
	public static final int NONE = 0;
	public static final int BUTTON_LEFT = 1;
	public static final int BUTTON_RIGHT = 2;
	public static final int BUTTON_MIDDLE = 3;
	
	public float x = 0;
	public float y = 0;
	public float z = 0;
	public int button = Mouse.NONE;
	public boolean dragging = false;
	public int delta = 0;

	public Mouse() {}
	
	public static Mouse newMouse()
	{
		InputManager inputManager = InputManager.getInstance();
		Mouse mouse = new Mouse();
		mouse.x = inputManager.mouseX;
		mouse.y = inputManager.mouseY;
		mouse.button = inputManager.mouseButton;
		mouse.dragging = inputManager.mouseDragging;
		mouse.delta = inputManager.mouseDelta;
		return mouse;
	}	
	
	protected static int getButton(MouseEvent event)
	{
		switch (event.getButton())
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
}
