//renaming to: j
package p;
class A{
	int j;
	int m(int j){
		new A(){
			int m(int i){
				return i;
			}
		};
		return j + m(m(j));
	};
}   