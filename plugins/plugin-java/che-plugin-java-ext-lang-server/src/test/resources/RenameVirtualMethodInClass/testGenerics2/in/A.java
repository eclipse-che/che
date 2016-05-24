package p;

class A<E>{
    public boolean add(E e) {
        return true;
    }
}

class Sub<E extends Number> extends A<E> {
    public boolean add(E e) {
        if (e.doubleValue() > 0)
            return false;
        return super.add(e);
    }
}

class Unrelated<E> {
    public boolean add(E e) {
        return false;
    }
}