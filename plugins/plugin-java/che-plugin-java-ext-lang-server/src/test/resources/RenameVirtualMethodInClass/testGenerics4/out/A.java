package p;

/**
 * @see #doit(Number)
 * @see Sub#doit(Number)
 * @see Sub#doit(Number)
 * @see Unrelated1#takeANumber(Number)
 * @see Unrelated1#takeANumber(Object)
 * @see Unrelated1#takeANumber(Number)
 * @see Unrelated1#takeANumber(Integer)
 * @see Unrelated2#takeANumber(Number)
 */
class A<T>{
    public boolean doit(Number n) {
        return true;
    }
}

class Sub<E extends Number> extends A<E> {
    public boolean doit(Number n) {
        if (n.doubleValue() > 0)
            return false;
        return super.doit(n);
    }
}

class Unrelated1<E extends Number> {
    public boolean takeANumber(Number n) {
        return false;
    }
}

interface Unrelated2<E> {
    boolean takeANumber(Number n);
}

interface Unrelated3<T> {
    boolean takeANumber(Number n);
}