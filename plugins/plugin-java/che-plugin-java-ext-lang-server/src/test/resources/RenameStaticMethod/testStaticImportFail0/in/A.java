package p;
// rename A.m() -> k(): reference in B will be shadowed by B#k()
import static p.A.m;

public class A {
     public static void m() { }
     public static int m;
}

class B {
    void use() {
        int t= m;
        m();
    }
    void k() {}
}