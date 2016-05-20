package p;
class Outer {
    enum B {
       ONE, TWO, THREE
    }
}

class User {
    /**
     * Uses {@link p.Outer.B#ONE}.
     */
    Outer.B a= p.Outer.B.ONE;
}
