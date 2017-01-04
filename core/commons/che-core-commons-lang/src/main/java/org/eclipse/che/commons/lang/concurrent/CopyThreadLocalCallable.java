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
package org.eclipse.che.commons.lang.concurrent;

import java.util.concurrent.Callable;

/** @author andrew00x */
class CopyThreadLocalCallable<T> implements Callable<T> {
    private final Callable<? extends T>                        wrapped;
    private final ThreadLocalPropagateContext.ThreadLocalState threadLocalState;

    CopyThreadLocalCallable(Callable<? extends T> wrapped) {
        // Called from main thread. Copy the current values of all the ThreadLocal variables which registered in ThreadLocalPropagateContext.
        this.wrapped = wrapped;
        this.threadLocalState = ThreadLocalPropagateContext.currentThreadState();
    }

    @Override
    public T call() throws Exception {
        try {
            threadLocalState.propagate();
            return wrapped.call();
        } finally {
            threadLocalState.cleanup();
        }
    }

    public Callable<? extends T> getWrapped() {
        return wrapped;
    }
}
