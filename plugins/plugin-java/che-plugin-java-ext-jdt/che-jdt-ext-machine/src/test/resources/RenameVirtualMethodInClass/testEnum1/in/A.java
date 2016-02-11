package p;

interface A {
    int getNameLength();
}

enum Enum implements A{
    RED, GREEN, BLUE;
    public int getNameLength() {
        return name().length();
    }
}

class Name implements A {
    Enum fRed= Enum.RED;
    
    public int getNameLength() {
        return fRed.getNameLength();
    }
}

interface IOther {
    int getNameLength();
}
interface IOther2 {
    int getNameSize();
}
