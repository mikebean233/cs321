import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Scanner;

/* Cache Class--
 *   Cache object for implementing a cache
 */
public class Cache<T> implements CacheInterface<T> {

	//FIXME
	public double currProgress;

	// Instance variables
	private int sizeL1;
	private int numRefsL1;
	private int numHitsL1;

	private int sizeL2;
	private int numRefsL2;
	private int numHitsL2;

	// LinkedList
	LinkedList<T> cacheL1 = null;
	LinkedList<T> cacheL2 = null;

	// Dictionary File
	File dictFile = null;

	// 2-Level cache flag
	Boolean isTwoLevelCache = null;

	/*
	 *  Cache Constructor --
	 *  @param size Size of the cache implementation  
	 */

	public Cache(int sizeL1, String dictionaryFile) {

		this.sizeL1 = sizeL1;

		numRefsL1 = 0;
		numHitsL1 = 0;
		cacheL1 = new LinkedList<T>();

		dictFile = new File(dictionaryFile);

		isTwoLevelCache = false;
	}

	/*  Cache -- Overloaded constructor
	 * 				Two level cache case
	 * 
	 */

	public Cache(int sizeL1, int sizeL2, String dictionaryFile) {
		this.sizeL1 = sizeL1;
		this.sizeL2 = sizeL2;

		numRefsL1 = 0;
		numHitsL1 = 0;
		cacheL1 = new LinkedList<T>();

		numRefsL2 = 0;
		numHitsL2 = 0;
		cacheL2 = new LinkedList<T>();

		dictFile = new File(dictionaryFile);

		isTwoLevelCache = true;
	}

	public int getnumHits(int cacheLevel) {
		if (cacheLevel==1) {
			return numHitsL1;
		} else {
			return numHitsL2;
		}
	}

	public int getnumRefs(int cacheLevel) {
		if (cacheLevel==1) {
			return numRefsL1;
		} else {
			return numRefsL2;
		}
	}

	public T getObject(T object) {
		numRefsL1++;  // Increment L1 cache refs no matter what

		// Return object placeholder
		T rtnObject = null;
		if(cacheL1.contains(object)) {
			numHitsL1++;
			moveToFront(1,object);
			//  If it's a 2-level cache, move to front for both caches
			if(isTwoLevelCache) {
				moveToFront(2,object);
			}
		} else {

			// Either look in L2, or pull from disk
			addObject(object);
			// Should be in L1 after call to addObject()
			rtnObject = cacheL1.getFirst();
		}


		System.out.println("CurrentProgress: "+currProgress);

		// Print out the cache stats as we go
		System.out.println("CacheL1: ["+numHitsL1+"/"+numRefsL1+"]");

		if (isTwoLevelCache) {
			System.out.println("CacheL2: ["+numHitsL2+"/"+numRefsL2+"]");
		}

		System.out.println();
		// Return the requested object to the caller
		return rtnObject;
	}

	/*
	 * addObject -- 
	 *   Identifies the lowest order location for the requested object,
	 *   and adds it to the top of the L1 cache.  If the object is not 
	 *   found in the L2 cache then it is also added to the top of the 
	 *   L2 cache.
	 * 
	 *   @param 	T 	object	The object to be added to the cache heirarchy
	 *   @return 	void
	 */
	
	// Since this is a generic class, the (T) cast from 
	// a String need to be suppressed.
	@SuppressWarnings("unchecked") 
	public void addObject(T object) {

		// Generic object for storing the requested object
		T obj = null;
		
		// If this is a single level cache, add the object from
		// the dictionary, if it's a 2 level cache, search L2 first
		if (!isTwoLevelCache) {
			
			// Scanner object for adding the object to L1 cache
			Scanner LineScan = null;
			
			// Use a buffered reader to read in the file bytes in large chunks
			try {
				// Assign the scanner reference to a scanner object
				// Specify the buffer size for the buffered reader
				// to be equal to the specified input file, and maximize the gain 
				// from buffering the input stream.
				LineScan = new Scanner(new BufferedInputStream(new FileInputStream(dictFile), (int) dictFile.length()));
			} catch (FileNotFoundException e) {
				// Throw an exception if the file isn't found
				e.printStackTrace();
			}
			
			// Boolean flag for knowing when to stop reading in the file info
			Boolean foundIt = false;
			
			// Loop for reading in the file input lines
			while(LineScan.hasNext() && !foundIt) {
				
				// Create an array of tokens, splitting the line based on whitespace and punctuation
				String[] tokenArr = LineScan.nextLine().split("\\s+|,|;|\t|\\(|\\)|\\.");
				
				// Loop through the array of tokens an compare each one with the requested object
				for(String s : tokenArr) {
					
					// If the current string matches the requested object, we add it to L1, 
					// and stop looping					
					if (s.equals((String) object)) {
						// We found the object
						cacheL1.addFirst((T) s);
						
						// If we've filled the cache, we need to remove an object
						if (cacheL1.size() >= sizeL1) {
							removeObject(1); // Pop the LRU object								
						}
						
						// Don't need the next line
						foundIt=true;

						// Break out of the tokenArray for loop
						break; 
					}
				}
			}
			
			// We're done scanning in lines
			LineScan.close();
		} else {
			
			// Increment the reference count for L2
			numRefsL2++;
			
			// This is a 2 level cache, search L2 first
			if (cacheL2.contains(object)) {
				
				// Increment the hits for L2 cache
				numHitsL2++;
				
				// Move this object to the first position in L2
				moveToFront(2,object);
				
				// Retrieve the first object in L2
				obj = cacheL2.getFirst();
				
				// Add this object to L1's head
				cacheL1.addFirst(obj);
			} else {
				
				// The object was not found in L1 cache, so we need to add it from the dictionary				
				// Pull the object from the dictionary
				// add it to L2
				
				// Scanner object 
				Scanner LineScan = null;
				
				// Use a buffered reader to read in the file bytes in large chunks
				try {
					// Assign the scanner reference to a scanner object
					// Specify the buffer size for the buffered reader
					// to be equal to the specified input file, and maximize the gain 
					// from buffering the input stream.
					LineScan = new Scanner(new BufferedInputStream(new FileInputStream(dictFile), (int) dictFile.length()));
				} catch (FileNotFoundException e) {
					// The file isn't found, throw an exception
					e.printStackTrace();
				}
				
				// Boolean flag to stop looping
				Boolean foundIt = false;
				
				// Loop through the input file until the object is found
				while(LineScan.hasNext() && !foundIt) {
					
					// Create an array of tokens, splitting the line based on whitespace and punctuation
					String[] tokenArr = LineScan.nextLine().split("\\s+|,|;|\t|\\(|\\)|\\.");
					
					// Loop through the array of tokens an compare each one with the requested object
					for(String s : tokenArr) {

						// If the current string matches the requested object, we add it to L1, 
						// and stop looping	
						if (s.equals((String) object)){
							// We found the object
							cacheL2.addFirst((T) s);
							
							// Remove the Least recently used object in L2, if it's full
							if (cacheL2.size() >= sizeL2) {
								removeObject(2); // Pop the LRU object								
							}

							// Retrieve the head node from the L2 cache
							obj = cacheL2.getFirst();
							
							// Assert the boolean to stop reading in lines
							foundIt = true;
							
							// Add the newfound object to the head of the L1 cache
							cacheL1.addFirst(obj);

							// Remove the LRU object from L1 cache if it's full
							if (cacheL1.size() >= sizeL1) {
								removeObject(1); // Pop the LRU object								
							}
							
							// Break out of the tokenArray for loop
							break;
						}
					}
				}
				// Close the input stream for rerading in lines
				LineScan.close();
			}
		}
	}
	
	/*
	 * removeObject --
	 *   Removes the Last recently used object from the cache.
	 * 
	 *   @param 	Integer		cacheLevel	Which cache are we requesting? 
	 *   @return	void
	 */
	
	public void removeObject(int cacheLevel) {
		// We're only removing the last object in the cache,
		// So we don't need to specify which one to remove.
		if (cacheLevel == 1) {
			cacheL1.removeLast();
		} else {
			cacheL2.removeLast();
		}
	}

	/*
	 * clearCache --
	 *   Clears out the contents of the requested cache.
	 * 
	 *   @param 	Integer		cacheLevel	Which cache are we requesting? 
	 *   @return	void
	 */
	
	public void clearCache(int cacheLevel) {
		// Clear out the cache
		if (cacheLevel==1) {
			cacheL1.clear();			
		} else {
			cacheL2.clear();
		}
	}

	/*
	 * moveToFront --
	 *   Moves the requested object to the front of 
	 *   the cache being requested.  The cache implementations
	 *   are done with linkedLists, so these nodes are removed
	 *   from their current position and moved to the head node.
	 * 
	 *   @param 	Integer		cacheLevel	Which cache are we requesting? 
	 * 	 @param		T			object		The object we're moving
	 *   @return	void
	 */
	
	private void moveToFront(int cacheLevel, T object){

		// Switch based on which cache level we're interested in
		switch (cacheLevel) {
			// L1 cache is requested
			case 1:
				// Add the object to the first position
				// and remove it from its former position
				cacheL1.addFirst(object);
				cacheL1.remove(object);			
				break;
			case 2:
				// Add the object to the first position
				// and remove it from its former position
				cacheL2.addFirst(object);
				cacheL2.remove(object);
				break;
			default:
				// Nothing here
				break;
		}
	}

	/*
	 * toString override --
	 *   Since this class handles either a 1-Cache or a 2-Cache
	 *   we need to switch between either printing one or printing
	 *   2.
	 *      
	 * @param 	void
	 * @return 	String	outStr 		Contents of the cache(s)
	 */

	public String toString() {

		// Empty string to store the cache contents
		String outStr = "";

		// Call makeString for the L1 cache
		// Append to this output string
		outStr += makeString(1);

		// Append a line feed to the output string
		outStr+="\n\n";

		// If it's a 2-Cache, call makeString again to 
		// Build up the L2 cache contents
		if(isTwoLevelCache) {
			outStr += makeString(2);
		}

		// Return the output string to the caller
		return outStr;
	}

	/*
	 * makeString --
	 *   Fills an String variable with the cache contents and 
	 *   returns it to the caller.
	 *      
	 * @param  	Int 	cacheLevel	Are we requesting the L1 
	 * 								cache or the L2 cache?	
	 * 
	 * @return 	String 	outStr		Contents of the cache
	 */
	
	public String makeString(int cacheLevel) {

		// Prints out the contents of the cache
		String outStr = new String();

		// Holds the size of the cache that's being requested
		String cacheSize;
		
		// Use the iterator to step through the elements of
		// the particular cache we're interested in.
		Iterator<T> cacheIter = null;

		// Pull in the cache size and get an iterator reference
		// for the requested cache.
		if (cacheLevel==1) {
			cacheSize = Integer.toString(sizeL1);
			cacheIter = cacheL1.descendingIterator();
		} else {
			cacheSize = Integer.toString(sizeL2);
			cacheIter = cacheL2.descendingIterator();
		}

		// Build up the output string's header
		outStr+="Cache["+cacheSize+"] Contents: \n {";

		// Step through the cache, using the iterator
		while(cacheIter.hasNext()) {
			outStr+="\t"+cacheIter.next().toString()+"\n";

		}
		
		// Finish off the output string
		outStr+=" }\n";
		
		// Return the output string to the caller
		return outStr;
	}
}