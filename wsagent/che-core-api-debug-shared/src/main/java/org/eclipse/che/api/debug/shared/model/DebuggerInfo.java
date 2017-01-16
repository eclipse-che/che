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
package org.eclipse.che.api.debug.shared.model;

import org.eclipse.che.commons.annotation.Nullable;

/**
 * Summary of debugger information.
 *
 * @author Anatoliy Bazko
 */
public interface DebuggerInfo {
    /**
     * The host where debugger is connected to.
     */
    @Nullable
    String getHost();

    /**
     * The port where debugger is connected to.
     */
    int getPort();

    /**
     * The debugger name.
     */
    String getName();

    /**
     * The debugger version.
     */
    String getVersion();

    /**
     * The pid where debugger is connected to.
     */
    int getPid();

    /**
     * The binary file used by debugger.
     */
    @Nullable
    String getFile();
}
