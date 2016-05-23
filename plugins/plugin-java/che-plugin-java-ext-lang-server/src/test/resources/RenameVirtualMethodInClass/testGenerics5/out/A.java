package p;

class A {
    A variant() { return null; }
}

class B extends A {
    B variant() { return null; }
}

class C {
    /**
     * @see A#variant()
     * @see B#variant()
     * @return A#covariant()
     */
    A covariant() { 
        return true ? new A().variant() : new B().variant();
    }
}
