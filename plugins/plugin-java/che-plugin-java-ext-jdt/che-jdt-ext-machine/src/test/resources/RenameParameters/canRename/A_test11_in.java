//rename to j
package p;
class A{
	int k;
	void m(int i){
		A a= new A(){
			void m(int i){
				i++;
			}
		};
	}
}