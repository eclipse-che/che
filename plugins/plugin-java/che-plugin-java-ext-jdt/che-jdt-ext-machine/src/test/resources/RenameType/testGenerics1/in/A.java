package p;
class A<T> {
    public A() {}
    public <T>A(T t) {}
    public <X>A(T t, X x) {}

    void m(A a) {
        new A<T>();
        new A<T>(null);
        new <String>A<T>(null, "y");
    };
}

class X {
    void x(A a) {
        new A<Integer>();
        new A<Integer>(null);
        new <String>A<Integer>(new Integer(1), "x");
    };
}
