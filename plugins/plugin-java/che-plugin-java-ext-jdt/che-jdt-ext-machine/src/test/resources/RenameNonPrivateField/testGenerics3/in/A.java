package p;

class A<T> {
    T f;
    
    public T getF() {
        return f;
    }
    
    public void setF(T f) {
        this.f = f;
    }
}

class B<E extends Number> extends A<E> {
    public E getF() {
        return super.f;
    }
    public void setF(E f) {
        super.setF(f);
    }
}