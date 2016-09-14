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
package org.eclipse.che.api.environment.server.compose;

import java.util.Objects;

/**
 * Describes how to build image for container creation.
 *
 * @author Alexander Garagatyi
 */
public class BuildContextImpl {
    private String context;
    private String dockerfile;

    public BuildContextImpl() {}

    public BuildContextImpl(String context, String dockerfile) {
        this.context = context;
        this.dockerfile = dockerfile;
    }

    public BuildContextImpl(BuildContextImpl buildContext) {
        this.context = buildContext.getContext();
        this.dockerfile = buildContext.getDockerfile();
    }

    /**
     * Build context.
     *
     * <p/>Can be git repository, url to Dockerfile.
     */
    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public BuildContextImpl withContext(String context) {
        this.context = context;
        return this;
    }

    /**
     * Alternate Dockerfile.
     *
     * <p/> Needed if dockerfile has non-default name or is not placed in the root of build context.
     */
    public String getDockerfile() {
        return dockerfile;
    }

    public void setDockerfile(String dockerfile) {
        this.dockerfile = dockerfile;
    }

    public BuildContextImpl withDockerfile(String dockerfile) {
        this.dockerfile = dockerfile;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BuildContextImpl)) return false;
        BuildContextImpl that = (BuildContextImpl)o;
        return Objects.equals(context, that.context) &&
               Objects.equals(dockerfile, that.dockerfile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(context, dockerfile);
    }

    @Override
    public String toString() {
        return "BuildContextImpl{" +
               "context='" + context + '\'' +
               ", dockerfile='" + dockerfile + '\'' +
               '}';
    }
}
