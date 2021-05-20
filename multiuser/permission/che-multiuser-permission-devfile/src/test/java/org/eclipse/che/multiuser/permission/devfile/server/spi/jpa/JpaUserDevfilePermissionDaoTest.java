/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.permission.devfile.server.spi.jpa;

import static org.eclipse.che.commons.lang.NameGenerator.generate;
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
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.devfile.server.model.impl.UserDevfileImpl;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.commons.test.tck.TckModule;
import org.eclipse.che.commons.test.tck.TckResourcesCleaner;
import org.eclipse.che.multiuser.permission.devfile.server.TestObjectGenerator;
import org.eclipse.che.multiuser.permission.devfile.server.model.impl.UserDevfilePermissionImpl;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class JpaUserDevfilePermissionDaoTest {

  private JpaUserDevfilePermissionDao userDevfilePermissionsDao;
  private EntityManager manager;
  private TckResourcesCleaner tckResourcesCleaner;

  @BeforeMethod
  private void setUpManager() {
    final Injector injector =
        Guice.createInjector(new JpaTckModule(), new ExceptionEntityManagerModule());
    manager = injector.getInstance(EntityManager.class);
    userDevfilePermissionsDao = injector.getInstance(JpaUserDevfilePermissionDao.class);
    tckResourcesCleaner = injector.getInstance(TckResourcesCleaner.class);
  }

  @AfterMethod
  private void cleanup() {
    manager.getTransaction().begin();
    final List<Object> entities = new ArrayList<>();
    entities.addAll(manager.createQuery("SELECT p FROM UserDevfilePermission p").getResultList());
    entities.addAll(manager.createQuery("SELECT d FROM UserDevfile d").getResultList());
    entities.addAll(manager.createQuery("SELECT a FROM Account a").getResultList());
    entities.addAll(manager.createQuery("SELECT u FROM Usr u").getResultList());
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

    // Persist the account
    manager.getTransaction().begin();
    manager.persist(TestObjectGenerator.TEST_ACCOUNT);
    manager.getTransaction().commit();
    manager.clear();

    final UserDevfileImpl userDevfile = TestObjectGenerator.createUserDevfile();
    // Persist the userdevfile
    manager.getTransaction().begin();
    manager.persist(userDevfile);
    manager.getTransaction().commit();
    manager.clear();

    final UserImpl user = new UserImpl(generate("user", 6), "user0@com.com", "usr0");
    // Persist the user
    manager.getTransaction().begin();
    manager.persist(user);
    manager.getTransaction().commit();
    manager.clear();

    // Persist the worker
    UserDevfilePermissionImpl worker =
        new UserDevfilePermissionImpl(
            userDevfile.getId(), user.getId(), Collections.singletonList(SET_PERMISSIONS));
    manager.getTransaction().begin();
    manager.persist(worker);
    manager.getTransaction().commit();
    manager.clear();

    userDevfilePermissionsDao.exists(user.getId(), userDevfile.getId(), SET_PERMISSIONS);
  }

  public class ExceptionEntityManagerModule extends TckModule {

    @Override
    protected void configure() {
      MethodInterceptor interceptor = new EntityManagerExceptionInterceptor();
      requestInjection(interceptor);
      bindInterceptor(
          Matchers.subclassesOf(JpaUserDevfilePermissionDao.class), names("doGet"), interceptor);
    }
  }
}
