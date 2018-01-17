import java.io.FileNotFoundException;

/**
 * 
 */

/**
 * @author Rob
 *
 */
public interface CacheInterface<T> {

	
	/* getObject
	 *   -- Fetches an object from the cache to 
	 *      provide to the CPU.
	 */
	public T getObject(T object);
	
	/* addObject
	 *   -- Adds an object to the cache.
	 */
	public void addObject(T object);	
	
	/* removeObject
	 *   -- Removes an object from the end of the cache. 
	 */
	public void removeObject();	
	
	/* clearCache
	 *   -- Removes an object from the cache. 
	 */
	public void clearCache();
	

	
}
