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
package org.eclipse.che.api.workspace.server;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STARTING;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STOPPED;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_LIMIT_ATTRIBUTE;
import static org.eclipse.che.api.workspace.server.WorkspaceManager.UPDATED_ATTRIBUTE_NAME;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.util.Strings.isNullOrEmpty;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.eclipse.che.account.api.AccountManager;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.model.workspace.runtime.Machine;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.MachineImpl;
import org.eclipse.che.api.workspace.server.model.impl.RecipeImpl;
import org.eclipse.che.api.workspace.server.model.impl.RuntimeIdentityImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.InternalRuntime;
import org.eclipse.che.api.workspace.server.spi.RuntimeContext;
import org.eclipse.che.api.workspace.server.spi.RuntimeInfrastructure;
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Covers main cases of {@link WorkspaceManager}.
 *
 * @author Yevhenii Voevodin
 * @author Anton Korneta
 */
@Listeners(value = {MockitoTestNGListener.class})
public class WorkspaceManagerTest {

  private static final String USER_ID = "user123";
  private static final String NAMESPACE_1 = "namespace/test1";
  private static final String NAMESPACE_2 = "namespace/test2";

  @Mock private WorkspaceDao workspaceDao;
  @Mock private WorkspaceRuntimes runtimes;
  @Mock private AccountManager accountManager;
  @Mock private EventService eventService;
  @Mock private WorkspaceValidator validator;
  @Mock private RuntimeInfrastructure infrastructure;

  @Captor private ArgumentCaptor<WorkspaceImpl> workspaceCaptor;

  private WorkspaceManager workspaceManager;

  @BeforeMethod
  public void setUp() throws Exception {
    workspaceManager =
        new WorkspaceManager(workspaceDao, runtimes, eventService, accountManager, validator);
    when(accountManager.getByName(NAMESPACE_1))
        .thenReturn(new AccountImpl("accountId", NAMESPACE_1, "test"));
    when(accountManager.getByName(NAMESPACE_2))
        .thenReturn(new AccountImpl("accountId2", NAMESPACE_2, "test"));
    when(workspaceDao.create(any(WorkspaceImpl.class)))
        .thenAnswer(invocation -> invocation.getArguments()[0]);
    when(workspaceDao.update(any(WorkspaceImpl.class)))
        .thenAnswer(invocation -> invocation.getArguments()[0]);

    EnvironmentContext.setCurrent(
        new EnvironmentContext() {
          @Override
          public Subject getSubject() {
            return new SubjectImpl(NAMESPACE_1, USER_ID, "token", false);
          }
        });
  }

  @Test
  public void createsWorkspace() throws Exception {
    final WorkspaceConfig cfg = createConfig();

    final WorkspaceImpl workspace = workspaceManager.createWorkspace(cfg, NAMESPACE_1, null);

    assertNotNull(workspace);
    assertFalse(isNullOrEmpty(workspace.getId()));
    assertEquals(workspace.getNamespace(), NAMESPACE_1);
    assertEquals(workspace.getConfig(), cfg);
    assertFalse(workspace.isTemporary());
    assertEquals(workspace.getStatus(), STOPPED);
    assertNotNull(workspace.getAttributes().get(WorkspaceManager.CREATED_ATTRIBUTE_NAME));
    verify(workspaceDao).create(workspace);
  }

  @Test
  public void getsWorkspaceByIdWithoutRuntime() throws Exception {
    WorkspaceImpl workspace = createAndMockWorkspace();

    final WorkspaceImpl result = workspaceManager.getWorkspace(workspace.getId());

    assertEquals(result, workspace);
  }

  @Test
  public void getsWorkspaceByIdWithRuntime() throws Exception {
    final WorkspaceImpl workspace = createAndMockWorkspace();
    mockStart(workspace);
    mockRuntime(workspace, STARTING);
    // createAndMockRuntime(workspace, WorkspaceStatus.STARTING);
    final WorkspaceImpl result =
        workspaceManager.startWorkspace(
            workspace.getId(), workspace.getConfig().getDefaultEnv(), emptyMap());

    assertEquals(
        result.getStatus(), STARTING, "Workspace status must be taken from the runtime instance");
  }

  @Test
  public void getsWorkspaceByName() throws Exception {
    final WorkspaceImpl workspace = createAndMockWorkspace();

    final WorkspaceImpl result =
        workspaceManager.getWorkspace(workspace.getConfig().getName(), workspace.getNamespace());

    assertEquals(result, workspace);
  }

  @Test(expectedExceptions = NotFoundException.class)
  public void throwsNotFoundExceptionWhenWorkspaceDoesNotExist() throws Exception {
    when(workspaceDao.get(any())).thenThrow(new NotFoundException("not found"));

    workspaceManager.getWorkspace("workspace123");
  }

  @Test
  public void getsWorkspaceByKey() throws Exception {
    final WorkspaceImpl workspace = createAndMockWorkspace();

    final WorkspaceImpl result =
        workspaceManager.getWorkspace(
            workspace.getNamespace() + ":" + workspace.getConfig().getName());

    assertEquals(result, workspace);
  }

  @Test
  public void getsWorkspaceByKeyWithoutOwner() throws Exception {
    final WorkspaceImpl workspace = createAndMockWorkspace();

    final WorkspaceImpl result =
        workspaceManager.getWorkspace(":" + workspace.getConfig().getName());

    assertEquals(result, workspace);
  }

  @Test
  public void getsWorkspacesAvailableForUserWithRuntimes() throws Exception {
    final WorkspaceConfig config = createConfig();

    final WorkspaceImpl workspace1 = createAndMockWorkspace(config, NAMESPACE_1);
    final WorkspaceImpl workspace2 = createAndMockWorkspace(config, NAMESPACE_2);
    final TestInternalRuntime runtime2 = mockRuntime(workspace2, RUNNING);
    when(workspaceDao.getWorkspaces(NAMESPACE_1)).thenReturn(asList(workspace1, workspace2));

    final List<WorkspaceImpl> result = workspaceManager.getWorkspaces(NAMESPACE_1, true);

    assertEquals(result.size(), 2);
    final WorkspaceImpl res1 = result.get(0);
    assertEquals(
        res1.getStatus(), STOPPED, "Workspace status wasn't changed from STARTING to STOPPED");
    assertNull(res1.getRuntime(), "Workspace has unexpected runtime");
    assertFalse(res1.isTemporary(), "Workspace must be permanent");
    final WorkspaceImpl res2 = result.get(1);
    assertEquals(
        res2.getStatus(),
        RUNNING,
        "Workspace status wasn't changed to the runtime instance status");
    assertEquals(res2.getRuntime(), runtime2, "Workspace doesn't have expected runtime");
    assertFalse(res2.isTemporary(), "Workspace must be permanent");
  }

  @Test
  public void getsWorkspacesAvailableForUserWithoutRuntimes() throws Exception {
    // given
    final WorkspaceConfig config = createConfig();

    final WorkspaceImpl workspace1 = createAndMockWorkspace(config, NAMESPACE_1);
    final WorkspaceImpl workspace2 = createAndMockWorkspace(config, NAMESPACE_2);

    when(workspaceDao.getWorkspaces(NAMESPACE_1)).thenReturn(asList(workspace1, workspace2));
    mockRuntimeStatus(workspace1, STOPPED);
    mockRuntimeStatus(workspace2, RUNNING);

    doNothing().when(runtimes).injectRuntime(workspace1);

    // when
    final List<WorkspaceImpl> result = workspaceManager.getWorkspaces(NAMESPACE_1, false);

    // then
    assertEquals(result.size(), 2);

    final WorkspaceImpl res1 = result.get(0);
    assertEquals(
        res1.getStatus(), STOPPED, "Workspace status wasn't changed from STARTING to STOPPED");
    assertNull(res1.getRuntime(), "Workspace has unexpected runtime");
    assertFalse(res1.isTemporary(), "Workspace must be permanent");

    final WorkspaceImpl res2 = result.get(1);
    assertEquals(
        res2.getStatus(),
        RUNNING,
        "Workspace status wasn't changed to the runtime instance status");
    assertNull(res1.getRuntime(), "Workspace has unexpected runtime");
    assertFalse(res2.isTemporary(), "Workspace must be permanent");
  }

  @Test
  public void getsWorkspacesByNamespaceWithoutRuntimes() throws Exception {
    // given
    final WorkspaceImpl workspace = createAndMockWorkspace();
    mockRuntimeStatus(workspace, RUNNING);

    // when
    final List<WorkspaceImpl> result =
        workspaceManager.getByNamespace(workspace.getNamespace(), false);

    // then
    assertEquals(result.size(), 1);

    final WorkspaceImpl res1 = result.get(0);
    assertEquals(
        res1.getStatus(),
        RUNNING,
        "Workspace status wasn't changed to the runtime instance status");
    assertNull(res1.getRuntime(), "workspace has unexpected runtime");
    assertFalse(res1.isTemporary(), "Workspace must be permanent");
  }

  @Test
  public void getsWorkspacesByNamespaceWithRuntimes() throws Exception {
    // given
    final WorkspaceImpl workspace = createAndMockWorkspace();
    final TestInternalRuntime runtime = mockRuntime(workspace, RUNNING);

    // when
    final List<WorkspaceImpl> result =
        workspaceManager.getByNamespace(workspace.getNamespace(), true);

    // then
    assertEquals(result.size(), 1);

    final WorkspaceImpl res1 = result.get(0);
    assertEquals(
        res1.getStatus(),
        RUNNING,
        "Workspace status wasn't changed to the runtime instance status");
    assertEquals(res1.getRuntime(), runtime, "Workspace doesn't have expected runtime");
    assertFalse(res1.isTemporary(), "Workspace must be permanent");
  }

  @Test
  public void getsWorkspaceByNameReturnWorkspaceWithStatusEqualToItsRuntimeStatus()
      throws Exception {
    final WorkspaceImpl workspace = createAndMockWorkspace();
    mockRuntime(workspace, STARTING);

    final WorkspaceImpl result =
        workspaceManager.getWorkspace(workspace.getConfig().getName(), workspace.getNamespace());

    assertEquals(
        result.getStatus(), STARTING, "Workspace status must be taken from the runtime instance");
  }

  @Test
  public void updatesWorkspace() throws Exception {
    final WorkspaceImpl workspace = new WorkspaceImpl(createAndMockWorkspace());
    workspace.setTemporary(true);
    workspace.getAttributes().put("new attribute", "attribute");
    when(workspaceDao.update(any())).thenAnswer(inv -> inv.getArguments()[0]);

    workspaceManager.updateWorkspace(workspace.getId(), workspace);

    verify(workspaceDao).update(workspace);
  }

  @Test
  public void updatesWorkspaceAndReturnWorkspaceWithStatusEqualToItsRuntimeStatus()
      throws Exception {
    final WorkspaceImpl workspace = createAndMockWorkspace();
    mockRuntime(workspace, STARTING);

    final WorkspaceImpl updated = workspaceManager.updateWorkspace(workspace.getId(), workspace);

    assertEquals(updated.getStatus(), STARTING);
  }

  @Test
  public void removesWorkspace() throws Exception {
    final WorkspaceImpl workspace = createAndMockWorkspace();

    workspaceManager.removeWorkspace(workspace.getId());

    verify(workspaceDao).remove(workspace.getId());
  }

  @Test(expectedExceptions = ConflictException.class)
  public void throwsExceptionWhenRemoveNotStoppedWorkspace() throws Exception {
    final WorkspaceImpl workspace = createAndMockWorkspace();
    when(runtimes.hasRuntime(workspace.getId())).thenReturn(true);

    workspaceManager.removeWorkspace(workspace.getId());
  }

  @Test
  public void startsWorkspaceById() throws Exception {
    final WorkspaceImpl workspace = createAndMockWorkspace();
    mockStart(workspace);

    workspaceManager.startWorkspace(
        workspace.getId(), workspace.getConfig().getDefaultEnv(), emptyMap());

    verify(runtimes).startAsync(workspace, workspace.getConfig().getDefaultEnv(), emptyMap());
    assertNotNull(workspace.getAttributes().get(UPDATED_ATTRIBUTE_NAME));
  }

  @Test
  public void startsWorkspaceWithDefaultEnvironment() throws Exception {
    final WorkspaceImpl workspace = createAndMockWorkspace();
    mockStart(workspace);

    workspaceManager.startWorkspace(workspace.getId(), null, emptyMap());

    verify(runtimes).startAsync(workspace, workspace.getConfig().getDefaultEnv(), emptyMap());
  }

  @Test
  public void startsWorkspaceWithProvidedEnvironment() throws Exception {
    final WorkspaceConfigImpl config = createConfig();
    final EnvironmentImpl environment = new EnvironmentImpl(null, emptyMap(), emptyList());
    config.getEnvironments().put("non-default-env", environment);
    final WorkspaceImpl workspace = createAndMockWorkspace(config, NAMESPACE_1);

    mockAnyWorkspaceStart();

    workspaceManager.startWorkspace(workspace.getId(), "non-default-env", emptyMap());

    verify(runtimes).startAsync(workspaceCaptor.capture(), eq("non-default-env"), any());
    assertEquals(workspaceCaptor.getValue().getConfig(), config);
  }

  @Test(
    expectedExceptions = NotFoundException.class,
    expectedExceptionsMessageRegExp = "Workspace '.*' doesn't contain environment '.*'"
  )
  public void throwsNotFoundExceptionWhenStartWorkspaceWithNotExistingEnv() throws Exception {
    final WorkspaceImpl workspace = createAndMockWorkspace();

    workspaceManager.startWorkspace(workspace.getId(), "fake", null);
  }

  @Test
  public void startsTemporaryWorkspace() throws Exception {
    final WorkspaceConfigImpl workspaceConfig = createConfig();
    final WorkspaceImpl workspace = createAndMockWorkspace(workspaceConfig, NAMESPACE_1);
    mockRuntime(workspace, STARTING);
    mockAnyWorkspaceStart();
    when(workspaceDao.get(workspace.getId())).thenReturn(workspace);

    workspaceManager.startWorkspace(workspaceConfig, workspace.getNamespace(), true, emptyMap());

    verify(runtimes)
        .startAsync(workspaceCaptor.capture(), eq(workspace.getConfig().getDefaultEnv()), any());
    assertTrue(workspaceCaptor.getValue().isTemporary());
  }

  @Test
  public void stopsWorkspace() throws Exception {
    final WorkspaceImpl workspace = createAndMockWorkspace(createConfig(), NAMESPACE_1);
    mockRuntime(workspace, RUNNING);
    mockAnyWorkspaceStop();

    workspaceManager.stopWorkspace(workspace.getId(), emptyMap());

    verify(runtimes).stopAsync(workspace, emptyMap());
    verify(workspaceDao).update(workspaceCaptor.capture());
    assertNotNull(workspaceCaptor.getValue().getAttributes().get(UPDATED_ATTRIBUTE_NAME));
  }

  @Test
  public void removesTemporaryWorkspaceAfterStop() throws Exception {
    final WorkspaceImpl workspace = createAndMockWorkspace();
    workspace.setTemporary(true);
    mockRuntime(workspace, RUNNING);
    mockAnyWorkspaceStop();

    workspaceManager.stopWorkspace(workspace.getId(), emptyMap());

    verify(runtimes).stopAsync(workspace, emptyMap());
    verify(workspaceDao).remove(workspace.getId());
  }

  @Test
  public void removesTemporaryWorkspaceAfterStopFailed() throws Exception {
    final WorkspaceImpl workspace = createAndMockWorkspace();
    workspace.setTemporary(true);
    mockRuntime(workspace, RUNNING);
    doThrow(new ConflictException("runtime stop failed"))
        .when(runtimes)
        .stopAsync(workspace, emptyMap());
    mockAnyWorkspaceStop();

    workspaceManager.stopWorkspace(workspace.getId(), emptyMap());
    verify(workspaceDao).remove(workspace.getId());
  }

  @Test
  public void removesTemporaryWorkspaceAfterStartFailed() throws Exception {
    final WorkspaceConfigImpl workspaceConfig = createConfig();
    final WorkspaceImpl workspace = createAndMockWorkspace(workspaceConfig, NAMESPACE_1);
    workspace.setTemporary(true);
    mockAnyWorkspaceStartFailed(new ServerException("start failed"));

    workspaceManager.startWorkspace(workspaceConfig, workspace.getNamespace(), true, emptyMap());

    verify(workspaceDao, times(1)).remove(anyString());
  }

  private void mockRuntimeStatus(WorkspaceImpl workspace, WorkspaceStatus status) {
    when(runtimes.getStatus(workspace.getId())).thenReturn(status);
  }

  private TestInternalRuntime mockRuntime(WorkspaceImpl workspace, WorkspaceStatus status)
      throws Exception {
    RuntimeIdentity identity =
        new RuntimeIdentityImpl(
            workspace.getId(), workspace.getConfig().getDefaultEnv(), workspace.getNamespace());
    //        doAnswer(inv -> {
    //            final WorkspaceImpl ws = (WorkspaceImpl)inv.getArguments()[0];
    //            ws.setStatus(status);
    //            return ws;
    //        }).when(runtimes).injectStatus(workspace);
    MachineImpl machine1 = spy(createMachine());
    MachineImpl machine2 = spy(createMachine());
    Map<String, Machine> machines = new HashMap<>();
    machines.put("machine1", machine1);
    machines.put("machine2", machine2);
    TestInternalRuntime runtime = new TestInternalRuntime(mockContext(identity), machines);
    doAnswer(
            inv -> {
              workspace.setStatus(status);
              workspace.setRuntime(runtime);
              return null;
            })
        .when(runtimes)
        .injectRuntime(workspace);
    when(runtimes.isAnyRunning()).thenReturn(true);
    return runtime;
  }

  private WorkspaceImpl createAndMockWorkspace() throws Exception {
    return createAndMockWorkspace(createConfig(), NAMESPACE_1);
  }

  private WorkspaceImpl createAndMockWorkspace(WorkspaceConfig cfg, String namespace)
      throws Exception {
    WorkspaceImpl workspace =
        WorkspaceImpl.builder()
            .generateId()
            .setConfig(cfg)
            .setAccount(new AccountImpl("id", namespace, "type"))
            .setStatus(STOPPED)
            .build();
    when(workspaceDao.get(workspace.getId())).thenReturn(workspace);
    when(workspaceDao.get(workspace.getConfig().getName(), workspace.getNamespace()))
        .thenReturn(workspace);
    when(workspaceDao.get(workspace.getConfig().getName(), NAMESPACE_1)).thenReturn(workspace);
    when(workspaceDao.getByNamespace(workspace.getNamespace()))
        .thenReturn(singletonList(workspace));
    when(workspaceDao.getByNamespace(NAMESPACE_1)).thenReturn(singletonList(workspace));
    when(workspaceDao.getWorkspaces(USER_ID)).thenReturn(singletonList(workspace));
    return workspace;
  }

  private void mockStart(Workspace workspace) throws Exception {
    CompletableFuture<Void> cmpFuture = CompletableFuture.completedFuture(null);
    when(runtimes.startAsync(eq(workspace), eq(workspace.getConfig().getDefaultEnv()), any()))
        .thenReturn(cmpFuture);
  }

  private void mockAnyWorkspaceStart() throws Exception {
    CompletableFuture<Void> cmpFuture = CompletableFuture.completedFuture(null);
    when(runtimes.startAsync(any(), anyString(), any())).thenReturn(cmpFuture);
  }

  private void mockAnyWorkspaceStop() throws Exception {
    CompletableFuture<Void> cmpFuture = CompletableFuture.completedFuture(null);
    when(runtimes.stopAsync(any(), any())).thenReturn(cmpFuture);
  }

  private void mockAnyWorkspaceStartFailed(Exception cause) throws Exception {
    final CompletableFuture<Void> cmpFuture = new CompletableFuture<>();
    cmpFuture.completeExceptionally(cause);
    when(runtimes.startAsync(any(), anyString(), any())).thenReturn(cmpFuture);
  }

  private static WorkspaceConfigImpl createConfig() {
    MachineConfigImpl machineConfig =
        new MachineConfigImpl(
            singletonList("org.eclipse.che.ws-agent"),
            null,
            singletonMap("CHE_ENV", "value"),
            singletonMap(MEMORY_LIMIT_ATTRIBUTE, "10000"));
    EnvironmentImpl environment =
        new EnvironmentImpl(
            new RecipeImpl("type", "contentType", "content", null),
            singletonMap("dev-machine", machineConfig),
            emptyList());
    return WorkspaceConfigImpl.builder()
        .setName("dev-workspace")
        .setDefaultEnv("dev-env")
        .setEnvironments(singletonMap("dev-env", environment))
        .build();
  }

  private MachineImpl createMachine() {
    return new MachineImpl(emptyMap(), emptyMap());
  }

  private RuntimeContext mockContext(RuntimeIdentity identity) throws Exception {
    RuntimeContext context = mock(RuntimeContext.class);
    doReturn(context).when(infrastructure).prepare(eq(identity), any(InternalEnvironment.class));
    when(context.getInfrastructure()).thenReturn(infrastructure);
    when(context.getIdentity()).thenReturn(identity);
    return context;
  }

  private static class TestInternalRuntime extends InternalRuntime<RuntimeContext> {
    final Map<String, Machine> machines;

    TestInternalRuntime(RuntimeContext context, Map<String, Machine> machines) {
      super(context, null, false);
      this.machines = machines;
    }

    @Override
    protected Map<String, Machine> getInternalMachines() {
      return machines;
    }

    @Override
    public Map<String, String> getProperties() {
      return Collections.emptyMap();
    }

    @Override
    protected void internalStop(Map stopOptions) throws InfrastructureException {
      throw new UnsupportedOperationException();
    }

    @Override
    protected void internalStart(Map startOptions) throws InfrastructureException {
      throw new UnsupportedOperationException();
    }
  }
}
