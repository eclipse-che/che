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
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.lang.Pair;

import static org.eclipse.che.workspace.infrastructure.docker.DockerMachine.USER_TOKEN;

/**
 * Provides environment variable with a token that should be used by servers in a container to access Che master API.
 *
 * @author Alexander Garagatyi
 */
public class UserTokenEnvVarProvider implements ServerEnvironmentVariableProvider {
    @Override
    public Pair<String, String> get(RuntimeIdentity runtimeIdentity) {
        return Pair.of(USER_TOKEN, EnvironmentContext.getCurrent().getSubject().getToken());
    }
}
