package p;

class A<T>{
	T thing;
	
	public T getThing() {
		return thing;
	}
	
	public void setThing(T t) {
		thing= t;
	}
}

class B {
	static {
		A a= new A();
		Object o= a.thing;
		
		A<Number> an= new A<Number>();
		an.setThing(new Double(1.3d));
		
		A<? extends Number> at= new A<Integer>();
		Number tee=at.getThing();
		at.setThing(null);
	}
}