package p;
public class A{
	public static int length(){ return 0;};
}
class F{
	static class B{
		public static int length(){ return 42;};
	};
}
class FF extends F{
	int m(){
		return A.length();
	}
}
