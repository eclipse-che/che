package p;

public class SomeFieldClass {
	
	SomeFieldClass someFieldClass;
	
	void foo() {
		SomeFieldClass someLocal= someFieldClass;
		
		foo2(someFieldClass);
			
	}

	private void foo2(SomeFieldClass x) {
		// TODO Auto-generated method stub
		
	}

}
