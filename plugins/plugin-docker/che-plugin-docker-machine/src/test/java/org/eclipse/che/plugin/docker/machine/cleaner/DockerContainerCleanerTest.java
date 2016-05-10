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
package org.eclipse.che.plugin.docker.machine.cleaner;

import org.eclipse.che.api.machine.server.MachineRegistry;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.model.impl.MachineImpl;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.client.json.ContainerListEntry;
import org.eclipse.che.plugin.docker.client.params.RemoveContainerParams;
import org.eclipse.che.plugin.docker.machine.DockerContainerNameGenerator;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Optional;

import static java.util.Optional.of;
import static java.util.Arrays.asList;
import static org.eclipse.che.plugin.docker.machine.DockerContainerNameGenerator.ContainerNameInfo;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test for {@link DockerContainerCleaner}
 *
 * @author Alexander Andrienko
 */
@Listeners(MockitoTestNGListener.class)
public class DockerContainerCleanerTest {

    private static final String machineId1   = "machineid1";
    private static final String workspaceId1 = "workspaceid1";

    private static final String machineId2   = "machineid2";
    private static final String workspaceId2 = "workspaceid2";

    private static final String containerName1 = "containerName1";
    private static final String containerId1   = "containerId1";

    private static final String containerName2 = "containerName2";
    private static final String containerId2   = "containerId2";

    private static final String containerName3 = "containerName3";
    private static final String containerId3   = "containerId3";

    private static final String EXITED_STATUS  = "exited";
    private static final String RUNNING_STATUS = "Up 6 hour ago";

    @Mock
    private MachineRegistry              machineRegistry;
    @Mock
    private DockerConnector              dockerConnector;
    @Mock
    private DockerContainerNameGenerator nameGenerator;

    @Mock
    private MachineImpl machineImpl1;
    @Mock
    private MachineImpl machineImpl2;

    @Mock
    private ContainerListEntry container1;
    @Mock
    private ContainerListEntry container2;
    @Mock
    private ContainerListEntry container3;

    @Mock
    private ContainerNameInfo containerNameInfo1;
    @Mock
    private ContainerNameInfo containerNameInfo2;
    @Mock
    private ContainerNameInfo containerNameInfo3;

    @InjectMocks
    private DockerContainerCleaner cleaner;

    @BeforeMethod
    public void setUp() throws MachineException, IOException {
        when(machineRegistry.isExist(machineId1)).thenReturn(true);
        when(machineImpl1.getId()).thenReturn(machineId1);
        when(machineImpl1.getWorkspaceId()).thenReturn(workspaceId1);

        when(dockerConnector.listContainers()).thenReturn(asList(container1, container2, container3));

        when(container1.getNames()).thenReturn(new String[] {containerName1});
        when(container1.getStatus()).thenReturn(RUNNING_STATUS);
        when(container1.getId()).thenReturn(containerId1);

        when(container2.getNames()).thenReturn(new String[] {containerName2});
        when(container2.getStatus()).thenReturn(RUNNING_STATUS);
        when(container2.getId()).thenReturn(containerId2);

        when(container3.getNames()).thenReturn(new String[] {containerName3});
        when(container3.getStatus()).thenReturn(RUNNING_STATUS);
        when(container3.getId()).thenReturn(containerId3);

        when(nameGenerator.parse(containerName1)).thenReturn(of(containerNameInfo1));
        when(nameGenerator.parse(containerName2)).thenReturn(of(containerNameInfo2));
        when(nameGenerator.parse(containerName3)).thenReturn(of(containerNameInfo3));

        when(containerNameInfo1.getMachineId()).thenReturn(machineId1);
        when(containerNameInfo1.getWorkspaceId()).thenReturn(workspaceId1);

        when(containerNameInfo2.getMachineId()).thenReturn(machineId2);
        when(containerNameInfo2.getWorkspaceId()).thenReturn(workspaceId2);
    }

    @Test
    public void cleanerShouldKillAndRemoveContainerIfThisContainerIsRunningAndContainerNameInfoIsNotEmptyAndContainerIsNotExistInTheAPI()
            throws MachineException, IOException {
        cleaner.run();

        verify(dockerConnector).listContainers();

        verify(nameGenerator, times(3)).parse(anyString());
        verify(machineRegistry, times(3)).isExist(anyString());

        verify(dockerConnector, times(2)).killContainer(anyString());
        verify(dockerConnector, times(2)).removeContainer(Matchers.<RemoveContainerParams>anyObject());

        verify(dockerConnector, never()).killContainer(containerId1);
        verify(dockerConnector, never()).removeContainer(RemoveContainerParams.from(containerId1).withForce(true).withRemoveVolumes(true));
    }

    @Test
    public void cleanerShouldRemoveButShouldNotKillContainerWithStatusNotRunning() throws IOException, MachineException {
        when(container2.getStatus()).thenReturn(EXITED_STATUS);
        cleaner.run();

        verify(dockerConnector, never()).killContainer(containerId2);
        verify(dockerConnector).removeContainer(RemoveContainerParams.from(containerId2).withForce(true).withRemoveVolumes(true));
    }

    @Test
    public void cleanerShouldNotKillAndRemoveContainerIfMachineManagerDetectedExistingThisContainerInTheAPI() throws IOException {
        when(machineRegistry.isExist(anyString())).thenReturn(true);

        cleaner.run();

        verify(dockerConnector, never()).killContainer(anyString());

        verify(dockerConnector, never()).removeContainer(Matchers.<RemoveContainerParams>anyObject());
    }

    @Test
    public void cleanerShouldNotKillAndRemoveContainerIfContainerNameInfoIsEmpty() throws IOException {
        when(nameGenerator.parse(anyString())).thenReturn(Optional.empty());

        cleaner.run();

        verify(dockerConnector, never()).killContainer(anyString());

        verify(dockerConnector, never()).removeContainer(Matchers.<RemoveContainerParams>anyObject());
    }
}
