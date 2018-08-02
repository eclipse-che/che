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
package org.eclipse.che.api.workspace.activity.spi.tck;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.workspace.activity.WorkspaceActivityDao;
import org.eclipse.che.api.workspace.activity.WorkspaceExpiration;
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
import org.eclipse.che.commons.test.tck.TckListener;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepositoryException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Max Shaposhnik (mshaposh@redhat.com) */
@Listeners(TckListener.class)
@Test(suiteName = WorkspaceActivityDaoTest.SUITE_NAME)
public class WorkspaceActivityDaoTest {

  static final String SUITE_NAME = "WorkspaceActivityDaoTck";

  private static final int COUNT = 3;

  @Inject private WorkspaceActivityDao workspaceActivityDao;

  private AccountImpl[] accounts = new AccountImpl[COUNT];
  private WorkspaceImpl[] workspaces = new WorkspaceImpl[COUNT];
  private WorkspaceExpiration[] expirations = new WorkspaceExpiration[COUNT];

  @Inject private TckRepository<AccountImpl> accountTckRepository;

  @Inject private TckRepository<WorkspaceExpiration> expirationTckRepository;

  @Inject private TckRepository<WorkspaceImpl> wsTckRepository;

  @BeforeMethod
  private void setUp() throws TckRepositoryException {

    for (int i = 0; i < COUNT; i++) {
      accounts[i] = new AccountImpl("accountId" + i, "accountName" + i, "test");
      // 2 workspaces share 1 namespace
      workspaces[i] = createWorkspace("ws" + i, accounts[i / 2], "name-" + i);

      expirations[i] = new WorkspaceExpiration("ws" + i, (i + 1) * 1_000_000);
    }

    accountTckRepository.createAll(asList(accounts));
    wsTckRepository.createAll(asList(workspaces));
    expirationTckRepository.createAll(asList(expirations));
  }

  @AfterMethod
  private void cleanup() throws TckRepositoryException {
    expirationTckRepository.removeAll();
    wsTckRepository.removeAll();
    accountTckRepository.removeAll();
  }

  @Test
  public void shouldFindExpirationsByTimestamp() throws Exception {
    List<String> expected =
        Arrays.asList(expirations[0].getWorkspaceId(), expirations[1].getWorkspaceId());
    List<String> found = workspaceActivityDao.findExpired(2_500_000);

    assertEquals(found, expected);
  }

  @Test(dependsOnMethods = "shouldFindExpirationsByTimestamp")
  public void shouldRemoveExpirationsByWsId() throws Exception {
    List<String> expected = Collections.singletonList(expirations[1].getWorkspaceId());

    workspaceActivityDao.removeExpiration(expirations[0].getWorkspaceId());

    List<String> found = workspaceActivityDao.findExpired(2_500_000);
    assertEquals(found, expected);
  }

  @Test(dependsOnMethods = "shouldFindExpirationsByTimestamp")
  public void shouldUpdateExpirations() throws Exception {

    List<String> expected =
        Arrays.asList(
            expirations[0].getWorkspaceId(),
            expirations[2].getWorkspaceId(),
            expirations[1].getWorkspaceId());
    workspaceActivityDao.setExpiration(
        new WorkspaceExpiration(expirations[2].getWorkspaceId(), 1_750_000));

    List<String> found = workspaceActivityDao.findExpired(2_500_000);
    assertEquals(found, expected);
  }

  @Test(dependsOnMethods = {"shouldFindExpirationsByTimestamp", "shouldRemoveExpirationsByWsId"})
  public void shouldAddExpirations() throws Exception {

    List<String> expected =
        Arrays.asList(expirations[0].getWorkspaceId(), expirations[1].getWorkspaceId());
    workspaceActivityDao.removeExpiration(expirations[1].getWorkspaceId());

    // create new again
    workspaceActivityDao.setExpiration(
        new WorkspaceExpiration(expirations[1].getWorkspaceId(), 1_250_000));

    List<String> found = workspaceActivityDao.findExpired(1_500_000);
    assertEquals(found, expected);
  }

  private static WorkspaceConfigImpl createWorkspaceConfig(String name) {
    // Project Sources configuration
    final SourceStorageImpl source1 = new SourceStorageImpl();
    source1.setType("type1");
    source1.setLocation("location1");

    // Project Configuration
    final ProjectConfigImpl pCfg1 = new ProjectConfigImpl();
    pCfg1.setPath("/path1");
    pCfg1.setType("type1");
    pCfg1.setName("project1");
    pCfg1.setDescription("description1");
    pCfg1.getMixins().addAll(asList("mixin1", "mixin2"));
    pCfg1.setSource(source1);

    final List<ProjectConfigImpl> projects = new ArrayList<>(Collections.singletonList(pCfg1));

    // Commands
    final CommandImpl cmd1 = new CommandImpl("name1", "cmd1", "type1");
    final List<CommandImpl> commands = new ArrayList<>(Collections.singletonList(cmd1));

    // OldMachine configs
    final MachineConfigImpl exMachine1 = new MachineConfigImpl();
    final ServerConfigImpl serverConf1 =
        new ServerConfigImpl("2265", "http", "path1", singletonMap("key", "value"));
    exMachine1.setServers(ImmutableMap.of("ref1", serverConf1));
    exMachine1.setInstallers(ImmutableList.of("agent5", "agent4"));
    exMachine1.setAttributes(singletonMap("att1", "val"));
    exMachine1.setEnv(ImmutableMap.of("CHE_ENV1", "value", "CHE_ENV2", "value"));
    exMachine1.setVolumes(ImmutableMap.of("vol1", new VolumeImpl().withPath("/path/1")));

    // Environments
    final RecipeImpl recipe1 = new RecipeImpl();
    recipe1.setLocation("https://eclipse.che/Dockerfile");
    recipe1.setType("dockerfile");
    recipe1.setContentType("text/x-dockerfile");
    recipe1.setContent("content");
    final EnvironmentImpl env1 = new EnvironmentImpl();
    env1.setMachines(new HashMap<>(ImmutableMap.of("machine1", exMachine1)));
    env1.setRecipe(recipe1);

    final RecipeImpl recipe2 = new RecipeImpl();
    recipe2.setLocation("https://eclipse.che/Dockerfile");
    recipe2.setType("dockerfile");
    recipe2.setContentType("text/x-dockerfile");
    recipe2.setContent("content");

    final Map<String, EnvironmentImpl> environments = ImmutableMap.of("env1", env1);

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

  private static WorkspaceImpl createWorkspace(String id, AccountImpl account, String name) {
    final WorkspaceConfigImpl wCfg = createWorkspaceConfig(name);
    // Workspace
    final WorkspaceImpl workspace = new WorkspaceImpl();
    workspace.setId(id);
    workspace.setAccount(account);
    wCfg.setName(name);
    workspace.setConfig(wCfg);
    return workspace;
  }
}
