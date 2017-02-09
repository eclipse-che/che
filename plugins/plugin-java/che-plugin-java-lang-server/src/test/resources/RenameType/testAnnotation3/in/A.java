package p;

import java.util.ArrayList;

@Test.A
class Test {
    @interface A {
        String value() default "NULL";
    }
    
    @A("A and p.Test.A and p.A and q.Test.A")
    void test () {
        ArrayList<String> list= new ArrayList<String>() {
            void sort() {
                @A
                int current= 0;
                current++;
            }
        };
    }
}
