package p;

class A<T> {
    T f;
}

class B<E extends Number> extends A<E> {
    E e= f;
}