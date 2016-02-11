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
package org.eclipse.che.plugin.docker.client;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.plugin.docker.client.json.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Named;
import java.io.IOException;
import java.util.Set;

/**
 * Verifies compatibility with docker api version.
 *
 * @author Anton Korneta
 */
@Singleton
public class DockerVersionVerifier {

    private final DockerConnector dockerConnector;
    private final Set<String>     supportedVersions;

    private static final Logger LOG = LoggerFactory.getLogger(DockerVersionVerifier.class);

    @Inject
    public DockerVersionVerifier(DockerConnector dockerConnector, @Named("machine.supported_docker_version") String[] supportedVersions) {
        this.dockerConnector = dockerConnector;
        this.supportedVersions = Sets.newHashSet(supportedVersions);
    }

    /**
     * Check docker version compatibility.
     */
    @PostConstruct
    void checkCompatibility() throws ServerException {
        try {
            Version versionInfo = dockerConnector.getVersion();
            if (!supportedVersions.contains(versionInfo.getVersion())) {
                throw new ServerException("Unsupported docker version " + versionInfo.getVersion());
            }
        } catch (IOException e) {
            LOG.info(e.getMessage());
            throw new ServerException("Impossible to get docker version", e);
        }
    }
}
