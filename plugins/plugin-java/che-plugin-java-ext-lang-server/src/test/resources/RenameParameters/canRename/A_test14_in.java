//rename to: j
package p;
class A{
	A i;
	A m(A i){
		return i.m(i.m(this.i));
	}
}