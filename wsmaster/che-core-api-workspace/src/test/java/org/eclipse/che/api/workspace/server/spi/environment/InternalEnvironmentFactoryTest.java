/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server.spi.environment;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_LIMIT_ATTRIBUTE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.installer.server.InstallerRegistry;
import org.eclipse.che.api.installer.shared.model.Installer;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.RecipeImpl;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link InternalEnvironmentFactory}.
 *
 * @author Sergii Leshchenko
 */
@Listeners(MockitoTestNGListener.class)
public class InternalEnvironmentFactoryTest {

  public static final long RAM_LIMIT = 3072;

  @Mock private InstallerRegistry installerRegistry;
  @Mock private RecipeRetriever recipeRetriever;
  @Mock private MachineConfigsValidator machinesValidator;

  @Captor private ArgumentCaptor<Map<String, InternalMachineConfig>> machinesCaptor;

  private InternalEnvironmentFactory<InternalEnvironment> environmentFactory;

  @BeforeMethod
  public void setUp() throws Exception {
    environmentFactory =
        spy(new TestEnvironmentFactory(installerRegistry, recipeRetriever, machinesValidator));
    final InternalEnvironment internalEnv = mock(InternalEnvironment.class);
    when(internalEnv.getMachines()).thenReturn(Collections.emptyMap());
    when(environmentFactory.doCreate(any(), anyMap(), anyList())).thenReturn(internalEnv);
  }

  @Test
  public void shouldUseRetrievedRecipeWhileInternalEnvironmentCreation() throws Exception {
    // given
    RecipeImpl recipe = new RecipeImpl("type", "contentType", "content", "location");
    InternalRecipe retrievedRecipe = mock(InternalRecipe.class);
    doReturn(retrievedRecipe).when(recipeRetriever).getRecipe(any());

    EnvironmentImpl env = new EnvironmentImpl(recipe, null);

    // when
    environmentFactory.create(env);

    // then
    verify(recipeRetriever).getRecipe(recipe);
    verify(environmentFactory).doCreate(eq(retrievedRecipe), any(), any());
  }

  @Test
  public void shouldUseRetrievedInstallerWhileInternalEnvironmentCreation() throws Exception {
    // given
    List<Installer> installersToRetrieve = singletonList(mock(Installer.class));
    doReturn(installersToRetrieve).when(installerRegistry).getOrderedInstallers(anyList());

    List<String> sourceInstallers = singletonList("ws-agent");
    MachineConfigImpl machineConfig = new MachineConfigImpl().withInstallers(sourceInstallers);
    EnvironmentImpl env = new EnvironmentImpl(null, ImmutableMap.of("machine", machineConfig));

    // when
    environmentFactory.create(env);

    // then
    verify(installerRegistry).getOrderedInstallers(sourceInstallers);
    verify(environmentFactory).doCreate(any(), machinesCaptor.capture(), any());
    Map<String, InternalMachineConfig> internalMachines = machinesCaptor.getValue();
    assertEquals(internalMachines.get("machine").getInstallers(), installersToRetrieve);
  }

  @Test
  public void shouldUseNormalizedServersWhileInternalEnvironmentCreation() throws Exception {
    // given
    ServerConfigImpl server =
        new ServerConfigImpl("8080", "http", "/api", singletonMap("key", "value"));

    Map<String, ServerConfig> normalizedServers =
        ImmutableMap.of("server", mock(ServerConfig.class));
    doReturn(normalizedServers).when(environmentFactory).normalizeServers(any());

    ImmutableMap<String, ServerConfigImpl> sourceServers = ImmutableMap.of("server", server);
    MachineConfigImpl machineConfig = new MachineConfigImpl(null, sourceServers, null, null, null);

    EnvironmentImpl env = new EnvironmentImpl(null, ImmutableMap.of("machine", machineConfig));

    // when
    environmentFactory.create(env);

    // then
    verify(environmentFactory).normalizeServers(sourceServers);
    verify(environmentFactory).doCreate(any(), machinesCaptor.capture(), any());
    Map<String, InternalMachineConfig> internalMachines = machinesCaptor.getValue();
    assertEquals(internalMachines.get("machine").getServers(), normalizedServers);
  }

  @Test
  public void shouldReturnCreatedInternalEnvironment() throws Exception {
    // given
    InternalEnvironment expectedEnv = mock(InternalEnvironment.class);
    when(environmentFactory.doCreate(any(), any(), any())).thenReturn(expectedEnv);
    Environment env = mock(Environment.class);

    // when
    InternalEnvironment createdEnv = environmentFactory.create(env);

    // then
    assertEquals(createdEnv, expectedEnv);
  }

  @Test
  public void normalizeServersProtocols() throws InfrastructureException {
    ServerConfigImpl serverWithoutProtocol =
        new ServerConfigImpl("8080", "http", "/api", singletonMap("key", "value"));
    ServerConfigImpl udpServer =
        new ServerConfigImpl("8080/udp", "http", "/api", singletonMap("key", "value"));
    ServerConfigImpl normalizedServer =
        new ServerConfigImpl("8080/tcp", "http", "/api", singletonMap("key", "value"));

    Map<String, ServerConfig> servers = new HashMap<>();
    servers.put("serverWithoutProtocol", serverWithoutProtocol);
    servers.put("udpServer", udpServer);

    Map<String, ServerConfig> normalizedServers = environmentFactory.normalizeServers(servers);

    assertEquals(
        normalizedServers,
        ImmutableMap.of("serverWithoutProtocol", normalizedServer, "udpServer", udpServer));
  }

  @Test
  public void testSetsDefaultRamLimitAttributeToMachineThatDoesNotContainIt() throws Exception {
    final Map<String, String> attributes = new HashMap<>();
    final InternalEnvironment internalEnv = mock(InternalEnvironment.class);
    final InternalMachineConfig machine = mock(InternalMachineConfig.class);
    when(environmentFactory.doCreate(any(), any(), any())).thenReturn(internalEnv);
    when(internalEnv.getMachines()).thenReturn(ImmutableMap.of("testMachine", machine));
    when(machine.getAttributes()).thenReturn(attributes);

    environmentFactory.create(mock(Environment.class));

    assertTrue(attributes.containsKey(MEMORY_LIMIT_ATTRIBUTE));
    assertEquals(attributes.get(MEMORY_LIMIT_ATTRIBUTE), String.valueOf(RAM_LIMIT * 2 << 19));
  }

  @Test
  public void testDoNotOverrideRamLimitAttributeWhenMachineAlreadyContainsIt() throws Exception {
    final String ramLimit = "2147483648";
    final InternalEnvironment internalEnv = mock(InternalEnvironment.class);
    final InternalMachineConfig machine = mock(InternalMachineConfig.class);
    when(environmentFactory.doCreate(any(), any(), any())).thenReturn(internalEnv);
    when(internalEnv.getMachines()).thenReturn(ImmutableMap.of("testMachine", machine));
    when(machine.getAttributes()).thenReturn(ImmutableMap.of(MEMORY_LIMIT_ATTRIBUTE, ramLimit));

    environmentFactory.create(mock(Environment.class));

    assertEquals(machine.getAttributes().get(MEMORY_LIMIT_ATTRIBUTE), ramLimit);
  }

  private static class TestEnvironmentFactory
      extends InternalEnvironmentFactory<InternalEnvironment> {

    private TestEnvironmentFactory(
        InstallerRegistry installerRegistry,
        RecipeRetriever recipeRetriever,
        MachineConfigsValidator machinesValidator) {
      super(installerRegistry, recipeRetriever, machinesValidator, RAM_LIMIT);
    }

    @Override
    protected InternalEnvironment doCreate(InternalRecipe recipe, Map machines, List list)
        throws InfrastructureException, ValidationException {
      return null;
    }
  }
}
