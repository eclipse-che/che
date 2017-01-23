/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.openshift.client.kubernetes;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.che.plugin.docker.client.json.ContainerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;

/**
 * Provides API for managing Kubernetes {@link EnvVar}
 */
public final class KubernetesEnvVar {
    private static final Logger LOG = LoggerFactory.getLogger(KubernetesEnvVar.class);

    private KubernetesEnvVar() {
    }

    /**
     * Retrieves list of {@link EnvVar} based on environment variables specified
     * in {@link ContainerConfig}
     *
     * @param envVariables
     * @return list of {@link EnvVar}
     */
    public static List<EnvVar> getEnvFrom(String[] envVariables) {
        LOG.info("Container environment variables:");
        List<EnvVar> env = new ArrayList<>();
        for (String envVariable : envVariables) {
            String[] nameAndValue = envVariable.split("=", 2);
            String varName = nameAndValue[0];
            String varValue = nameAndValue[1];
            EnvVar envVar = new EnvVarBuilder().withName(varName).withValue(varValue).build();
            env.add(envVar);
            LOG.info("- {}={}",varName, varValue);
        }
        return env;
    }
}
