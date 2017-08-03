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
package org.eclipse.che.workspace.infrastructure.docker.provisioner.server;

import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.commons.lang.Pair;

import javax.inject.Inject;
import javax.inject.Named;

import static org.eclipse.che.workspace.infrastructure.docker.DockerMachine.JAVA_OPTS_VARIABLE;

/**
 * Add env variable to docker machine with java opts
 *
 * @author Roman Iuvshyn
 * @author Alexander Garagatyi
 */
public class JavaOptsEnvVariableProvider implements ServerEnvironmentVariableProvider {
    private String javaOpts;

    @Inject
    public JavaOptsEnvVariableProvider(@Named("che.workspace.java.options") String javaOpts) {
        this.javaOpts = javaOpts;
    }

    @Override
    public Pair<String, String> get(RuntimeIdentity runtimeIdentity) {
        return Pair.of(JAVA_OPTS_VARIABLE, javaOpts);
    }
}
