package p;

class A<E extends Number> {
    private int count;

    public int getCount() {
        return count;
    }

    public void setCount(int c) {
        count= c;
    }
    
    void test() {
        Integer i= getCount();
        setCount(i);
        new A<Double>().setCount(1);
        i= new A<Number>().getCount();
    }
}
