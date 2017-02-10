package p;

class A {
    A covariant() { return null; }
}

class B extends A {
    B covariant() { return null; }
}

class C {
    /**
     * @see A#covariant()
     * @see B#covariant()
     * @return A#covariant()
     */
    A covariant() { 
        return true ? new A().covariant() : new B().covariant();
    }
}
