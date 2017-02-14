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

import org.eclipse.che.plugin.docker.client.MessageProcessor;

import javax.validation.constraints.NotNull;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * Arguments holder for {@link org.eclipse.che.plugin.docker.client.DockerConnector#attachContainer(AttachContainerParams, MessageProcessor)} .
 *
 * @author Mykola Morhun
 */
public class AttachContainerParams {

    private String  container;
    private Boolean stream;

    /**
     * Creates arguments holder with required parameters.
     *
     * @param container
     *         id or name of container
     * @return arguments holder with required parameters
     */
    public static AttachContainerParams create(@NotNull String container) {
        return new AttachContainerParams().withContainer(container);
    }

    private AttachContainerParams() {}

    /**
     * Adds container to this parameters.
     *
     * @param container
     *         id or name of container
     * @return this params instance
     * @throws NullPointerException
     *         if {@code container} is null
     */
    public AttachContainerParams withContainer(@NotNull String container) {
        requireNonNull(container);
        this.container = container;
        return this;
    }

    /**
     * Flag for getting output stream from a container.
     *
     * @param stream
     *         if {@code true} gets output stream from container.<br/>
     *         Note, that live stream blocks until container is running.<br/>
     *         When using the TTY setting is enabled when from container, the stream is the raw data
     *          from the process PTY and client’s stdin.
     *         When the TTY is disabled, then the stream is multiplexed to separate stdout and stderr.
     * @return this params instance
     */
    public AttachContainerParams withStream(boolean stream) {
        this.stream = stream;
        return this;
    }

    public String getContainer() {
        return container;
    }

    public Boolean isStream() {
        return stream;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AttachContainerParams that = (AttachContainerParams)o;
        return Objects.equals(container, that.container) &&
               Objects.equals(stream, that.stream);
    }

    @Override
    public int hashCode() {
        return Objects.hash(container, stream);
    }

}
