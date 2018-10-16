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

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.provision.env.AgentAuthEnableEnvVarProvider;
import org.eclipse.che.api.workspace.server.spi.provision.env.MachineTokenEnvVarProvider;
import org.eclipse.che.api.workspace.server.wsplugins.model.PluginMeta;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.BrokersResult;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Alexander Garagatyi */
@Listeners(MockitoTestNGListener.class)
public class BrokerEnvironmentFactoryTest {

  @Mock private AgentAuthEnableEnvVarProvider authEnableEnvVarProvider;
  @Mock private MachineTokenEnvVarProvider machineTokenEnvVarProvider;
  @Mock private RuntimeIdentity runtimeId;

  private BrokerEnvironmentFactory<KubernetesEnvironment> factory;

  @BeforeMethod
  public void setUp() throws Exception {
    factory =
        new BrokerEnvironmentFactory<KubernetesEnvironment>(
            "http://localhost:8080",
            "Never",
            authEnableEnvVarProvider,
            machineTokenEnvVarProvider,
            ImmutableMap.of(
                "Test type 1", "testRepo/image:tag", "Test type 2", "testRepo/image2:tag2")) {
          @Override
          protected KubernetesEnvironment doCreate(BrokersConfigs brokersConfigs) {
            return null;
          }
        };

    when(authEnableEnvVarProvider.get(any(RuntimeIdentity.class)))
        .thenReturn(new Pair<>("test1", "value1"));
    when(machineTokenEnvVarProvider.get(any(RuntimeIdentity.class)))
        .thenReturn(new Pair<>("test2", "value2"));
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
}
