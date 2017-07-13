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

import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.plugin.docker.client.DockerApiVersionPathPrefixProvider;
import org.eclipse.che.plugin.docker.client.DockerConnectorConfiguration;
import org.eclipse.che.plugin.docker.client.DockerRegistryAuthResolver;
import org.eclipse.che.plugin.docker.client.connection.DockerConnectionFactory;
import org.eclipse.che.plugin.docker.client.json.ContainerConfig;
import org.eclipse.che.plugin.docker.client.json.ImageConfig;
import org.eclipse.che.plugin.docker.client.params.CreateContainerParams;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

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
    private static final boolean  CREATE_WORKSPACE_DIRS = true;


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
    @Mock
    private OpenShiftPvcHelper                 openShiftPvcHelper;

    private OpenShiftConnector                 openShiftConnector;

    private Set<OpenShiftCommandAppender>      openShiftCommandAppenderSet = new HashSet<>();

    @BeforeMethod
    public void setup() {

        //When
        openShiftConnector = new OpenShiftConnector(dockerConnectorConfiguration,
                                                    dockerConnectionFactory,
                                                    authManager,
                                                    dockerApiVersionPathPrefixProvider,
                                                    openShiftPvcHelper,
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
                                                    SECURE_ROUTES,
                                                    CREATE_WORKSPACE_DIRS,
                                                    openShiftCommandAppenderSet);
    }

    @Test
    public void shouldGetWorkspaceIDWhenAValidOneIsProvidedInCreateContainerParams() throws IOException {
        //Given
        String expectedWorkspaceID="abcd1234";
        ContainerConfig containerConfig = mock(ContainerConfig.class);
        CreateContainerParams createContainerParams = CreateContainerParams.create(containerConfig);

        when(containerConfig.getEnv()).thenReturn(CONTAINER_ENV_VARIABLES);

        String workspaceID = openShiftConnector.getCheWorkspaceId(createContainerParams);

        //Then
        assertEquals(workspaceID, expectedWorkspaceID);
    }


    @Test
    public void shouldGetCommandOverrides() {

        openShiftCommandAppenderSet.add(new OpenShiftWorkspaceLogCommandAppender());
        ImageConfig imageConfig = Mockito.mock(ImageConfig.class);
        when(imageConfig.getCmd()).thenReturn(new String[] {"/bin/sh", "-c", "tail -f /dev/null"});

        List<String> updatedCommand = this.openShiftConnector.getCommand(imageConfig);
        Assert.assertNotNull(updatedCommand);
        Assert.assertEquals(updatedCommand.size(), 3);
        Assert.assertTrue(updatedCommand.get(2).contains("logs"));
    }


    @Test
    public void shouldGetCommandNotOverridesWithoutAppender() {

        ImageConfig imageConfig = Mockito.mock(ImageConfig.class);
        when(imageConfig.getCmd()).thenReturn(new String[] {"/bin/sh", "-c", "tail -f /dev/null"});

        List<String> updatedCommand = this.openShiftConnector.getCommand(imageConfig);
        Assert.assertNotNull(updatedCommand);
        Assert.assertEquals(updatedCommand.size(), 3);
        Assert.assertFalse(updatedCommand.get(2).contains("logs"));
    }



    @Test
    public void shouldGetCommandNotOverridesWithoutRightCommand() {

        ImageConfig imageConfig = Mockito.mock(ImageConfig.class);
        when(imageConfig.getCmd()).thenReturn(new String[] {"/bin/python"});

        List<String> updatedCommand = this.openShiftConnector.getCommand(imageConfig);
        Assert.assertNotNull(updatedCommand);
        Assert.assertEquals(updatedCommand.size(), 1);
        Assert.assertFalse(updatedCommand.get(0).contains("logs"));
    }
}
