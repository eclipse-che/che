package p;

class A<T> {
    T g;
}

class B<E extends Number> extends A<E> {
    E e= g;
}