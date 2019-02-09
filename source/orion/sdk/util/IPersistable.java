package orion.sdk.util;


public interface IPersistable
{	
	/**
	 * Reads this persistable object from a binary.
	 */
	public abstract void read(StructuredBinary binary) throws Exception;

	/**
	 * Writes this persistable object to a binary
	 */
	public abstract StructuredBinary write() throws Exception;
}
