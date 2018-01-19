import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import javax.swing.text.html.HTMLDocument.Iterator;

public class CacheTester {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

		Cache<String> cacheTest = null;
		
		
		if (Integer.parseInt(args[0])==1) {
			// 1 Level cache
			int cacheSize = Integer.parseInt(args[1]);
			String dictFileName = args[2];
			
			File dictFile = new File(dictFileName);
			
			// How many tokens should we test?
			int numTokens = Integer.parseInt(args[3]);
			
			int tokenIdx = 0;
			
			Scanner LineScan = null;
			
			// Generate a list of random numbers to build the test array
			Integer[] chooseWords = new Integer[numTokens];
			String[] chosenWords = new String[numTokens];
			Random rand = new Random();
			
			
			for (int i=0; i<numTokens; i++) {
				// build up chooseWords
				chooseWords[i] = 5;//rand.nextInt(500) + 1; // Choose a random number <1,500>
			}
			
			
			try {
				LineScan = new Scanner(new BufferedInputStream(new FileInputStream(dictFile)));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			int currChooseWord = chooseWords[0];
			int chooseWordIdx = 1;
			int wordIdx = 0;
			
			boolean done = false;
			
			while(LineScan.hasNext() && !done) {
				Scanner tokenScan = new Scanner(LineScan.nextLine());
				while (tokenScan.hasNext() && !done) {
					String thisToken = tokenScan.next();
					wordIdx++;
					if (wordIdx==currChooseWord){
						chosenWords[chooseWordIdx-1] = thisToken;// + "\t["+wordIdx+"]";
						wordIdx=0;
						currChooseWord = chooseWords[chooseWordIdx];

						chooseWordIdx++;
					}
					
					if (chooseWordIdx==numTokens) {
						// Break out
						done = true;
					}
					
				}
				tokenScan.close();
			}
			LineScan.close();
			
//			for(String t : chosenWords) {
//				System.out.println(t);
//			}
			
//			System.exit(0);
			
			// Construct the 1-Cache and test
			cacheTest = new Cache<String>(cacheSize,dictFileName);	
			
			for(String s : chosenWords) {
				cacheTest.getObject(s);
//				System.out.println(cacheTest);
			}
			
			// Print out the cache contents
//			System.out.println(cacheTest);
			
			// Print out the stats
			System.out.println("L1 Cache Stats:\n\t");
			System.out.println("numHits: "+cacheTest.getnumHits(1)+"\t");
			System.out.println("numRefs: "+cacheTest.getnumRefs(1));
			
			
			
//			System.out.println(cacheTest);			
			
		} else if (Integer.parseInt(args[0])==2) {
			// 2-Level cache
			int cacheSizeL1 = Integer.parseInt(args[1]);
			int cacheSizeL2 = Integer.parseInt(args[2]);
			String dictFileName = args[3];

			File dictFile = new File(dictFileName);
			
			// Construct the 2-Cache and test
			cacheTest = new Cache<String>(cacheSizeL1, cacheSizeL2,dictFileName);
			
//			System.out.println(cacheTest);
		
			// How many tokens should we test?
			int numTokens = 50;
			
			if(args.length==5) {
				numTokens = Integer.parseInt(args[4]);
			}
			
			int tokenIdx = 0;
			
			Scanner LineScan = null;
			
			// Generate a list of random numbers to build the test array
			Integer[] chooseWords = new Integer[numTokens];
			String[] chosenWords = new String[numTokens];
			Random rand = new Random();
			
			
			for (int i=0; i<numTokens; i++) {
				// build up chooseWords
				chooseWords[i] = 1;//rand.nextInt(500) + 1; // Choose a random number <1,500>
			}
			
			
			try {
				LineScan = new Scanner(new BufferedInputStream(new FileInputStream(dictFile)));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			int currChooseWord = chooseWords[0];
			int chooseWordIdx = 1;
			int wordIdx = 0;
			
			boolean done = false;
	
			cacheTest = new Cache<String>(cacheSizeL1,cacheSizeL2,dictFileName);
			
			while(LineScan.hasNext() && !done) {
				Scanner tokenScan = new Scanner(LineScan.nextLine());
				while (tokenScan.hasNext() && !done) {
					String thisToken = tokenScan.next();
					wordIdx++;
				
					if (args.length==5) { // # tokens to test is specified
					
						if (wordIdx==currChooseWord){
							chosenWords[chooseWordIdx-1] = thisToken;// + "\t["+wordIdx+"]";
							wordIdx=0;
							currChooseWord = chooseWords[chooseWordIdx];
		
							chooseWordIdx++;
						}
						
						if (chooseWordIdx==numTokens) {
							// Break out
							done = true;
						}
					} else {
						cacheTest.getObject(thisToken);
					}
					
				}
				tokenScan.close();
			}
			LineScan.close();
			
	//		for(String t : chosenWords) {
	//			System.out.println(t);
	//		}
			
	//		System.exit(0);
			
			// Construct the 1-Cache and test
	
			int loopIdx = 0;
			if (args.length==5) {
				for(String s : chosenWords) {
	//				System.out.println("Current Loop: ["+loopIdx+"]\n\n");
					cacheTest.getObject(s);
		//			System.out.println(cacheTest);
					loopIdx++;
				}
			}
			
			// Print out the cache contents
	//		System.out.println(cacheTest);
			
			// Print out the stats
			System.out.println("..............................");
			int totRefs = cacheTest.getnumRefs(1)+cacheTest.getnumRefs(2);
			int totHits = cacheTest.getnumHits(1)+cacheTest.getnumHits(2);
			System.out.println("Total number of references:\t"+totRefs);
			System.out.println("Total number of cache hits:\t"+totHits);
			double globalHitRatio = ((double)totHits/(double)totRefs);
			Double PrglobalHitRatio = new Double(globalHitRatio);
			System.out.println("L1 Refs: "+cacheTest.getnumRefs(1));
			System.out.println("The global hit ratio\t\t\t\t"+((double)totHits/(double)cacheTest.getnumRefs(1)));
			System.out.println("Number of 1st-level cache hits:\t"+(cacheTest.getnumHits(1)));
			System.out.println("1st-level cache hit ratio\t\t\t"+(((double)cacheTest.getnumHits(1)/(double)cacheTest.getnumRefs(1))));
			System.out.println("Number of 2nd-level cache hits:\t"+cacheTest.getnumHits(2));
			System.out.println("2nd-level cache hit ratio\t\t\t"+(((double)cacheTest.getnumHits(2)/(double)cacheTest.getnumRefs(2))));
		} else {
			throw new Exception("Error! Incorrect Arguments Provided!");
		}		
	}
}
