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
package org.eclipse.che.workspace.infrastructure.docker.local;

import org.eclipse.che.plugin.docker.client.DockerConnectorConfiguration;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Collections;
import java.util.Set;

import static org.eclipse.che.workspace.infrastructure.docker.DockerMachine.CHE_HOST;

/**
 * Provides hosts set with location of che server as single entry.
 *
 * @author Alexander Garagatyi
 */
public class CheDockerExtraHostProvider implements Provider<Set<String>> {
    private Set<String> extraHosts;

    @Inject
    public CheDockerExtraHostProvider(DockerConnectorConfiguration dockerConnectorConfiguration) {
        // add Che server to hosts list
        String cheHost = dockerConnectorConfiguration.getDockerHostIp();
        extraHosts = Collections.singleton(CHE_HOST.concat(":").concat(cheHost));
    }

    @Override
    public Set<String> get() {
        return extraHosts;
    }
}
