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

import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.fabric8.kubernetes.api.model.EnvVar;

public class KubernetesEnvVarTest {

    @Test
    public void shouldReturnContainerEnvFromEnvVariableArray() {
        // Given
        String[] envVariables = {
                "CHE_LOCAL_CONF_DIR=/mnt/che/conf",
                "USER_TOKEN=dummy_token",
                "CHE_API_ENDPOINT=http://172.17.0.4:8080/wsmaster/api",
                "JAVA_OPTS=-Xms256m -Xmx2048m -Djava.security.egd=file:/dev/./urandom",
                "CHE_WORKSPACE_ID=workspaceID",
                "CHE_PROJECTS_ROOT=/projects",
                "TOMCAT_HOME=/home/user/tomcat8",
                "M2_HOME=/home/user/apache-maven-3.3.9",
                "TERM=xterm",
                "LANG=en_US.UTF-8"
        };

        // When
        List<EnvVar> env = KubernetesEnvVar.getEnvFrom(envVariables);

        // Then
        List<String> keysAndValues = env.stream().map(k -> k.getName() + "=" + k.getValue()).collect(Collectors.toList());
        assertTrue(Arrays.stream(envVariables).anyMatch(keysAndValues::contains));
    }

}
