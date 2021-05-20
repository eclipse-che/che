/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.permission.workspace.server.spi.tck;

import static org.testng.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.commons.test.tck.TckListener;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepositoryException;
import org.eclipse.che.multiuser.api.permission.server.AbstractPermissionsDomain;
import org.eclipse.che.multiuser.permission.workspace.server.model.impl.WorkerImpl;
import org.eclipse.che.multiuser.permission.workspace.server.spi.WorkerDao;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Compatibility test for {@link WorkerDao}
 *
 * @author Max Shaposhnik
 */
@Listeners(TckListener.class)
@Test(suiteName = "WorkerDaoTck")
public class WorkerDaoTest {

  @Inject private WorkerDao workerDao;

  @Inject private TckRepository<WorkerImpl> workerRepository;

  @Inject private TckRepository<UserImpl> userRepository;

  @Inject private TckRepository<AccountImpl> accountRepository;

  @Inject private TckRepository<WorkspaceImpl> workspaceRepository;

  WorkerImpl[] workers;

  @BeforeMethod
  public void setUp() throws TckRepositoryException {
    workers =
        new WorkerImpl[] {
          new WorkerImpl("ws1", "user1", Arrays.asList("read", "use", "run")),
          new WorkerImpl("ws1", "user2", Arrays.asList("read", "use")),
          new WorkerImpl("ws2", "user1", Arrays.asList("read", "run")),
          new WorkerImpl("ws2", "user2", Arrays.asList("read", "use", "run", "configure")),
          new WorkerImpl("ws2", "user0", Arrays.asList("read", "use", "run", "configure"))
        };

    final UserImpl[] users =
        new UserImpl[] {
          new UserImpl("user0", "user0@com.com", "usr0"),
          new UserImpl("user1", "user1@com.com", "usr1"),
          new UserImpl("user2", "user2@com.com", "usr2")
        };
    userRepository.createAll(Arrays.asList(users));

    AccountImpl account = new AccountImpl("account1", "accountName", "test");
    accountRepository.createAll(Collections.singletonList(account));
    workspaceRepository.createAll(
        Arrays.asList(
            new WorkspaceImpl(
                "ws0",
                account,
                new WorkspaceConfigImpl("ws-name0", "", "cfg0", null, null, null, null)),
            new WorkspaceImpl(
                "ws1",
                account,
                new WorkspaceConfigImpl("ws-name1", "", "cfg1", null, null, null, null)),
            new WorkspaceImpl(
                "ws2",
                account,
                new WorkspaceConfigImpl("ws-name2", "", "cfg2", null, null, null, null))));

    workerRepository.createAll(
        Stream.of(workers).map(WorkerImpl::new).collect(Collectors.toList()));
  }

  @AfterMethod
  public void cleanUp() throws TckRepositoryException {
    workerRepository.removeAll();
    workspaceRepository.removeAll();
    userRepository.removeAll();
    accountRepository.removeAll();
  }

  /* WorkerDao.store() tests */
  @Test
  public void shouldStoreWorker() throws Exception {
    WorkerImpl worker = new WorkerImpl("ws0", "user0", Arrays.asList("read", "use", "run"));
    workerDao.store(worker);
    Assert.assertEquals(workerDao.getWorker("ws0", "user0"), new WorkerImpl(worker));
  }

  @Test
  public void shouldReplaceExistingWorkerOnStoring() throws Exception {
    WorkerImpl replace = new WorkerImpl("ws1", "user1", Collections.singletonList("read"));
    workerDao.store(replace);
    Assert.assertEquals(workerDao.getWorker("ws1", "user1"), replace);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowExceptionWhenStoringArgumentIsNull() throws Exception {
    workerDao.store(null);
  }

  /* WorkerDao.getWorker() tests */
  @Test
  public void shouldGetWorkerByWorkspaceIdAndUserId() throws Exception {
    Assert.assertEquals(workerDao.getWorker("ws1", "user1"), workers[0]);
    Assert.assertEquals(workerDao.getWorker("ws2", "user2"), workers[3]);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowExceptionWhenGetWorkerWorkspaceIdArgumentIsNull() throws Exception {
    workerDao.getWorker(null, "user1");
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowExceptionWhenGetWorkerUserIdArgumentIsNull() throws Exception {
    workerDao.getWorker("ws1", null);
  }

  @Test(expectedExceptions = NotFoundException.class)
  public void shouldThrowNotFoundExceptionOnGetIfWorkerWithSuchWorkspaceIdOrUserIdDoesNotExist()
      throws Exception {
    workerDao.getWorker("ws9", "user1");
  }

  /* WorkerDao.getWorkers() tests */
  @Test
  public void shouldGetWorkersByWorkspaceId() throws Exception {
    Page<WorkerImpl> workersPage = workerDao.getWorkers("ws2", 1, 1);

    final List<WorkerImpl> fetchedWorkers = workersPage.getItems();
    assertEquals(workersPage.getTotalItemsCount(), 3);
    assertEquals(workersPage.getItemsCount(), 1);
    assertTrue(
        fetchedWorkers.contains(workers[2])
            ^ fetchedWorkers.contains(workers[3])
            ^ fetchedWorkers.contains(workers[4]));
  }

  @Test
  public void shouldGetWorkersByUserId() throws Exception {
    List<WorkerImpl> actual = workerDao.getWorkersByUser("user1");
    List<WorkerImpl> expected = Arrays.asList(workers[0], workers[2]);
    assertEquals(actual.size(), expected.size());
    assertTrue(new HashSet<>(actual).equals(new HashSet<>(expected)));
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowExceptionWhenGetWorkersByWorkspaceArgumentIsNull() throws Exception {
    workerDao.getWorkers(null, 1, 0);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowExceptionWhenGetWorkersByUserArgumentIsNull() throws Exception {
    workerDao.getWorkersByUser(null);
  }

  @Test
  public void shouldReturnEmptyListIfWorkersWithSuchWorkspaceIdDoesNotFound() throws Exception {
    assertEquals(0, workerDao.getWorkers("unexisted_ws", 1, 0).getItemsCount());
  }

  @Test
  public void shouldReturnEmptyListIfWorkersWithSuchUserIdDoesNotFound() throws Exception {
    assertEquals(0, workerDao.getWorkersByUser("unexisted_user").size());
  }

  /* WorkerDao.removeWorker() tests */
  @Test
  public void shouldRemoveWorker() throws Exception {
    workerDao.removeWorker("ws1", "user1");
    assertEquals(1, workerDao.getWorkersByUser("user1").size());
    assertNull(notFoundToNull(() -> workerDao.getWorker("ws1", "user1")));
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowExceptionWhenRemoveWorkerWorkspaceIdArgumentIsNull() throws Exception {
    workerDao.removeWorker(null, "user1");
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowExceptionWhenRemoveWorkerUserIdArgumentIsNull() throws Exception {
    workerDao.removeWorker("ws1", null);
  }

  @Test(expectedExceptions = ServerException.class)
  public void shouldThrowNotFoundExceptionOnRemoveIfWorkerWithSuchWorkspaceIdDoesNotExist()
      throws Exception {
    workerDao.removeWorker("unexisted_ws", "user1");
  }

  @Test(expectedExceptions = ServerException.class)
  public void shouldThrowNotFoundExceptionOnRemoveIfWorkerWithSuchUserIdDoesNotExist()
      throws Exception {
    workerDao.removeWorker("ws1", "unexisted_user");
  }

  public static class TestDomain extends AbstractPermissionsDomain<WorkerImpl> {
    public TestDomain() {
      super("workspace", Arrays.asList("read", "write", "use", "delete"));
    }

    @Override
    protected WorkerImpl doCreateInstance(
        String userId, String instanceId, List<String> allowedActions) {
      return new WorkerImpl(userId, instanceId, allowedActions);
    }
  }

  private static <T> T notFoundToNull(Callable<T> action) throws Exception {
    try {
      return action.call();
    } catch (NotFoundException x) {
      return null;
    }
  }
}
