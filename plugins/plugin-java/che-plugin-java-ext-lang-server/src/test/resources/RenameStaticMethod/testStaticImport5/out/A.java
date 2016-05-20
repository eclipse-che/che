package p;
//static import imports two methods
import static p.A.k;
import static p.A.m;

public class A {
    public static void k() { }
    public static void m(int arg) { }
}

class B {
    void use() {
        k();
        m(1);
    }
}