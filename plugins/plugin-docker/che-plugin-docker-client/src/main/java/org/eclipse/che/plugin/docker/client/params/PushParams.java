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
package org.eclipse.che.plugin.docker.client.params;

import org.eclipse.che.plugin.docker.client.ProgressMonitor;

import javax.validation.constraints.NotNull;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * Arguments holder for {@link org.eclipse.che.plugin.docker.client.DockerConnector#push(PushParams, ProgressMonitor)}.
 *
 * @author Mykola Morhun
 */
public class PushParams {

    private String repository;
    private String tag;
    private String registry;

    /**
     * Creates arguments holder with required parameters.
     *
     * @param repository
     *         repository name
     * @return arguments holder with required parameters
     * @throws NullPointerException
     *         if {@code registry} is null
     */
    public static PushParams from(@NotNull String repository) {
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
     *         if {@code repository} is null
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

    public String getRepository() {
        return repository;
    }

    public String getTag() {
        return tag;
    }

    public String getRegistry() {
        return registry;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PushParams that = (PushParams)o;
        return Objects.equals(repository, that.repository) &&
               Objects.equals(tag, that.tag) &&
               Objects.equals(registry, that.registry);
    }

    @Override
    public int hashCode() {
        return Objects.hash(repository, tag, registry);
    }

}
