package p;

enum A {
    RED, GREEN, BLUE, YELLOW;
    A buddy;
    public A getBuddy() {
        return buddy;
    }
    public void setBuddy(A b) {
        buddy= b;
    }
}

class User {
    void m() {
        A.RED.setBuddy(A.GREEN);
        if (A.RED.getBuddy() == A.GREEN) {
            A.GREEN.buddy= null;
        }
    }
}