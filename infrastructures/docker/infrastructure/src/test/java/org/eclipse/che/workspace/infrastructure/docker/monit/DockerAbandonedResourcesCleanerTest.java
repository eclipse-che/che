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
package org.eclipse.che.workspace.infrastructure.docker.monit;

import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;

/**
 * Test for {@link DockerAbandonedResourcesCleaner}
 *
 * @author Alexander Andrienko
 */
@Listeners(MockitoTestNGListener.class)
public class DockerAbandonedResourcesCleanerTest {
  /*
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

      private static final String abandonedNetworkId    = "abandonedNetworkId";
      private static final String usedNetworkId         = "usedNetworkId";
      private static final String additionalNetworkId   = "CheAdditionalNetworkId";

      private static final String abandonedNetworkName  = "workspace1234567890abcdef_1234567890abcdef";
      private static final String usedNetworkName       = "workspace0987654321zyxwvu_0987654321zyxwvu";
      private static final String additionalNetworkName = "CheAdditionalNetwork";

      @Mock
      private CheEnvironmentEngine         environmentEngine;
      @Mock
      private DockerConnectorProvider      dockerConnectorProvider;
      @Mock
      private DockerConnector              dockerConnector;
      @Mock
      private DockerContainerNameGenerator nameGenerator;
      @Mock
      private WorkspaceRuntimes            workspaceRuntimes;

      @Mock
      private Instance instance;
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
      private DockerContainerNameGenerator.ContainerNameInfo containerNameInfo1;
      @Mock
      private DockerContainerNameGenerator.ContainerNameInfo containerNameInfo2;
      @Mock
      private DockerContainerNameGenerator.ContainerNameInfo containerNameInfo3;

      @Mock
      private Network abandonedNetwork;
      @Mock
      private Network usedNetwork;
      @Mock
      private Network additionalNetwork;

      @Mock
      private ContainerInNetwork containerInNetwork1;

      private List<Network>                   networks;
      private Map<String, ContainerInNetwork> abandonedNetworkContainers;
      private Map<String, ContainerInNetwork> usedNetworkContainers;
      private Map<String, ContainerInNetwork> additionalNetworkContainers = new HashMap<>();

      private Set<Set<String>> additionalNetworks = new HashSet<>();
      private Set<String>      userNetworks       = new HashSet<>(Arrays.asList(additionalNetworkName));

      private DockerAbandonedResourcesCleaner cleaner;

      @BeforeMethod
      public void setUp() throws Exception {
          networks = new ArrayList<>();
          abandonedNetworkContainers = new HashMap<>();
          usedNetworkContainers = new HashMap<>();

          when(dockerConnectorProvider.get()).thenReturn(dockerConnector);
          cleaner = spy(new DockerAbandonedResourcesCleaner(environmentEngine,
                                                            dockerConnectorProvider,
                                                            nameGenerator,
                                                            workspaceRuntimes,
                                                            additionalNetworks));

          when(environmentEngine.getMachine(workspaceId1, machineId1)).thenReturn(instance);
          when(environmentEngine.getMachine(workspaceId2, machineId2)).thenThrow(new NotFoundException("test"));
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

          when(containerNameInfo3.getMachineId()).thenReturn(machineId2);
          when(containerNameInfo3.getWorkspaceId()).thenReturn(workspaceId2);

          when(dockerConnector.getNetworks(any())).thenReturn(networks);

          when(abandonedNetwork.getId()).thenReturn(abandonedNetworkId);
          when(usedNetwork.getId()).thenReturn(usedNetworkId);
          when(additionalNetwork.getId()).thenReturn(abandonedNetworkId);

          when(abandonedNetwork.getName()).thenReturn(abandonedNetworkName);
          when(usedNetwork.getName()).thenReturn(usedNetworkName);
          when(additionalNetwork.getName()).thenReturn(abandonedNetworkName);

          when(abandonedNetwork.getContainers()).thenReturn(abandonedNetworkContainers);
          when(usedNetwork.getContainers()).thenReturn(usedNetworkContainers);
          when(additionalNetwork.getContainers()).thenReturn(additionalNetworkContainers);
      }

      @Test
      public void cleanerShouldRunCleanOfContainerAndThenCleanOfNetworks() {
          // when
          cleaner.run();

          // then
          verify(cleaner).cleanContainers();
          verify(cleaner).cleanNetworks();
      }

      @Test
      public void cleanerShouldRunCleanNetworksEvenIfCleanOfContainersFailed() throws IOException {
          // given
          when(dockerConnector.listContainers()).thenThrow(new IOException("Error while fetching docker containers list"));

          // when
          cleaner.run();

          // then
          verify(cleaner).cleanNetworks();
      }

      @Test
      public void cleanerShouldKillAndRemoveContainerIfThisContainerIsRunningAndContainerNameInfoIsNotEmptyAndContainerIsNotExistInTheAPI()
              throws Exception {
          cleaner.cleanContainers();

          verify(dockerConnector).listContainers();

          verify(nameGenerator, times(3)).parse(anyString());
          verify(environmentEngine, times(3)).getMachine(anyString(), anyString());

          verify(dockerConnector, times(2)).killContainer(anyString());
          verify(dockerConnector, times(2)).removeContainer(Matchers.anyObject());

          verify(dockerConnector, never()).killContainer(containerId1);
          verify(dockerConnector, never()).removeContainer(RemoveContainerParams.create(containerId1).withForce(true).withRemoveVolumes(true));
      }

      @Test
      public void cleanerShouldRemoveButShouldNotKillContainerWithStatusNotRunning() throws Exception {
          when(container2.getStatus()).thenReturn(EXITED_STATUS);
          cleaner.cleanContainers();

          verify(dockerConnector, never()).killContainer(containerId2);
          verify(dockerConnector).removeContainer(RemoveContainerParams.create(containerId2).withForce(true).withRemoveVolumes(true));
      }

      @Test
      public void cleanerShouldNotKillAndRemoveContainerIfMachineManagerDetectedExistingThisContainerInTheAPI() throws Exception {
          when(environmentEngine.getMachine(anyString(), anyString())).thenReturn(instance);

          cleaner.cleanContainers();

          verify(dockerConnector, never()).killContainer(anyString());

          verify(dockerConnector, never()).removeContainer(Matchers.anyObject());
      }

      @Test
      public void cleanerShouldNotKillAndRemoveContainerIfContainerNameInfoIsEmpty() throws IOException {
          when(nameGenerator.parse(anyString())).thenReturn(Optional.empty());

          cleaner.cleanContainers();

          verify(dockerConnector, never()).killContainer(anyString());

          verify(dockerConnector, never()).removeContainer(Matchers.anyObject());
      }

      @Test
      public void shouldRemoveAbandonedNetwork() throws IOException {
          // given
          networks.add(abandonedNetwork);

          // when
          cleaner.cleanNetworks();

          // then
          verify(dockerConnector).removeNetwork(eq(abandonedNetworkId));
      }

      @Test
      public void shouldNotRemoveNetworkIfItNameNotMatchCheNetworkPattern() throws IOException {
          // given
          when(abandonedNetwork.getName()).thenReturn("UserNetwork");
          networks.add(abandonedNetwork);

          // when
          cleaner.cleanNetworks();

          // then
          verify(dockerConnector, never()).removeNetwork(eq(abandonedNetworkId));
      }

      @Test
      public void shouldNotRemoveNetworkWhenItContainsContainer() throws IOException {
          // given
          usedNetworkContainers.put(containerId1, containerInNetwork1);

          networks.add(usedNetwork);

          // when
          cleaner.cleanNetworks();

          // then
          verify(dockerConnector, never()).removeNetwork(eq(usedNetworkId));
      }

      @Test
      public void shouldNotRemoveNetworkWhichIsInWorkspaceRuntime() throws IOException {
          // given
          final String usedNetworkWorkspace = usedNetworkName.substring(0, 25);
          when(workspaceRuntimes.hasRuntime(usedNetworkWorkspace)).thenReturn(true);

          networks.add(usedNetwork);

          // when
          cleaner.cleanNetworks();

          // then
          verify(dockerConnector, never()).removeNetwork(eq(usedNetworkId));
      }

      @Test
      public void shouldRemoveOnlyAbandonedNetworks() throws IOException {
          // given
          usedNetworkContainers.put(containerId1, containerInNetwork1);

          networks.add(abandonedNetwork);
          networks.add(usedNetwork);

          // when
          cleaner.cleanNetworks();

          // then
          verify(dockerConnector).removeNetwork(abandonedNetworkId);
          verify(dockerConnector, never()).removeNetwork(usedNetworkId);
      }

      @Test
      public void shouldNotRemoveNetworkIfItInAdditionalNetworksList() throws IOException {
          // given
          additionalNetworks.add(userNetworks);
          cleaner = spy(new DockerAbandonedResourcesCleaner(environmentEngine,
                                                            dockerConnectorProvider,
                                                            nameGenerator,
                                                            workspaceRuntimes,
                                                            additionalNetworks));
          networks.add(additionalNetwork);

          // when
          cleaner.cleanNetworks();

          // then
          verify(dockerConnector, never()).removeNetwork(additionalNetworkId);
      }

      @Test
      public void shouldNotRemoveNetworkIfItInAdditionalNetworksListAndHasNameAsNetworkToRemove() throws IOException {
          // given
          final String additionalNetworkName = "workspace1additional1netw_ork1name4test148";
          userNetworks = new HashSet<>(Arrays.asList(additionalNetworkName));
          additionalNetworks.add(userNetworks);
          cleaner = spy(new DockerAbandonedResourcesCleaner(environmentEngine,
                                                            dockerConnectorProvider,
                                                            nameGenerator,
                                                            workspaceRuntimes,
                                                            additionalNetworks));

          when(additionalNetwork.getName()).thenReturn(additionalNetworkName);
          networks.add(additionalNetwork);

          // when
          cleaner.cleanNetworks();

          // then
          verify(dockerConnector, never()).removeNetwork(additionalNetworkId);
      }

      @Test
      public void shouldRemoveAbandonedNetworkEvenIfRemovingOfPreviousOneFailed() throws IOException {
          // given
          doThrow(new IOException("Failed to remove docker network")).when(dockerConnector).removeNetwork(usedNetworkId);

          networks.add(abandonedNetwork);
          networks.add(usedNetwork);

          // when
          cleaner.cleanNetworks();

          // then
          verify(dockerConnector).removeNetwork(abandonedNetworkId);
          verify(dockerConnector).removeNetwork(usedNetworkId);
      }

      @Test
      public void shouldBeAbleToRemoveSeveralAbandonedNetworks() throws IOException {
          // given
          final Network abandonedNetwork2 = mock(Network.class);
          final String abandonedNetwork2Id = "network2";
          when(abandonedNetwork2.getId()).thenReturn(abandonedNetwork2Id);
          when(abandonedNetwork2.getName()).thenReturn("workspace0w5kg95j93kd9a1l_cjmd8rbnf9j9dnso");
          when(abandonedNetwork2.getContainers()).thenReturn(new HashMap<>());

          final Network userNetwork = mock(Network.class);
          final String userNetworkId = "network4";
          when(userNetwork.getId()).thenReturn(userNetworkId);
          when(userNetwork.getName()).thenReturn("userNetwork");
          when(userNetwork.getContainers()).thenReturn(new HashMap<>());

          usedNetworkContainers.put(containerId1, containerInNetwork1);

          networks.add(usedNetwork);
          networks.add(abandonedNetwork);
          networks.add(abandonedNetwork2);
          networks.add(userNetwork);

          // when
          cleaner.cleanNetworks();

          // then
          verify(dockerConnector, never()).removeNetwork(usedNetworkId);
          verify(dockerConnector, never()).removeNetwork(userNetworkId);
          verify(dockerConnector).removeNetwork(abandonedNetworkId);
          verify(dockerConnector).removeNetwork(abandonedNetworkId);
      }
  */
}
