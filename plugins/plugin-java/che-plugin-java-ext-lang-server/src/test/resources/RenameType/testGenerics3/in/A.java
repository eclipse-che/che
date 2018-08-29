package p;

import java.util.ArrayList;
import java.util.Comparator;

class A {
    ArrayList<? super A> fSink;
}

class U extends ArrayList<A> {
    public boolean add(A arg0) {
        return false;
    }
}

class V<Q extends A> implements Comparator<Q> {
    public int compare(Q arg0, Q arg1) {
        return 0;
    }
}
