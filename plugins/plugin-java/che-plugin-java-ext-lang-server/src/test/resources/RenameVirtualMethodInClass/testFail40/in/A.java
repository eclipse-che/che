package p;

class A {
    void m(int primitive) {}
    void k(Integer reference) {}
    
    void use() {
        m(12);
        k(13);
    }
}