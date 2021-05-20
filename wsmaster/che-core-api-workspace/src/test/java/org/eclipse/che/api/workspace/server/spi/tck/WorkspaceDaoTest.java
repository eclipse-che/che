/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server.spi.tck;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import javax.inject.Inject;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.event.BeforeWorkspaceRemovedEvent;
import org.eclipse.che.api.workspace.server.model.impl.CommandImpl;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.ProjectConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.RecipeImpl;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.SourceStorageImpl;
import org.eclipse.che.api.workspace.server.model.impl.VolumeImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ActionImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ComponentImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.DevfileImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.EndpointImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.EntrypointImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.EnvImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.MetadataImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ProjectImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.SourceImpl;
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;
import org.eclipse.che.api.workspace.shared.event.WorkspaceRemovedEvent;
import org.eclipse.che.commons.test.tck.TckListener;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepositoryException;
import org.eclipse.che.core.db.cascade.CascadeEventSubscriber;
import org.eclipse.che.core.db.cascade.event.CascadeEvent;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link WorkspaceDao} contract.
 *
 * @author Yevhenii Voevodin
 */
@Listeners(TckListener.class)
@Test(suiteName = WorkspaceDaoTest.SUITE_NAME)
public class WorkspaceDaoTest {

  public static final String SUITE_NAME = "WorkspaceDaoTck";

  private static final int COUNT_OF_WORKSPACES = 5;
  private static final int DEVFILE_WORKSPACE_INDEX = COUNT_OF_WORKSPACES - 1;
  private static final int COUNT_OF_ACCOUNTS = 3;

  @Inject private TckRepository<WorkspaceImpl> workspaceRepo;

  @Inject private TckRepository<AccountImpl> accountRepo;

  @Inject private WorkspaceDao workspaceDao;

  @Inject private EventService eventService;

  private AccountImpl[] accounts;

  private WorkspaceImpl[] workspaces;

  @AfterMethod
  public void removeEntities() throws TckRepositoryException {
    workspaceRepo.removeAll();
    accountRepo.removeAll();
  }

  @BeforeMethod
  public void createEntities() throws TckRepositoryException {
    accounts = new AccountImpl[COUNT_OF_ACCOUNTS];
    for (int i = 0; i < COUNT_OF_ACCOUNTS; i++) {
      accounts[i] = new AccountImpl("accountId" + i, "accountName" + i, "test");
    }
    workspaces = new WorkspaceImpl[COUNT_OF_WORKSPACES];
    for (int i = 0; i < COUNT_OF_WORKSPACES; i++) {
      // 2 workspaces share 1 namespace
      AccountImpl account = accounts[i / 2];
      // Last one is made from devfile
      if (i < DEVFILE_WORKSPACE_INDEX) {
        workspaces[i] = createWorkspaceFromConfig("workspace-" + i, account, "name-" + i);
      } else {
        workspaces[i] = createWorkspaceFromDevfile("workspace-" + i, account, "name-" + i);
      }
    }
    accountRepo.createAll(Arrays.asList(accounts));
    workspaceRepo.createAll(Stream.of(workspaces).map(WorkspaceImpl::new).collect(toList()));
  }

  @Test
  public void shouldBeAbleToCountWorkspaces() throws ServerException {
    assertEquals(workspaceDao.getWorkspacesTotalCount(), COUNT_OF_WORKSPACES);
  }

  @Test
  public void shouldBeAbleToCountNewWorkspaces() throws ServerException, TckRepositoryException {
    // given
    // when
    workspaceRepo.createAll(
        ImmutableList.of(createWorkspaceFromDevfile("id222", accounts[0], "name-bbb")));
    // then
    assertEquals(workspaceDao.getWorkspacesTotalCount(), COUNT_OF_WORKSPACES + 1);
  }

  @Test
  public void shouldBeAbleToSubtractRemovedWorkspaces()
      throws ServerException, TckRepositoryException {
    // given
    // when
    workspaceDao.remove(workspaces[1].getId());
    // then
    assertEquals(workspaceDao.getWorkspacesTotalCount(), COUNT_OF_WORKSPACES - 1);
  }

  @Test
  public void shouldGetWorkspaceById() throws Exception {
    final WorkspaceImpl workspace = workspaces[0];

    assertEquals(workspaceDao.get(workspace.getId()), new WorkspaceImpl(workspace));
  }

  @Test(expectedExceptions = NotFoundException.class)
  public void shouldThrowNotFoundExceptionWhenGettingNonExistingWorkspaceById() throws Exception {
    workspaceDao.get("non-existing-id");
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNpeWhenGettingWorkspaceByIdWhereIdIsNull() throws Exception {
    workspaceDao.get(null);
  }

  @Test
  public void shouldGetWorkspacesByNamespace() throws Exception {
    final WorkspaceImpl workspace1 = workspaces[0];
    final WorkspaceImpl workspace2 = workspaces[1];
    assertEquals(
        workspace1.getNamespace(), workspace2.getNamespace(), "Namespaces must be the same");

    final Page<WorkspaceImpl> found = workspaceDao.getByNamespace(workspace1.getNamespace(), 6, 0);

    assertEquals(new HashSet<>(found.getItems()), new HashSet<>(asList(workspace1, workspace2)));
    assertEquals(found.getTotalItemsCount(), 2);
    assertEquals(found.getItemsCount(), 2);
  }

  @Test
  public void emptyListShouldBeReturnedWhenThereAreNoWorkspacesInGivenNamespace() throws Exception {
    assertTrue(workspaceDao.getByNamespace("non-existing-namespace", 30, 0).isEmpty());
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNpeWhenGettingWorkspaceByNullNamespace() throws Exception {
    workspaceDao.getByNamespace(null, 30, 0);
  }

  @Test
  public void shouldGetWorkspaceByConfigNameAndNamespace() throws Exception {
    final WorkspaceImpl workspace = workspaces[0];

    assertEquals(
        workspaceDao.get(workspace.getName(), workspace.getNamespace()),
        new WorkspaceImpl(workspace));
  }

  @Test
  public void shouldGetWorkspaceByDevfileNameAndNamespace() throws Exception {
    final WorkspaceImpl workspace = workspaces[4];

    assertEquals(
        workspaceDao.get(workspace.getDevfile().getName(), workspace.getNamespace()),
        new WorkspaceImpl(workspace));
  }

  @Test(expectedExceptions = NotFoundException.class)
  public void shouldThrowNotFoundExceptionWhenWorkspaceWithSuchNameDoesNotExist() throws Exception {
    final WorkspaceImpl workspace = workspaces[0];

    workspaceDao.get("non-existing-name", workspace.getNamespace());
  }

  @Test(expectedExceptions = NotFoundException.class)
  public void shouldThrowNotFoundExceptionWhenWorkspaceWithSuchNamespaceDoesNotExist()
      throws Exception {
    final WorkspaceImpl workspace = workspaces[0];

    workspaceDao.get(workspace.getName(), "non-existing-namespace");
  }

  @Test(expectedExceptions = NotFoundException.class)
  public void shouldThrowNotFoundExceptionWhenWorkspaceWithSuchNameDoesNotExistInGiveWorkspace()
      throws Exception {
    final WorkspaceImpl workspace1 = workspaces[0];
    final WorkspaceImpl workspace2 = workspaces[2];

    workspaceDao.get(workspace1.getName(), workspace2.getNamespace());
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNpeWhenGettingWorkspaceByNameAndNamespaceWhereNameIsNull()
      throws Exception {
    workspaceDao.get(null, workspaces[0].getNamespace());
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNpeWhenGettingWorkspaceByNameAndNamespaceWhereNamespaceIsNull()
      throws Exception {
    workspaceDao.get(workspaces[0].getName(), null);
  }

  @Test(
      expectedExceptions = NotFoundException.class,
      dependsOnMethods = "shouldThrowNotFoundExceptionWhenGettingNonExistingWorkspaceById")
  public void shouldRemoveWorkspace() throws Exception {
    final WorkspaceImpl workspace = workspaces[0];

    workspaceDao.remove(workspace.getId());
    WorkspaceImpl removedWorkspace = workspaceDao.get(workspace.getId());
    assertEquals(removedWorkspace, workspace);
  }

  @Test(dependsOnMethods = "shouldGetWorkspaceById")
  public void shouldNotRemoveWorkspaceWhenSubscriberThrowsExceptionOnWorkspaceRemoving()
      throws Exception {
    final WorkspaceImpl workspace = workspaces[0];
    CascadeEventSubscriber<BeforeWorkspaceRemovedEvent> subscriber = mockCascadeEventSubscriber();
    doThrow(new ServerException("error")).when(subscriber).onCascadeEvent(any());
    eventService.subscribe(subscriber, BeforeWorkspaceRemovedEvent.class);

    try {
      workspaceDao.remove(workspace.getId());
      fail("WorkspaceDao#remove had to throw server exception");
    } catch (ServerException ignored) {
    }

    assertEquals(workspaceDao.get(workspace.getId()), workspace);
    eventService.unsubscribe(subscriber, BeforeWorkspaceRemovedEvent.class);
  }

  @Test
  public void shouldGetWorkspacesByNonTemporary() throws Exception {
    final WorkspaceImpl workspace = workspaces[4];
    workspace.setTemporary(true);
    workspaceDao.update(workspace);

    Page<WorkspaceImpl> firstPage = workspaceDao.getWorkspaces(false, 2, 0);

    assertEquals(firstPage.getItems().size(), 2);
    assertEquals(firstPage.getTotalItemsCount(), 4);
    assertEquals(
        new HashSet<>(firstPage.getItems()), new HashSet<>(asList(workspaces[0], workspaces[1])));

    Page<WorkspaceImpl> secondPage = workspaceDao.getWorkspaces(false, 2, 2);

    assertEquals(secondPage.getItems().size(), 2);
    assertEquals(secondPage.getTotalItemsCount(), 4);
    assertEquals(
        new HashSet<>(secondPage.getItems()), new HashSet<>(asList(workspaces[2], workspaces[3])));
  }

  @Test
  public void shouldGetWorkspacesByTemporary() throws Exception {
    final WorkspaceImpl workspace = workspaces[0];
    workspace.setTemporary(true);
    workspaceDao.update(workspace);

    Page<WorkspaceImpl> result = workspaceDao.getWorkspaces(true, 30, 0);

    assertEquals(result.getItems().size(), 1);
    assertEquals(result.getTotalItemsCount(), 1);
    assertEquals(result.getItems().iterator().next(), workspaceDao.get(workspace.getId()));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void shouldThrowIllegalStateExceptionOnNegativeLimit() throws Exception {
    workspaceDao.getWorkspaces(true, 0, -2);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void shouldThrowIllegalStateExceptionOnNegativeSkipCount() throws Exception {
    workspaceDao.getWorkspaces(true, -2, 0);
  }

  @Test
  public void shouldPublicRemoveWorkspaceEventAfterRemoveWorkspace() throws Exception {
    final boolean[] isNotified = new boolean[] {false};
    eventService.subscribe(event -> isNotified[0] = true, WorkspaceRemovedEvent.class);

    workspaceDao.remove(workspaces[0].getId());

    assertTrue(isNotified[0], "Event subscriber notified");
  }

  @Test
  public void shouldNotThrowExceptionWhenRemovingNonExistingWorkspace() throws Exception {
    workspaceDao.remove("non-existing-id");
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNpeWhenRemovingNull() throws Exception {
    workspaceDao.remove(null);
  }

  @Test(dependsOnMethods = "shouldGetWorkspaceById")
  public void shouldCreateWorkspaceWithConfig() throws Exception {
    final WorkspaceImpl workspace =
        createWorkspaceFromConfig("new-workspace", accounts[0], "new-name");

    workspaceDao.create(workspace);

    assertEquals(
        workspaceDao.get(workspace.getId()), new WorkspaceImpl(workspace, workspace.getAccount()));
  }

  @Test(dependsOnMethods = "shouldGetWorkspaceById")
  public void shouldCreateWorkspaceWithDevfile() throws Exception {
    final WorkspaceImpl workspace =
        createWorkspaceFromDevfile("new-workspace", accounts[1], "new-name");

    workspaceDao.create(workspace);

    assertEquals(
        workspaceDao.get(workspace.getId()), new WorkspaceImpl(workspace, workspace.getAccount()));
  }

  @Test(
      expectedExceptions = ConflictException.class,
      expectedExceptionsMessageRegExp =
          "Workspace with id 'new-id' or name 'name-0' in namespace 'accountName0' already exists")
  public void shouldNotCreateWorkspaceWithConfigWithANameWhichAlreadyExistsInGivenNamespace()
      throws Exception {
    final WorkspaceImpl workspace = workspaces[0];
    assertNull(workspace.getDevfile());
    final WorkspaceImpl newWorkspace =
        createWorkspaceFromConfig(
            "new-id", workspace.getAccount(), workspace.getConfig().getName());

    workspaceDao.create(newWorkspace);
  }

  @Test(
      expectedExceptions = ConflictException.class,
      expectedExceptionsMessageRegExp =
          "Workspace with id 'new-id' or name 'name-1' in namespace 'accountName0' already exists")
  public void shouldNotCreateWorkspaceWithDevfileWithANameWhichAlreadyExistsInGivenNamespace()
      throws Exception {
    final WorkspaceImpl workspace = workspaces[1];
    assertNull(workspace.getDevfile());
    final WorkspaceImpl newWorkspace =
        createWorkspaceFromDevfile(
            "new-id", workspace.getAccount(), workspace.getConfig().getName());

    workspaceDao.create(newWorkspace);
  }

  @Test
  public void shouldCreateWorkspaceWithNameWhichDoesNotExistInGivenNamespace() throws Exception {
    final WorkspaceImpl workspace = workspaces[0];
    final WorkspaceImpl workspace2 = workspaces[3];

    final WorkspaceImpl newWorkspace =
        createWorkspaceFromConfig(
            "new-id", workspace.getAccount(), workspace2.getConfig().getName());
    final WorkspaceImpl expected = new WorkspaceImpl(newWorkspace, newWorkspace.getAccount());
    expected.setAccount(newWorkspace.getAccount());
    assertEquals(workspaceDao.create(newWorkspace), expected);
  }

  @Test(expectedExceptions = ConflictException.class)
  public void shouldThrowConflictExceptionWhenCreatingWorkspaceWithExistingId() throws Exception {
    final WorkspaceImpl workspace = workspaces[0];

    final WorkspaceImpl newWorkspace =
        createWorkspaceFromConfig(workspace.getId(), accounts[0], "new-name");

    workspaceDao.create(newWorkspace);
  }

  @Test(dependsOnMethods = "shouldGetWorkspaceById")
  public void shouldUpdateWorkspaceWithConfig() throws Exception {
    final WorkspaceImpl workspace = new WorkspaceImpl(workspaces[0], workspaces[0].getAccount());

    // Remove an existing project configuration from workspace
    workspace.getConfig().getProjects().remove(1);

    // Add new project to the workspace configuration
    final SourceStorageImpl source3 = new SourceStorageImpl();
    source3.setType("type3");
    source3.setLocation("location3");
    source3.setParameters(
        new HashMap<>(
            ImmutableMap.of(
                "param1", "value1",
                "param2", "value2",
                "param3", "value3")));
    final ProjectConfigImpl newProjectCfg = new ProjectConfigImpl();
    newProjectCfg.setPath("/path3");
    newProjectCfg.setType("type3");
    newProjectCfg.setName("project3");
    newProjectCfg.setDescription("description3");
    newProjectCfg.getMixins().addAll(asList("mixin3", "mixin4"));
    newProjectCfg.setSource(source3);
    newProjectCfg.getAttributes().put("new-key", asList("1", "2"));
    workspace.getConfig().getProjects().add(newProjectCfg);

    // Update an existing project configuration
    final ProjectConfigImpl projectCfg = workspace.getConfig().getProjects().get(0);
    projectCfg.getAttributes().clear();
    projectCfg.getSource().setLocation("new-location");
    projectCfg.getSource().setType("new-type");
    projectCfg.getSource().getParameters().put("new-param", "new-param-value");
    projectCfg.getMixins().add("new-mixin");
    projectCfg.setPath("/new-path");
    projectCfg.setDescription("new project description");

    // Remove an existing command
    workspace.getConfig().getCommands().remove(1);

    // Add a new command
    final CommandImpl newCmd = new CommandImpl();
    newCmd.setName("name3");
    newCmd.setType("type3");
    newCmd.setCommandLine("cmd3");
    newCmd
        .getAttributes()
        .putAll(
            ImmutableMap.of(
                "attr1", "value1",
                "attr2", "value2",
                "attr3", "value3"));
    workspace.getConfig().getCommands().add(newCmd);

    // Update an existing command
    final CommandImpl command = workspace.getConfig().getCommands().get(0);
    command.setName("new-name");
    command.setType("new-type");
    command.setCommandLine("new-command-line");
    command.getAttributes().clear();

    // Add a new environment
    final RecipeImpl newRecipe = new RecipeImpl();
    newRecipe.setLocation("new-location");
    newRecipe.setType("new-type");
    newRecipe.setContentType("new-content-type");
    newRecipe.setContent("new-content");
    final MachineConfigImpl newMachine = new MachineConfigImpl();
    final ServerConfigImpl serverConf1 =
        new ServerConfigImpl("2265", "http", "path1", singletonMap("key", "value"));
    final ServerConfigImpl serverConf2 =
        new ServerConfigImpl("2266", "ftp", "path2", singletonMap("key", "value"));
    newMachine.setServers(ImmutableMap.of("ref1", serverConf1, "ref2", serverConf2));
    newMachine.setAttributes(singletonMap("att1", "val"));
    newMachine.setAttributes(singletonMap("CHE_ENV", "value"));
    final EnvironmentImpl newEnv = new EnvironmentImpl();
    newEnv.setMachines(ImmutableMap.of("new-machine", newMachine));
    newEnv.setRecipe(newRecipe);
    workspace.getConfig().getEnvironments().put("new-env", newEnv);

    // Update an existing environment
    final EnvironmentImpl defaultEnv =
        workspace.getConfig().getEnvironments().get(workspace.getConfig().getDefaultEnv());
    // Remove an existing machine config
    final List<String> machineNames = new ArrayList<>(defaultEnv.getMachines().keySet());
    // Update an existing machine
    final MachineConfigImpl existingMachine = defaultEnv.getMachines().get(machineNames.get(1));
    existingMachine.setAttributes(
        ImmutableMap.of(
            "attr1", "value1",
            "attr2", "value2",
            "attr3", "value3"));
    existingMachine.getServers().clear();
    existingMachine
        .getServers()
        .put(
            "new-ref",
            new ServerConfigImpl(
                "new-port", "new-protocol", "new-path", singletonMap("key", "value")));
    defaultEnv.getMachines().remove(machineNames.get(0));
    defaultEnv.getRecipe().setContent("updated-content");
    defaultEnv.getRecipe().setContentType("updated-content-type");
    defaultEnv.getRecipe().setLocation("updated-location");
    defaultEnv.getRecipe().setType("updated-type");

    // Remove an existing environment
    final Optional<String> nonDefaultEnv =
        workspace
            .getConfig()
            .getEnvironments()
            .keySet()
            .stream()
            .filter(key -> !key.equals(workspace.getConfig().getDefaultEnv()))
            .findAny();
    assertTrue(nonDefaultEnv.isPresent());
    workspace.getConfig().getEnvironments().remove(nonDefaultEnv.get());

    // Update workspace configuration
    final WorkspaceConfigImpl wCfg = workspace.getConfig();
    wCfg.setDefaultEnv("new-env");
    wCfg.setName("new-name");
    wCfg.setDescription("This is a new description");

    // Update workspace object
    workspace.setAccount(new AccountImpl("accId", "new-namespace", "test"));
    workspace.getAttributes().clear();

    workspaceDao.update(workspace);

    assertEquals(
        workspaceDao.get(workspace.getId()), new WorkspaceImpl(workspace, workspace.getAccount()));
  }

  @Test(dependsOnMethods = "shouldGetWorkspaceById")
  public void shouldUpdateWorkspaceWithDevfile() throws Exception {
    final WorkspaceImpl workspace =
        new WorkspaceImpl(
            workspaces[DEVFILE_WORKSPACE_INDEX], workspaces[DEVFILE_WORKSPACE_INDEX].getAccount());

    // Remove an existing project configuration from workspace
    workspace.getDevfile().getProjects().remove(1);

    final SourceImpl source3 =
        new SourceImpl(
            "type3",
            "http://location",
            "branch3",
            "point3",
            "tag3",
            "commit3",
            "sparseCheckoutDir3");
    ProjectImpl newProject = new ProjectImpl("project3", source3, "path3");
    workspace.getDevfile().getProjects().add(newProject);

    // Update an existing project configuration
    final ProjectImpl projectCfg = workspace.getDevfile().getProjects().get(0);
    projectCfg.getSource().setLocation("new-location");
    projectCfg.getSource().setType("new-type");
    projectCfg.getSource().setBranch("new-branch");
    projectCfg.getSource().setCommitId("new-commit");
    projectCfg.getSource().setTag("new-tag");
    projectCfg.getSource().setStartPoint("new-point");
    projectCfg.getSource().setSparseCheckoutDir("new-sparse-checkout-dir");

    // Remove an existing command
    workspace.getDevfile().getCommands().remove(1);

    ActionImpl action3 =
        new ActionImpl("exec3", "component3", "run.sh", "/home/user/3", null, null);
    ActionImpl action4 =
        new ActionImpl("exec4", "component4", "run.sh", "/home/user/4", null, null);
    // Add a new command
    final org.eclipse.che.api.workspace.server.model.impl.devfile.CommandImpl newCmd =
        new org.eclipse.che.api.workspace.server.model.impl.devfile.CommandImpl(
            "command-3", singletonList(action3), singletonMap("attr3", "value3"), null);
    workspace.getDevfile().getCommands().add(newCmd);

    // Update an existing command
    final org.eclipse.che.api.workspace.server.model.impl.devfile.CommandImpl command =
        workspace.getDevfile().getCommands().get(0);
    command.setName("new-name");
    command.setActions(asList(action4));
    command.getAttributes().clear();

    workspace.getDevfile().getComponents().remove(1);

    EntrypointImpl entrypoint3 =
        new EntrypointImpl(
            "parentName",
            singletonMap("parent3", "selector3"),
            "containerName3",
            asList("command3", "command5"),
            asList("arg3", "arg5"));

    org.eclipse.che.api.workspace.server.model.impl.devfile.VolumeImpl volume3 =
        new org.eclipse.che.api.workspace.server.model.impl.devfile.VolumeImpl("name3", "path3");

    EnvImpl env3 = new EnvImpl("name3", "value3");
    EndpointImpl endpoint3 = new EndpointImpl("name3", 3333, singletonMap("key3", "value3"));

    ComponentImpl component3 = workspace.getDevfile().getComponents().get(0);
    new ComponentImpl(
        "kubernetes",
        "component3",
        "eclipse/che-theia/0.0.1",
        ImmutableMap.of("java.home", "/opt/jdk11"),
        "https://mysite.com/registry/somepath",
        "/dev.yaml",
        null,
        ImmutableMap.of("app.kubernetes.io/component", "webapp"),
        singletonList(entrypoint3),
        "image",
        "1256G",
        "123G",
        "2",
        "1",
        false,
        false,
        singletonList("command"),
        singletonList("arg"),
        singletonList(volume3),
        singletonList(env3),
        singletonList(endpoint3));
    component3.setSelector(singletonMap("key3", "value3"));

    // Update workspace object
    workspace.setAccount(new AccountImpl("accId", "new-namespace", "test"));
    workspace.getAttributes().clear();

    workspaceDao.update(workspace);

    assertEquals(
        workspaceDao.get(workspace.getId()), new WorkspaceImpl(workspace, workspace.getAccount()));
  }

  @Test(expectedExceptions = NotFoundException.class)
  public void shouldNotUpdateWorkspaceWhichDoesNotExist() throws Exception {
    final WorkspaceImpl workspace = workspaces[0];
    workspace.setId("non-existing-workspace");

    workspaceDao.update(workspace);
  }

  @Test(
      expectedExceptions = ConflictException.class,
      expectedExceptionsMessageRegExp =
          "Workspace with name 'name-1' in namespace 'accountName0' already exists")
  public void shouldNotUpdateWorkspaceWithReservedNameFromConfig() throws Exception {
    final WorkspaceImpl workspace1 = workspaces[0];
    final WorkspaceImpl workspace2 = workspaces[1];

    workspace1.getConfig().setName(workspace2.getName());

    workspaceDao.update(workspace1);
  }

  @Test(
      expectedExceptions = ConflictException.class,
      expectedExceptionsMessageRegExp =
          "Workspace with name 'name-3' in namespace 'accountName1' already exists")
  public void shouldNotUpdateWorkspaceWithReservedNameFromDevfile() throws Exception {
    final WorkspaceImpl workspace1 = workspaces[3];
    final WorkspaceImpl workspace2 = workspaces[DEVFILE_WORKSPACE_INDEX];

    workspace2.getDevfile().setName(workspace1.getConfig().getName());
    workspace2.setAccount(workspace1.getAccount());

    workspaceDao.update(workspace2);
  }

  @Test(dependsOnMethods = "shouldGetWorkspaceById")
  public void createsWorkspaceWithAProjectConfigContainingLongAttributeValues() throws Exception {
    WorkspaceImpl workspace = createWorkspaceFromConfig("new-workspace", accounts[0], "new-name");
    ProjectConfigImpl project = workspace.getConfig().getProjects().get(0);

    // long string
    char[] chars = new char[100_000];
    Arrays.fill(chars, 0, chars.length / 2, 'x');
    Arrays.fill(chars, chars.length / 2, chars.length, 'y');
    String value = new String(chars);
    project.getAttributes().put("long_value1", singletonList(value));
    project.getAttributes().put("long_value2", singletonList(value));

    workspaceDao.create(workspace);

    assertEquals(workspaceDao.get(workspace.getId()), new WorkspaceImpl(workspace));
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNpeWhenUpdatingNull() throws Exception {
    workspaceDao.update(null);
  }

  public static WorkspaceConfigImpl createWorkspaceConfig(String name) {
    // Project Sources configuration
    final SourceStorageImpl source1 = new SourceStorageImpl();
    source1.setType("type1");
    source1.setLocation("location1");
    source1.setParameters(
        new HashMap<>(
            ImmutableMap.of(
                "param1", "value1",
                "param2", "value2",
                "param3", "value3")));
    final SourceStorageImpl source2 = new SourceStorageImpl();
    source2.setType("type2");
    source2.setLocation("location2");
    source2.setParameters(
        new HashMap<>(
            ImmutableMap.of(
                "param4", "value1",
                "param5", "value2",
                "param6", "value3")));

    // Project Configuration
    final ProjectConfigImpl pCfg1 = new ProjectConfigImpl();
    pCfg1.setPath("/path1");
    pCfg1.setType("type1");
    pCfg1.setName("project1");
    pCfg1.setDescription("description1");
    pCfg1.getMixins().addAll(asList("mixin1", "mixin2"));
    pCfg1.setSource(source1);
    pCfg1
        .getAttributes()
        .putAll(ImmutableMap.of("key1", asList("v1", "v2"), "key2", asList("v1", "v2")));

    final ProjectConfigImpl pCfg2 = new ProjectConfigImpl();
    pCfg2.setPath("/path2");
    pCfg2.setType("type2");
    pCfg2.setName("project2");
    pCfg2.setDescription("description2");
    pCfg2.getMixins().addAll(asList("mixin3", "mixin4"));
    pCfg2.setSource(source2);
    pCfg2
        .getAttributes()
        .putAll(ImmutableMap.of("key3", asList("v1", "v2"), "key4", asList("v1", "v2")));

    final List<ProjectConfigImpl> projects = new ArrayList<>(asList(pCfg1, pCfg2));

    // Commands
    final CommandImpl cmd1 = new CommandImpl("name1", "cmd1", "type1");
    cmd1.getAttributes()
        .putAll(
            ImmutableMap.of(
                "key1", "value1",
                "key2", "value2",
                "key3", "value3"));
    final CommandImpl cmd2 = new CommandImpl("name2", "cmd2", "type2");
    cmd2.getAttributes()
        .putAll(
            ImmutableMap.of(
                "key4", "value4",
                "key5", "value5",
                "key6", "value6"));
    final List<CommandImpl> commands = new ArrayList<>(asList(cmd1, cmd2));

    // OldMachine configs
    final MachineConfigImpl exMachine1 = new MachineConfigImpl();
    final ServerConfigImpl serverConf1 =
        new ServerConfigImpl("2265", "http", "path1", singletonMap("key", "value"));
    final ServerConfigImpl serverConf2 =
        new ServerConfigImpl("2266", "ftp", "path2", singletonMap("key", "value"));
    exMachine1.setServers(ImmutableMap.of("ref1", serverConf1, "ref2", serverConf2));
    exMachine1.setAttributes(singletonMap("att1", "val"));
    exMachine1.setEnv(ImmutableMap.of("CHE_ENV1", "value", "CHE_ENV2", "value"));
    exMachine1.setVolumes(
        ImmutableMap.of(
            "vol1",
            new VolumeImpl().withPath("/path/1"),
            "vol2",
            new VolumeImpl().withPath("/path/2")));

    final MachineConfigImpl exMachine2 = new MachineConfigImpl();
    final ServerConfigImpl serverConf3 =
        new ServerConfigImpl("2333", "https", "path3", singletonMap("key", "value"));
    final ServerConfigImpl serverConf4 =
        new ServerConfigImpl("2334", "wss", "path4", singletonMap("key", "value"));
    exMachine2.setServers(ImmutableMap.of("ref1", serverConf3, "ref2", serverConf4));
    exMachine2.setAttributes(singletonMap("att1", "val"));
    exMachine2.setEnv(singletonMap("CHE_ENV2", "value"));
    exMachine2.setVolumes(ImmutableMap.of("vol2", new VolumeImpl().withPath("/path/2")));

    final MachineConfigImpl exMachine3 = new MachineConfigImpl();
    final ServerConfigImpl serverConf5 =
        new ServerConfigImpl("2333", "https", "path5", singletonMap("key", "value"));
    exMachine3.setServers(singletonMap("ref1", serverConf5));
    exMachine3.setAttributes(singletonMap("att1", "val"));
    exMachine3.setEnv(singletonMap("CHE_ENV3", "value"));
    exMachine3.setVolumes(ImmutableMap.of("vol3", new VolumeImpl().withPath("/path/3")));

    // Environments
    final RecipeImpl recipe1 = new RecipeImpl();
    recipe1.setLocation("https://eclipse.che/Dockerfile");
    recipe1.setType("dockerfile");
    recipe1.setContentType("text/x-dockerfile");
    recipe1.setContent("content");
    final EnvironmentImpl env1 = new EnvironmentImpl();
    env1.setMachines(
        new HashMap<>(
            ImmutableMap.of(
                "machine1", exMachine1,
                "machine2", exMachine2,
                "machine3", exMachine3)));
    env1.setRecipe(recipe1);

    final RecipeImpl recipe2 = new RecipeImpl();
    recipe2.setLocation("https://eclipse.che/Dockerfile");
    recipe2.setType("dockerfile");
    recipe2.setContentType("text/x-dockerfile");
    recipe2.setContent("content");
    final EnvironmentImpl env2 = new EnvironmentImpl();
    env2.setMachines(
        new HashMap<>(
            ImmutableMap.of(
                "machine1", new MachineConfigImpl(exMachine1),
                "machine3", new MachineConfigImpl(exMachine3))));
    env2.setRecipe(recipe2);

    final Map<String, EnvironmentImpl> environments = ImmutableMap.of("env1", env1, "env2", env2);

    // Workspace configuration
    final WorkspaceConfigImpl wCfg = new WorkspaceConfigImpl();
    wCfg.setDefaultEnv("env1");
    wCfg.setName(name);
    wCfg.setDescription("description");
    wCfg.setCommands(commands);
    wCfg.setProjects(projects);
    wCfg.setEnvironments(new HashMap<>(environments));

    return wCfg;
  }

  public static WorkspaceImpl createWorkspaceFromConfig(
      String id, AccountImpl account, String name) {
    final WorkspaceConfigImpl wCfg = createWorkspaceConfig(name);
    // Workspace
    final WorkspaceImpl workspace = new WorkspaceImpl();
    workspace.setId(id);
    workspace.setAccount(account);
    wCfg.setName(name);
    workspace.setConfig(wCfg);
    workspace.setAttributes(
        new HashMap<>(
            ImmutableMap.of(
                "attr1", "value1",
                "attr2", "value2",
                "attr3", "value3")));
    workspace.setConfig(wCfg);
    return workspace;
  }

  public static WorkspaceImpl createWorkspaceFromDevfile(
      String id, AccountImpl account, String name) {
    final DevfileImpl devfile = createDevfile(name);
    // Workspace
    final WorkspaceImpl workspace = new WorkspaceImpl();
    workspace.setId(id);
    workspace.setAccount(account);
    workspace.setDevfile(devfile);
    workspace.setAttributes(
        new HashMap<>(
            ImmutableMap.of(
                "attr1", "value1",
                "attr2", "value2",
                "attr3", "value3")));
    return workspace;
  }

  private static DevfileImpl createDevfile(String name) {

    SourceImpl source1 =
        new SourceImpl(
            "type1",
            "http://location",
            "branch1",
            "point1",
            "tag1",
            "commit1",
            "sparseCheckoutDir1");
    ProjectImpl project1 = new ProjectImpl("project1", source1, "path1");

    SourceImpl source2 =
        new SourceImpl(
            "type2",
            "http://location",
            "branch2",
            "point2",
            "tag2",
            "commit2",
            "sparseCheckoutDir2");
    ProjectImpl project2 = new ProjectImpl("project2", source2, "path2");

    ActionImpl action1 =
        new ActionImpl("exec1", "component1", "run.sh", "/home/user/1", null, null);
    ActionImpl action2 =
        new ActionImpl("exec2", "component2", "run.sh", "/home/user/2", null, null);

    org.eclipse.che.api.workspace.server.model.impl.devfile.CommandImpl command1 =
        new org.eclipse.che.api.workspace.server.model.impl.devfile.CommandImpl(
            name + "-1", singletonList(action1), singletonMap("attr1", "value1"), null);
    org.eclipse.che.api.workspace.server.model.impl.devfile.CommandImpl command2 =
        new org.eclipse.che.api.workspace.server.model.impl.devfile.CommandImpl(
            name + "-2", singletonList(action2), singletonMap("attr2", "value2"), null);

    EntrypointImpl entrypoint1 =
        new EntrypointImpl(
            "parentName1",
            singletonMap("parent1", "selector1"),
            "containerName1",
            asList("command1", "command2"),
            asList("arg1", "arg2"));

    EntrypointImpl entrypoint2 =
        new EntrypointImpl(
            "parentName2",
            singletonMap("parent2", "selector2"),
            "containerName2",
            asList("command3", "command4"),
            asList("arg3", "arg4"));

    org.eclipse.che.api.workspace.server.model.impl.devfile.VolumeImpl volume1 =
        new org.eclipse.che.api.workspace.server.model.impl.devfile.VolumeImpl("name1", "path1");

    org.eclipse.che.api.workspace.server.model.impl.devfile.VolumeImpl volume2 =
        new org.eclipse.che.api.workspace.server.model.impl.devfile.VolumeImpl("name2", "path2");

    EnvImpl env1 = new EnvImpl("name1", "value1");
    EnvImpl env2 = new EnvImpl("name2", "value2");

    EndpointImpl endpoint1 = new EndpointImpl("name1", 1111, singletonMap("key1", "value1"));
    EndpointImpl endpoint2 = new EndpointImpl("name2", 2222, singletonMap("key2", "value2"));

    ComponentImpl component1 =
        new ComponentImpl(
            "kubernetes",
            "component1",
            "eclipse/che-theia/0.0.1",
            ImmutableMap.of("java.home", "/home/user/jdk11"),
            "https://mysite.com/registry/somepath1",
            "/dev.yaml",
            "refcontent1",
            ImmutableMap.of("app.kubernetes.io/component", "db"),
            asList(entrypoint1, entrypoint2),
            "image",
            "256G",
            "128M",
            "2",
            "130m",
            false,
            false,
            singletonList("command"),
            singletonList("arg"),
            asList(volume1, volume2),
            asList(env1, env2),
            asList(endpoint1, endpoint2));
    component1.setSelector(singletonMap("key1", "value1"));

    ComponentImpl component2 =
        new ComponentImpl(
            "kubernetes",
            "component2",
            "eclipse/che-theia/0.0.1",
            ImmutableMap.of(
                "java.home",
                "/home/user/jdk11aertwertert",
                "java.boolean",
                true,
                "java.integer",
                123444),
            "https://mysite.com/registry/somepath2",
            "/dev.yaml",
            "refcontent2",
            ImmutableMap.of("app.kubernetes.io/component", "webapp"),
            asList(entrypoint1, entrypoint2),
            "image",
            "256G",
            "256M",
            "3",
            "180m",
            false,
            false,
            singletonList("command"),
            singletonList("arg"),
            asList(volume1, volume2),
            asList(env1, env2),
            asList(endpoint1, endpoint2));
    component2.setSelector(singletonMap("key2", "value2"));

    return new DevfileImpl(
        "0.0.1",
        asList(project1, project2),
        asList(component1, component2),
        asList(command1, command2),
        singletonMap("attribute1", "value1"),
        new MetadataImpl(name));
  }

  private <T extends CascadeEvent> CascadeEventSubscriber<T> mockCascadeEventSubscriber() {
    @SuppressWarnings("unchecked")
    CascadeEventSubscriber<T> subscriber = mock(CascadeEventSubscriber.class);
    doCallRealMethod().when(subscriber).onEvent(any());
    return subscriber;
  }
}
