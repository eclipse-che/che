package p;

import java.util.Collection;
import java.util.List;

class A<S extends Number & Cloneable> {
    S t;
    S transform(S t) {
        return t;
    }
    Collection<? super S> add(List<? extends S> t) {
        return null;
    }
    
    class Inner<I extends S> {
        S tee;
    }
}