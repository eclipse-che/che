/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.machine.server.jpa;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;

import com.google.inject.Guice;
import com.google.inject.Injector;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import org.eclipse.che.api.machine.server.jpa.JpaRecipePermissionsDao.RemovePermissionsBeforeRecipeRemovedEventSubscriber;
import org.eclipse.che.api.machine.server.recipe.RecipeImpl;
import org.eclipse.che.api.machine.server.recipe.RecipePermissionsImpl;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.commons.test.db.H2TestHelper;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests for {@link RemovePermissionsBeforeRecipeRemovedEventSubscriber}
 *
 * @author Sergii Leschenko
 */
public class RemovePermissionsBeforeRecipeRemovedEventSubscriberTest {
  private EntityManager manager;
  private JpaRecipeDao recipeDao;
  private JpaRecipePermissionsDao recipePermissionsDao;

  private RemovePermissionsBeforeRecipeRemovedEventSubscriber subscriber;

  private RecipeImpl recipe;
  private UserImpl[] users;
  private RecipePermissionsImpl[] recipePermissions;

  @BeforeClass
  public void setupEntities() throws Exception {
    recipe =
        new RecipeImpl(
            "recipe1",
            "test",
            "creator",
            "dockerfile",
            "FROM test",
            singletonList("test"),
            "test recipe");
    users = new UserImpl[3];
    for (int i = 0; i < 3; i++) {
      users[i] = new UserImpl("user" + i, "user" + i + "@test.com", "username" + i);
    }
    recipePermissions = new RecipePermissionsImpl[3];
    for (int i = 0; i < 3; i++) {
      recipePermissions[i] =
          new RecipePermissionsImpl(users[i].getId(), recipe.getId(), asList("read", "update"));
    }

    Injector injector = Guice.createInjector(new MachineJpaModule(), new JpaTestModule());

    manager = injector.getInstance(EntityManager.class);
    recipeDao = injector.getInstance(JpaRecipeDao.class);
    recipePermissionsDao = injector.getInstance(JpaRecipePermissionsDao.class);

    subscriber = injector.getInstance(RemovePermissionsBeforeRecipeRemovedEventSubscriber.class);
    subscriber.subscribe();
  }

  @BeforeMethod
  public void setUp() throws Exception {
    manager.getTransaction().begin();
    manager.persist(recipe);
    Stream.of(users).forEach(manager::persist);
    Stream.of(recipePermissions).forEach(manager::persist);
    manager.getTransaction().commit();
  }

  @AfterMethod
  public void cleanup() {
    manager.getTransaction().begin();
    manager
        .createQuery(
            "SELECT recipePermissions FROM RecipePermissions recipePermissions",
            RecipePermissionsImpl.class)
        .getResultList()
        .forEach(manager::remove);
    manager
        .createQuery("SELECT recipe FROM Recipe recipe", RecipeImpl.class)
        .getResultList()
        .forEach(manager::remove);
    manager
        .createQuery("SELECT usr FROM Usr usr", UserImpl.class)
        .getResultList()
        .forEach(manager::remove);
    manager.getTransaction().commit();
  }

  @AfterClass
  public void shutdown() throws Exception {
    subscriber.unsubscribe();
    manager.getEntityManagerFactory().close();
    H2TestHelper.shutdownDefault();
  }

  @Test
  public void shouldRemoveAllRecipePermissionsWhenRecipeIsRemoved() throws Exception {
    recipeDao.remove(recipe.getId());

    assertEquals(recipePermissionsDao.getByInstance(recipe.getId(), 1, 0).getTotalItemsCount(), 0);
  }

  @Test
  public void shouldRemoveAllRecipePermissionsWhenPageSizeEqualsToOne() throws Exception {
    subscriber.removeRecipePermissions(recipe.getId(), 1);

    assertEquals(recipePermissionsDao.getByInstance(recipe.getId(), 1, 0).getTotalItemsCount(), 0);
  }
}
