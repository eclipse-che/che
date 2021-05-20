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
package org.eclipse.che.multiuser.permission.devfile.server.jpa;

import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.multiuser.permission.devfile.server.TestObjectGenerator.createUserDevfile;
import static org.eclipse.che.multiuser.permission.devfile.server.UserDevfileDomain.DELETE;
import static org.eclipse.che.multiuser.permission.devfile.server.UserDevfileDomain.READ;
import static org.eclipse.che.multiuser.permission.devfile.server.UserDevfileDomain.UPDATE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import com.google.inject.Injector;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.persistence.EntityManager;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.devfile.UserDevfile;
import org.eclipse.che.api.devfile.server.model.impl.UserDevfileImpl;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.eclipse.che.commons.test.tck.TckResourcesCleaner;
import org.eclipse.che.multiuser.permission.devfile.server.model.impl.UserDevfilePermissionImpl;
import org.eclipse.che.multiuser.permission.devfile.server.spi.jpa.MultiuserJpaUserDevfileDao;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MultiuserJpaUserDevfileDaoTest {
  private TckResourcesCleaner tckResourcesCleaner;
  private EntityManager manager;
  private MultiuserJpaUserDevfileDao dao;

  private List<UserDevfilePermissionImpl> permissions;
  private List<UserImpl> users;
  private List<UserDevfileImpl> userDevfiles;
  private List<AccountImpl> accounts;

  @BeforeClass
  public void setupEntities() throws Exception {
    permissions =
        ImmutableList.of(
            new UserDevfilePermissionImpl(
                "devfile_id1", "user1", Arrays.asList(READ, DELETE, UPDATE)),
            new UserDevfilePermissionImpl("devfile_id2", "user1", Arrays.asList(READ, UPDATE)),
            new UserDevfilePermissionImpl("devfile_id3", "user1", Arrays.asList(DELETE, UPDATE)),
            new UserDevfilePermissionImpl(
                "devfile_id1", "user2", Arrays.asList(READ, DELETE, UPDATE)));

    users =
        ImmutableList.of(
            new UserImpl("user1", "user1@com.com", "usr1"),
            new UserImpl("user2", "user2@com.com", "usr2"));
    accounts =
        ImmutableList.of(
            new AccountImpl("acc-1", NameGenerator.generate("account", 6), "user"),
            new AccountImpl("acc-2", NameGenerator.generate("account", 6), "user"));
    userDevfiles =
        ImmutableList.of(
            createUserDevfile("devfile_id1", accounts.get(0), generate("name", 6)),
            createUserDevfile("devfile_id2", accounts.get(0), generate("name", 6)),
            createUserDevfile("devfile_id3", accounts.get(0), generate("name", 6)));
    Injector injector = Guice.createInjector(new UserDevfileTckModule());
    manager = injector.getInstance(EntityManager.class);
    dao = injector.getInstance(MultiuserJpaUserDevfileDao.class);
    tckResourcesCleaner = injector.getInstance(TckResourcesCleaner.class);
  }

  @BeforeMethod
  public void setUp() throws Exception {
    manager.getTransaction().begin();

    users.stream().map(UserImpl::new).forEach(manager::persist);
    accounts.stream().map(AccountImpl::new).forEach(manager::persist);
    userDevfiles.stream().map(UserDevfileImpl::new).forEach(manager::persist);
    permissions.stream().map(UserDevfilePermissionImpl::new).forEach(manager::persist);
    manager.getTransaction().commit();
    manager.clear();
  }

  @AfterMethod
  public void cleanup() {
    manager.getTransaction().begin();

    manager
        .createQuery("SELECT e FROM UserDevfilePermission e", UserDevfilePermissionImpl.class)
        .getResultList()
        .forEach(manager::remove);

    manager
        .createQuery("SELECT w FROM UserDevfile w", UserDevfileImpl.class)
        .getResultList()
        .forEach(manager::remove);

    manager
        .createQuery("SELECT a FROM Account a", AccountImpl.class)
        .getResultList()
        .forEach(manager::remove);
    manager
        .createQuery("SELECT u FROM Usr u", UserImpl.class)
        .getResultList()
        .forEach(manager::remove);

    manager.getTransaction().commit();
  }

  @Test
  public void shouldGetTotalWorkspaceCount() throws ServerException {
    assertEquals(dao.getTotalCount(), 3);
  }

  @AfterClass
  public void shutdown() throws Exception {
    tckResourcesCleaner.clean();
    EnvironmentContext.reset();
  }

  @Test
  public void shouldFindDevfilesByByPermissions() throws Exception {
    EnvironmentContext expected = EnvironmentContext.getCurrent();
    expected.setSubject(new SubjectImpl("user", users.get(0).getId(), "token", false));
    List<UserDevfile> results =
        dao.getDevfiles(30, 0, Collections.emptyList(), Collections.emptyList()).getItems();
    assertEquals(results.size(), 2);
    assertTrue(results.contains(userDevfiles.get(0)));
    assertTrue(results.contains(userDevfiles.get(1)));
  }
}
