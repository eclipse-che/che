package p;

public class SomeClass {
	
	SomeClass anotherSomeClass= new SomeClass() {
		
		private void foo() {
			
			class X {
				SomeClass someClassInInner;
			}
		}
	};

}
