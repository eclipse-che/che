//can't rename m to wait
//see the lang spec: 9.2
package p;
interface I{
	String m() throws InterruptedException;
}