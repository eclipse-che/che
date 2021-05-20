/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
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
import static java.util.Map.of;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STARTING;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STOPPED;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_LIMIT_ATTRIBUTE;
import static org.eclipse.che.api.workspace.server.devfile.Constants.CURRENT_API_VERSION;
import static org.eclipse.che.api.workspace.shared.Constants.CREATED_ATTRIBUTE_NAME;
import static org.eclipse.che.api.workspace.shared.Constants.ERROR_MESSAGE_ATTRIBUTE_NAME;
import static org.eclipse.che.api.workspace.shared.Constants.LAST_ACTIVE_INFRASTRUCTURE_NAMESPACE;
import static org.eclipse.che.api.workspace.shared.Constants.LAST_ACTIVITY_TIME;
import static org.eclipse.che.api.workspace.shared.Constants.REMOVE_WORKSPACE_AFTER_STOP;
import static org.eclipse.che.api.workspace.shared.Constants.STOPPED_ABNORMALLY_ATTRIBUTE_NAME;
import static org.eclipse.che.api.workspace.shared.Constants.STOPPED_ATTRIBUTE_NAME;
import static org.eclipse.che.api.workspace.shared.Constants.UPDATED_ATTRIBUTE_NAME;
import static org.eclipse.che.api.workspace.shared.Constants.WORKSPACE_GENERATE_NAME_CHARS_APPEND;
import static org.eclipse.che.api.workspace.shared.Constants.WORKSPACE_INFRASTRUCTURE_NAMESPACE_ATTRIBUTE;
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
import static org.mockito.Mockito.never;
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
import com.google.common.collect.ImmutableSet;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.eclipse.che.api.user.server.PreferenceManager;
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
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.NamespaceResolutionContext;
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
  private static final String INFRA_NAMESPACE = "ns";

  @Mock private WorkspaceDao workspaceDao;
  @Mock private WorkspaceRuntimes runtimes;
  @Mock private AccountManager accountManager;
  @Mock private EventService eventService;
  @Mock private WorkspaceValidator validator;
  @Mock private DevfileConverter devfileConverter;
  @Mock private DevfileIntegrityValidator devfileIntegrityValidator;
  @Mock private PreferenceManager preferenceManager;

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
            preferenceManager,
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

    when(runtimes.isInfrastructureNamespaceValid(any())).thenReturn(true);
  }

  @Test
  public void createsWorkspace() throws Exception {
    final WorkspaceConfig cfg = createConfig();

    when(runtimes.evalInfrastructureNamespace(any())).thenReturn("ns");

    final WorkspaceImpl workspace =
        workspaceManager.createWorkspace(cfg, NAMESPACE_1, ImmutableMap.of("attr", "value"));

    assertNotNull(workspace);
    assertFalse(isNullOrEmpty(workspace.getId()));
    assertEquals(workspace.getNamespace(), NAMESPACE_1);
    assertEquals(workspace.getConfig(), cfg);
    assertFalse(workspace.isTemporary());
    assertEquals(workspace.getStatus(), STOPPED);
    assertNotNull(workspace.getAttributes().get(CREATED_ATTRIBUTE_NAME));
    assertEquals("ns", workspace.getAttributes().get(WORKSPACE_INFRASTRUCTURE_NAMESPACE_ATTRIBUTE));
    verify(workspaceDao).create(workspace);
    verify(validator).validateAttributes(ImmutableMap.of("attr", "value"));
  }

  @Test
  public void createsWorkspaceFromDevfile()
      throws ValidationException, ConflictException, NotFoundException, ServerException,
          InfrastructureException {
    final DevfileImpl devfile = new DevfileImpl();
    devfile.setApiVersion(CURRENT_API_VERSION);
    devfile.setName("ws");
    when(runtimes.evalInfrastructureNamespace(any())).thenReturn("ns");
    Workspace workspace = workspaceManager.createWorkspace(devfile, NAMESPACE_1, null, null);
    assertEquals(workspace.getDevfile(), devfile);
  }

  @Test
  public void createsWorkspaceFromDevfileWithGenerateName()
      throws ValidationException, ConflictException, NotFoundException, ServerException,
          InfrastructureException {
    final String testDevfileGenerateName = "ws-";
    final DevfileImpl devfile = new DevfileImpl();
    devfile.setApiVersion(CURRENT_API_VERSION);
    devfile.getMetadata().setGenerateName(testDevfileGenerateName);
    when(runtimes.evalInfrastructureNamespace(any())).thenReturn("ns");
    Workspace workspace = workspaceManager.createWorkspace(devfile, NAMESPACE_1, null, null);

    assertTrue(workspace.getDevfile().getName().startsWith(testDevfileGenerateName));
    assertEquals(
        workspace.getDevfile().getName().length(),
        testDevfileGenerateName.length() + WORKSPACE_GENERATE_NAME_CHARS_APPEND);
  }

  @Test
  public void evaluatesInfraNamespaceIfMissingOnWorkspaceCreation() throws Exception {
    when(runtimes.evalInfrastructureNamespace(any())).thenReturn("evaluated");

    final WorkspaceConfig cfg = createConfig();

    final WorkspaceImpl workspace = workspaceManager.createWorkspace(cfg, NAMESPACE_1, null);

    assertNotNull(workspace);
    assertNotNull(
        workspace.getAttributes().get(WORKSPACE_INFRASTRUCTURE_NAMESPACE_ATTRIBUTE), "evaluated");
    verify(workspaceDao).create(workspace);
    verify(runtimes)
        .evalInfrastructureNamespace(
            new NamespaceResolutionContext(workspace.getId(), USER_ID, NAMESPACE_1));
  }

  @Test
  public void propagatesInfraNamespaceFromAttributesOnWorkspaceCreation() throws Exception {
    final WorkspaceConfig cfg = createConfig();

    final WorkspaceImpl workspace =
        workspaceManager.createWorkspace(
            cfg,
            NAMESPACE_1,
            ImmutableMap.of(WORKSPACE_INFRASTRUCTURE_NAMESPACE_ATTRIBUTE, "user-defined"));

    assertNotNull(workspace);
    assertNotNull(
        workspace.getAttributes().get(WORKSPACE_INFRASTRUCTURE_NAMESPACE_ATTRIBUTE),
        "user-defined");
    verify(workspaceDao).create(workspace);
    verify(runtimes, never()).evalInfrastructureNamespace(any());
  }

  @Test
  public void nameIsUsedWhenNameAndGenerateNameSet()
      throws ValidationException, ConflictException, NotFoundException, ServerException,
          InfrastructureException {
    final String devfileName = "workspacename";
    final DevfileImpl devfile = new DevfileImpl();
    devfile.setApiVersion(CURRENT_API_VERSION);
    devfile.getMetadata().setName(devfileName);
    devfile.getMetadata().setGenerateName("this_will_not_be_set_as_a_name");
    when(runtimes.evalInfrastructureNamespace(any())).thenReturn("ns");
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
    Map<String, String> oldAttributes = new HashMap<>(workspace.getAttributes());
    workspace.setTemporary(true);
    workspace.getAttributes().put("new attribute", "attribute");
    when(workspaceDao.update(any())).thenAnswer(inv -> inv.getArguments()[0]);

    workspaceManager.updateWorkspace(workspace.getId(), workspace);

    verify(workspaceDao).update(workspace);
    verify(validator).validateUpdateAttributes(oldAttributes, workspace.getAttributes());
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
    WorkspaceImpl workspace =
        createAndMockWorkspace(
            devfile,
            NAMESPACE_1,
            ImmutableMap.of(WORKSPACE_INFRASTRUCTURE_NAMESPACE_ATTRIBUTE, INFRA_NAMESPACE));

    EnvironmentImpl environment = new EnvironmentImpl(null, emptyMap());
    Command command = new CommandImpl("cmd", "echo hello", "custom");
    WorkspaceConfigImpl convertedConfig =
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
  public void evaluatesLegacyInfraNamespaceIfMissingOnWorkspaceStart() throws Exception {
    DevfileImpl devfile = mock(DevfileImpl.class);
    WorkspaceImpl workspace = createAndMockWorkspace(devfile, NAMESPACE_1, new HashMap<>());
    workspace.getAttributes().remove(WORKSPACE_INFRASTRUCTURE_NAMESPACE_ATTRIBUTE);
    when(runtimes.evalInfrastructureNamespace(any())).thenReturn("evaluated-legacy");

    EnvironmentImpl environment = new EnvironmentImpl(null, emptyMap());
    Command command = new CommandImpl("cmd", "echo hello", "custom");
    WorkspaceConfigImpl convertedConfig =
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
    verify(workspaceDao, times(2)).update(workspaceCaptor.capture());
    assertEquals(
        workspaceCaptor
            .getAllValues()
            .get(0)
            .getAttributes()
            .get(WORKSPACE_INFRASTRUCTURE_NAMESPACE_ATTRIBUTE),
        "evaluated-legacy");
    verify(runtimes)
        .evalInfrastructureNamespace(
            new NamespaceResolutionContext(workspace.getId(), USER_ID, NAMESPACE_1));
  }

  @Test
  public void evaluatesDefaultInfraNamespaceIfInvalidOnWorkspaceStart() throws Exception {
    DevfileImpl devfile = mock(DevfileImpl.class);
    WorkspaceImpl workspace =
        createAndMockWorkspace(
            devfile,
            NAMESPACE_1,
            ImmutableMap.of(WORKSPACE_INFRASTRUCTURE_NAMESPACE_ATTRIBUTE, "-invalid-dns-name"));
    when(runtimes.evalInfrastructureNamespace(any())).thenReturn("evaluated-legal");

    EnvironmentImpl environment = new EnvironmentImpl(null, emptyMap());
    Command command = new CommandImpl("cmd", "echo hello", "custom");
    WorkspaceConfigImpl convertedConfig =
        new WorkspaceConfigImpl(
            "any",
            "",
            "default",
            singletonList(command),
            emptyList(),
            ImmutableMap.of("default", environment),
            ImmutableMap.of("attr", "value"));
    when(devfileConverter.convert(any())).thenReturn(convertedConfig);
    when(runtimes.isInfrastructureNamespaceValid(eq("-invalid-dns-name"))).thenReturn(false);

    mockAnyWorkspaceStart();

    workspaceManager.startWorkspace(workspace.getId(), null, emptyMap());

    verify(runtimes).startAsync(eq(workspace), eq(null), anyMap());
    verify(workspaceDao, times(2)).update(workspaceCaptor.capture());
    assertEquals(
        workspaceCaptor
            .getAllValues()
            .get(0)
            .getAttributes()
            .get(WORKSPACE_INFRASTRUCTURE_NAMESPACE_ATTRIBUTE),
        "evaluated-legal");
    verify(runtimes)
        .evalInfrastructureNamespace(
            new NamespaceResolutionContext(workspace.getId(), USER_ID, NAMESPACE_1));
  }

  @Test
  public void doNotEvaluateInfraNamespaceIfItIsSpecifiedOnWorkspaceStart() throws Exception {
    DevfileImpl devfile = mock(DevfileImpl.class);
    WorkspaceImpl workspace =
        createAndMockWorkspace(
            devfile,
            NAMESPACE_1,
            ImmutableMap.of(WORKSPACE_INFRASTRUCTURE_NAMESPACE_ATTRIBUTE, "user-defined"));

    EnvironmentImpl environment = new EnvironmentImpl(null, emptyMap());
    Command command = new CommandImpl("cmd", "echo hello", "custom");
    WorkspaceConfigImpl convertedConfig =
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
    verify(workspaceDao, times(2)).update(workspaceCaptor.capture());
    assertEquals(
        workspaceCaptor
            .getAllValues()
            .get(0)
            .getAttributes()
            .get(WORKSPACE_INFRASTRUCTURE_NAMESPACE_ATTRIBUTE),
        "user-defined");
    verify(runtimes, never()).evalInfrastructureNamespace(any());
  }

  @Test
  public void startsTemporaryWorkspace() throws Exception {
    final WorkspaceConfigImpl workspaceConfig = createConfig();
    final WorkspaceImpl workspace = createAndMockWorkspace(workspaceConfig, NAMESPACE_1);
    mockRuntime(workspace, STARTING);
    mockAnyWorkspaceStart();
    when(workspaceDao.get(workspace.getId())).thenReturn(workspace);

    // It is not possible to specify the target namespace in the workspace config supplied to the
    // startWorkspace method below, so let's configure returning the default namespace
    lenient().when(runtimes.evalInfrastructureNamespace(any())).thenReturn(INFRA_NAMESPACE);

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
  public void removesWorkspaceAfterStop() throws Exception {
    final WorkspaceImpl workspace = createAndMockWorkspace();
    workspace.setTemporary(true);
    mockRuntime(workspace, RUNNING);
    mockAnyWorkspaceStop();

    workspaceManager.stopWorkspace(workspace.getId(), of(REMOVE_WORKSPACE_AFTER_STOP, "true"));

    verify(runtimes).stopAsync(workspace, of(REMOVE_WORKSPACE_AFTER_STOP, "true"));
    verify(workspaceDao).remove(workspace.getId());
  }

  @Test
  public void removesTemporaryWorkspaceAfterStopIfRequested() throws Exception {
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

    mockAnyWorkspaceStartFailed(new ServerException("start failed"));

    // It is not possible to specify the target namespace in the workspace config supplied to the
    // startWorkspace method below, so let's configure returning the default namespace
    when(runtimes.evalInfrastructureNamespace(any())).thenReturn(INFRA_NAMESPACE);

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

  @Test
  public void startsWorkspaceShouldCleanPreferences() throws Exception {
    final WorkspaceImpl workspace = createAndMockWorkspace();
    Runtime runtime = mockRuntime(workspace, WorkspaceStatus.RUNNING);
    workspace.setRuntime(runtime);
    Map<String, String> pref = new HashMap<>(2);
    pref.put(LAST_ACTIVITY_TIME, "now");
    pref.put(LAST_ACTIVE_INFRASTRUCTURE_NAMESPACE, "my_namespace");
    when(preferenceManager.find("owner")).thenReturn(pref);

    mockAnyWorkspaceStart();
    workspaceManager.startWorkspace(
        workspace.getId(), workspace.getConfig().getDefaultEnv(), emptyMap());

    verify(runtimes).startAsync(workspace, workspace.getConfig().getDefaultEnv(), emptyMap());
    assertNotNull(workspace.getAttributes().get(UPDATED_ATTRIBUTE_NAME));
    verify(preferenceManager)
        .remove("owner", Arrays.asList(LAST_ACTIVITY_TIME, LAST_ACTIVE_INFRASTRUCTURE_NAMESPACE));
  }

  @Test
  public void stopsLastWorkspaceShouldUpdatePreferences() throws Exception {
    final WorkspaceImpl workspace = createAndMockWorkspace(createConfig(), NAMESPACE_1);
    mockRuntime(workspace, RUNNING);
    mockAnyWorkspaceStop();
    when(runtimes.isAnyActive()).thenReturn(false);
    long epochSecond = Clock.systemDefaultZone().instant().getEpochSecond();
    workspaceManager.stopWorkspace(workspace.getId(), emptyMap());
    verify(runtimes).stopAsync(workspace, emptyMap());
    verify(workspaceDao).update(workspaceCaptor.capture());
    verify(preferenceManager).find("owner");
    Map<String, String> pref = new HashMap<>(2);
    pref.put(LAST_ACTIVITY_TIME, Long.toString(epochSecond));
    pref.put(LAST_ACTIVE_INFRASTRUCTURE_NAMESPACE, INFRA_NAMESPACE);
    verify(preferenceManager).update("owner", pref);
  }

  @Test
  public void stopsWorkspaceShouldNotUpdatePreferencesIfOtherWorkspaceRunning() throws Exception {
    final WorkspaceImpl workspace = createAndMockWorkspace(createConfig(), NAMESPACE_1);
    mockRuntime(workspace, RUNNING);
    mockAnyWorkspaceStop();
    when(runtimes.getActive(anyString()))
        .thenReturn(ImmutableSet.of(NameGenerator.generate("ws", 5)));
    long millis = System.currentTimeMillis();
    workspaceManager.stopWorkspace(workspace.getId(), emptyMap());
    verify(runtimes).stopAsync(workspace, emptyMap());
    verify(workspaceDao).update(workspaceCaptor.capture());
    Map<String, String> pref = new HashMap<>(2);
    pref.put(LAST_ACTIVITY_TIME, Long.toString(millis));
    pref.put(LAST_ACTIVE_INFRASTRUCTURE_NAMESPACE, INFRA_NAMESPACE);
    verify(preferenceManager, never()).find("owner");
    verify(preferenceManager, never())
        .remove("owner", Arrays.asList(LAST_ACTIVITY_TIME, LAST_ACTIVE_INFRASTRUCTURE_NAMESPACE));
  }

  private void mockRuntimeStatus(WorkspaceImpl workspace, WorkspaceStatus status) {
    lenient().when(runtimes.getStatus(workspace.getId())).thenReturn(status);
  }

  private TestRuntime mockRuntime(WorkspaceImpl workspace, WorkspaceStatus status)
      throws Exception {
    MachineImpl machine1 = createMachine();
    MachineImpl machine2 = createMachine();
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
    return createAndMockWorkspace(devfile, namespace, emptyMap());
  }

  private WorkspaceImpl createAndMockWorkspace(
      Devfile devfile, String namespace, Map<String, String> attributes) throws Exception {
    WorkspaceImpl workspace =
        WorkspaceImpl.builder()
            .setDevfile(devfile)
            .generateId()
            .setAccount(new AccountImpl("id", namespace, "type"))
            .setAttributes(attributes)
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

    // this is set after the creation
    workspace.getAttributes().put(WORKSPACE_INFRASTRUCTURE_NAMESPACE_ATTRIBUTE, INFRA_NAMESPACE);

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
