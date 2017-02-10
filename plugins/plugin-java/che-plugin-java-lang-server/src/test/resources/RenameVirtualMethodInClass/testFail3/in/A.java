package p;
//can't rename m in A - must do it in B
class B{
	int m(){}
}
class C extends B{
}
class A extends C{
	int m(){}
}