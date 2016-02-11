//rename to: j
package p;
class A{
	A i;
	A m(A j){
		return j.m(j.m(this.i));
	}
}