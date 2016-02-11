class Test {
    public static void main(String[] args) {
        new A<Number>().k(new Double(1));
        new A<Integer>().k(new Integer(2));

        new Impl().m(new Integer(3));
        new Impl().k(new Float(4));
        
        A<Number> a= new Impl();
        a.k(new Integer(6));
        a.k(new Double(7));
    }
}


class A<G> {
	void k(G g) { System.out.println("A#m(G): " + g); }
}

class Impl extends A<Number> {
	void m(Integer g) { System.out.println("nonripple Impl#m(Integer): " + g);}
	void k(Number g) { System.out.println("Impl#m(Number): " + g); }
}
