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
package org.eclipse.che.api.workspace.activity.spi.tck;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STARTING;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STOPPED;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import javax.inject.Inject;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.workspace.activity.WorkspaceActivity;
import org.eclipse.che.api.workspace.activity.WorkspaceActivityDao;
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
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Max Shaposhnik (mshaposh@redhat.com) */
@Listeners(TckListener.class)
@Test(suiteName = WorkspaceActivityDaoTest.SUITE_NAME)
public class WorkspaceActivityDaoTest {

  static final String SUITE_NAME = "WorkspaceActivityDaoTck";

  private static final int COUNT = 3;

  private static final long DEFAULT_RUN_TIMEOUT = 0L;

  @Inject private WorkspaceActivityDao workspaceActivityDao;

  private AccountImpl[] accounts = new AccountImpl[COUNT];
  private WorkspaceImpl[] workspaces = new WorkspaceImpl[COUNT];
  private WorkspaceActivity[] activities = new WorkspaceActivity[COUNT];

  @Inject private TckRepository<AccountImpl> accountTckRepository;

  @Inject private TckRepository<WorkspaceActivity> activityTckRepository;

  @Inject private TckRepository<WorkspaceImpl> wsTckRepository;

  @BeforeMethod
  private void setUp() throws TckRepositoryException {
    for (int i = 0; i < COUNT; i++) {
      accounts[i] = new AccountImpl("accountId" + i, "accountName" + i, "test");
      // 2 workspaces share 1 namespace
      workspaces[i] = createWorkspace("ws" + i, accounts[i / 2], "name-" + i);

      long base = (long) i + 1;
      WorkspaceActivity a = new WorkspaceActivity();
      a.setWorkspaceId("ws" + i);
      a.setCreated(base);
      a.setLastStarting(base * 10);
      a.setLastRunning(base * 100);
      a.setLastStopping(base * 1_000);
      a.setLastStopped(base * 10_000);
      a.setExpiration(base * 1_000_000);

      activities[i] = a;
    }

    accountTckRepository.createAll(asList(accounts));
    wsTckRepository.createAll(asList(workspaces));
    activityTckRepository.createAll(asList(activities));
  }

  @AfterMethod
  private void cleanup() throws TckRepositoryException {
    activityTckRepository.removeAll();
    wsTckRepository.removeAll();
    accountTckRepository.removeAll();
  }

  @Test
  public void shouldFindExpirationsByTimestamp() throws Exception {
    List<String> expected = asList(activities[0].getWorkspaceId(), activities[1].getWorkspaceId());
    List<String> found = workspaceActivityDao.findExpiredIdle(2_500_000);

    assertEquals(found, expected);
  }

  @Test(dependsOnMethods = "shouldFindExpirationsByTimestamp")
  public void shouldRemoveExpirationsByWsId() throws Exception {
    List<String> expected = singletonList(activities[1].getWorkspaceId());

    workspaceActivityDao.removeExpiration(activities[0].getWorkspaceId());

    List<String> found = workspaceActivityDao.findExpiredIdle(2_500_000);
    assertEquals(found, expected);
  }

  @Test(dependsOnMethods = "shouldFindExpirationsByTimestamp")
  public void shouldExpireWorkspaceThatExceedsRunTimeout() throws Exception {
    List<String> expected = singletonList(activities[0].getWorkspaceId());

    // Need more accurate activities for this test
    workspaceActivityDao.removeActivity("ws0");
    workspaceActivityDao.removeActivity("ws1");
    workspaceActivityDao.removeActivity("ws2");

    activityTckRepository.createAll(createWorkspaceActivitiesWithStatuses());

    List<String> found = workspaceActivityDao.findExpiredIdle(8_000_000);
    assertEquals(found, expected);
  }

  @Test(dependsOnMethods = "shouldFindExpirationsByTimestamp")
  public void shouldUpdateExpirations() throws Exception {
    List<String> expected =
        asList(
            activities[0].getWorkspaceId(),
            activities[2].getWorkspaceId(),
            activities[1].getWorkspaceId());

    workspaceActivityDao.setExpirationTime(activities[2].getWorkspaceId(), 1_750_000);

    List<String> found = workspaceActivityDao.findExpiredIdle(2_500_000);
    assertEquals(found, expected);
  }

  @Test(
      expectedExceptions = ServerException.class,
      expectedExceptionsMessageRegExp =
          "Can not create activity record since the specified workspace with id 'non-existing' does not exist.",
      dependsOnMethods = "shouldFindExpirationsByTimestamp")
  public void shouldThrowServerExceptionOnUpdateExpirationsWhenSuchWorkspaceDoesNotExist()
      throws Exception {
    workspaceActivityDao.setExpirationTime("non-existing", 1_750_000);
  }

  @Test(dependsOnMethods = {"shouldFindExpirationsByTimestamp", "shouldRemoveExpirationsByWsId"})
  public void shouldAddExpirations() throws Exception {

    List<String> expected = asList(activities[0].getWorkspaceId(), activities[1].getWorkspaceId());
    workspaceActivityDao.removeExpiration(activities[1].getWorkspaceId());

    // create new again
    workspaceActivityDao.setExpirationTime(activities[1].getWorkspaceId(), 1_250_000);

    List<String> found = workspaceActivityDao.findExpiredIdle(1_500_000);
    assertEquals(found, expected);
  }

  @Test(
      expectedExceptions = ConflictException.class,
      expectedExceptionsMessageRegExp = "Activity record for workspace ID ws0 already exists.")
  public void shouldThrowServerExceptionWhenSuchWorkspaceActivityAlreadyExist() throws Exception {
    workspaceActivityDao.createActivity(activities[0]);
  }

  @Test(
      expectedExceptions = ServerException.class,
      expectedExceptionsMessageRegExp =
          "Can not create activity record since the specified "
              + "workspace with id 'non-existing' does not exist.")
  public void shouldThrowServerExceptionWhenWorkspaceDoesNotExist() throws Exception {
    activities[0].setWorkspaceId("non-existing");

    workspaceActivityDao.createActivity(activities[0]);
  }

  @Test(
      expectedExceptions = ServerException.class,
      expectedExceptionsMessageRegExp =
          "Can not create activity record since the specified "
              + "workspace with id 'non-existing' does not exist.",
      dependsOnMethods = "shouldFindExpirationsByTimestamp")
  public void shouldThrowServerExceptionOnStatusChangeTimeWhenSuchWorkspaceDoesNotExist()
      throws Exception {
    workspaceActivityDao.setStatusChangeTime("non-existing", STOPPED, 1_750_000);
  }

  @Test
  public void shouldNotCareAboutCreatedAndStatusChangeOrder() throws Exception {
    Page<String> found =
        workspaceActivityDao.findInStatusSince(System.currentTimeMillis(), STARTING, 1000, 0);

    assertTrue(found.isEmpty());

    workspaceActivityDao.setCreatedTime(activities[0].getWorkspaceId(), 1L);
    workspaceActivityDao.setStatusChangeTime(activities[0].getWorkspaceId(), STARTING, 2L);

    workspaceActivityDao.setStatusChangeTime(activities[1].getWorkspaceId(), STARTING, 2L);
    workspaceActivityDao.setCreatedTime(activities[1].getWorkspaceId(), 1L);

    found = workspaceActivityDao.findInStatusSince(System.currentTimeMillis(), STARTING, 1000, 0);

    assertEquals(
        found.getItems(), asList(activities[0].getWorkspaceId(), activities[1].getWorkspaceId()));
  }

  @Test(dataProvider = "allWorkspaceStatuses")
  public void shouldFindActivityByLastStatusChangeTime(WorkspaceStatus status) throws Exception {
    Page<String> found =
        workspaceActivityDao.findInStatusSince(System.currentTimeMillis(), status, 1000, 0);

    assertTrue(found.isEmpty());

    workspaceActivityDao.setCreatedTime(activities[0].getWorkspaceId(), 1L);
    workspaceActivityDao.setStatusChangeTime(activities[0].getWorkspaceId(), status, 2L);

    workspaceActivityDao.setStatusChangeTime(activities[1].getWorkspaceId(), status, 5L);
    workspaceActivityDao.setCreatedTime(activities[1].getWorkspaceId(), 1L);

    found = workspaceActivityDao.findInStatusSince(3L, status, 1000, 0);

    assertEquals(found.getItems(), singletonList(activities[0].getWorkspaceId()));
  }

  @Test(dataProvider = "allWorkspaceStatuses")
  public void shouldCount0WorkspacesWhenNoActivityRecorded(WorkspaceStatus status)
      throws Exception {
    long count = workspaceActivityDao.countWorkspacesInStatus(status, System.currentTimeMillis());
    assertEquals(count, 0);
  }

  @Test(dataProvider = "allWorkspaceStatuses")
  public void shouldCountWorkspacesInStatusIgnoringNewerActivity(WorkspaceStatus status)
      throws Exception {
    // given
    workspaceActivityDao.setCreatedTime(activities[0].getWorkspaceId(), 1L);
    workspaceActivityDao.setStatusChangeTime(activities[0].getWorkspaceId(), status, 2L);

    workspaceActivityDao.setStatusChangeTime(activities[1].getWorkspaceId(), status, 5L);
    workspaceActivityDao.setCreatedTime(activities[1].getWorkspaceId(), 1L);

    // when
    long countIgnoringNewer = workspaceActivityDao.countWorkspacesInStatus(status, 3L);
    long countIncludingLatest = workspaceActivityDao.countWorkspacesInStatus(status, 6L);

    // then
    assertEquals(countIgnoringNewer, 1);
    assertEquals(countIncludingLatest, 2);
  }

  @Test
  public void createdTimeMustSetLastStopped() throws TckRepositoryException, ServerException {
    // given new workspace with created activity
    AccountImpl newAccount = new AccountImpl("new_account", "new_account", "test");
    WorkspaceImpl workspace = createWorkspace("new_ws", newAccount, "new_ws");
    accountTckRepository.createAll(singletonList(newAccount));
    wsTckRepository.createAll(singletonList(workspace));

    Page<String> stopped =
        workspaceActivityDao.findInStatusSince(Instant.now().toEpochMilli(), STOPPED, 100, 0);
    assertTrue(stopped.isEmpty());

    // when created workspace activity
    workspaceActivityDao.setCreatedTime("new_ws", Instant.now().toEpochMilli());

    // then find STOPPED must return it and activity must have set last_stopped timestamp
    stopped = workspaceActivityDao.findInStatusSince(Instant.now().toEpochMilli(), STOPPED, 100, 0);
    assertFalse(stopped.isEmpty());
    assertEquals(stopped.getItemsCount(), 1);
    WorkspaceActivity activity = workspaceActivityDao.findActivity("new_ws");
    assertNotNull(activity.getLastStopped());
  }

  @DataProvider(name = "allWorkspaceStatuses")
  public Object[][] getWorkspaceStatus() {
    return Stream.of(WorkspaceStatus.values())
        .map(s -> new WorkspaceStatus[] {s})
        .toArray(Object[][]::new);
  }

  /**
   * Helper function that creates workspaces that are in the RUNNING and STOPPED state for
   * shouldExpireWorkspaceThatExceedsRunTimeout
   *
   * @return A list of WorkspaceActivity objects
   */
  private List<WorkspaceActivity> createWorkspaceActivitiesWithStatuses() {
    WorkspaceActivity[] a = new WorkspaceActivity[3];
    a[0] = new WorkspaceActivity();
    a[0].setWorkspaceId("ws0");
    a[0].setStatus(WorkspaceStatus.RUNNING);
    a[0].setCreated(1_000_000);
    a[0].setLastStarting(1_000_000);
    a[0].setLastRunning(1_000_100);
    a[0].setLastStopped(0);
    a[0].setLastStopping(0);
    a[0].setExpiration(1_100_000L);

    a[1] = new WorkspaceActivity();
    a[1].setWorkspaceId("ws1");
    a[1].setStatus(WorkspaceStatus.RUNNING);
    a[1].setCreated(7_000_000);
    a[1].setLastStarting(7_000_000);
    a[1].setLastRunning(7_100_000);
    a[1].setLastStopped(0);
    a[1].setLastStopping(0);
    a[1].setExpiration(8_000_000L);

    a[2] = new WorkspaceActivity();
    a[2].setWorkspaceId("ws2");
    a[2].setStatus(WorkspaceStatus.STOPPED);
    a[2].setCreated(1_000_200);
    a[2].setLastStarting(1_000_200);
    a[2].setLastRunning(1_000_300);
    a[2].setLastStopped(1_000_400);
    a[2].setLastStopping(1_000_350);
    return asList(a);
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

    final List<ProjectConfigImpl> projects = new ArrayList<>(singletonList(pCfg1));

    // Commands
    final CommandImpl cmd1 = new CommandImpl("name1", "cmd1", "type1");
    final List<CommandImpl> commands = new ArrayList<>(singletonList(cmd1));

    // OldMachine configs
    final MachineConfigImpl exMachine1 = new MachineConfigImpl();
    final ServerConfigImpl serverConf1 =
        new ServerConfigImpl("2265", "http", "path1", singletonMap("key", "value"));
    exMachine1.setServers(ImmutableMap.of("ref1", serverConf1));
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
