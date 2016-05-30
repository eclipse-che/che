package p;

public class SomeOtherFieldClass {
	
	SomeOtherFieldClass someOtherFieldClass;
	
	void foo() {
		SomeOtherFieldClass someLocal= someOtherFieldClass;
		
		foo2(someOtherFieldClass);
			
	}

	private void foo2(SomeOtherFieldClass x) {
		// TODO Auto-generated method stub
		
	}

}
