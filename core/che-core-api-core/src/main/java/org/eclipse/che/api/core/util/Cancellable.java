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
package org.eclipse.che.api.core.util;

/**
 * Implementation of this interface may be used with {@link Watchdog} to make possible terminate task by timeout.
 *
 * @author andrew00x
 */
public interface Cancellable {
    interface Callback {
        /**
         * Notified when Cancellable cancelled.
         *
         * @param cancellable
         *         Cancellable
         */
        void cancelled(Cancellable cancellable);
    }

    /**
     * Attempts to cancel execution of this {@code Cancellable}.
     *
     * @throws Exception
     *         if cancellation is failed
     */
    void cancel() throws Exception;
}
