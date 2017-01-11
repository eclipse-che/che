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
 * Arguments holder for {@link org.eclipse.che.plugin.docker.client.DockerConnector#killContainer(KillContainerParams)}.
 *
 * @author Mykola Morhun
 */
public class KillContainerParams {

    private String  container;
    private Integer signal;

    /**
     * Creates arguments holder with required parameters.
     *
     * @param container
     *          container identifier, either id or name
     * @return arguments holder with required parameters
     */
    public static KillContainerParams create(@NotNull String container) {
        return new KillContainerParams().withContainer(container);
    }

    private KillContainerParams() {}

    /**
     * Adds container to this parameters.
     *
     * @param container
     *         container identifier, either id or name
     * @return this params instance
     * @throws NullPointerException
     *         if {@code container} is null
     */
    public KillContainerParams withContainer(@NotNull String container) {
        requireNonNull(container);
        this.container = container;
        return this;
    }

    /**
     * Adds signal code to this parameters.
     *
     * @param signal
     *         code of signal, e.g. 9 in case of SIGKILL
     * @return this params instance
     */
    public KillContainerParams withSignal(int signal) {
        this.signal = signal;
        return this;
    }

    public String getContainer() {
        return container;
    }

    public Integer getSignal() {
        return signal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KillContainerParams that = (KillContainerParams)o;
        return Objects.equals(container, that.container) &&
               Objects.equals(signal, that.signal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(container, signal);
    }

}
