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
 * Defines states for the install process of plugins
 * @author Florent Benoit
 */
public enum IPluginInstallStatus {

    /**
     * Install process defined but not yet started.
     */
    WAIT,

    /**
     * Process has finished successfully
     */
    SUCCESS,

    /**
     * Process is currently in progress.
     */
    IN_PROGRESS,

    /**
     * The current process failed to execute.
     */
    FAILED

}
