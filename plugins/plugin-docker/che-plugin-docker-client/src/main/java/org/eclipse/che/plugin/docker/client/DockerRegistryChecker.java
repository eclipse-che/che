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

import org.eclipse.che.commons.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Checks that docker registry is available.
 *
 * @author Yevhenii Voevodin
 * @author Alexander Andrienko
 */
@Singleton
public class DockerRegistryChecker {

    private static final Logger LOG = LoggerFactory.getLogger(DockerRegistryChecker.class);

    @Inject
    @Nullable
    @Named("che.docker.registry")
    private String machineDockerRegistry;

    @Inject
    @Named("che.docker.registry_for_snapshots")
    private boolean snapshotUseRegistry;

    /**
     * Checks that registry is available and if it is not - logs warning message.
     */
    @PostConstruct
    private void checkRegistryIsAvailable() throws IOException {
        if (snapshotUseRegistry && !isNullOrEmpty(machineDockerRegistry)) {
            String registryUrl = "http://" + machineDockerRegistry;

            LOG.info("Probing registry '{}'", registryUrl);
            final HttpURLConnection conn = (HttpURLConnection) new URL(registryUrl).openConnection();
            conn.setConnectTimeout(30 * 1000);
            try {
                final int responseCode = conn.getResponseCode();
                LOG.info("Probe of registry '{}' succeed with HTTP response code '{}'", registryUrl, responseCode);
            } catch (IOException ioEx) {
                LOG.warn("Docker registry {} is not available, " +
                         "which means that you won't be able to save snapshots of your workspaces." +
                         "\nHow to configure registry?" +
                         "\n\tLocal registry  -> https://docs.docker.com/registry/" +
                         "\n\tRemote registry -> set up 'che.docker.registry.auth.*' properties", registryUrl);
            } finally {
                conn.disconnect();
            }
        }
    }
}
