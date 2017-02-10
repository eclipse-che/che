package p;

class Generic<E> {
    enum A {
        ONE {
            A get2ndPower() {
                return ONE;
            }
        },
        TWO {
            A get2ndPower() {
                return MANY;
            }
        },
        MANY {
            A get2ndPower() {
                return MANY;
            }
        };
        abstract A get2ndPower();
    }
}
