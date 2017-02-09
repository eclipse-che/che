package p;

class A {
	public static final int value= 12;
	
	@I
	boolean f1;
	@I(1)
	boolean f2;
	@I(value)
	boolean f3;
	@I(value=1)
	boolean f4;
	@I(value=1, x=2)
	boolean f5;
	@I(x=2)
	boolean f6;
}

@interface I {
    int x() default 0;
    int value() default 0;
}
