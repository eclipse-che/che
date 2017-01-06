/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.maven.server.rmi;

/**
 * Mutable version of {@link java.util.Optional}
 *
 * @author Evgen Vidolob
 */
public class Ref<T> {
    private T value;

    private Ref(T value) {
        this.value = value;
    }

    public static <T> Ref<T> ofNull() {
        return new Ref<>(null);
    }

    public static <T> Ref<T> of(T value) {
        return new Ref<>(value);
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public boolean isNull() {
        return value == null;
    }
}
