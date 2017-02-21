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
package org.eclipse.che.plugin.openshift.client;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.io.IOException;

import io.fabric8.kubernetes.client.ConfigBuilder;
import org.eclipse.che.plugin.docker.client.DockerApiVersionPathPrefixProvider;
import org.eclipse.che.plugin.docker.client.DockerConnectorConfiguration;
import org.eclipse.che.plugin.docker.client.DockerRegistryAuthResolver;
import org.eclipse.che.plugin.docker.client.connection.DockerConnectionFactory;
import org.eclipse.che.plugin.docker.client.json.ContainerConfig;
import org.eclipse.che.plugin.docker.client.params.CreateContainerParams;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class OpenShiftConnectorTest {
    private static final String[] CONTAINER_ENV_VARIABLES = {"CHE_WORKSPACE_ID=abcd1234"};
    private static final String   CHE_DEFAULT_OPENSHIFT_PROJECT_NAME = "eclipse-che";
    private static final String   CHE_DEFAULT_OPENSHIFT_SERVICEACCOUNT = "cheserviceaccount";
    private static final String   OPENSHIFT_API_ENDPOINT_MINISHIFT = "https://192.168.64.2:8443/";
    private static final String   OPENSHIFT_DEFAULT_USER_NAME = "openshift-dev";
    private static final String   OPENSHIFT_DEFAULT_USER_PASSWORD = "devel";
    private static final int      OPENSHIFT_LIVENESS_PROBE_DELAY = 300;
    private static final int      OPENSHIFT_LIVENESS_PROBE_TIMEOUT = 1;
    private static final String   OPENSHIFT_DEFAULT_TOKEN = "91XMfu-FuNDkGjcIh6b0y1EtCvztGeSsSqRrWhBfyL8";

    @Mock
    private DockerConnectorConfiguration       dockerConnectorConfiguration;
    @Mock
    private DockerConnectionFactory            dockerConnectionFactory;
    @Mock
    private DockerRegistryAuthResolver         authManager;
    @Mock
    private DockerApiVersionPathPrefixProvider dockerApiVersionPathPrefixProvider;
    @Mock
    private CreateContainerParams              createContainerParams;

    private OpenShiftConnector                 openShiftConnector;

    @Test
    public void shouldGetWorkspaceIDWhenAValidOneIsProvidedInCreateContainerParams() throws IOException {
        //Given
        String expectedWorkspaceID="abcd1234";
        ContainerConfig containerConfig = mock(ContainerConfig.class);
        CreateContainerParams createContainerParams = CreateContainerParams.create(containerConfig);

        when(containerConfig.getEnv()).thenReturn(CONTAINER_ENV_VARIABLES);

        //When
        openShiftConnector = new OpenShiftConnector(new ConfigBuilder(),
                dockerConnectorConfiguration,
                dockerConnectionFactory,
                authManager,
                dockerApiVersionPathPrefixProvider,
                OPENSHIFT_API_ENDPOINT_MINISHIFT,
                OPENSHIFT_DEFAULT_TOKEN,
                OPENSHIFT_DEFAULT_USER_NAME,
                OPENSHIFT_DEFAULT_USER_PASSWORD,
                CHE_DEFAULT_OPENSHIFT_PROJECT_NAME,
                CHE_DEFAULT_OPENSHIFT_SERVICEACCOUNT,
                OPENSHIFT_LIVENESS_PROBE_DELAY,
                OPENSHIFT_LIVENESS_PROBE_TIMEOUT);
        String workspaceID = openShiftConnector.getCheWorkspaceId(createContainerParams);

        //Then
        assertEquals(workspaceID, expectedWorkspaceID);
    }

    @Test
    public void shouldUseTokenWhenProvided() {
        // Given
        ConfigBuilder configBuilder = spy(new ConfigBuilder());

        // When
        openShiftConnector = new OpenShiftConnector(configBuilder,
                dockerConnectorConfiguration,
                dockerConnectionFactory,
                authManager,
                dockerApiVersionPathPrefixProvider,
                OPENSHIFT_API_ENDPOINT_MINISHIFT,
                OPENSHIFT_DEFAULT_TOKEN,
                OPENSHIFT_DEFAULT_USER_NAME,
                OPENSHIFT_DEFAULT_USER_PASSWORD,
                CHE_DEFAULT_OPENSHIFT_PROJECT_NAME,
                CHE_DEFAULT_OPENSHIFT_SERVICEACCOUNT,
                OPENSHIFT_LIVENESS_PROBE_DELAY,
                OPENSHIFT_LIVENESS_PROBE_TIMEOUT);

        // Then
        verify(configBuilder,times(1)).withOauthToken(OPENSHIFT_DEFAULT_TOKEN);
        verify(configBuilder,times(0)).withUsername(OPENSHIFT_DEFAULT_USER_NAME);
    }

    @Test
    public void shouldUsePasswordWhenTokenIsNotProvided() {
        // Given
        ConfigBuilder configBuilder = spy(new ConfigBuilder());

        // When
        openShiftConnector = new OpenShiftConnector(configBuilder,
                dockerConnectorConfiguration,
                dockerConnectionFactory,
                authManager,
                dockerApiVersionPathPrefixProvider,
                OPENSHIFT_API_ENDPOINT_MINISHIFT,
                "",
                OPENSHIFT_DEFAULT_USER_NAME,
                OPENSHIFT_DEFAULT_USER_PASSWORD,
                CHE_DEFAULT_OPENSHIFT_PROJECT_NAME,
                CHE_DEFAULT_OPENSHIFT_SERVICEACCOUNT,
                OPENSHIFT_LIVENESS_PROBE_DELAY,
                OPENSHIFT_LIVENESS_PROBE_TIMEOUT);

        // Then
        verify(configBuilder,times(0)).withOauthToken(OPENSHIFT_DEFAULT_TOKEN);
        verify(configBuilder,times(1)).withUsername(OPENSHIFT_DEFAULT_USER_NAME);
    }

}
