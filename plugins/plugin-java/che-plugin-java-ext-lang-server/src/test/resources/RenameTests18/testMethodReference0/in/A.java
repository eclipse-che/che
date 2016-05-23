package p;

import java.util.function.*;

class I<E> {
    <F> String searchForRefs() {
        return "";
    }
    /**
     * @see I#searchForRefs()
     */
	public void bar() {
        this.searchForRefs();
        Supplier<String> v1 = new I<Integer>()::searchForRefs;
        Supplier<String> v2 = this::searchForRefs;
        Function<I<Integer>, String> v3 = I<Integer>::searchForRefs;
        Function<I<Integer>, String> v4 = I::searchForRefs;
        Function<I<Integer>, String> v5 = I::<Object>searchForRefs;
        searchForRefs();
    }
}
class Sub extends I<Object> {
    Supplier<String> hexer3 = super::searchForRefs;
}