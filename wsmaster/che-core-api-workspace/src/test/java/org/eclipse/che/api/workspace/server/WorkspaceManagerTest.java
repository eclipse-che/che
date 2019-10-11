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
import static org.eclipse.che.api.workspace.server.devfile.Constants.CURRENT_API_VERSION;
import static org.eclipse.che.api.workspace.shared.Constants.CREATED_ATTRIBUTE_NAME;
import static org.eclipse.che.api.workspace.shared.Constants.ERROR_MESSAGE_ATTRIBUTE_NAME;
import static org.eclipse.che.api.workspace.shared.Constants.STOPPED_ABNORMALLY_ATTRIBUTE_NAME;
import static org.eclipse.che.api.workspace.shared.Constants.STOPPED_ATTRIBUTE_NAME;
import static org.eclipse.che.api.workspace.shared.Constants.UPDATED_ATTRIBUTE_NAME;
import static org.eclipse.che.api.workspace.shared.Constants.WORKSPACE_GENERATE_NAME_CHARS_APPEND;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
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

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.eclipse.che.account.api.AccountManager;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.Runtime;
import org.eclipse.che.api.core.model.workspace.Warning;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.model.workspace.config.Command;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.core.model.workspace.devfile.Devfile;
import org.eclipse.che.api.core.model.workspace.runtime.Machine;
import org.eclipse.che.api.core.model.workspace.runtime.MachineStatus;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.devfile.convert.DevfileConverter;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileFormatException;
import org.eclipse.che.api.workspace.server.devfile.validator.DevfileIntegrityValidator;
import org.eclipse.che.api.workspace.server.model.impl.CommandImpl;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.MachineImpl;
import org.eclipse.che.api.workspace.server.model.impl.RecipeImpl;
import org.eclipse.che.api.workspace.server.model.impl.RuntimeImpl;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WarningImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.DevfileImpl;
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.api.workspace.shared.dto.devfile.DevfileDto;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.commons.lang.Pair;
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
  @Mock private DevfileConverter devfileConverter;
  @Mock private DevfileIntegrityValidator devfileIntegrityValidator;

  @Captor private ArgumentCaptor<WorkspaceImpl> workspaceCaptor;

  private WorkspaceManager workspaceManager;

  @BeforeMethod
  public void setUp() throws Exception {
    workspaceManager =
        new WorkspaceManager(
            workspaceDao,
            runtimes,
            eventService,
            accountManager,
            validator,
            devfileIntegrityValidator);
    lenient()
        .when(accountManager.getByName(NAMESPACE_1))
        .thenReturn(new AccountImpl("accountId", NAMESPACE_1, "test"));
    lenient()
        .when(accountManager.getByName(NAMESPACE_2))
        .thenReturn(new AccountImpl("accountId2", NAMESPACE_2, "test"));
    lenient()
        .when(workspaceDao.create(any(WorkspaceImpl.class)))
        .thenAnswer(invocation -> invocation.getArguments()[0]);
    lenient()
        .when(workspaceDao.update(any(WorkspaceImpl.class)))
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
    assertNotNull(workspace.getAttributes().get(CREATED_ATTRIBUTE_NAME));
    verify(workspaceDao).create(workspace);
  }

  @Test
  public void createsWorkspaceFromDevfile()
      throws ValidationException, ConflictException, NotFoundException, ServerException {
    final DevfileImpl devfile = new DevfileImpl();
    devfile.setApiVersion(CURRENT_API_VERSION);
    devfile.setName("ws");
    Workspace workspace = workspaceManager.createWorkspace(devfile, NAMESPACE_1, null, null);
    assertEquals(workspace.getDevfile(), devfile);
  }

  @Test
  public void createsWorkspaceFromDevfileWithGenerateName()
      throws ValidationException, ConflictException, NotFoundException, ServerException {
    final String testDevfileGenerateName = "ws-";
    final DevfileImpl devfile = new DevfileImpl();
    devfile.setApiVersion(CURRENT_API_VERSION);
    devfile.getMetadata().setGenerateName(testDevfileGenerateName);
    Workspace workspace = workspaceManager.createWorkspace(devfile, NAMESPACE_1, null, null);

    assertTrue(workspace.getDevfile().getName().startsWith(testDevfileGenerateName));
    assertEquals(
        workspace.getDevfile().getName().length(),
        testDevfileGenerateName.length() + WORKSPACE_GENERATE_NAME_CHARS_APPEND);
  }

  @Test
  public void nameIsUsedWhenNameAndGenerateNameSet()
      throws ValidationException, ConflictException, NotFoundException, ServerException {
    final String devfileName = "workspacename";
    final DevfileImpl devfile = new DevfileImpl();
    devfile.setApiVersion(CURRENT_API_VERSION);
    devfile.getMetadata().setName(devfileName);
    devfile.getMetadata().setGenerateName("this_will_not_be_set_as_a_name");
    Workspace workspace = workspaceManager.createWorkspace(devfile, NAMESPACE_1, null, null);

    assertEquals(workspace.getDevfile().getName(), devfileName);
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
        workspaceManager.getWorkspace(workspace.getName(), workspace.getNamespace());

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
        workspaceManager.getWorkspace(workspace.getNamespace() + ":" + workspace.getName());

    assertEquals(result, workspace);
  }

  @Test
  public void getsWorkspaceByKeyWithoutOwner() throws Exception {
    final WorkspaceImpl workspace = createAndMockWorkspace();

    final WorkspaceImpl result = workspaceManager.getWorkspace(":" + workspace.getName());

    assertEquals(result, workspace);
  }

  @Test
  public void getsWorkspacesAvailableForUserWithRuntimes() throws Exception {
    final WorkspaceConfig config = createConfig();

    final WorkspaceImpl workspace1 = createAndMockWorkspace(config, NAMESPACE_1);
    final WorkspaceImpl workspace2 = createAndMockWorkspace(config, NAMESPACE_2);
    final TestRuntime runtime2 = mockRuntime(workspace2, RUNNING);
    when(workspaceDao.getWorkspaces(eq(NAMESPACE_1), anyInt(), anyLong()))
        .thenReturn(new Page<>(asList(workspace1, workspace2), 0, 2, 2));

    final Page<WorkspaceImpl> result = workspaceManager.getWorkspaces(NAMESPACE_1, true, 30, 0);

    assertEquals(result.getItems().size(), 2);
    final WorkspaceImpl res1 = result.getItems().get(0);
    assertEquals(
        res1.getStatus(), STOPPED, "Workspace status wasn't changed from STARTING to STOPPED");
    assertNull(res1.getRuntime(), "Workspace has unexpected runtime");
    assertFalse(res1.isTemporary(), "Workspace must be permanent");
    final WorkspaceImpl res2 = result.getItems().get(1);
    assertEquals(
        res2.getStatus(),
        RUNNING,
        "Workspace status wasn't changed to the runtime instance status");
    assertEquals(
        res2.getRuntime(), new RuntimeImpl(runtime2), "Workspace doesn't have expected runtime");
    assertFalse(res2.isTemporary(), "Workspace must be permanent");
  }

  @Test
  public void getsWorkspacesAvailableForUserWithoutRuntimes() throws Exception {
    // given
    final WorkspaceConfig config = createConfig();

    final WorkspaceImpl workspace1 = createAndMockWorkspace(config, NAMESPACE_1);
    final WorkspaceImpl workspace2 = createAndMockWorkspace(config, NAMESPACE_2);

    when(workspaceDao.getWorkspaces(eq(NAMESPACE_1), anyInt(), anyLong()))
        .thenReturn(new Page<>(asList(workspace1, workspace2), 0, 2, 2));
    mockRuntimeStatus(workspace1, STOPPED);
    mockRuntimeStatus(workspace2, RUNNING);

    doNothing().when(runtimes).injectRuntime(workspace1);

    // when
    final Page<WorkspaceImpl> result = workspaceManager.getWorkspaces(NAMESPACE_1, false, 30, 0);

    // then
    assertEquals(result.getItems().size(), 2);

    final WorkspaceImpl res1 = result.getItems().get(0);
    assertEquals(
        res1.getStatus(), STOPPED, "Workspace status wasn't changed from STARTING to STOPPED");
    assertNull(res1.getRuntime(), "Workspace has unexpected runtime");
    assertFalse(res1.isTemporary(), "Workspace must be permanent");

    final WorkspaceImpl res2 = result.getItems().get(1);
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
    final Page<WorkspaceImpl> result =
        workspaceManager.getByNamespace(workspace.getNamespace(), false, 30, 0);

    // then
    assertEquals(result.getItems().size(), 1);

    final WorkspaceImpl res1 = result.getItems().get(0);
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
    final TestRuntime runtime = mockRuntime(workspace, RUNNING);

    // when
    final Page<WorkspaceImpl> result =
        workspaceManager.getByNamespace(workspace.getNamespace(), true, 30, 0);

    // then
    assertEquals(result.getItems().size(), 1);

    final WorkspaceImpl res1 = result.getItems().get(0);
    assertEquals(
        res1.getStatus(),
        RUNNING,
        "Workspace status wasn't changed to the runtime instance status");
    assertEquals(
        res1.getRuntime(), new RuntimeImpl(runtime), "Workspace doesn't have expected runtime");
    assertFalse(res1.isTemporary(), "Workspace must be permanent");
  }

  @Test
  public void getsWorkspaceByNameReturnWorkspaceWithStatusEqualToItsRuntimeStatus()
      throws Exception {
    final WorkspaceImpl workspace = createAndMockWorkspace();
    mockRuntime(workspace, STARTING);

    final WorkspaceImpl result =
        workspaceManager.getWorkspace(workspace.getName(), workspace.getNamespace());

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

  @Test(
      expectedExceptions = IllegalArgumentException.class,
      expectedExceptionsMessageRegExp =
          "Required non-null workspace configuration or devfile update but not both")
  public void shouldThrowExceptionWhenTryingToUpdateWorkspaceWithoutWorkspaceConfigAndDevfile()
      throws Exception {
    final WorkspaceImpl workspace = WorkspaceImpl.builder().generateId().build();

    workspaceManager.updateWorkspace(workspace.getId(), workspace);
  }

  @Test(
      expectedExceptions = IllegalArgumentException.class,
      expectedExceptionsMessageRegExp =
          "Required non-null workspace configuration or devfile update but not both")
  public void shouldThrowExceptionWhenTryingToUpdateWorkspaceWithWorkspaceConfigAndDevfile()
      throws Exception {
    final Workspace workspace =
        newDto(WorkspaceDto.class)
            .withId("workspace123")
            .withDevfile(newDto(DevfileDto.class))
            .withConfig(newDto(WorkspaceConfigDto.class));

    workspaceManager.updateWorkspace(workspace.getId(), workspace);
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

    verify(runtimes).startAsync(workspace, null, emptyMap());
  }

  @Test
  public void startsWorkspaceWithProvidedEnvironment() throws Exception {
    final WorkspaceConfigImpl config = createConfig();
    final EnvironmentImpl environment = new EnvironmentImpl(null, emptyMap());
    config.getEnvironments().put("non-default-env", environment);
    final WorkspaceImpl workspace = createAndMockWorkspace(config, NAMESPACE_1);

    mockAnyWorkspaceStart();

    workspaceManager.startWorkspace(workspace.getId(), "non-default-env", emptyMap());

    verify(runtimes).startAsync(eq(workspace), eq("non-default-env"), anyMap());
  }

  @Test
  public void startsWorkspaceWithDevfile() throws Exception {
    DevfileImpl devfile = mock(DevfileImpl.class);
    WorkspaceImpl workspace = createAndMockWorkspace(devfile, NAMESPACE_1);

    EnvironmentImpl environment = new EnvironmentImpl(null, emptyMap());
    Command command = new CommandImpl("cmd", "echo hello", "custom");
    WorkspaceConfig convertedConfig =
        new WorkspaceConfigImpl(
            "any",
            "",
            "default",
            singletonList(command),
            emptyList(),
            ImmutableMap.of("default", environment),
            ImmutableMap.of("attr", "value"));
    when(devfileConverter.convert(any())).thenReturn(convertedConfig);

    mockAnyWorkspaceStart();

    workspaceManager.startWorkspace(workspace.getId(), null, emptyMap());

    verify(runtimes).startAsync(eq(workspace), eq(null), anyMap());
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
        .startAsync(workspaceCaptor.capture(), eq(workspaceConfig.getDefaultEnv()), anyMap());
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
    assertNotNull(workspaceCaptor.getValue().getAttributes().get(STOPPED_ATTRIBUTE_NAME));
    assertFalse(
        Boolean.valueOf(
            workspaceCaptor.getValue().getAttributes().get(STOPPED_ABNORMALLY_ATTRIBUTE_NAME)));
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
  public void setsErrorAttributesAfterWorkspaceStartFailed() throws Exception {
    final WorkspaceConfigImpl workspaceConfig = createConfig();
    final WorkspaceImpl workspace = createAndMockWorkspace(workspaceConfig, NAMESPACE_1);
    mockAnyWorkspaceStartFailed(new ServerException("start failed"));

    workspaceManager.startWorkspace(workspace.getId(), null, null);
    // the first update is capturing the start time, the second update is capturing the error
    verify(workspaceDao, times(2)).update(workspaceCaptor.capture());
    Workspace ws = workspaceCaptor.getAllValues().get(1);
    assertNotNull(ws.getAttributes().get(STOPPED_ATTRIBUTE_NAME));
    assertTrue(Boolean.valueOf(ws.getAttributes().get(STOPPED_ABNORMALLY_ATTRIBUTE_NAME)));
    assertEquals(ws.getAttributes().get(ERROR_MESSAGE_ATTRIBUTE_NAME), "start failed");
  }

  @Test
  public void clearsErrorAttributesAfterWorkspaceStart() throws Exception {
    final WorkspaceConfigImpl workspaceConfig = createConfig();
    final WorkspaceImpl workspace = createAndMockWorkspace(workspaceConfig, NAMESPACE_1);
    workspace
        .getAttributes()
        .put(STOPPED_ATTRIBUTE_NAME, Long.toString(System.currentTimeMillis()));
    workspace.getAttributes().put(STOPPED_ABNORMALLY_ATTRIBUTE_NAME, Boolean.TRUE.toString());
    workspace.getAttributes().put(ERROR_MESSAGE_ATTRIBUTE_NAME, "start failed");
    when(workspaceDao.get(workspace.getId())).thenReturn(workspace);
    mockStart(workspace);

    workspaceManager.startWorkspace(workspace.getId(), null, emptyMap());
    verify(workspaceDao, atLeastOnce()).update(workspaceCaptor.capture());
    Workspace ws = workspaceCaptor.getAllValues().get(workspaceCaptor.getAllValues().size() - 1);
    assertNull(ws.getAttributes().get(STOPPED_ATTRIBUTE_NAME));
    assertNull(ws.getAttributes().get(STOPPED_ABNORMALLY_ATTRIBUTE_NAME));
    assertNull(ws.getAttributes().get(ERROR_MESSAGE_ATTRIBUTE_NAME));
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

  @Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "boom")
  public void shouldFailTocreateWorkspaceUsingInconsistentDevfile() throws Exception {
    // given
    doThrow(new DevfileFormatException("boom"))
        .when(devfileIntegrityValidator)
        .validateContentReferences(any(), any());

    Devfile devfile = mock(Devfile.class);

    // when
    workspaceManager.createWorkspace(devfile, "ns", emptyMap(), null);

    // then exception is thrown
  }

  private void mockRuntimeStatus(WorkspaceImpl workspace, WorkspaceStatus status) {
    lenient().when(runtimes.getStatus(workspace.getId())).thenReturn(status);
  }

  private TestRuntime mockRuntime(WorkspaceImpl workspace, WorkspaceStatus status)
      throws Exception {
    MachineImpl machine1 = spy(createMachine());
    MachineImpl machine2 = spy(createMachine());
    Map<String, Machine> machines = new HashMap<>();
    machines.put("machine1", machine1);
    machines.put("machine2", machine2);

    List<WarningImpl> warnings = new ArrayList<>();
    warnings.add(new WarningImpl(103, "used default value"));
    warnings.add(new WarningImpl(105, "specified configuration parameter is ignored"));

    TestRuntime runtime = new TestRuntime(machines, workspace.getConfig().getCommands(), warnings);
    lenient()
        .doAnswer(
            inv -> {
              workspace.setStatus(status);
              workspace.setRuntime(
                  new RuntimeImpl(
                      runtime.getActiveEnv(),
                      runtime.getMachines(),
                      runtime.getOwner(),
                      runtime.getCommands(),
                      runtime.getWarnings()));
              return null;
            })
        .when(runtimes)
        .injectRuntime(workspace);
    lenient().when(runtimes.isAnyActive()).thenReturn(true);

    return runtime;
  }

  private WorkspaceImpl createAndMockWorkspace() throws Exception {
    return createAndMockWorkspace(createConfig(), NAMESPACE_1);
  }

  private WorkspaceImpl createAndMockWorkspace(Devfile devfile, String namespace) throws Exception {
    WorkspaceImpl workspace =
        WorkspaceImpl.builder()
            .setDevfile(devfile)
            .generateId()
            .setAccount(new AccountImpl("id", namespace, "type"))
            .setStatus(STOPPED)
            .build();
    lenient().when(workspaceDao.get(workspace.getId())).thenReturn(workspace);
    lenient()
        .when(workspaceDao.get(workspace.getName(), workspace.getNamespace()))
        .thenReturn(workspace);
    lenient().when(workspaceDao.get(workspace.getName(), NAMESPACE_1)).thenReturn(workspace);
    lenient()
        .when(workspaceDao.getByNamespace(eq(workspace.getNamespace()), anyInt(), anyLong()))
        .thenReturn(new Page<>(singletonList(workspace), 0, 1, 1));
    lenient()
        .when(workspaceDao.getByNamespace(eq(NAMESPACE_1), anyInt(), anyLong()))
        .thenReturn(new Page<>(singletonList(workspace), 0, 1, 1));
    lenient()
        .when(workspaceDao.getWorkspaces(eq(USER_ID), anyInt(), anyLong()))
        .thenReturn(new Page<>(singletonList(workspace), 0, 1, 1));
    return workspace;
  }

  private WorkspaceImpl createAndMockWorkspace(WorkspaceConfig cfg, String namespace)
      throws Exception {
    WorkspaceImpl workspace =
        WorkspaceImpl.builder()
            .setConfig(cfg)
            .generateId()
            .setAccount(new AccountImpl("id", namespace, "type"))
            .setStatus(STOPPED)
            .build();
    lenient().when(workspaceDao.get(workspace.getId())).thenReturn(workspace);
    lenient()
        .when(workspaceDao.get(workspace.getName(), workspace.getNamespace()))
        .thenReturn(workspace);
    lenient().when(workspaceDao.get(workspace.getName(), NAMESPACE_1)).thenReturn(workspace);
    lenient()
        .when(workspaceDao.getByNamespace(eq(workspace.getNamespace()), anyInt(), anyLong()))
        .thenReturn(new Page<>(singletonList(workspace), 0, 1, 1));
    lenient()
        .when(workspaceDao.getByNamespace(eq(NAMESPACE_1), anyInt(), anyLong()))
        .thenReturn(new Page<>(singletonList(workspace), 0, 1, 1));
    lenient()
        .when(workspaceDao.getWorkspaces(eq(USER_ID), anyInt(), anyLong()))
        .thenReturn(new Page<>(singletonList(workspace), 0, 1, 1));
    return workspace;
  }

  private void mockStart(WorkspaceImpl workspace) throws Exception {
    CompletableFuture<Void> cmpFuture = CompletableFuture.completedFuture(null);
    lenient().when(runtimes.startAsync(eq(workspace), any(), any())).thenReturn(cmpFuture);
  }

  private void mockAnyWorkspaceStart() throws Exception {
    CompletableFuture<Void> cmpFuture = CompletableFuture.completedFuture(null);
    lenient().when(runtimes.startAsync(any(), any(), any())).thenReturn(cmpFuture);
  }

  private void mockAnyWorkspaceStop() throws Exception {
    CompletableFuture<Void> cmpFuture = CompletableFuture.completedFuture(null);
    doReturn(cmpFuture).when(runtimes).stopAsync(any(), any());
  }

  private void mockAnyWorkspaceStartFailed(Exception cause) throws Exception {
    final CompletableFuture<Void> cmpFuture = new CompletableFuture<>();
    cmpFuture.completeExceptionally(cause);
    lenient().when(runtimes.startAsync(any(), any(), any())).thenReturn(cmpFuture);
  }

  private static WorkspaceConfigImpl createConfig() {
    MachineConfigImpl machineConfig =
        new MachineConfigImpl(
            singletonList("org.eclipse.che.ws-agent"),
            singletonMap("server", createServerConfig()),
            singletonMap("CHE_ENV", "value"),
            singletonMap(MEMORY_LIMIT_ATTRIBUTE, "10000"),
            emptyMap());
    EnvironmentImpl environment =
        new EnvironmentImpl(
            new RecipeImpl("type", "contentType", "content", null),
            singletonMap("dev-machine", machineConfig));
    return WorkspaceConfigImpl.builder()
        .setName("dev-workspace")
        .setDefaultEnv("dev-env")
        .setEnvironments(singletonMap("dev-env", environment))
        .setCommands(asList(createCommand(), createCommand()))
        .build();
  }

  private Pair<String, Environment> getDefaultEnvironment(Workspace workspace) {
    return getEnvironment(workspace, workspace.getConfig().getDefaultEnv());
  }

  private Pair<String, Environment> getEnvironment(Workspace workspace, String envName) {
    return Pair.of(envName, workspace.getConfig().getEnvironments().get(envName));
  }

  private static ServerConfigImpl createServerConfig() {
    return new ServerConfigImpl("8080/tcp", "http", "/api", ImmutableMap.of("attr", "val"));
  }

  private static CommandImpl createCommand() {
    return new CommandImpl(NameGenerator.generate("command-", 5), "echo Hello", "custom");
  }

  private MachineImpl createMachine() {
    return new MachineImpl(emptyMap(), emptyMap(), MachineStatus.RUNNING);
  }

  private static class TestRuntime implements Runtime {

    final Map<String, Machine> machines;
    final List<? extends Command> commands;
    final List<? extends Warning> warnings;

    TestRuntime(
        Map<String, Machine> machines,
        List<? extends Command> commands,
        List<? extends Warning> warnings) {
      this.machines = machines;
      this.commands = commands;
      this.warnings = warnings;
    }

    @Override
    public String getActiveEnv() {
      return "default";
    }

    @Override
    public Map<String, ? extends Machine> getMachines() {
      return machines;
    }

    @Override
    public String getOwner() {
      return "owner";
    }

    @Override
    public List<? extends Warning> getWarnings() {
      return warnings;
    }

    @Override
    public List<? extends Command> getCommands() {
      return commands;
    }
  }
}
