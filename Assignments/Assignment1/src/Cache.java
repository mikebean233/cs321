import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Scanner;

/* Cache Class--
 *   Cache object for implementing a cache
 */
public class Cache<T> implements CacheInterface<T> {

	// Instance variables
	private int size;
	private int numRefs;
	private int numHits;
	private boolean isL1;
	
	// LinkedList
	LinkedList<T> cache = null;
	
	// Lower Level of cache
	Cache<T> l0_Cache = null;
	
	/*
	 *  Constructor --
	 *  @param size Size of the cache implementation  
	 */
	
	public Cache(int size, String dictionaryFile, boolean isL1) {

		// Initialize the instance variables and linkedList object
		this.size = size;
		numRefs = 0;
		numHits = 0;
		cache = new LinkedList<T>();	
		
		// If it's the L1 cache, assert the flag
		this.isL1 = isL1;
		
		// If it's L2 Cache, we need a reference to the L1
		if (!isL1) {
			l0_Cache = new Cache<T>(l1Size,)
		}
		
		// Fill up the cache (to have something to start with)
		try {
			fillCache(dictionaryFile);
		} catch (Exception e) {
			System.out.println("Error!  "+dictionaryFile+" could not be found!");
		}
	}

	@Override
	public T getObject(T object) {
		numRefs++;
		
		T rtnObject = null;
		if(cache.contains(object)) {
			numHits++;
			moveToFront(object);
		} else {
			// Pull the object from a lower level of memory
			if (isL1) {
				// We need to fill from the disk (dictionary file)
				rtnObject = findObject(object);
			} else {
				// We need to look in a lower level of hierarchy
				
			}
			addObject(rtnObject);
		}
		
		// Return the requested object to the caller
		return rtnObject;
	}

	private T findObject(T object) {
		// Finds the requested object in a lower level of hierarchy
		T rtnObject = null;
		
		// Using an ArrayList of memory types (Disk, L1 Cache, L2 Cache)
		// Search for the object in the next level down
		
		return rtnObject;
	}
	
	@Override
	public void addObject(T object) {
		// TODO Auto-generated method stub
		if(cache.size()==size) {
			removeObject();
		}
		cache.addFirst(object);
	}

	@Override
	public void removeObject() {
		// We're only removing the last object in the cache,
		// So we don't need to specify which one to remove.
		cache.removeLast();
	}

	@Override
	public void clearCache() {
		// Clear out the cache
		cache.clear();
	}
	
	// Private methods
	private void moveToFront(T object) {
		// Temp variable for storing the old first element
		Object first = null;
		
		if (!cache.contains(object)) {
			throw new NoSuchElementException();
		} else {
			first = cache.getFirst();
			cache.set(0, object);
//			cache.
		}
		
	}

	private void fillCache(String dict) throws FileNotFoundException {
		// TODO Auto-generated method stub
		
		int wordCount = 0; // Track how many words have been added
		
		// Open up the dictionary
		File dictFile = new File(dict);
		
		// Fail fast if the input dictionary file doesn't exist
		if (!dictFile.exists()) {
			throw new FileNotFoundException();
		}
		
		Scanner fileScan = new Scanner(dictFile);
		// Open the dictionary and read in the file's contents
		while (fileScan.hasNextLine()) {
			String line = fileScan.nextLine();
			Scanner scanToken = new Scanner(line);
			while(scanToken.hasNext()) {
				addObject((T)scanToken.next());	
				wordCount++;
				if(wordCount>=size) {
					scanToken.close();
					fileScan.close();
					return;
				}
			}
			scanToken.close();
		}
		fileScan.close();
	}
	
	public String toString() {
		// Prints out the contents of the cache
		String outStr = new String();
		
		// Build up the output string's header
		outStr+="Cache["+size+"] Contents: \n {";
		
		int iterIdx=0;
		
		Iterator<T> cacheIter = cache.descendingIterator();
		
		while(cacheIter.hasNext()) {
				outStr+="\t"+cacheIter.next().toString()+"\n";
			
		}
		outStr+=" }\n";
		return outStr;
	}
}
