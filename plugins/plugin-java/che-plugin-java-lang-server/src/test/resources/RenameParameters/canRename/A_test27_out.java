//renaming to: j
package p;
class A{
	int j;
	int m(final int j){
		new A(){
			int m(int o){
				return j;
			}
		};
		return j + m(m(j));
	};
}