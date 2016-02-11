public class ResolveLocalClass7 {
	void foo1() {
		
	}
	void foo2() {
		new Object() {
			
		};
		new Object() {
			class X {
				
			}
			void bar() {
				X var;
			}
		};
	}
}