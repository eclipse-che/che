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

import org.eclipse.che.api.machine.server.exception.MachineException;

/**
 * Client for communication with ssh machine using SSH protocol.
 *
 * <p/>Client should be started with {{@link #start()}} before performing communication with a server.
 * <br/>Server should be stopped with {{@link #stop()}} after finishing of communication with a server.
 *
 * @author Alexander Garagatyi
 */
public interface SshClient {

    /**
     * Gets address of server this SSH client is connected to.
     */
    String getHost();

    /**
     * Starts ssh client.
     *
     * <p/>Client should be stopped to perform connection cleanup on SSH server.
     */
    void start() throws MachineException;

    /**
     * Stops client to perform connection cleanup on SSH server.
     */
    void stop() throws MachineException;

    /**
     * Creates {@link SshProcess} that represents command that can be started over SSH protocol.
     *
     * @param commandLine
     *         command line to start over SSH
     * @return ssh process, it should be started separately.
     * @throws MachineException
     */
    SshProcess createProcess(String commandLine) throws MachineException;

    /**
     * Copies file(s) from local machine to remote machine using SSH protocol.
     *
     * <p/>Copying can be performed using SCP or SFTP.
     *
     * @param sourcePath
     *         path on localhost that should be copied
     * @param targetPath
     *         path on remote host where file(s) from sourcePath should be copied
     * @throws MachineException
     */
    void copy(String sourcePath, String targetPath) throws MachineException;
}
