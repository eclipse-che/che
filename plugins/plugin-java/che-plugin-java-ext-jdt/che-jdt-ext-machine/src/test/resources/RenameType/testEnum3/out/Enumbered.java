package p;

import static p.B.TWO;


public interface Enumbered {
    class Renumberer {
        B tweak(B a) {
            switch (a) {
            case ONE:
                return TWO;
            case TWO:
                return B.THREE;

            default:
                throw new IllegalArgumentException(a.toString());
            }
        }
    }
}
