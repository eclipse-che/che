package p;

public class A {
    public String runall(Runnable[] runnables) {
        return "A";
    }
    
    public static void main(String[] args) {
        Runnable r1 = null, r2 = null;
        System.out.println(new A().runall(new Runnable[] { r1, r2 }));
        System.out.println(new Sub().runall(new Runnable[] { r1, r2 }));
        System.out.println(new Sub().runall(r1, r2));
        System.out.println(new Sub2().runall(new Runnable[] { r1, r2 }));
    }
}

class Sub extends A {
    public String runall(Runnable... runnables) {
        return "Sub, " + super.runall(runnables);
    }
}

class Sub2 extends Sub {
    public String runall(Runnable[] runnables) {
        return "Sub2, " + super.runall(runnables);
    }
}
