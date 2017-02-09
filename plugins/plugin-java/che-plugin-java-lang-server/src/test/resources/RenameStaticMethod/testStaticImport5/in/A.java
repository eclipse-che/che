package p;
//static import imports two methods
import static p.A.m;

public class A {
    public static void m() { }
    public static void m(int arg) { }
}

class B {
    void use() {
        m();
        m(1);
    }
}