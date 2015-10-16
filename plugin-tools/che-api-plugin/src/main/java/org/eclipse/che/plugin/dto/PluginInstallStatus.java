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
package org.eclipse.che.plugin.dto;

/**
 * Status of the plugin installation
 * @author Florent Benoit
 */
public enum PluginInstallStatus {

    /**
     * Operation scheduled
     */
    WAIT,

    /**
     * Install has been successful
     */
    SUCCESS,

    /**
     * Install is still in progress.
     */
    IN_PROGRESS,

    /**
     * Install is finished but has failed
     */
    FAILED
}
