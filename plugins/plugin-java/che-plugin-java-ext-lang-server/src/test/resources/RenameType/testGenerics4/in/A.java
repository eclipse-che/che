package p;

import java.util.ArrayList;

class A {
    Class<? extends A> class1= A.this.getClass();
    Class<? extends A> class2= A.class;
    Class<A> class3= (Class<A>) A.this.getClass();
    X<A> getX() {
        X<A> x= new X<A>();
        x.t= new ArrayList<A>().toArray(new A[0]);
        return x;
    }
}

class X<T extends A> {
    T[] t;
}
