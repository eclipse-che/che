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
package org.eclipse.che.ide.ui.loaders.request;

/**
 * Loader factory.
 *
 * @author Vlad Zhukovskiy
 */
public interface LoaderFactory {
    /**
     * Create new loader with default message.
     *
     * @return new loader
     */
    MessageLoader newLoader();

    /**
     * Create new loader with initial message.
     *
     * @param message
     *         initial message
     * @return new loader with initial message
     */
    MessageLoader newLoader(String message);
}
