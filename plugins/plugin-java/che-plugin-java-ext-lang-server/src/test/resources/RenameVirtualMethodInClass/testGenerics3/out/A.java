package p;

/**
 * @see #addIfPositive(Object)
 * @see Sub#addIfPositive(Object)
 * @see Sub#addIfPositive(Number)
 * @see Unrelated1#add(Object)
 * @see Unrelated1#add(Number)
 * @see Unrelated1#add(Integer)
 * @see Unrelated3#add(T)
 */
class A<T>{
    public boolean addIfPositive(T t) {
        return true;
    }
}

class Sub<E extends Number> extends A<E> {
    public boolean addIfPositive(E e) {
        if (e.doubleValue() > 0)
            return false;
        return super.addIfPositive(e);
    }
}

class Unrelated1<E extends Number> {
    public boolean add(E e) {
        return false;
    }
}

interface Unrelated2<E> {
    boolean add(E e);
}

interface Unrelated3<T> {
    boolean add(T t);
}