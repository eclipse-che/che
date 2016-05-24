//renaming to: j
package p;
class A{
	int j;
	int m(final int i){
		new A(){
			int m(int o){
				return i;
			}
		};
		return i + m(m(i));
	};
}