package p;

interface A {
    int getNameSize();
}

enum Enum implements A{
    RED, GREEN, BLUE;
    public int getNameSize() {
        return name().length();
    }
}

class Name implements A {
    Enum fRed= Enum.RED;
    
    public int getNameSize() {
        return fRed.getNameSize();
    }
}

interface IOther {
    int getNameLength();
}
interface IOther2 {
    int getNameSize();
}
