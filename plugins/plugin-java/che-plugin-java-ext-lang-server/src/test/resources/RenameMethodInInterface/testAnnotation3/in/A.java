package p;

/**
 * @see I#value()
 */
class A {
    int getIofI() {
        I i= I.class.getAnnotation(I.class);
        return i.value();
    }
}

@interface I {
    int value();
}
