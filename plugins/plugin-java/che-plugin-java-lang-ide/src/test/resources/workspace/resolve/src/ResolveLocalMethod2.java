public class ResolveLocalMethod2 {
	void foo1() {
		
	}
	void foo2() {
		new Object() {
			
		};
		new Object() {
			void bar() {
			}
			void toto() {
				bar();
			}
		};
	}
}