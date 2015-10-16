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
 * Enumeration of all actions that can be applied on a plugin
 * @author Florent Benoit
 */
public enum PluginAction {

    /**
     * Undo the wanted operation of installing
     */
    UNDO_TO_INSTALL,

    /**
     * Require to install a plugin
     */
    TO_INSTALL,

    /**
     * Undo the wanted operation of uninstalling
     */
    UNDO_TO_UNINSTALL,

    /**
     * Require to uninstall a plugin
     */
    TO_UNINSTALL
}
