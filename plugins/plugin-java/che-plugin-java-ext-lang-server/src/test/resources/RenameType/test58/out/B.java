package p;
class Sup{
	static int CONSTANT= 0;
}
class B extends Sup {
}

class Test {
  public static void main(String[] arguments) {
    System.out.println(B.CONSTANT);
  }
}