package p;
@FunctionalInterface
interface I {
    int k();
}

public class A implements I {
    I i1= this::k;
    I i2= A::length;

    @Override
    public int k() {
        return 0;
    }

    public static int length() {
        return 42;
    }
}
