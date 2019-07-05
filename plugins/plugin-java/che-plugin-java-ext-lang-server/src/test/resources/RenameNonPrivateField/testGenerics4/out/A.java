package p;

class A<E extends Number> {
    private int number;

    public int getNumber() {
        return number;
    }

    public void setNumber(int c) {
        number= c;
    }
    
    void test() {
        Integer i= getNumber();
        setNumber(i);
        new A<Double>().setNumber(1);
        i= new A<Number>().getNumber();
    }
}
