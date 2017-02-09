package p;

class A<T>{
	T tee;
	
	public T getTee() {
		return tee;
	}
	
	public void setTee(T t) {
		tee= t;
	}
}

class B {
	static {
		A a= new A();
		Object o= a.tee;
		
		A<Number> an= new A<Number>();
		an.setTee(new Double(1.3d));
		
		A<? extends Number> at= new A<Integer>();
		Number tee=at.getTee();
		at.setTee(null);
	}
}