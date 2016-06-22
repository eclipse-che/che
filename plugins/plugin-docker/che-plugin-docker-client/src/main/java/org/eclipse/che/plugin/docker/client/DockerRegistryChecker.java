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

import com.google.common.annotations.VisibleForTesting;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonResponse;
import org.eclipse.che.commons.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.Boolean.TRUE;

/**
 * Checks that docker registry is available.
 *
 * @author Yevhenii Voevodin
 * @author Alexander Andrienko
 */
@Singleton
public class DockerRegistryChecker {

    private static final Logger LOG = LoggerFactory.getLogger(DockerRegistryChecker.class);

    private final HttpJsonRequestFactory requestFactory;
    private final String                 registryUrl;
    private final Boolean                snapshotUseRegistry;

    @Inject
    public DockerRegistryChecker(HttpJsonRequestFactory requestFactory,
                                 @Nullable @Named("machine.docker.registry") String registryUrl,
                                 @Nullable @Named("machine.docker.snapshot_use_registry") Boolean snapshotUseRegistry) {
        this.requestFactory = requestFactory;
        this.registryUrl = registryUrl;
        this.snapshotUseRegistry = snapshotUseRegistry;
    }

    /**
     * Checks that registry is available and if it is not - logs warning message.
     */
    @VisibleForTesting
    @PostConstruct
    void checkRegistryIsAvailable() {
        if (TRUE.equals(snapshotUseRegistry) && !isNullOrEmpty(registryUrl)) {
            String registryLink = "http://" + registryUrl;

            LOG.info("Probing registry '{}'", registryLink);

            try {
                HttpJsonResponse response = requestFactory.fromUrl(registryLink).setTimeout(30 * 1000).request();
                final int responseCode = response.getResponseCode();
                LOG.info("Probe of registry '{}' succeed with HTTP response code '{}'", registryLink, responseCode);
            } catch (ApiException | IOException ex) {
                LOG.warn("Docker registry " + registryLink + " is not available, " +
                         "which means that you won't be able to save snapshots of your workspaces." +
                         "\nHow to configure registry?" +
                         "\n\tLocal registry  -> https://docs.docker.com/registry/" +
                         "\n\tRemote registry -> set up 'docker.registry.auth.*' properties");
            }
        }
    }
}
