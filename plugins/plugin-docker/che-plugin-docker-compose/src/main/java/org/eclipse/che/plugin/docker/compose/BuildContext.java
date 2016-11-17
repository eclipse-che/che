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
package org.eclipse.che.plugin.docker.compose;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Describes how to build image for container creation.
 *
 * @author Alexander Garagatyi
 */
public class BuildContext {
    private String              context;
    private String              dockerfile;
    private Map<String, String> args;

    public BuildContext() {}

    public BuildContext(String context, String dockerfile, Map<String,String> args) {
        this.context = context;
        this.dockerfile = dockerfile;
        if (args != null) {
          this.args = new HashMap<>(args);
        }
    }

    public BuildContext(BuildContext buildContext) {
        this(buildContext.getContext(),buildContext.getDockerfile(), buildContext.getArgs());
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

    public BuildContext withContext(String context) {
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

    public BuildContext withDockerfile(String dockerfile) {
        this.dockerfile = dockerfile;
        return this;
    }

    /**
     * Args for Dockerfile build.
     */
    public Map<String,String> getArgs() {
        if (args == null) {
            args = new HashMap<>();
        }
        return args;
    }

    public void setArgs(Map<String,String> args) {
        this.args = args;
    }

    public BuildContext withArgs(Map<String,String> args) {
        this.args = args;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BuildContext)) return false;
        BuildContext that = (BuildContext)o;
        return Objects.equals(context, that.context) &&
               Objects.equals(dockerfile, that.dockerfile) &&
               Objects.equals(args, that.args);
    }

    @Override
    public int hashCode() {
        return Objects.hash(context, dockerfile, args);
    }

    @Override
    public String toString() {
        return "BuildContext{" +
               "context='" + context + '\'' +
                ", dockerfile='" + dockerfile + '\'' +
                ", args='" + args + '\'' +
               '}';
    }
}
