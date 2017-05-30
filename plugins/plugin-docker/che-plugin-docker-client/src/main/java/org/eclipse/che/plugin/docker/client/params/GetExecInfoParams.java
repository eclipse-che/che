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
 * Arguments holder for {@link org.eclipse.che.plugin.docker.client.DockerConnector#getExecInfo(GetExecInfoParams)}.
 *
 * @author Mykola Morhun
 */
public class GetExecInfoParams {

    private String execId;

    /**
     * Creates arguments holder with required parameters.
     *
     * @param execId
     *         exec id
     * @return arguments holder with required parameters
     */
    public static GetExecInfoParams create(@NotNull String execId) {
        return new GetExecInfoParams().withExecId(execId);
    }

    private GetExecInfoParams() {}

    /**
     * Adds exec it to this parameters.
     *
     * @param execId
     *         exec id
     * @return this params instance
     */
    public GetExecInfoParams withExecId(@NotNull String execId) {
        requireNonNull(execId);
        this.execId = execId;
        return this;
    }

    public String getExecId() {
        return execId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GetExecInfoParams that = (GetExecInfoParams)o;
        return Objects.equals(execId, that.execId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(execId);
    }

}
