package p;

import static p.A.*;

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