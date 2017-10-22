/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.docker.container;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.model.impl.RuntimeIdentityImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.infrastructure.docker.client.DockerConnector;
import org.eclipse.che.infrastructure.docker.client.json.ContainerListEntry;
import org.eclipse.che.infrastructure.docker.client.params.ListContainersParams;
import org.eclipse.che.workspace.infrastructure.docker.Labels;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** Tests {@link DockerContainers}. */
@Listeners(MockitoTestNGListener.class)
public class DockerContainersTest {

  @Mock private DockerConnector docker;

  @InjectMocks private DockerContainers containers;

  @Test
  public void findsIdentifiers() throws Exception {
    RuntimeIdentity id1 = new RuntimeIdentityImpl("workspace123", "default", "test");
    RuntimeIdentity id2 = new RuntimeIdentityImpl("workspace234", "default", "test");

    List<ContainerListEntry> entries =
        asList(mockContainer(id1, "container1"), mockContainer(id2, "container2"));
    when(docker.listContainers(anyObject())).thenReturn(entries);

    assertEquals(containers.findIdentities(), newHashSet(id1, id2));

    ArgumentCaptor<ListContainersParams> paramsCaptor =
        ArgumentCaptor.forClass(ListContainersParams.class);
    verify(docker).listContainers(paramsCaptor.capture());
    ListContainersParams params = paramsCaptor.getValue();
    assertEquals(
        new HashSet<>(params.getFilters().getFilter("label")),
        Sets.newHashSet(
            Labels.LABEL_WORKSPACE_ID, Labels.LABEL_WORKSPACE_ENV, Labels.LABEL_WORKSPACE_OWNER));
  }

  @Test(
    expectedExceptions = InfrastructureException.class,
    expectedExceptionsMessageRegExp = "oops"
  )
  public void
      findsIdentitiesRethrowsIOExceptionThrownWhileListingContainersAsInternalInfraException()
          throws Exception {
    when(docker.listContainers(anyObject())).thenThrow(new IOException("oops"));

    containers.findIdentities();
  }

  @Test
  public void findContainers() throws Exception {
    RuntimeIdentity id = new RuntimeIdentityImpl("workspace123", "default", "test");
    ContainerListEntry entry1 = mockContainer(id, "container1");
    ContainerListEntry entry2 = mockContainer(id, "container2");

    when(docker.listContainers(anyObject())).thenReturn(Arrays.asList(entry1, entry2));

    assertEquals(containers.find(id), Arrays.asList(entry1, entry2));

    ArgumentCaptor<ListContainersParams> paramsCaptor =
        ArgumentCaptor.forClass(ListContainersParams.class);
    verify(docker).listContainers(paramsCaptor.capture());
    ListContainersParams params = paramsCaptor.getValue();
    assertEquals(
        params.getFilters().getFilter("label"),
        Labels.newSerializer()
            .runtimeId(id)
            .labels()
            .entrySet()
            .stream()
            .map(entry -> entry.getKey() + '=' + entry.getValue())
            .collect(Collectors.toList()));
  }

  @Test(
    expectedExceptions = InternalInfrastructureException.class,
    expectedExceptionsMessageRegExp = "oops"
  )
  public void
      findContainersRethrowsIOExceptionThrownWhileListingContainersAsInternalInfraException()
          throws Exception {
    when(docker.listContainers(anyObject())).thenThrow(new IOException("oops"));

    containers.find(new RuntimeIdentityImpl("workspace123", "default", "test"));
  }

  private ContainerListEntry mockContainer(RuntimeIdentity runtimeId, String containerId)
      throws IOException {
    ContainerListEntry entry = new ContainerListEntry();
    entry.setLabels(Labels.newSerializer().runtimeId(runtimeId).labels());
    entry.setId(containerId);
    return entry;
  }
}
