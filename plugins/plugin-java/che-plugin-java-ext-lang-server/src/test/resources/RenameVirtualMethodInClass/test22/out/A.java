package p;
//renaming A.m to k
class B{
	private void m(){
		System.out.println("B.m");	
	}
	void fred(){
		m();
		B b= new B();
		b.m();	
		B b1= new A();
		b1.m();
		B bc= new C();
		bc.m();
		
		A ba= new A();
		ba.k();
		A ac= new C();
		ac.k();
		C c= new C();
		c.k();
		
	}
}
class A extends B{
	void k(){
		System.out.println("A.m");	
	}
}
class C extends A{
	void k(){
		System.out.println("C.m");	
	}
}

class test{
	public static void main(String[] args){
		new B().fred();
	}
}