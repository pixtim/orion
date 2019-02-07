package orion.sdk.input;


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
	 */
	public void mouseDown(Mouse mouse);
	
	/**
	 * Handles mouse up events.
	 */
	public void mouseUp(Mouse mouse);
	
	/**
	 * Handles mouse move events.
	 */
	public void mouseMove(Mouse mouse);
	
	/**
	 * Handles mouse wheel events
	 */
	public void mouseWheel(Mouse mouse);
	
	/**
	 * Handles key down events.
	 */
	public void keyDown(Keyboard keyboard);
	
	/**
	 * Handles key up events.
	 */
	public void keyUp(Keyboard keyboard);

}
