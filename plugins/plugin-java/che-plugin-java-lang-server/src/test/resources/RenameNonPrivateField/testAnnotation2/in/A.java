package p;

@interface Outer {
    int ZERO= 0;
    @interface A {
        int ZERO= 0;
    }
    String name() default "Z";
}

@Outer.A
class User {
    int NULL= Outer.A.ZERO;
}
