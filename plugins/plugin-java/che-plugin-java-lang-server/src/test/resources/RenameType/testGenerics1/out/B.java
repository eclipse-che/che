package p;
class B<T> {
    public B() {}
    public <T>B(T t) {}
    public <X>B(T t, X x) {}

    void m(B a) {
        new B<T>();
        new B<T>(null);
        new <String>B<T>(null, "y");
    };
}

class X {
    void x(B a) {
        new B<Integer>();
        new B<Integer>(null);
        new <String>B<Integer>(new Integer(1), "x");
    };
}
