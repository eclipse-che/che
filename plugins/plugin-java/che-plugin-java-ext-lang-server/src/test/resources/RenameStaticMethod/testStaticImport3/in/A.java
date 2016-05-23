package p;
// static import imports field and method
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
}