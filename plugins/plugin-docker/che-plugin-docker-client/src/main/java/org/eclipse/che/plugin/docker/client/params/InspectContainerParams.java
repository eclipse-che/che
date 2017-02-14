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
 * Arguments holder for{@link org.eclipse.che.plugin.docker.client.DockerConnector#inspectContainer(InspectContainerParams)}.
 *
 * @author Mykola Morhun
 */
public class InspectContainerParams {

    private String  container;
    private Boolean returnContainerSize;

    /**
     * Creates arguments holder with required parameters.
     *
     * @param container
     *         id or name of container
     * @return arguments holder with required parameters
     * @throws NullPointerException
     *         if {@code container} is null
     */
    public static InspectContainerParams create(@NotNull String container) {
        return new InspectContainerParams().withContainer(container);
    }

    private InspectContainerParams() {}

    /**
     * Adds container to this parameters.
     *
     * @param container
     *         id or name of container
     * @return this params instance
     * @throws NullPointerException
     *         if {@code container} is null
     */
    public InspectContainerParams withContainer(@NotNull String container) {
        requireNonNull(container);
        this.container = container;
        return this;
    }

    /**
     * Adds return container size flag to this parameters.
     *
     * @param returnContainerSize
     *         if {@code true} it will return container size information
     * @return this params instance
     */
    public InspectContainerParams withReturnContainerSize(boolean returnContainerSize) {
        this.returnContainerSize = returnContainerSize;
        return this;
    }

    public String getContainer() {
        return container;
    }

    public Boolean isReturnContainerSize() {
        return returnContainerSize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InspectContainerParams that = (InspectContainerParams)o;
        return Objects.equals(container, that.container) &&
               Objects.equals(returnContainerSize, that.returnContainerSize);
    }

    @Override
    public int hashCode() {
        return Objects.hash(container, returnContainerSize);
    }

}
