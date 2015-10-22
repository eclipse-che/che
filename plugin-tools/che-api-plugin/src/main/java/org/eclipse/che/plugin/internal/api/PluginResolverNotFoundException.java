/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.internal.api;

/**
 * Exception if plugin is not found during the resolution of the url
 * @author Florent Benoit
 */
public class PluginResolverNotFoundException extends PluginException {

    /**
     * Create exception with a given message.
     * @param message the error message
     */
    public PluginResolverNotFoundException(String message) {
        super(message);
    }

    /**
     * Create exception with a given message and a root cause.
     * @param message the error message
     *                @param e the root cause
     */
    public PluginResolverNotFoundException(String message, Throwable e) {
        super(message, e);
    }

}
