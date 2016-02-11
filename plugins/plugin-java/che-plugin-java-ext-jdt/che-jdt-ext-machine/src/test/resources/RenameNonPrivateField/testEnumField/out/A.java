package p;

enum A {
    RED, GREEN, BLUE, YELLOW;
    A other;
    public A getOther() {
        return other;
    }
    public void setOther(A b) {
        other= b;
    }
}

class User {
    void m() {
        A.RED.setOther(A.GREEN);
        if (A.RED.getOther() == A.GREEN) {
            A.GREEN.other= null;
        }
    }
}