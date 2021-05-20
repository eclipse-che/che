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
package org.eclipse.che.multiuser.permission.devfile.server.spi.tck;

import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.multiuser.permission.devfile.server.TestObjectGenerator.createUserDevfile;
import static org.eclipse.che.multiuser.permission.devfile.server.UserDevfileDomain.DELETE;
import static org.eclipse.che.multiuser.permission.devfile.server.UserDevfileDomain.READ;
import static org.eclipse.che.multiuser.permission.devfile.server.UserDevfileDomain.UPDATE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
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
import org.eclipse.che.api.devfile.server.model.impl.UserDevfileImpl;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.commons.test.tck.TckListener;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepositoryException;
import org.eclipse.che.multiuser.api.permission.server.AbstractPermissionsDomain;
import org.eclipse.che.multiuser.permission.devfile.server.TestObjectGenerator;
import org.eclipse.che.multiuser.permission.devfile.server.UserDevfileDomain;
import org.eclipse.che.multiuser.permission.devfile.server.model.UserDevfilePermission;
import org.eclipse.che.multiuser.permission.devfile.server.model.impl.UserDevfilePermissionImpl;
import org.eclipse.che.multiuser.permission.devfile.server.spi.UserDevfilePermissionDao;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** Compatibility test for {@link UserDevfilePermissionDao} */
@Listeners(TckListener.class)
@Test(suiteName = "UserDevfilePermissionTck")
public class UserDevfilePermissionDaoTest {

  @Inject private UserDevfilePermissionDao permissionDao;

  @Inject private TckRepository<UserDevfilePermission> permissionTckRepository;

  @Inject private TckRepository<UserImpl> userRepository;

  @Inject private TckRepository<UserDevfileImpl> devfileRepository;

  @Inject private TckRepository<AccountImpl> accountRepository;

  UserDevfilePermissionImpl[] permissions;

  @BeforeMethod
  public void setUp() throws TckRepositoryException {
    accountRepository.createAll(ImmutableList.of(TestObjectGenerator.TEST_ACCOUNT));
    permissions =
        new UserDevfilePermissionImpl[] {
          new UserDevfilePermissionImpl(
              "devfile_id1", "user1", Arrays.asList(READ, DELETE, UPDATE)),
          new UserDevfilePermissionImpl("devfile_id1", "user2", Arrays.asList(READ, DELETE)),
          new UserDevfilePermissionImpl("devfile_id2", "user1", Arrays.asList(READ, UPDATE)),
          new UserDevfilePermissionImpl(
              "devfile_id2", "user2", Arrays.asList(READ, DELETE, UPDATE)),
          new UserDevfilePermissionImpl("devfile_id2", "user0", Arrays.asList(READ, DELETE, UPDATE))
        };

    final UserImpl[] users =
        new UserImpl[] {
          new UserImpl("user0", "user0@com.com", "usr0"),
          new UserImpl("user1", "user1@com.com", "usr1"),
          new UserImpl("user2", "user2@com.com", "usr2")
        };
    userRepository.createAll(Arrays.asList(users));

    devfileRepository.createAll(
        ImmutableList.of(
            createUserDevfile("devfile_id1", generate("name", 6)),
            createUserDevfile("devfile_id2", generate("name", 6)),
            createUserDevfile("devfile_id3", generate("name", 6)),
            createUserDevfile("devfile_id4", generate("name", 6)),
            createUserDevfile("devfile_id5", generate("name", 6))));

    permissionTckRepository.createAll(
        Stream.of(permissions).map(UserDevfilePermissionImpl::new).collect(Collectors.toList()));
  }

  @AfterMethod
  public void cleanUp() throws TckRepositoryException {
    permissionTckRepository.removeAll();
    devfileRepository.removeAll();
    accountRepository.removeAll();
    userRepository.removeAll();
  }

  @Test
  public void shouldStorePermissions() throws Exception {
    UserDevfilePermissionImpl permission =
        new UserDevfilePermissionImpl("devfile_id1", "user0", Arrays.asList(READ, DELETE, UPDATE));
    permissionDao.store(permission);
    Assert.assertEquals(
        permissionDao.getUserDevfilePermission("devfile_id1", "user0"),
        new UserDevfilePermissionImpl(permission));
  }

  @Test
  public void shouldReplaceExistingPermissionOnStoring() throws Exception {
    UserDevfilePermissionImpl replace =
        new UserDevfilePermissionImpl("devfile_id1", "user1", Collections.singletonList("READ"));
    permissionDao.store(replace);
    Assert.assertEquals(permissionDao.getUserDevfilePermission("devfile_id1", "user1"), replace);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowExceptionWhenStoringArgumentIsNull() throws Exception {
    permissionDao.store(null);
  }

  @Test
  public void shouldGetPermissionByWorkspaceIdAndUserId() throws Exception {
    Assert.assertEquals(
        permissionDao.getUserDevfilePermission("devfile_id1", "user1"), permissions[0]);
    Assert.assertEquals(
        permissionDao.getUserDevfilePermission("devfile_id2", "user2"), permissions[3]);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowExceptionWhenGetPermissionDevfileIdArgumentIsNull() throws Exception {
    permissionDao.getUserDevfilePermission(null, "user1");
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowExceptionWhenGetPermissionUserIdArgumentIsNull() throws Exception {
    permissionDao.getUserDevfilePermission("devfile_id1", null);
  }

  @Test(expectedExceptions = NotFoundException.class)
  public void shouldThrowNotFoundExceptionOnGetIfPermissionWithSuchDevfileIdOrUserIdDoesNotExist()
      throws Exception {
    permissionDao.getUserDevfilePermission("devfile_id9", "user1");
  }

  @Test
  public void shouldGetPermissionsByDevfileId() throws Exception {
    Page<UserDevfilePermissionImpl> permissionsPage =
        permissionDao.getUserDevfilePermission("devfile_id2", 1, 1);

    final List<UserDevfilePermissionImpl> fetchedPermissions = permissionsPage.getItems();
    assertEquals(permissionsPage.getTotalItemsCount(), 3);
    assertEquals(permissionsPage.getItemsCount(), 1);
    assertTrue(
        fetchedPermissions.contains(permissions[2])
            ^ fetchedPermissions.contains(permissions[3])
            ^ fetchedPermissions.contains(permissions[4]));
  }

  @Test
  public void shouldGetPermissionsByUserId() throws Exception {
    List<UserDevfilePermissionImpl> actual = permissionDao.getUserDevfilePermissionByUser("user1");
    List<UserDevfilePermissionImpl> expected = Arrays.asList(permissions[0], permissions[2]);
    assertEquals(actual.size(), expected.size());
    assertTrue(new HashSet<>(actual).equals(new HashSet<>(expected)));
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowExceptionWhenGetPermissionsByDevfileArgumentIsNull() throws Exception {
    permissionDao.getUserDevfilePermission(null, 1, 0);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowExceptionWhenGetPermissionsByUserArgumentIsNull() throws Exception {
    permissionDao.getUserDevfilePermissionByUser(null);
  }

  @Test
  public void shouldReturnEmptyListIfPermissionsWithSuchDevfileIdDoesNotFound() throws Exception {
    assertEquals(
        0, permissionDao.getUserDevfilePermission("unexisted_devfile_id", 1, 0).getItemsCount());
  }

  @Test
  public void shouldReturnEmptyListIfPermissionsWithSuchUserIdDoesNotFound() throws Exception {
    assertEquals(0, permissionDao.getUserDevfilePermissionByUser("unexisted_user").size());
  }

  @Test
  public void shouldRemovePermission() throws Exception {
    permissionDao.removeUserDevfilePermission("devfile_id1", "user1");
    assertEquals(1, permissionDao.getUserDevfilePermissionByUser("user1").size());
    assertNull(
        notFoundToNull(() -> permissionDao.getUserDevfilePermission("devfile_id1", "user1")));
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowExceptionWhenRemovePermissionDevfileIdArgumentIsNull() throws Exception {
    permissionDao.removeUserDevfilePermission(null, "user1");
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowExceptionWhenRemovePermissionUserIdArgumentIsNull() throws Exception {
    permissionDao.removeUserDevfilePermission("devfile_id1", null);
  }

  @Test(expectedExceptions = ServerException.class)
  public void shouldThrowNotFoundExceptionOnRemoveIfPermissionWithSuchDevfileIdDoesNotExist()
      throws Exception {
    permissionDao.removeUserDevfilePermission("unexisted_ws", "user1");
  }

  @Test(expectedExceptions = ServerException.class)
  public void shouldThrowNotFoundExceptionOnRemoveIfPermissionWithSuchUserIdDoesNotExist()
      throws Exception {
    permissionDao.removeUserDevfilePermission("devfile_id1", "unexisted_user");
  }

  public static class TestDomain extends AbstractPermissionsDomain<UserDevfilePermissionImpl> {
    public TestDomain() {
      super(UserDevfileDomain.DOMAIN_ID, Arrays.asList(READ, DELETE, UPDATE));
    }

    @Override
    protected UserDevfilePermissionImpl doCreateInstance(
        String userId, String instanceId, List<String> allowedActions) {
      return new UserDevfilePermissionImpl(userId, instanceId, allowedActions);
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
