package p;

import static p.A.m;

public class A {
    public static int m() {
        return 0;
    }
}

class B {
    void use() {
        int t= m();
    }
}