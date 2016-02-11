package p;

interface A {
    int value(String s);
}

enum En implements A {
    ONE, TWO, THREE;
    public int value(String s) {
        return Integer.valueOf(s);
    }
}
