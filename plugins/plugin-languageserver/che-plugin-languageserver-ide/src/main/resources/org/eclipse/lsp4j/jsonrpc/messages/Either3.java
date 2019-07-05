/*
 * Copyright (c) 2017 TypeFox GmbH (http://www.typefox.io) and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.lsp4j.jsonrpc.messages;

import org.eclipse.lsp4j.jsonrpc.validation.NonNull;

/**
 * Union type for three types.
 */
public class Either3<T1, T2, T3> extends Either<T1, Either<T2, T3>> {
    
    public static <T1, T2, T3> Either3<T1, T2, T3> forFirst(@NonNull T1 first) {
        return new Either3<T1, T2, T3>(first, null);
    }

    public static <T1, T2, T3> Either3<T1, T2, T3> forSecond(@NonNull T2 second) {
        return new Either3<T1, T2, T3>(null, new Either<T2, T3>(second, null));
    }
    
    public static <T1, T2, T3> Either3<T1, T2, T3> forThird(@NonNull T3 third) {
        return new Either3<T1, T2, T3>(null, new Either<T2, T3>(null, third));
    }
    
    public static <T1, T2, T3> Either3<T1, T2, T3> forLeft3(@NonNull T1 first) {
        return new Either3<T1, T2, T3>(first, null);
    }
    
    public static <T1, T2, T3> Either3<T1, T2, T3> forRight3(@NonNull Either<T2, T3> right) {
        return new Either3<T1, T2, T3>(null, right);
    }

    protected Either3(T1 left, Either<T2, T3> right) {
        super(left, right);
    }
    
    public T1 getFirst() {
        return getLeft();
    }
    
    public T2 getSecond() {
        Either<T2, T3> right = getRight();
        if (right == null)
            return null;
        else
            return right.getLeft();
    }
    
    public T3 getThird() {
        Either<T2, T3> right = getRight();
        if (right == null)
            return null;
        else
            return right.getRight();
    }
    
    public boolean isFirst() {
        return isLeft();
    }
    
    public boolean isSecond() {
        return isRight() && getRight().isLeft();
    }
    
    public boolean isThird() {
        return isRight() && getRight().isRight();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("Either3 [").append("\n");
        builder.append("  first = ").append(getFirst()).append("\n");
        builder.append("  second = ").append(getSecond()).append("\n");
        builder.append("  third = ").append(getThird()).append("\n");
        return builder.append("]").toString();
    }

}
