package p;

import java.util.function.*;

class I<E> {
    <F> String searchForRefs1() {
        return "";
    }
    /**
     * @see I#searchForRefs1()
     */
	public void bar() {
        this.searchForRefs1();
        Supplier<String> v1 = new I<Integer>()::searchForRefs1;
        Supplier<String> v2 = this::searchForRefs1;
        Function<I<Integer>, String> v3 = I<Integer>::searchForRefs1;
        Function<I<Integer>, String> v4 = I::searchForRefs1;
        Function<I<Integer>, String> v5 = I::<Object>searchForRefs1;
        searchForRefs1();
    }
}
class Sub extends I<Object> {
    Supplier<String> hexer3 = super::searchForRefs1;
}