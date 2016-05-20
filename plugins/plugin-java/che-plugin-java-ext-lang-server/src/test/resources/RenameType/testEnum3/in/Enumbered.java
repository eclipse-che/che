package p;

import static p.A.TWO;


public interface Enumbered {
    class Renumberer {
        A tweak(A a) {
            switch (a) {
            case ONE:
                return TWO;
            case TWO:
                return A.THREE;

            default:
                throw new IllegalArgumentException(a.toString());
            }
        }
    }
}
