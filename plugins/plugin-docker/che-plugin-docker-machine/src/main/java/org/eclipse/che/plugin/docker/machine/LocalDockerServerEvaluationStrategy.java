/*******************************************************************************
 * Copyright (c) 2016-2017 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.docker.machine;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import org.eclipse.che.api.machine.server.model.impl.ServerImpl;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.plugin.docker.client.json.ContainerInfo;

import java.util.Map;

/**
 * Represents a server evaluation strategy for the configuration where the workspace server and
 * workspace containers are running on the same Docker network. Calling
 * {@link ServerEvaluationStrategy#getServers(ContainerInfo, String, Map)} will return a completed
 * {@link ServerImpl} with internal addresses set to the container's address within the internal
 * docker network. If the server cannot directly ping the container, communication will fail.
 *
 * <p>The addresses used for external address can be overridden via the property {@code che.docker.ip.external}.
 *
 * @author Angel Misevski <amisevsk@redhat.com>
 * @see ServerEvaluationStrategy
 */
public class LocalDockerServerEvaluationStrategy extends BaseServerEvaluationStrategy {

    @Inject
    public LocalDockerServerEvaluationStrategy(@Nullable @Named("che.docker.ip") String internalAddress,
                                               @Nullable @Named("che.docker.ip.external") String externalAddress) {
        super(internalAddress, externalAddress, null, null, null, true);
    }
}
