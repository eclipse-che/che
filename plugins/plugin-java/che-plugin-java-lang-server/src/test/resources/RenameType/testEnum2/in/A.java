package p;

enum A {
    ONE {
        String getKey() {
            return "eis";
        }
        boolean longerNameThan(A other) {
            return false;
        }
    },
    BIG {
        String getKey() {
            return "riesig";
        }
        boolean longerNameThan(A other) {
            return other != BIG;
        }
    };
    abstract boolean longerNameThan(A a);
}
