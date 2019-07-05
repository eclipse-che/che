/*
 * Copyright (c) 2016 TypeFox GmbH (http://www.typefox.io) and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.lsp4j.jsonrpc.messages;


import org.eclipse.lsp4j.jsonrpc.validation.NonNull;

/**
 * An either type maps union types in protocol specifications.
 */
public class Either<L, R> {

    public static <L, R> Either<L, R> forLeft(@NonNull L left) {
        return new Either<L, R>(left, null);
    }

    public static <L, R> Either<L, R> forRight(@NonNull R right) {
        return new Either<L, R>(null, right);
    }

    private final L left;
    private final R right;

    protected Either(L left, R right) {
        super();
        this.left = left;
        this.right = right;
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
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Either<?, ?>) {
            Either<?, ?> other = (Either<?, ?>) obj;
            return this.left != null && other.left != null && this.left.equals(other.left)
                    || this.right != null && other.right != null && this.right.equals(other.right);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        if (this.left != null)
            return this.left.hashCode();
        if (this.right != null)
            return this.right.hashCode();
        return 0;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder("Either [").append("\n");
        builder.append("  left = ").append(left).append("\n");
        builder.append("  right = ").append(right).append("\n");
        return builder.append("]").toString();
    }
}
