public class ResolveLocalConstructor {
	void foo() {
		class Y {
			public Y(int i) {
			}
			public	Y(String s) {
			}
			public void bar() {
				Y c = new Y("");
			}
		}
	}
}