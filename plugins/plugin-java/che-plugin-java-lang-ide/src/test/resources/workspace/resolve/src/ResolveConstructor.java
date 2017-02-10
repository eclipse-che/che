public class ResolveConstructor {
	public ResolveConstructor(int i) {
	}
	public	ResolveConstructor(String s) {
	}
	public void foo() {
		ResolveConstructor c = new ResolveConstructor("");
	}
}