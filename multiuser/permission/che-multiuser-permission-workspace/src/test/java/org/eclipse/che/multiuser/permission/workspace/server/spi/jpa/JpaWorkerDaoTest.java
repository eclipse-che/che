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
package org.eclipse.che.multiuser.permission.workspace.server.spi.jpa;

import static org.eclipse.che.inject.Matchers.names;
import static org.eclipse.che.multiuser.api.permission.server.AbstractPermissionsDomain.SET_PERMISSIONS;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.matcher.Matchers;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.persistence.EntityManager;
import org.aopalliance.intercept.MethodInterceptor;
import org.eclipse.che.account.shared.model.Account;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.commons.test.tck.TckModule;
import org.eclipse.che.commons.test.tck.TckResourcesCleaner;
import org.eclipse.che.multiuser.permission.workspace.server.model.impl.WorkerImpl;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class JpaWorkerDaoTest {

  private JpaWorkerDao workerDao;
  private EntityManager manager;
  private TckResourcesCleaner tckResourcesCleaner;

  @BeforeMethod
  private void setUpManager() {
    final Injector injector =
        Guice.createInjector(new JpaTckModule(), new ExceptionEntityManagerModule());
    manager = injector.getInstance(EntityManager.class);
    workerDao = injector.getInstance(JpaWorkerDao.class);
    tckResourcesCleaner = injector.getInstance(TckResourcesCleaner.class);
  }

  @AfterMethod
  private void cleanup() {
    manager.getTransaction().begin();
    final List<Object> entities = new ArrayList<>();
    entities.addAll(manager.createQuery("SELECT w FROM Worker w").getResultList());
    entities.addAll(manager.createQuery("SELECT w FROM Workspace w").getResultList());
    entities.addAll(manager.createQuery("SELECT u FROM Usr u").getResultList());
    entities.addAll(manager.createQuery("SELECT a FROM Account a").getResultList());
    for (Object entity : entities) {
      manager.remove(entity);
    }
    manager.getTransaction().commit();
    tckResourcesCleaner.clean();
  }

  @Test(
      expectedExceptions = ServerException.class,
      expectedExceptionsMessageRegExp = "Database exception")
  public void shouldThrowServerExceptionOnExistsWhenRuntimeExceptionOccursInDoGetMethod()
      throws Exception {

    final Account account = new AccountImpl("accountId", "namespace", "test");
    final WorkspaceImpl workspace =
        WorkspaceImpl.builder().setId("workspaceId").setAccount(account).build();

    // Persist the account
    manager.getTransaction().begin();
    manager.persist(account);
    manager.getTransaction().commit();
    manager.clear();

    // Persist the workspace
    manager.getTransaction().begin();
    manager.persist(workspace);
    manager.getTransaction().commit();
    manager.clear();
    final UserImpl user = new UserImpl("user0", "user0@com.com", "usr0");
    // Persist the user
    manager.getTransaction().begin();
    manager.persist(user);
    manager.getTransaction().commit();
    manager.clear();

    // Persist the worker
    WorkerImpl worker =
        new WorkerImpl("workspaceId", "user0", Collections.singletonList(SET_PERMISSIONS));
    manager.getTransaction().begin();
    manager.persist(worker);
    manager.getTransaction().commit();
    manager.clear();

    workerDao.exists("user0", "workspaceId", SET_PERMISSIONS);
  }

  public class ExceptionEntityManagerModule extends TckModule {

    @Override
    protected void configure() {
      MethodInterceptor interceptor = new EntityManagerExceptionInterceptor();
      requestInjection(interceptor);
      bindInterceptor(Matchers.subclassesOf(JpaWorkerDao.class), names("doGet"), interceptor);
    }
  }
}
