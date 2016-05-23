package p;

class A<E> {
    @I(num = 12)
    @interface I {
        @I(num=13)
        int num();
    }
    
    /**
     * @see I#num()
     */
    class Ref {
        int getIofI() {
            I i= I.class.getAnnotation(I.class);
            return i.num();
        }
    }
}