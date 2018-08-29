package p;

class A {
	public static final int value= 12;
	
	@I
	boolean f1;
	@I(number = 1)
	boolean f2;
	@I(number = value)
	boolean f3;
	@I(number=1)
	boolean f4;
	@I(number=1, x=2)
	boolean f5;
	@I(x=2)
	boolean f6;
}

@interface I {
    int x() default 0;
    int number() default 0;
}
