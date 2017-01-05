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

import javax.validation.constraints.NotNull;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * Arguments holder for {@link org.eclipse.che.plugin.docker.client.DockerConnector#getResource(GetResourceParams)}.
 *
 * @author Mykola Morhun
 */
public class GetResourceParams {

    private String container;
    private String sourcePath;

    /**
     * Creates arguments holder with required parameters.
     *
     * @param container
     *         container id or name
     * @param sourcePath
     *         info about this parameter see {@link #withSourcePath(String)}
     * @return arguments holder with required parameters
     * @throws NullPointerException
     *         if {@code container} or {@code sourcePath} is null
     */
    public static GetResourceParams create(@NotNull String container, @NotNull String sourcePath) {
        return new GetResourceParams().withContainer(container)
                                      .withSourcePath(sourcePath);
    }

    private GetResourceParams() {}

    /**
     * Adds container to this parameters.
     *
     * @param container
     *         container id or name
     * @return this params instance
     * @throws NullPointerException
     *         if {@code container} is null
     */
    public GetResourceParams withContainer(@NotNull String container) {
        requireNonNull(container);
        this.container = container;
        return this;
    }

    /**
     * Adds path to source archive to this parameters.
     *
     * @param sourcePath
     *         resource in the container’s filesystem to archive. Required.
     *         The resource specified by path must exist. It should end in '/' or '/.'.<br/>
     *         A symlink is always resolved to its target.
     * @return this params instance
     * @throws NullPointerException
     *         if {@code sourcePath} is null
     */
    public GetResourceParams withSourcePath(@NotNull String sourcePath) {
        requireNonNull(sourcePath);
        this.sourcePath = sourcePath;
        return this;
    }

    public String getContainer() {
        return container;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GetResourceParams that = (GetResourceParams)o;
        return Objects.equals(container, that.container) &&
               Objects.equals(sourcePath, that.sourcePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(container, sourcePath);
    }

}
