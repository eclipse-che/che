public class ResolveLocalField {
	void foo() {
		class Y {
			public int fred;
			public void bar() {
				this.fred = 0;
			}
		}
	}
}