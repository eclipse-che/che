//cannot rename to: j
package p;
interface B{
	int j= 0;
}
class A implements B{
	int m(int i){
		i= j;
		return 0;
	};
}