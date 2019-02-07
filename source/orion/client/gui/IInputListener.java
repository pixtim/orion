package orion.client.gui;


/**
 * Provides an interface for all GUI events.
 * 
 * @author Tim
 * @since 1.0.00
 */
public interface IInputListener
{

	
	/**
	 * Handles mouse down events
	 * @param x
	 * 	The x coordinate of the mouse.
	 * @param y
	 * 	The y coordinate of the mouse.
	 * @param button
	 * 	The button pressed.
	 */
	public void mouseDown(float x, float y, int button);
	
	/**
	 * Handles mouse up events.
	 * @param x
	 * 	The x coordinate of the mouse.
	 * @param y
	 * 	The y coordinate of the mouse.
	 * @param button
	 * 	The button released.
	 */
	public void mouseUp(float x, float y, int button);
	
	/**
	 * Handles mouse move events.
	 * @param x
	 * 	The x coordinate of the mouse.
	 * @param y
	 * 	The y coordinate of the mouse.
	 */
	public void mouseMove(float x, float y, int button);
	
	/**
	 * Handles mouse wheel events
	 * @param delta
	 * 	The change in the mouse wheel.
	 */
	public void mouseWheel(int delta);
	
	/**
	 * Handles key down events.
	 * @param key
	 * 	The key pressed.
	 */
	public void keyDown(int key);
	
	/**
	 * Handles key up events.
	 * @param key
	 * 	The key released.
	 */
	public void keyUp(int key);

}
