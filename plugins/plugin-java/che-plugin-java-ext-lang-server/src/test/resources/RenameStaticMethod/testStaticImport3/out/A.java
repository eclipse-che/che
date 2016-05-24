package p;
// static import imports field and method
import static p.A.k;
import static p.A.m;

public class A {
     public static void k() { }
     public static int m;
}

class B {
    void use() {
        int t= m;
        k();
    }
}