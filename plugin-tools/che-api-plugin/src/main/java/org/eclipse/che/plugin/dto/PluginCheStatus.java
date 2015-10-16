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
 * Enumeration of status if Che App is asked to be reloaded
 * @author Florent Benoit
 */
public enum PluginCheStatus {

    /**
     * Successfully reloaded.
     */
    RELOADED,

    /**
     * Failed to reload.
     */
    FAILED
}
