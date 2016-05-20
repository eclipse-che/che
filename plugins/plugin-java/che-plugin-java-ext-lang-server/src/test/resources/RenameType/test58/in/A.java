package p;
class Sup{
	static int CONSTANT= 0;
}
class A extends Sup {
}

class Test {
  public static void main(String[] arguments) {
    System.out.println(A.CONSTANT);
  }
}