/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.commons.lang.concurrent;

/**
 * Lock that is designed to use in try-with-resources statement.
 *
 * <p>Implementers should lock on instance creation
 * and unlock when {@link CloseableLock#close()} method invokes.
 *
 * @author Sergii Leschenko
 */
public interface CloseableLock extends AutoCloseable {
    /**
     * Unlocks this lock.
     *
     * This method is invoked automatically on objects managed by the
     * {@code try}-with-resources statement.
     */
    @Override
    void close();
}
