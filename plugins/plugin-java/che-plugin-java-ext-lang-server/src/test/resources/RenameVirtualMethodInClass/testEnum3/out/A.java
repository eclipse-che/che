package p;

class Generic<E> {
    enum A {
        ONE ;
        A get2ndPower() {
            return ONE;
        }
    }
    boolean test() {
        return A.ONE == A.ONE.get2ndPower();
    }
}
