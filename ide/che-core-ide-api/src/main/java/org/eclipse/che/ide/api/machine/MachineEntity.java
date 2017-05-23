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
package org.eclipse.che.ide.api.machine;

import org.eclipse.che.api.core.model.workspace.runtime.Machine;
import org.eclipse.che.api.core.model.workspace.runtime.Server;

import java.util.Map;

/**
 * Defines machine entity on client side.
 *
 * @author Roman Nikitenko
 */
public interface MachineEntity extends Machine {

    /** Returns {@code true} when the machine entity is development machine and {@code false} otherwise */
    boolean isDev();

    /** @deprecated use {@link #getName()} */
    @Deprecated
    String getId();

    /** Returns current machine's display name */
    String getName();

    /**
     * Returns current machine's display name.
     *
     * @deprecated use {@link #getName()} instead
     */
    @Deprecated
    String getDisplayName();

    /** Returns machine specific properties. */
    Map<String, String> getProperties();

    /** Returns url to connects to special WebSocket which allows get information from terminal on server side. */
    String getTerminalUrl();

    /** Returns url to connects to special WebSocket which allows execute command on given machine */
    String getExecAgentUrl();

    /** Returns mapping of exposed ports to {@link Server}. */
    Map<String, ? extends Server> getServers();

    /** Returns {@link Server} by reference or null if it not exists. */
    Server getServer(String ref);

    /** Returns {@link Machine descriptor} */
    Machine getDescriptor();

}
