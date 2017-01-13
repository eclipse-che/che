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
package org.eclipse.che.api.environment.server;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.environment.server.model.CheServiceImpl;
import org.eclipse.che.api.machine.server.spi.Instance;

/**
 * Starts compose services and networks.
 *
 * @author Alexander Garagatyi
 */
public interface MachineInstanceProvider {
    /**
     * Create docker container from compose service definition.
     *
     * @param namespace
     *         namespace of workspace that owns provided service
     * @param workspaceId
     *         ID of workspace that owns provided service
     * @param envName
     *         name of environment that owns provided service
     * @param machineName
     *         name of machine which represents provided service
     * @param isDev
     *         whether service will contain ws-agent or not
     * @param networkName
     *         name of docker network which service should join
     * @param service
     *         description of docker compose service
     * @param machineLogger
     *         consumer of logs of service
     * @return machine instance
     * @throws ServerException
     *         if any error occurs
     */
    Instance startService(String namespace,
                          String workspaceId,
                          String envName,
                          String machineName,
                          boolean isDev,
                          String networkName,
                          CheServiceImpl service,
                          LineConsumer machineLogger) throws ServerException;

    /**
     * Creates network for compose services.
     *
     * @param networkName
     *         name of network
     * @throws ServerException
     *         if any error occurs
     */
    void createNetwork(String networkName) throws ServerException;

    /**
     * Destroys network for compose services.
     *
     * @param networkName
     *         name of network to destroy
     * @throws ServerException
     *         if any error occurs
     */
    void destroyNetwork(String networkName) throws ServerException;
}
