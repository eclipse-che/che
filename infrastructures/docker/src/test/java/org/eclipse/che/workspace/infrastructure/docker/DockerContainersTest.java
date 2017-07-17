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
package org.eclipse.che.workspace.infrastructure.docker;

import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.api.workspace.server.spi.RuntimeIdentityImpl;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.client.json.ContainerConfig;
import org.eclipse.che.plugin.docker.client.json.ContainerInfo;
import org.eclipse.che.plugin.docker.client.json.ContainerListEntry;
import org.eclipse.che.plugin.docker.client.params.ListContainersParams;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/** Tests {@link DockerContainers}. */
@Listeners(MockitoTestNGListener.class)
public class DockerContainersTest {

    @Mock
    private DockerConnector docker;

    @InjectMocks
    private DockerContainers containers;

    @Test
    public void findsIdentifiers() throws Exception {
        RuntimeIdentity id1 = new RuntimeIdentityImpl("workspace123", "default", "test");
        RuntimeIdentity id2 = new RuntimeIdentityImpl("workspace234", "default", "test");

        List<ContainerListEntry> entries = asList(mockContainer(id1, "container1"), mockContainer(id2, "container2"));
        when(docker.listContainers(ListContainersParams.create().withAll(false))).thenReturn(entries);

        assertEquals(containers.findIdentities(), newHashSet(id1, id2));
    }

    @Test(expectedExceptions = InfrastructureException.class, expectedExceptionsMessageRegExp = "oops")
    public void findsIdentitiesRethrowsIOExceptionThrownWhileListingContainersAsInternalInfraException() throws Exception {
        when(docker.listContainers(ListContainersParams.create().withAll(false))).thenThrow(new IOException("oops"));

        containers.findIdentities();
    }

    @Test
    public void findContainers() throws Exception {
        RuntimeIdentity id1 = new RuntimeIdentityImpl("workspace123", "default", "test");
        ContainerListEntry entry1 = mockContainer(id1, "container1");
        ContainerListEntry entry2 = mockContainer(id1, "container2");

        RuntimeIdentity id2 = new RuntimeIdentityImpl("workspace234", "default", "test");
        ContainerListEntry entry3 = mockContainer(id2, "container3");

        List<ContainerListEntry> entries = asList(entry1, entry2, entry3);
        when(docker.listContainers(ListContainersParams.create().withAll(false))).thenReturn(entries);

        assertEquals(containers.find(id1)
                               .stream()
                               .map(ContainerListEntry::getId)
                               .collect(Collectors.toSet()), newHashSet(entry1.getId(), entry2.getId()));
        assertEquals(containers.find(id2)
                               .stream()
                               .map(ContainerListEntry::getId)
                               .collect(Collectors.toSet()), newHashSet(entry3.getId()));
    }

    @Test(expectedExceptions = InternalInfrastructureException.class, expectedExceptionsMessageRegExp = "oops")
    public void findContainersRethrowsIOExceptionThrownWhileListingContainersAsInternalInfraException() throws Exception {
        when(docker.listContainers(ListContainersParams.create().withAll(false))).thenThrow(new IOException("oops"));

        containers.find(new RuntimeIdentityImpl("workspace123", "default", "test"));
    }

    @Test(expectedExceptions = InternalInfrastructureException.class, expectedExceptionsMessageRegExp = "oops")
    public void findContainersRethrowsIOExceptionThrownWhileInspectingContainersAsInternalInfraException() throws Exception {
        RuntimeIdentity id = new RuntimeIdentityImpl("workspace123", "default", "test");
        List<ContainerListEntry> entries = Collections.singletonList(mockContainer(id, "container"));
        when(docker.listContainers(ListContainersParams.create().withAll(false))).thenReturn(entries);

        when(docker.inspectContainer(anyString())).thenThrow(new IOException("oops"));

        containers.find(id);
    }

    private ContainerListEntry mockContainer(RuntimeIdentity runtimeId, String containerId) throws IOException {
        ContainerListEntry entry = new ContainerListEntry();
        entry.setLabels(Labels.newSerializer().runtimeId(runtimeId).labels());
        entry.setId(containerId);
        return entry;
    }
}
