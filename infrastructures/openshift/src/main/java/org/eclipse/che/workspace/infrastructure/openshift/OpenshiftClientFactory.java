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
package org.eclipse.che.workspace.infrastructure.openshift;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.openshift.client.DefaultOpenShiftClient;

import javax.inject.Inject;
import javax.inject.Named;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * @author Sergii Leshchenko
 */
public class OpenshiftClientFactory {
    private final Config config;

    @Inject
    public OpenshiftClientFactory(@Named("che.infra.openshift.master_url") String masterUrl,
                                  @Named("che.infra.openshift.username") String username,
                                  @Named("che.infra.openshift.password") String password,
                                  @Named("che.infra.openshift.trust_certs") boolean doTrustCerts) {
        config = new Config();
        if (!isNullOrEmpty(masterUrl)) {
            config.setMasterUrl(masterUrl);
        }

        if (!isNullOrEmpty(username)) {
            config.setUsername(username);
        }

        if (!isNullOrEmpty(password)) {
            config.setPassword(password);
        }

        config.setTrustCerts(doTrustCerts);
    }

    public DefaultOpenShiftClient create() {
        return new DefaultOpenShiftClient(config);
    }
}
