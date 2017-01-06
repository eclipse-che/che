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
package org.eclipse.che.plugin.machine.ssh;

import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.machine.server.exception.MachineException;

/**
 * Represents process created with {@link SshClient}.
 *
 * @author Alexander Garagatyi
 */
public interface SshProcess {
    void start() throws MachineException;

    void start(LineConsumer outErr) throws MachineException;

    void start(LineConsumer out, LineConsumer err) throws MachineException;

    int getExitCode();

    void kill() throws MachineException;
}
