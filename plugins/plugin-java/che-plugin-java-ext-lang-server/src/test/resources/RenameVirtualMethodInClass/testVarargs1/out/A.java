package p;

public class A {
    public String runThese(Runnable[] runnables) {
        return "A";
    }
    
    public static void main(String[] args) {
        Runnable r1 = null, r2 = null;
        System.out.println(new A().runThese(new Runnable[] { r1, r2 }));
        System.out.println(new Sub().runThese(new Runnable[] { r1, r2 }));
        System.out.println(new Sub().runThese(r1, r2));
        System.out.println(new Sub2().runThese(new Runnable[] { r1, r2 }));
    }
}

class Sub extends A {
    public String runThese(Runnable... runnables) {
        return "Sub, " + super.runThese(runnables);
    }
}

class Sub2 extends Sub {
    public String runThese(Runnable[] runnables) {
        return "Sub2, " + super.runThese(runnables);
    }
}
