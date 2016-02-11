public class ResolveLocalMethod {
	void bar() {
		class Y {
			void foo(int i) {
			}
			void foo(String s) {
			}
			void bar() {
				new Y().foo("");
			}
		}
	}
}