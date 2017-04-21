package org.eclipse.lsp4j.jsonrpc.messages;

import org.eclipse.lsp4j.jsonrpc.validation.NonNull;

/**
 * An either type maps union types in protocol specifications.
 *
 * @param <L>
 * @param <R>
 */
public class Either<L, R> {

    private final L left;
    private final R right;

    protected Either(L left, R right) {
        super();
        this.left = left;
        this.right = right;
    }

    public static <L, R> Either<L, R> forLeft(@NonNull L left) {
        return new Either<L, R>(left, null);
    }

    public static <L, R> Either<L, R> forRight(@NonNull R right) {
        return new Either<L, R>(null, right);
    }

    public L getLeft() {
        return left;
    }

    public R getRight() {
        return right;
    }

    public boolean isLeft() {
        return left != null;
    }

    public boolean isRight() {
        return right != null;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder("Either [").append("\n");
        builder.append("  left = ").append(left).append("\n");
        builder.append("  right = ").append(right).append("\n");
        return builder.append("]").toString();
    }
}
