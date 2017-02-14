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

import java.util.Arrays;
import java.util.Objects;

import static java.util.Objects.requireNonNull;
import static org.eclipse.che.plugin.docker.client.params.ParamsUtils.requireNonEmptyArray;

/**
 * Arguments holder for {@link org.eclipse.che.plugin.docker.client.DockerConnector#createExec(CreateExecParams)}.
 *
 * @author Mykola Morhun
 */
public class CreateExecParams {

    private String   container;
    private Boolean  detach;
    private String[] cmd;

    /**
     * Creates arguments holder with required parameters.
     *
     * @param container
     *         info about this parameter see {@link #withContainer(String)}
     * @param cmd
     *         info about this parameter see {@link #withCmd(String[])}
     * @return arguments holder with required parameters
     * @throws NullPointerException
     *         if {@code container} or {@code cmd} is null
     */
    public static CreateExecParams create(@NotNull String container, @NotNull String[] cmd) {
        return new CreateExecParams().withContainer(container)
                                     .withCmd(cmd);
    }

    private CreateExecParams() {}

    /**
     * Adds container to this parameters.
     *
     * @param container
     *         id or name of container
     * @return this params instance
     * @throws NullPointerException
     *         if {@code container} is null
     */
    public CreateExecParams withContainer(@NotNull String container) {
        requireNonNull(container);
        this.container = container;
        return this;
    }

    /**
     * Adds detach stdout & stderr flag to this parameters.
     *
     * @param detach
     *         is stdout & stderr detached
     * @return this params instance
     */
    public CreateExecParams withDetach(boolean detach) {
        this.detach = detach;
        return this;
    }

    /**
     * Adds command to run into this parameters.
     *
     * @param cmd
     *         command to run specified as a string or an array of strings
     * @return this params instance
     * @throws NullPointerException
     *         if {@code cmd} is null
     * @throws IllegalArgumentException
     *         if {@code cmd} is empty
     */
    public CreateExecParams withCmd(@NotNull String[] cmd) {
        requireNonNull(cmd);
        requireNonEmptyArray(cmd);
        if (cmd[0].isEmpty()) {
            throw new IllegalArgumentException("Create exec parameters: no command specified");
        }
        this.cmd = cmd;
        return this;
    }

    public String getContainer() {
        return container;
    }

    public Boolean isDetach() {
        return detach;
    }

    public String[] getCmd() {
        return cmd;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CreateExecParams)) {
            return false;
        }
        final CreateExecParams that = (CreateExecParams)obj;
        return Objects.equals(container, that.container)
               && Objects.equals(detach, that.detach)
               && Arrays.equals(cmd, that.cmd);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(container);
        hash = 31 * hash + Objects.hashCode(detach);
        hash = 31 * hash + Arrays.hashCode(cmd);
        return hash;
    }

    @Override
    public String toString() {
        return "CreateExecParams{" +
               "container='" + container + '\'' +
               ", detach=" + detach +
               ", cmd=" + Arrays.toString(cmd) +
               '}';
    }
}
