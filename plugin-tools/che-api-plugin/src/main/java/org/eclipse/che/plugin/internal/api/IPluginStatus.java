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
 * @author Florent Benoit
 */
public enum IPluginStatus {

    /**
     * Plugin staged. Will be installed at the next build operation.
     */
    STAGED_INSTALL,

    /**
     * Plugin staged. Will be uninstalled at the next build operation.
     */
    STAGED_UNINSTALL,

    /**
     * Plugin has been installed.
     */
    INSTALLED,

    /**
     * Available to be staged
     */
    AVAILABLE,

    /**
     * Deleted when plugin has been removed
     */
    REMOVED
}
