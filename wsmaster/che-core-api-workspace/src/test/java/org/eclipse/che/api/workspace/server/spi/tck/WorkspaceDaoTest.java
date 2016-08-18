/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.workspace.server.spi.tck;

import com.google.common.collect.ImmutableMap;

import org.eclipse.che.account.shared.model.Account;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.machine.server.model.impl.CommandImpl;
import org.eclipse.che.api.machine.server.model.impl.LimitsImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineSourceImpl;
import org.eclipse.che.api.machine.server.model.impl.ServerConfImpl;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.ProjectConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.SourceStorageImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;
import org.eclipse.che.commons.test.tck.TckModuleFactory;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepositoryException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.HashMap;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Tests {@link WorkspaceDao} contract.
 *
 * @author Yevhenii Voevodin
 */
@Guice(moduleFactory = TckModuleFactory.class)
@Test(suiteName = WorkspaceDaoTest.SUITE_NAME)
public class WorkspaceDaoTest {

    public static final String SUITE_NAME = "WorkspaceDaoTck";

    private static final int COUNT_OF_WORKSPACES = 5;
    private static final int COUNT_OF_ACCOUNTS   = 3;

    @Inject
    private TckRepository<WorkspaceImpl> workspaceRepo;

    @Inject
    private TckRepository<AccountImpl> accountRepo;

    @Inject
    private WorkspaceDao workspaceDao;

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
            workspaces[i] = createWorkspace("workspace-" + i, accounts[i / 2], "name-" + i);
        }
        accountRepo.createAll(asList(accounts));
        workspaceRepo.createAll(asList(workspaces));
    }

    @Test
    public void shouldGetWorkspaceById() throws Exception {
        final WorkspaceImpl workspace = workspaces[0];

        assertEquals(workspaceDao.get(workspace.getId()), workspace);
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
        assertEquals(workspace1.getNamespace(), workspace2.getNamespace(), "Namespaces must be the same");

        final List<WorkspaceImpl> found = workspaceDao.getByNamespace(workspace1.getNamespace());

        assertEquals(new HashSet<>(found), new HashSet<>(asList(workspace1, workspace2)));
    }

    @Test
    public void emptyListShouldBeReturnedWhenThereAreNoWorkspacesInGivenNamespace() throws Exception {
        assertTrue(workspaceDao.getByNamespace("non-existing-namespace").isEmpty());
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeWhenGettingWorkspaceByNullNamespace() throws Exception {
        workspaceDao.getByNamespace(null);
    }

    @Test
    public void shouldGetWorkspaceByNameAndNamespace() throws Exception {
        final WorkspaceImpl workspace = workspaces[0];

        assertEquals(workspaceDao.get(workspace.getName(), workspace.getNamespace()), workspace);
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowNotFoundExceptionWhenWorkspaceWithSuchNameDoesNotExist() throws Exception {
        final WorkspaceImpl workspace = workspaces[0];

        workspaceDao.get("non-existing-name", workspace.getNamespace());
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowNotFoundExceptionWhenWorkspaceWithSuchNamespaceDoesNotExist() throws Exception {
        final WorkspaceImpl workspace = workspaces[0];

        workspaceDao.get(workspace.getName(), "non-existing-namespace");
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowNotFoundExceptionWhenWorkspaceWithSuchNameDoesNotExistInGiveWorkspace() throws Exception {
        final WorkspaceImpl workspace1 = workspaces[0];
        final WorkspaceImpl workspace2 = workspaces[2];

        workspaceDao.get(workspace1.getName(), workspace2.getNamespace());
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeWhenGettingWorkspaceByNameAndNamespaceWhereNameIsNull() throws Exception {
        workspaceDao.get(null, workspaces[0].getNamespace());
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeWhenGettingWorkspaceByNameAndNamespaceWhereNamespaceIsNull() throws Exception {
        workspaceDao.get(workspaces[0].getName(), null);
    }

    @Test(expectedExceptions = NotFoundException.class,
          dependsOnMethods = "shouldThrowNotFoundExceptionWhenGettingNonExistingWorkspaceById")
    public void shouldRemoveWorkspace() throws Exception {
        final WorkspaceImpl workspace = workspaces[0];

        workspaceDao.remove(workspace.getId());
        workspaceDao.get(workspace.getId());
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
    public void shouldCreateWorkspace() throws Exception {
        final WorkspaceImpl workspace = createWorkspace("new-workspace", accounts[0], "new-name");

        workspaceDao.create(workspace);

        assertEquals(workspaceDao.get(workspace.getId()), new WorkspaceImpl(workspace, workspace.getAccount()));
    }

    @Test(expectedExceptions = ConflictException.class)
    public void shouldNotCreateWorkspaceWithANameWhichAlreadyExistsInGivenNamespace() throws Exception {
        final WorkspaceImpl workspace = workspaces[0];

        final WorkspaceImpl newWorkspace = createWorkspace("new-id", workspace.getAccount(), workspace.getName());

        workspaceDao.create(newWorkspace);
    }

    @Test
    public void shouldCreateWorkspaceWithNameWhichDoesNotExistInGivenNamespace() throws Exception {
        final WorkspaceImpl workspace = workspaces[0];
        final WorkspaceImpl workspace2 = workspaces[4];

        final WorkspaceImpl newWorkspace = createWorkspace("new-id", workspace.getAccount(), workspace2.getName());
        final WorkspaceImpl expected = new WorkspaceImpl(newWorkspace, newWorkspace.getAccount());
        expected.setAccount(newWorkspace.getAccount());
        assertEquals(workspaceDao.create(newWorkspace), expected);
    }

    @Test(expectedExceptions = ConflictException.class)
    public void shouldThrowConflictExceptionWhenCreatingWorkspaceWithExistingId() throws Exception {
        final WorkspaceImpl workspace = workspaces[0];

        final WorkspaceImpl newWorkspace = createWorkspace(workspace.getId(), accounts[0], "new-name");

        workspaceDao.create(newWorkspace);
    }

    @Test(dependsOnMethods = "shouldGetWorkspaceById")
    public void shouldUpdateWorkspace() throws Exception {
        final WorkspaceImpl workspace = new WorkspaceImpl(workspaces[0], workspaces[0].getAccount());

        // Remove an existing project configuration from workspace
        workspace.getConfig().getProjects().remove(1);

        // Add new project to the workspace configuration
        final SourceStorageImpl source3 = new SourceStorageImpl();
        source3.setType("type3");
        source3.setLocation("location3");
        source3.setParameters(new HashMap<>(ImmutableMap.of("param1", "value1",
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
        newCmd.getAttributes().putAll(ImmutableMap.of("attr1", "value1",
                                                      "attr2", "value2",
                                                      "attr3", "value3"));
        workspace.getConfig().getCommands().add(newCmd);

        // Update an existing command
        final CommandImpl command = workspace.getConfig().getCommands().get(0);
        command.setName("new-name");
        command.setType("new-type");
        command.setCommandLine("new-command-line");
        command.getAttributes().clear();

        // Remove an existing machine config
        workspace.getConfig().getEnvironments().get(0).getMachineConfigs().remove(1);

        // Add a new machine config
        final MachineConfigImpl newMachineCfg = new MachineConfigImpl();
        newMachineCfg.setName("name3");
        newMachineCfg.setDev(false);
        newMachineCfg.setType("type3");
        newMachineCfg.setLimits(new LimitsImpl(2048));
        newMachineCfg.getEnvVariables().putAll(ImmutableMap.of("env4", "value4",
                                                               "env5", "value5",
                                                               "env6", "value6"));
        newMachineCfg.setServers(new ArrayList<>(singleton(new ServerConfImpl("ref", "port", "protocol", "path"))));
        newMachineCfg.setSource(new MachineSourceImpl("type", "location", "content"));
        workspace.getConfig().getEnvironments().get(0).getMachineConfigs().add(newMachineCfg);

        // Update an existing machine configuration
        final MachineConfigImpl machineCfg = workspace.getConfig().getEnvironments().get(0).getMachineConfigs().get(0);
        machineCfg.getEnvVariables().clear();
        machineCfg.setType("new-type");
        machineCfg.setName("new-name");
        machineCfg.getLimits().setRam(512);
        machineCfg.getServers().clear();
        machineCfg.getServers().add(new ServerConfImpl("new-ref", "new-port", "new-protocol", "new-path"));
        machineCfg.getSource().setType("new-type");
        machineCfg.getSource().setLocation("new-location");
        machineCfg.getSource().setContent("new-content");

        // Remove an existing environment
        workspace.getConfig().getEnvironments().remove(1);

        // Add a new environment
        final EnvironmentImpl newEnv = new EnvironmentImpl();
        newEnv.setName("new-env");
        final MachineConfigImpl newEnvMachineCfg = new MachineConfigImpl(newMachineCfg);
        newEnvMachineCfg.setDev(true);
        newEnv.getMachineConfigs().add(newEnvMachineCfg);
        workspace.getConfig().getEnvironments().add(newEnv);

        // Update an existing environment
        final EnvironmentImpl environment = workspace.getConfig().getEnvironments().get(0);
        environment.setName("new-name");

        // Update workspace configuration
        final WorkspaceConfigImpl wCfg = workspace.getConfig();
        wCfg.setDefaultEnv(newEnv.getName());
        wCfg.setName("new-name");
        wCfg.setDescription("This is a new description");

        // Update workspace object
        workspace.setName("new-name");
        workspace.setAccount(new AccountImpl("accId", "new-namespace", "test"));
        workspace.getAttributes().clear();

        workspaceDao.update(workspace);

        assertEquals(workspaceDao.get(workspace.getId()), new WorkspaceImpl(workspace, workspace.getAccount()));
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldNotUpdateWorkspaceWhichDoesNotExist() throws Exception {
        final WorkspaceImpl workspace = workspaces[0];
        workspace.setId("non-existing-workspace");

        workspaceDao.update(workspace);
    }

    @Test(expectedExceptions = ConflictException.class)
    public void shouldNotUpdateWorkspaceWithReservedName() throws Exception {
        final WorkspaceImpl workspace1 = workspaces[0];
        final WorkspaceImpl workspace2 = workspaces[1];

        workspace1.setName(workspace2.getName());

        workspaceDao.update(workspace1);
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
        source1.setParameters(new HashMap<>(ImmutableMap.of("param1", "value1",
                                                            "param2", "value2",
                                                            "param3", "value3")));
        final SourceStorageImpl source2 = new SourceStorageImpl();
        source2.setType("type2");
        source2.setLocation("location2");
        source2.setParameters(new HashMap<>(ImmutableMap.of("param4", "value1",
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
        pCfg1.getAttributes().putAll(ImmutableMap.of("key1", asList("v1", "v2"), "key2", asList("v1", "v2")));

        final ProjectConfigImpl pCfg2 = new ProjectConfigImpl();
        pCfg2.setPath("/path2");
        pCfg2.setType("type2");
        pCfg2.setName("project2");
        pCfg2.setDescription("description2");
        pCfg2.getMixins().addAll(asList("mixin3", "mixin4"));
        pCfg2.setSource(source2);
        pCfg2.getAttributes().putAll(ImmutableMap.of("key3", asList("v1", "v2"), "key4", asList("v1", "v2")));

        final List<ProjectConfigImpl> projects = new ArrayList<>(asList(pCfg1, pCfg2));

        // Commands
        final CommandImpl cmd1 = new CommandImpl("name1", "cmd1", "type1");
        cmd1.getAttributes().putAll(ImmutableMap.of("key1", "value1",
                                                    "key2", "value2",
                                                    "key3", "value3"));
        final CommandImpl cmd2 = new CommandImpl("name2", "cmd2", "type2");
        cmd2.getAttributes().putAll(ImmutableMap.of("key4", "value4",
                                                    "key5", "value5",
                                                    "key6", "value6"));
        final List<CommandImpl> commands = new ArrayList<>(asList(cmd1, cmd2));

        // Machine configs
        final MachineConfigImpl mCfg1 = new MachineConfigImpl();
        mCfg1.setName("name1");
        mCfg1.setDev(true);
        mCfg1.setType("type1");
        mCfg1.setLimits(new LimitsImpl(2048));
        mCfg1.getEnvVariables().putAll(ImmutableMap.of("env1", "value1",
                                                       "env2", "value2",
                                                       "env3", "value3"));
        mCfg1.getServers().addAll(singleton(new ServerConfImpl("ref1", "port1", "protocol1", "path1")));
        mCfg1.setSource(new MachineSourceImpl("type1", "location1", "content1"));

        final MachineConfigImpl mCfg2 = new MachineConfigImpl();
        mCfg2.setName("name2");
        mCfg2.setDev(false);
        mCfg2.setType("type2");
        mCfg2.setLimits(new LimitsImpl(512));
        mCfg2.getEnvVariables().putAll(ImmutableMap.of("env4", "value4",
                                                       "env5", "value5",
                                                       "env6", "value6"));
        mCfg2.getServers().add(new ServerConfImpl("ref2", "port2", "protocol2", "path2"));
        mCfg2.setSource(new MachineSourceImpl("type2", "location2", "content2"));

        final List<MachineConfigImpl> machineConfigs = new ArrayList<>(asList(mCfg1, mCfg2));

        // Environments
        final EnvironmentImpl env1 = new EnvironmentImpl();
        env1.setName("env1");
        env1.setMachineConfigs(machineConfigs);

        final EnvironmentImpl env2 = new EnvironmentImpl();
        env2.setName("env2");
        env2.setMachineConfigs(machineConfigs.stream()
                                             .map(MachineConfigImpl::new)
                                             .collect(Collectors.toList()));

        final List<EnvironmentImpl> environments = new ArrayList<>(asList(env1, env2));

        // Workspace configuration
        final WorkspaceConfigImpl wCfg = new WorkspaceConfigImpl();
        wCfg.setDefaultEnv(env1.getName());
        wCfg.setName(name);
        wCfg.setDescription("description");
        wCfg.setCommands(commands);
        wCfg.setProjects(projects);
        wCfg.setEnvironments(environments);
        return wCfg;
    }

    public static WorkspaceImpl createWorkspace(String id, AccountImpl account, String name) {
        final WorkspaceConfigImpl wCfg = createWorkspaceConfig(name);
        // Workspace
        final WorkspaceImpl workspace = new WorkspaceImpl();
        workspace.setStatus(WorkspaceStatus.STOPPED);
        workspace.setId(id);
        workspace.setAccount(account);
        workspace.setName(name);
        workspace.setAttributes(new HashMap<>(ImmutableMap.of("attr1", "value1",
                                                              "attr2", "value2",
                                                              "attr3", "value3")));
        workspace.setConfig(wCfg);

        return workspace;
    }
}
