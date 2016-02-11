package p;

import java.util.ArrayList;

@Test.B
class Test {
    @interface B {
        String value() default "NULL";
    }
    
    @B("B and p.Test.B and p.A and q.Test.A")
    void test () {
        ArrayList<String> list= new ArrayList<String>() {
            void sort() {
                @B
                int current= 0;
                current++;
            }
        };
    }
}
