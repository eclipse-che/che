package p;

enum B {
    ONE {
        String getKey() {
            return "eis";
        }
        boolean longerNameThan(B other) {
            return false;
        }
    },
    BIG {
        String getKey() {
            return "riesig";
        }
        boolean longerNameThan(B other) {
            return other != BIG;
        }
    };
    abstract boolean longerNameThan(B a);
}
