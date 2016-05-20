class Test {
    public static void main(String[] args) {
        new A<Number>().m(new Double(1));
        new A<Integer>().m(new Integer(2));

        new Impl().m(new Integer(3));
        new Impl().m(new Float(4));
        
        A<Number> a= new Impl();
        a.m(new Integer(6));
        a.m(new Double(7));
    }
}


class A<G> {
	void m(G g) { System.out.println("A#m(G): " + g); }
}

class Impl extends A<Number> {
	void m(Integer g) { System.out.println("nonripple Impl#m(Integer): " + g);}
	void m(Number g) { System.out.println("Impl#m(Number): " + g); }
}
