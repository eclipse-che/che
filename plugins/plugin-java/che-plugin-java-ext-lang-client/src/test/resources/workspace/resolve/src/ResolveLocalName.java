public class ResolveLocalName {
	public void foo(){
		Object var1 = new Object();
		int var2 = 1;
		var1.toString();
		var2++;
		if (var2 == 3) {
			Object var3 = var1;
			var3.hashCode();
		} else {
			Object var3 = new Object();
			var3.toString();
		}
		final int var4 = 1;
		new Object() {
			public String toString() {
				return "var4 = " + var4;
			}
		};
	}
}