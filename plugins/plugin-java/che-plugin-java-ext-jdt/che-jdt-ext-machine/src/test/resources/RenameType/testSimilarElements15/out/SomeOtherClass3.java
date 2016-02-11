package p;

public class SomeOtherClass3 extends Exception {
	
	public void foo3() {
		
		try {
			throw new SomeOtherClass3();
		} catch (SomeOtherClass3 lvSomeOtherClass3) {
			
		}
	}

}
