/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.api.permission.server.spi.tck;

import static java.util.Arrays.asList;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.commons.test.tck.TckListener;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.multiuser.api.permission.server.AbstractPermissionsDomain;
import org.eclipse.che.multiuser.api.permission.server.jpa.JpaSystemPermissionsDao;
import org.eclipse.che.multiuser.api.permission.server.model.impl.SystemPermissionsImpl;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Max Shaposhnik */
@Listeners(TckListener.class)
@Test(suiteName = "SystemPermissionsDaoTck")
public class SystemPermissionsDaoTest {

  @Inject private JpaSystemPermissionsDao dao;

  private UserImpl[] users;

  private SystemPermissionsImpl[] systemPermissions;

  @Inject private TckRepository<UserImpl> userRepository;
  @Inject private TckRepository<SystemPermissionsImpl> systemRepository;

  @BeforeMethod
  public void setupEntities() throws Exception {
    systemPermissions =
        new SystemPermissionsImpl[] {
          new SystemPermissionsImpl("user1", asList("read", "use", "run")),
          new SystemPermissionsImpl("user2", asList("read", "use")),
          new SystemPermissionsImpl("user3", asList("read", "use"))
        };

    users =
        new UserImpl[] {
          new UserImpl("user1", "user1@com.com", "usr1"),
          new UserImpl("user2", "user2@com.com", "usr2"),
          new UserImpl("user3", "user3@com.com", "usr3")
        };

    userRepository.createAll(asList(users));
    systemRepository.createAll(
        Stream.of(systemPermissions).map(SystemPermissionsImpl::new).collect(Collectors.toList()));
  }

  @AfterMethod
  public void cleanup() throws Exception {
    systemRepository.removeAll();
    userRepository.removeAll();
  }

  @Test
  public void shouldReturnAllPermissionsWhenGetByInstance() throws Exception {
    final Page<SystemPermissionsImpl> permissionsPage = dao.getByInstance(null, 1, 1);
    final List<SystemPermissionsImpl> permissions = permissionsPage.getItems();

    assertEquals(permissionsPage.getTotalItemsCount(), 3);
    assertEquals(permissionsPage.getItemsCount(), 1);
    assertTrue(
        permissions.contains(systemPermissions[0])
            ^ permissions.contains(systemPermissions[1])
            ^ permissions.contains(systemPermissions[2]));
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowExceptionWhenGetPermissionsUserIdArgumentIsNull() throws Exception {
    dao.get(null, "instance");
  }

  @Test
  public void doesNotThrowNpeWhenInstanceIsNull() throws Exception {
    dao.get(users[0].getId(), null);
  }

  @Test
  public void shouldBeAbleToGetPermissions() throws Exception {
    final SystemPermissionsImpl result1 =
        dao.get(systemPermissions[0].getUserId(), systemPermissions[0].getInstanceId());
    final SystemPermissionsImpl result2 =
        dao.get(systemPermissions[1].getUserId(), systemPermissions[1].getInstanceId());

    assertEquals(result1, systemPermissions[0]);
    assertEquals(result2, systemPermissions[1]);
  }

  public static class TestDomain extends AbstractPermissionsDomain<SystemPermissionsImpl> {
    public TestDomain() {
      super("system", asList("read", "write", "use"));
    }

    @Override
    protected SystemPermissionsImpl doCreateInstance(
        String userId, String instanceId, List allowedActions) {
      return null;
    }
  }
}
