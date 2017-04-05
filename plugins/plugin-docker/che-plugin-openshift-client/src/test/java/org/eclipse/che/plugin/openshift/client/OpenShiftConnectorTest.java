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
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.io.IOException;

import org.eclipse.che.api.core.notification.EventService;
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
    private static final int      OPENSHIFT_LIVENESS_PROBE_DELAY = 300;
    private static final int      OPENSHIFT_LIVENESS_PROBE_TIMEOUT = 1;
    private static final String   OPENSHIFT_DEFAULT_WORKSPACE_PERSISTENT_VOLUME_CLAIM = "che_claim_data";
    private static final String   OPENSHIFT_DEFAULT_WORKSPACE_QUANTITY = "10Gi";
    private static final String   OPENSHIFT_DEFAULT_WORKSPACE_STORAGE = "/data/workspaces";
    private static final String   OPENSHIFT_DEFAULT_WORKSPACE_PROJECTS_STORAGE = "/projects";
    private static final String   CHE_DEFAULT_SERVER_EXTERNAL_ADDRESS = "che.openshift.mini";
    private static final String   CHE_WORKSPACE_CPU_LIMIT = "1";
    private static final boolean  SECURE_ROUTES = false;


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
    @Mock
    private EventService                       eventService;

    private OpenShiftConnector                 openShiftConnector;

    @Test
    public void shouldGetWorkspaceIDWhenAValidOneIsProvidedInCreateContainerParams() throws IOException {
        //Given
        String expectedWorkspaceID="abcd1234";
        ContainerConfig containerConfig = mock(ContainerConfig.class);
        CreateContainerParams createContainerParams = CreateContainerParams.create(containerConfig);

        when(containerConfig.getEnv()).thenReturn(CONTAINER_ENV_VARIABLES);

        //When
        openShiftConnector = new OpenShiftConnector(dockerConnectorConfiguration,
                                                    dockerConnectionFactory,
                                                    authManager,
                                                    dockerApiVersionPathPrefixProvider,
                                                    eventService,
                                                    CHE_DEFAULT_SERVER_EXTERNAL_ADDRESS,
                                                    CHE_DEFAULT_OPENSHIFT_PROJECT_NAME,
                                                    OPENSHIFT_LIVENESS_PROBE_DELAY,
                                                    OPENSHIFT_LIVENESS_PROBE_TIMEOUT,
                                                    OPENSHIFT_DEFAULT_WORKSPACE_PERSISTENT_VOLUME_CLAIM,
                                                    OPENSHIFT_DEFAULT_WORKSPACE_QUANTITY,
                                                    OPENSHIFT_DEFAULT_WORKSPACE_STORAGE,
                                                    OPENSHIFT_DEFAULT_WORKSPACE_PROJECTS_STORAGE,
                                                    CHE_WORKSPACE_CPU_LIMIT,
                                                    null,
                                                    SECURE_ROUTES);
        String workspaceID = openShiftConnector.getCheWorkspaceId(createContainerParams);

        //Then
        assertEquals(workspaceID, expectedWorkspaceID);
    }
}
