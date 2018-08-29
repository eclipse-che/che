package p;

/**
 * @see I#num()
 */
class A {
    int getIofI() {
        I i= I.class.getAnnotation(I.class);
        return i.num();
    }
}

@interface I {
    int num();
}
