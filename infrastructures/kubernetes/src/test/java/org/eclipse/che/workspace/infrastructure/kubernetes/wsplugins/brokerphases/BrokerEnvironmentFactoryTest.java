/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.brokerphases;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.PodSpec;
import java.util.Collection;
import java.util.List;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.provision.env.AgentAuthEnableEnvVarProvider;
import org.eclipse.che.api.workspace.server.spi.provision.env.MachineTokenEnvVarProvider;
import org.eclipse.che.api.workspace.server.wsplugins.model.PluginMeta;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.Containers;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.BrokersResult;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.brokerphases.BrokerEnvironmentFactory.BrokersConfigs;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Alexander Garagatyi */
@Listeners(MockitoTestNGListener.class)
public class BrokerEnvironmentFactoryTest {

  private static final String SUPPORTED_TYPE = "Test type 1";
  private static final String INIT_IMAGE = "init:image";
  private static final String IMAGE_PULL_POLICY = "Never";
  private static final String PUSH_ENDPOINT = "http://localhost:8080";

  @Mock private AgentAuthEnableEnvVarProvider authEnableEnvVarProvider;
  @Mock private MachineTokenEnvVarProvider machineTokenEnvVarProvider;
  @Mock private RuntimeIdentity runtimeId;

  private BrokerEnvironmentFactory<KubernetesEnvironment> factory;

  @BeforeMethod
  public void setUp() throws Exception {
    factory =
        spy(
            new BrokerEnvironmentFactory<KubernetesEnvironment>(
                PUSH_ENDPOINT,
                IMAGE_PULL_POLICY,
                authEnableEnvVarProvider,
                machineTokenEnvVarProvider,
                ImmutableMap.of(
                    SUPPORTED_TYPE, "testRepo/image:tag", "Test type 2", "testRepo/image2:tag2"),
                INIT_IMAGE) {
              @Override
              protected KubernetesEnvironment doCreate(BrokersConfigs brokersConfigs) {
                return null;
              }
            });

    when(authEnableEnvVarProvider.get(any(RuntimeIdentity.class)))
        .thenReturn(new Pair<>("test1", "value1"));
    when(machineTokenEnvVarProvider.get(any(RuntimeIdentity.class)))
        .thenReturn(new Pair<>("test2", "value2"));
    when(runtimeId.getEnvName()).thenReturn("env");
    when(runtimeId.getOwnerId()).thenReturn("owner");
    when(runtimeId.getWorkspaceId()).thenReturn("wsid");
  }

  @Test(
      expectedExceptions = InfrastructureException.class,
      expectedExceptionsMessageRegExp = "Plugin '.*:.*' has invalid type 'null'")
  public void shouldThrowExceptionIfBrokerTypeIsMissing() throws Exception {
    // given
    Collection<PluginMeta> metas = singletonList(new PluginMeta());

    // when
    factory.create(metas, runtimeId, new BrokersResult());
  }

  @Test(
      expectedExceptions = InfrastructureException.class,
      expectedExceptionsMessageRegExp =
          "Plugin '.*:.*' has unsupported type 'Unsupported test type'")
  public void shouldThrowExceptionIfImageForBrokerIsNonFound() throws Exception {
    // given
    Collection<PluginMeta> metas = singletonList(new PluginMeta().type("Unsupported test type"));

    // when
    factory.create(metas, runtimeId, new BrokersResult());
  }

  @Test
  public void testInitBrokerContainer() throws Exception {
    // given
    Collection<PluginMeta> metas = singletonList(new PluginMeta().type(SUPPORTED_TYPE));
    ArgumentCaptor<BrokersConfigs> captor = ArgumentCaptor.forClass(BrokersConfigs.class);

    // when
    factory.create(metas, runtimeId, new BrokersResult());

    // then
    verify(factory).doCreate(captor.capture());
    BrokersConfigs brokersConfigs = captor.getValue();
    List<Container> initContainers = brokersConfigs.pod.getSpec().getInitContainers();
    assertEquals(initContainers.size(), 1);
    Container initContainer = initContainers.get(0);
    assertEquals(initContainer.getName(), "init-image");
    assertEquals(initContainer.getImage(), INIT_IMAGE);
    assertEquals(initContainer.getImagePullPolicy(), IMAGE_PULL_POLICY);
    assertEquals(
        initContainer.getEnv(),
        asList(new EnvVar("test1", "value1", null), new EnvVar("test2", "value2", null)));
    assertEquals(
        initContainer.getArgs().toArray(),
        new String[] {
          "-push-endpoint",
          PUSH_ENDPOINT,
          "-runtime-id",
          String.format(
              "%s:%s:%s",
              runtimeId.getWorkspaceId(), runtimeId.getEnvName(), runtimeId.getOwnerId())
        });
    assertEquals(Containers.getRamLimit(initContainer), 262144000);
    assertEquals(Containers.getRamLimit(initContainer), 262144000);
  }

  @Test
  public void shouldNameContainersAfterPluginBrokerImage() throws Exception {
    // given
    Collection<PluginMeta> metas = singletonList(new PluginMeta().type(SUPPORTED_TYPE));
    ArgumentCaptor<BrokersConfigs> captor = ArgumentCaptor.forClass(BrokersConfigs.class);

    // when
    factory.create(metas, runtimeId, new BrokersResult());

    // then
    verify(factory).doCreate(captor.capture());
    BrokersConfigs brokersConfigs = captor.getValue();
    PodSpec brokerPodSpec = brokersConfigs.pod.getSpec();

    List<Container> initContainers = brokerPodSpec.getInitContainers();
    assertEquals(initContainers.size(), 1);
    assertEquals(initContainers.get(0).getName(), "init-image");

    List<Container> containers = brokerPodSpec.getContainers();
    assertEquals(containers.size(), 1);
    assertEquals(containers.get(0).getName(), "testrepo-image-tag");
  }
}
