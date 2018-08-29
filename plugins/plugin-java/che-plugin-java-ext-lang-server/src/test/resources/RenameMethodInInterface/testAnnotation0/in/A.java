package p;

/**
 * @see #name()
 * @see I#name()
 * @see p.I#name()
 */
@I(name="X")
@interface I {
    @I()
    String name() default IDefault.NAME;
    
    @I
    interface IDefault {
        public @I(name=IDefault.NAME) final String NAME= "Me";
    }
}
