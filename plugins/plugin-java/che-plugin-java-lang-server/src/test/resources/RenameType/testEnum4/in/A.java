package p;
class Outer {
    enum A {
       ONE, TWO, THREE
    }
}

class User {
    /**
     * Uses {@link p.Outer.A#ONE}.
     */
    Outer.A a= p.Outer.A.ONE;
}
