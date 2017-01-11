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
package org.eclipse.che.ide.rest;

/**
 * Loader which display progress information of specific long live process.
 *
 * @author Vlad Zhukovskyi
 */
public interface AsyncRequestLoader {

    /**
     * Show loader.
     */
    void show();

    /**
     * Show loader with specific message.
     *
     * @param message
     *         loader message
     */
    void show(String message);

    /**
     * Hide loader.
     */
    void hide();

    /**
     * Set message to current loader.
     *
     * @param message
     *         loader message
     */
    void setMessage(String message);
}
