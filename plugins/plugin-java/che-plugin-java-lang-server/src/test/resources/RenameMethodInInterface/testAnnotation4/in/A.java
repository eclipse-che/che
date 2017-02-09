package p;

class A<E> {
    @I(12)
    @interface I {
        @I(value=13)
        int value();
    }
    
    /**
     * @see I#value()
     */
    class Ref {
        int getIofI() {
            I i= I.class.getAnnotation(I.class);
            return i.value();
        }
    }
}