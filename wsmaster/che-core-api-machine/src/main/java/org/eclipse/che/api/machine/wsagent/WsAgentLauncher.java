/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.machine.wsagent;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.machine.server.exception.MachineException;

/**
 * Starts ws agent in the machine and wait until ws agent sends notification about its start
 *
 * @author Alexander Garagatyi
 */
public interface WsAgentLauncher {
    void startWsAgent(String workspaceId) throws NotFoundException, MachineException, InterruptedException;
}
