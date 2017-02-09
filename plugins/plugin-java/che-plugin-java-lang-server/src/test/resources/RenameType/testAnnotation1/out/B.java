package p;
@interface B {
    enum E {
       ONE, TWO, THREE
    }
}

/**
 * @see p.B
 */
@B
class Client {
    @Deprecated @B() void bad() {
        
    }
}