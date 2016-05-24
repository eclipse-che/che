//renaming to: j
package p;
class A{
	int j;
	int m(int i){
		new A(){
			int m(int i){
				return i;
			}
		};
		return i + m(m(i));
	};
}   