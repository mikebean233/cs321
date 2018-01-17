
public class CacheTester {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		int cacheSize = Integer.parseInt(args[0]);
		String dictionaryFileName = args[1];
		
		Cache<String> cacheTest = new Cache(cacheSize,dictionaryFileName);
		
		// Check to see the contents of the cache
		System.out.println(cacheTest);
		
	}

}
