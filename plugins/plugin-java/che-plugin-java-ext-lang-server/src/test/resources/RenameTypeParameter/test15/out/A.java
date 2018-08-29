package p;

/**
 * @param <S> the element
 * @see A#T
 * @see A#take(S) hint: T is not supported here
 */
class A<S> {
    S T;
    /**
     * @param T the element to assign to {@link #T}
     */
    void take(S T) {
        this.T= T;
    }
}
