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
package org.eclipse.che.api.promises.client;

import org.eclipse.che.commons.annotation.Nullable;

/**
 * Represents a promise rejection reason.
 *
 * @author Mickaël Leduque
 * @author Artem Zatsarynnyi
 */
public interface PromiseError {

    /**
     * Returns the error message.
     *
     * @return the error message
     */
    @Nullable
    String getMessage();

    /**
     * Returns the error cause. May returns {@code null} in case
     * this {@link PromiseError} represents a JS Error object.
     *
     * @return the error cause
     */
    @Nullable
    Throwable getCause();
}
