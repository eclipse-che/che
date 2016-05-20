//cannot rename to: j
package p;
class A{
	void m(int i){
		try{
			m(1);
		}
		catch (Throwable j){
		}
	};
}