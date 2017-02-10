package p;

class A<T> {
    T g;
    
    public T getG() {
        return g;
    }
    
    public void setG(T f) {
        this.g = f;
    }
}

class B<E extends Number> extends A<E> {
    public E getG() {
        return super.g;
    }
    public void setG(E f) {
        super.setG(f);
    }
}