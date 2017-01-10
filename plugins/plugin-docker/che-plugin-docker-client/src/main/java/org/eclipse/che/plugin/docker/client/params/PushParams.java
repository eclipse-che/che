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
package org.eclipse.che.plugin.docker.client.params;

import org.eclipse.che.plugin.docker.client.ProgressMonitor;
import org.eclipse.che.plugin.docker.client.dto.AuthConfigs;

import javax.validation.constraints.NotNull;

import java.util.Objects;

import static java.util.Objects.requireNonNull;
import static org.eclipse.che.plugin.docker.client.DockerRegistryAuthResolver.DEFAULT_REGISTRY;
import static org.eclipse.che.plugin.docker.client.DockerRegistryAuthResolver.DEFAULT_REGISTRY_SYNONYMS;

/**
 * Arguments holder for {@link org.eclipse.che.plugin.docker.client.DockerConnector#push(PushParams, ProgressMonitor)}.
 *
 * @author Mykola Morhun
 */
public class PushParams {

    private String      repository;
    private String      tag;
    private String      registry;
    private AuthConfigs authConfigs;

    /**
     * Creates arguments holder with required parameters.
     *
     * @param repository
     *         repository name
     * @return arguments holder with required parameters
     * @throws NullPointerException
     *         if {@code repository} null
     */
    public static PushParams create(@NotNull String repository) {
        return new PushParams().withRepository(repository);
    }

    private PushParams() {}

    /**
     * Adds repository to this parameters.
     *
     * @param repository
     *         repository name
     * @return this params instance
     * @throws NullPointerException
     *         if {@code repository} null
     */
    public PushParams withRepository(@NotNull String repository) {
        requireNonNull(repository);
        this.repository = repository;
        return this;
    }

    /**
     * Adds tag to this parameters.
     *
     * @param tag
     *         tag of the image
     * @return this params instance
     */
    public PushParams withTag(String tag) {
        this.tag = tag;
        return this;
    }

    /**
     * Adds registry to this parameters.
     *
     * @param registry
     *         registry url
     * @return this params instance
     */
    public PushParams withRegistry(String registry) {
        this.registry = registry;
        return this;
    }

    /**
     * Adds auth configuration to this parameters.
     *
     * @param authConfigs
     *         authentication configuration for registries
     * @return this params instance
     */
    public PushParams withAuthConfigs(AuthConfigs authConfigs) {
        this.authConfigs = authConfigs;
        return this;
    }

    public String getRepository() {
        return repository;
    }

    public String getTag() {
        return tag;
    }

    public String getRegistry() {
        return registry;
    }

    public AuthConfigs getAuthConfigs() {
        return authConfigs;
    }

    /**
     * Returns full repo.
     * It has following format: [registry/]image
     * In case of docker.io registry is omitted,
     *  otherwise it may cause some troubles with swarm
     */
    public String getFullRepo() {
        if (registry == null || DEFAULT_REGISTRY_SYNONYMS.contains(registry)) {
            return repository;
        } else {
            return registry + '/' + repository;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PushParams that = (PushParams)o;
        return Objects.equals(repository, that.repository) &&
               Objects.equals(tag, that.tag) &&
               Objects.equals(registry, that.registry) &&
               Objects.equals(authConfigs, that.authConfigs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(repository, tag, registry, authConfigs);
    }

}
