package p;

/**
 * @param <T> the element
 * @see A#T
 * @see A#take(T) hint: T is not supported here
 */
class A<T> {
    T T;
    /**
     * @param T the element to assign to {@link #T}
     */
    void take(T T) {
        this.T= T;
    }
}
