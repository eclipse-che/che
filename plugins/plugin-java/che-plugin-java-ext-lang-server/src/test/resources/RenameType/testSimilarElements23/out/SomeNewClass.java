package p;

public class SomeNewClass {
	
	SomeNewClass anotherSomeNewClass= new SomeNewClass() {
		
		private void foo() {
			
			class X {
				SomeNewClass someNewClassInInner;
			}
		}
	};

}
