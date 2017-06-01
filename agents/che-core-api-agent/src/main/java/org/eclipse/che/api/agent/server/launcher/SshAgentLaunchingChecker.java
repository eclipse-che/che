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
package org.eclipse.che.api.agent.server.launcher;

import org.eclipse.che.api.agent.shared.model.Agent;
import org.eclipse.che.api.core.model.machine.Server;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.api.machine.server.spi.InstanceProcess;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Checks readiness of ssh via sending global request.
 * For more details see <a href="https://tools.ietf.org/html/rfc4254">RFC-4254</a>.
 *
 * @author Mykola Morhun
 */
public class SshAgentLaunchingChecker implements AgentLaunchingChecker {
    private static final String SSH_PORT = "22/tcp";

    private static final byte[] SSH_GLOBAL_REQUEST_BYTES;
    static {
        final byte   SSH_MSG_GLOBAL_REQUEST = 80;
        final byte[] SSH_CHANNEL_TYPE       = "test".getBytes(StandardCharsets.US_ASCII);
        final byte   SSH_WANT_REPLAY        = 1;

        byte[] requestBytes = null;
        try (ByteArrayOutputStream sshGlobalRequestBytes = new ByteArrayOutputStream()) {
            sshGlobalRequestBytes.write(SSH_MSG_GLOBAL_REQUEST);
            sshGlobalRequestBytes.write(SSH_CHANNEL_TYPE);
            sshGlobalRequestBytes.write(SSH_WANT_REPLAY);

            requestBytes = sshGlobalRequestBytes.toByteArray();
        } catch (IOException ignore) { /* will never happen */ }
        SSH_GLOBAL_REQUEST_BYTES = requestBytes;
    }

    @Override
    public boolean isLaunched(Agent agent, InstanceProcess process, Instance machine) throws MachineException {
        Server server = machine.getRuntime().getServers().get(SSH_PORT);
        if (server != null) {
            try {
                String[] sshServerHostAndPort = server.getProperties().getInternalAddress().split(":");
                try (Socket socket = new Socket(sshServerHostAndPort[0], Integer.parseInt(sshServerHostAndPort[1]));
                     BufferedOutputStream sshRequest = new BufferedOutputStream(socket.getOutputStream());
                     BufferedInputStream sshResponse = new BufferedInputStream(socket.getInputStream())) {

                    sshRequest.write(SSH_GLOBAL_REQUEST_BYTES);
                    sshRequest.flush();
                    // Actual response is not needed, just make sure that ssh server give a response.
                    if (sshResponse.read() != -1) {
                        return true;
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return false;
    }
}
