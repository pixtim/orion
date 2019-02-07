package orion.sdk.events;

public interface IUpdatable
{
	/**
	 * Updates this updatable.
	 * @param dt The time in seconds since this object was last updated.
	 * @throws Exception
	 */
	public void update(float dt) throws Exception;
	
	/**
	 * The minimum amount of time that needs to pass before this object is updated. 
	 * @return
	 */
	public float getUpdatePeriod();
	
	public boolean shouldUpdate();
}
