package p;
class A {
  protected void foo(Object o){}
  protected void foo(String s){}
}
class B extends A{
	protected void foo(Object o){}
	protected void foo(String s){}
}