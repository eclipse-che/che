package p;
public class A{
	public static int length(){ return 0;};
}
interface I{
	static class B{
		public static int length(){ return 0;};
	}
}
class FF implements I{
	int m(){
		return A.length();
	}
}