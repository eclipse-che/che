//rename to j
package p;
class A{
	void m(final int i){
		A a= new A(){
			void m(int k){
				k= i;
			}
		};
	}
}