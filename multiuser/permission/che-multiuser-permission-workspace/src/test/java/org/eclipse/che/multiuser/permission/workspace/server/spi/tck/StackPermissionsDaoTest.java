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
package org.eclipse.che.multiuser.permission.workspace.server.spi.tck;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackImpl;
import org.eclipse.che.commons.test.tck.TckListener;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepositoryException;
import org.eclipse.che.multiuser.api.permission.server.AbstractPermissionsDomain;
import org.eclipse.che.multiuser.api.permission.server.spi.PermissionsDao;
import org.eclipse.che.multiuser.api.permission.shared.model.Permissions;
import org.eclipse.che.multiuser.permission.workspace.server.stack.StackPermissionsImpl;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Max Shaposhnik */
@Listeners(TckListener.class)
@Test(suiteName = "StackPermissionsDaoTck")
public class StackPermissionsDaoTest {

  @Inject private PermissionsDao<StackPermissionsImpl> dao;
  @Inject private TckRepository<StackPermissionsImpl> permissionsRepository;
  @Inject private TckRepository<UserImpl> userRepository;
  @Inject private TckRepository<StackImpl> stackRepository;

  private StackPermissionsImpl[] permissions;

  @BeforeMethod
  public void setUp() throws TckRepositoryException {
    permissions =
        new StackPermissionsImpl[] {
          new StackPermissionsImpl("user1", "stack1", asList("read", "use", "run")),
          new StackPermissionsImpl("user2", "stack1", asList("read", "use")),
          new StackPermissionsImpl("user1", "stack2", asList("read", "run")),
          new StackPermissionsImpl("user2", "stack2", asList("read", "use", "run", "configure")),
          new StackPermissionsImpl("user", "stack2", asList("read", "use", "run", "configure"))
        };

    final UserImpl[] users =
        new UserImpl[] {
          new UserImpl("user", "user@com.com", "usr"),
          new UserImpl("user1", "user1@com.com", "usr1"),
          new UserImpl("user2", "user2@com.com", "usr2")
        };
    userRepository.createAll(asList(users));

    // Workspace configuration
    final WorkspaceConfigImpl wCfg = new WorkspaceConfigImpl();
    wCfg.setDefaultEnv("env1");
    wCfg.setName("ws1");
    wCfg.setDescription("description");
    stackRepository.createAll(
        asList(
            new StackImpl("stack1", "st1", null, null, null, null, wCfg, null, null),
            new StackImpl("stack2", "st2", null, null, null, null, wCfg, null, null)));

    permissionsRepository.createAll(
        Stream.of(permissions).map(StackPermissionsImpl::new).collect(Collectors.toList()));
  }

  @AfterMethod
  public void cleanUp() throws TckRepositoryException {
    permissionsRepository.removeAll();
    stackRepository.removeAll();
    userRepository.removeAll();
  }

  /* StackPermissionsDao.store() tests */
  @Test
  public void shouldStorePermissions() throws Exception {
    final StackPermissionsImpl permissions =
        new StackPermissionsImpl("user", "stack1", asList("read", "use"));

    dao.store(permissions);

    final Permissions result = dao.get(permissions.getUserId(), permissions.getInstanceId());
    assertEquals(result, new StackPermissionsImpl(permissions));
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowExceptionWhenStoringArgumentIsNull() throws Exception {
    dao.store(null);
  }

  @Test
  public void shouldReplacePermissionsOnStoringWhenItHasAlreadyExisted() throws Exception {
    StackPermissionsImpl oldPermissions = permissions[0];

    StackPermissionsImpl newPermissions =
        new StackPermissionsImpl(
            oldPermissions.getUserId(), oldPermissions.getInstanceId(), singletonList("read"));
    dao.store(newPermissions);

    final Permissions result = dao.get(oldPermissions.getUserId(), oldPermissions.getInstanceId());

    assertEquals(newPermissions, result);
  }

  @Test
  public void shouldReturnsSupportedDomainsIds() {
    assertEquals(dao.getDomain(), new TestDomain());
  }

  /* StackPermissionsDao.remove() tests */
  @Test
  public void shouldRemovePermissions() throws Exception {
    StackPermissionsImpl testPermission = permissions[3];

    dao.remove(testPermission.getUserId(), testPermission.getInstanceId());

    assertFalse(
        dao.exists(
            testPermission.getUserId(),
            testPermission.getInstanceId(),
            testPermission.getActions().get(0)));
  }

  @Test(
      expectedExceptions = NotFoundException.class,
      expectedExceptionsMessageRegExp =
          "Permissions on stack 'instance' of user 'user' was not found.")
  public void shouldThrowNotFoundExceptionWhenPermissionsWasNotFoundOnRemove() throws Exception {
    dao.remove("user", "instance");
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowExceptionWhenRemovePermissionsUserIdArgumentIsNull() throws Exception {
    dao.remove(null, "instance");
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowExceptionWhenRemovePermissionsInstanceIdArgumentIsNull() throws Exception {
    dao.remove("user", null);
  }

  /* StackPermissionsDao.getByInstance() tests */
  @Test
  public void shouldGetPermissionsByInstance() throws Exception {
    final Page<StackPermissionsImpl> permissionsPage =
        dao.getByInstance(permissions[2].getInstanceId(), 1, 1);
    final List<StackPermissionsImpl> fetchedPermissions = permissionsPage.getItems();

    assertEquals(3, permissionsPage.getTotalItemsCount());
    assertEquals(1, permissionsPage.getItemsCount());
    assertTrue(
        fetchedPermissions.contains(permissions[2])
            ^ fetchedPermissions.contains(this.permissions[3])
            ^ fetchedPermissions.contains(this.permissions[4]));
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowExceptionWhenGetByInstanceInstanceIdArgumentIsNull() throws Exception {
    dao.getByInstance(null, 1, 0);
  }

  /* StackPermissionsDao.get() tests */
  @Test
  public void shouldBeAbleToGetPermissions() throws Exception {

    final StackPermissionsImpl result1 =
        dao.get(permissions[0].getUserId(), permissions[0].getInstanceId());
    final StackPermissionsImpl result2 =
        dao.get(permissions[2].getUserId(), permissions[2].getInstanceId());

    assertEquals(result1, permissions[0]);
    assertEquals(result2, permissions[2]);
  }

  @Test(
      expectedExceptions = NotFoundException.class,
      expectedExceptionsMessageRegExp =
          "Permissions on stack 'instance' of user 'user' was not found.")
  public void
      shouldThrowNotFoundExceptionWhenThereIsNotAnyPermissionsForGivenUserAndDomainAndInstance()
          throws Exception {
    dao.get("user", "instance");
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowExceptionWhenGetPermissionsUserIdArgumentIsNull() throws Exception {
    dao.get(null, "instance");
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowExceptionWhenGetPermissionsInstanceIdArgumentIsNull() throws Exception {
    dao.get("user", null);
  }

  /* StackPermissionsDao.exists() tests */
  @Test
  public void shouldBeAbleToCheckPermissionExistence() throws Exception {

    StackPermissionsImpl testPermission = permissions[0];

    final boolean readPermissionExisted =
        dao.exists(testPermission.getUserId(), testPermission.getInstanceId(), "read");
    final boolean fakePermissionExisted =
        dao.exists(testPermission.getUserId(), testPermission.getInstanceId(), "fake");

    assertEquals(readPermissionExisted, testPermission.getActions().contains("read"));
    assertEquals(fakePermissionExisted, testPermission.getActions().contains("fake"));
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowExceptionWhenPermissionsExistsUserIdArgumentIsNull() throws Exception {
    dao.exists(null, "instance", "action");
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowExceptionWhenPermissionsExistsInstanceIdArgumentIsNull() throws Exception {
    dao.exists("user", null, "action");
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowExceptionWhenPermissionsExistsActionArgumentIsNull() throws Exception {
    dao.exists("user", "instance", null);
  }

  public static class TestDomain extends AbstractPermissionsDomain<StackPermissionsImpl> {
    public TestDomain() {
      super("stack", asList("read", "write", "use", "delete"));
    }

    @Override
    protected StackPermissionsImpl doCreateInstance(
        String userId, String instanceId, List<String> allowedActions) {
      return new StackPermissionsImpl(userId, instanceId, allowedActions);
    }
  }
}
