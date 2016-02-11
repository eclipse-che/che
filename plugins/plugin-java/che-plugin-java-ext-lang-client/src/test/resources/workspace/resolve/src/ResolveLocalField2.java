public class ResolveLocalField2 {
	void foo1() {
		
	}
	void foo2() {
		new Object() {
			
		};
		new Object() {
			Object var;
			void bar() {
				var = null;
			}
		};
	}
}