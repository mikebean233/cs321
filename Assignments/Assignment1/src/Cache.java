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
		//if (!isTwoLevelCache /*cacheLevel==1*/) {
			if(cacheL1.contains(object)) {
				numHitsL1++;
				try {
					moveToFront(1,object);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//  If it's a 2-level cache, move to front for both caches
				if(isTwoLevelCache) {
					numHitsL2++;
					try {
						moveToFront(2,object);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				
			} else {
				
				numRefsL2++;
				// Either look in L2, or pull from disk
				addObject(object);
				// Should be in L1 after call to addObject()
				rtnObject = cacheL1.getFirst();

			}

		// Return the requested object to the caller
		return rtnObject;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addObject(T object) {

		T obj = null;
		
		// If this is a single level cache, add the object from
		// the dictionary, if it's a 2 level cache, search L2 first
		if (!isTwoLevelCache) {
			// The object was not found in L1 cache, so we need to add it from the dictionary
			Scanner LineScan = null;
			try {
				LineScan = new Scanner(new BufferedInputStream(new FileInputStream(dictFile)));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Boolean foundIt = false;
			while(LineScan.hasNext() && !foundIt) {
				Scanner tokenScan = new Scanner(LineScan.nextLine());
				while (tokenScan.hasNext() && !foundIt) {
					String thisToken = tokenScan.next();
					if (thisToken.equals((String) object)){
						// We found the object
						cacheL1.addFirst((T) thisToken);
						if (cacheL1.size() >= sizeL1) {
							removeObject(1); // Pop the LRU object								
						}
						foundIt=true;
					}
				}
				tokenScan.close();
			}
			LineScan.close();
		} else {
			// This is a 2 level cache, search L2 first
			if (cacheL2.contains(object)) {
				numHitsL2++;
				// Move this object to the first position in L2
				try {
					moveToFront(2,object);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				obj = cacheL2.getFirst();
				cacheL2.addFirst(obj);
			} else {
				// Pull the object from the dictionary
				// And add it to L2
				// The object was not found in L1 cache, so we need to add it from the dictionary
				Scanner LineScan = null;
				try {
					LineScan = new Scanner(new BufferedInputStream(new FileInputStream(dictFile)));
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Boolean foundIt = false;
				while(LineScan.hasNext() && !foundIt) {
					Scanner tokenScan = new Scanner(LineScan.nextLine());
					
					while (tokenScan.hasNext() && !foundIt) {
						String thisToken = tokenScan.next();
						
						if (thisToken.equals((String) object)){
							// We found the object
							cacheL2.addFirst((T) thisToken);
							if (cacheL2.size() >= sizeL2) {
								removeObject(2); // Pop the LRU object								
							}

							obj = cacheL2.getFirst();
							foundIt = true;
							cacheL1.addFirst(obj);
							
							if (cacheL1.size() >= sizeL1) {
								removeObject(1); // Pop the LRU object								
							}
						}
					}
					tokenScan.close();
				}
				LineScan.close();
			}
			
			
		}
	}

	@Override
	public void removeObject(int cacheLevel) {
		// We're only removing the last object in the cache,
		// So we don't need to specify which one to remove.
		if (cacheLevel == 1) {
			cacheL1.removeLast();
		} else {
			cacheL2.removeLast();
		}
	}

	@Override
	public void clearCache(int cacheLevel) {
		// Clear out the cache
		if (cacheLevel==1) {
			cacheL1.clear();			
		} else {
			cacheL2.clear();
		}
	}
	
	// Private methods
	private void moveToFront(int cacheLevel, T object) throws Exception {
		switch (cacheLevel) {
			case 1:
				if (!cacheL1.contains(object)) {
					throw new NoSuchElementException();
				} else {
					cacheL1.addFirst(object);
					//FIXME -- Remove the object from its former position
					cacheL1.remove(object);
				}			
				break;
			case 2:
				if (!cacheL2.contains(object)) {
					throw new NoSuchElementException();
				} else {
					cacheL2.addFirst(object);
					//FIXME -- Remove the object from its former position
					cacheL2.remove(object);
				}	
				break;
			default:
				throw new Exception();
//				break;
		}
	}

	
	public String toString() {

		String outStr = "";
		
		outStr += makeString(1);
		
		outStr+="\n\n";
		
		if(isTwoLevelCache) {
			outStr += makeString(2);
		}
		
		return outStr;
	}
	
	public String makeString(int cacheLevel) {
		// Prints out the contents of the cache
		String outStr = new String();
		
		String cacheSize;
		Iterator<T> cacheIter = null;
		
		
		if (cacheLevel==1) {
			cacheSize = Integer.toString(sizeL1);
			cacheIter = cacheL1.descendingIterator();
		} else {
			cacheSize = Integer.toString(sizeL2);
			cacheIter = cacheL2.descendingIterator();
		}
		
		// Build up the output string's header
		outStr+="Cache["+cacheSize+"] Contents: \n {";
		
		while(cacheIter.hasNext()) {
				outStr+="\t"+cacheIter.next().toString()+"\n";
			
		}
		outStr+=" }\n";
		return outStr;
	}
}
