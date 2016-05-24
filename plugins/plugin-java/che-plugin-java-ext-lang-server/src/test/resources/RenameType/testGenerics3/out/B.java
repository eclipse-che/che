package p;

import java.util.ArrayList;
import java.util.Comparator;

class B {
    ArrayList<? super B> fSink;
}

class U extends ArrayList<B> {
    public boolean add(B arg0) {
        return false;
    }
}

class V<Q extends B> implements Comparator<Q> {
    public int compare(Q arg0, Q arg1) {
        return 0;
    }
}
