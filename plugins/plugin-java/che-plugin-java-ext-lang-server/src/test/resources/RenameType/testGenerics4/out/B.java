package p;

import java.util.ArrayList;

class B {
    Class<? extends B> class1= B.this.getClass();
    Class<? extends B> class2= B.class;
    Class<B> class3= (Class<B>) B.this.getClass();
    X<B> getX() {
        X<B> x= new X<B>();
        x.t= new ArrayList<B>().toArray(new B[0]);
        return x;
    }
}

class X<T extends B> {
    T[] t;
}
