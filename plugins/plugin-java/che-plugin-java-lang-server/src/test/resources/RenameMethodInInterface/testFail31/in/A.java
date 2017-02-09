//can't rename A.toString
package p;
interface I{
	public String toString();
}
class A implements I{
	public java.lang.String toString();
}