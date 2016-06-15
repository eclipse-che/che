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
package org.eclipse.che.ide.api.project.tree.generic;

/**
 * Common protocol for elements that must be opened before they can be used.
 *
 * @author Evgen Vidolob
 */
@Deprecated
public interface Openable {
    /**
     * Closes this element.
     * Closing an element which is not open has no effect.
     * <p/>
     * <p>Note: although {@link #close} is exposed in the API, clients are
     * not expected to open and close elements - the IDE does this automatically
     * as elements are accessed.
     */
    void close();

    /**
     * Returns whether this openable is open. This is a handle-only method.
     *
     * @return true if this openable is open, false otherwise
     */
    boolean isOpened();

    /**
     * Opens this element and all parent elements that are not already open.
     * <p/>
     * <p>Note: although {@link #open} is exposed in the API, clients are
     * not expected to open and close elements - the IDE does this automatically
     * as elements are accessed.
     */
    void open();
}
