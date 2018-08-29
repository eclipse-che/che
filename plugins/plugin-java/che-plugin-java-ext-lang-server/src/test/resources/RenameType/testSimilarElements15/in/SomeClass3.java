package p;

public class SomeClass3 extends Exception {
	
	public void foo3() {
		
		try {
			throw new SomeClass3();
		} catch (SomeClass3 lvSomeClass3) {
			
		}
	}

}
