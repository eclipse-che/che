package p;
@FunctionalInterface
interface I {
    int m();
}

public class A implements I {
    I i1= this::m;
    I i2= A::length;

    @Override
    public int m() {
        return 0;
    }

    public static int length() {
        return 42;
    }
}
