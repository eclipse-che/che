package p;

import java.util.Collection;
import java.util.List;

class A<T extends Number & Cloneable> {
    T t;
    T transform(T t) {
        return t;
    }
    Collection<? super T> add(List<? extends T> t) {
        return null;
    }
    
    class Inner<I extends T> {
        T tee;
    }
}