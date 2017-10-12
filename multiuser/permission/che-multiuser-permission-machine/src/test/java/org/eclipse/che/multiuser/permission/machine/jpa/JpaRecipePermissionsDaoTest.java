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
package org.eclipse.che.multiuser.permission.machine.jpa;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;

import com.google.inject.Guice;
import com.google.inject.Injector;
import java.util.List;
import javax.persistence.EntityManager;
import org.eclipse.che.api.recipe.OldRecipeImpl;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.commons.test.db.H2JpaCleaner;
import org.eclipse.che.multiuser.permission.machine.recipe.RecipePermissionsImpl;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Max Shaposhnik */
public class JpaRecipePermissionsDaoTest {
  private H2JpaCleaner h2JpaCleaner;
  private EntityManager manager;
  private JpaRecipePermissionsDao dao;

  private RecipePermissionsImpl[] permissions;
  private UserImpl[] users;
  private OldRecipeImpl[] recipes;

  @BeforeClass
  public void setupEntities() throws Exception {
    permissions =
        new RecipePermissionsImpl[] {
          new RecipePermissionsImpl("user1", "recipe1", asList("read", "use", "run")),
          new RecipePermissionsImpl("user2", "recipe1", asList("read", "use")),
          new RecipePermissionsImpl("user1", "recipe2", asList("read", "run")),
          new RecipePermissionsImpl("user2", "recipe2", asList("read", "use", "run", "configure"))
        };

    users =
        new UserImpl[] {
          new UserImpl("user1", "user1@com.com", "usr1"),
          new UserImpl("user2", "user2@com.com", "usr2")
        };

    recipes =
        new OldRecipeImpl[] {
          new OldRecipeImpl("recipe1", "rc1", null, null, null, null, null),
          new OldRecipeImpl("recipe2", "rc2", null, null, null, null, null)
        };

    Injector injector = Guice.createInjector(new JpaTestModule(), new MultiuserMachineJpaModule());
    manager = injector.getInstance(EntityManager.class);
    dao = injector.getInstance(JpaRecipePermissionsDao.class);
    h2JpaCleaner = injector.getInstance(H2JpaCleaner.class);
  }

  @BeforeMethod
  public void setUp() throws Exception {
    manager.getTransaction().begin();
    for (UserImpl user : users) {
      manager.persist(user);
    }

    for (OldRecipeImpl recipe : recipes) {
      manager.persist(recipe);
    }

    for (RecipePermissionsImpl recipePermissions : permissions) {
      manager.persist(recipePermissions);
    }
    manager.getTransaction().commit();
    manager.clear();
  }

  @AfterMethod
  public void cleanup() {
    manager.getTransaction().begin();

    manager
        .createQuery("SELECT p FROM RecipePermissions p", RecipePermissionsImpl.class)
        .getResultList()
        .forEach(manager::remove);

    manager
        .createQuery("SELECT r FROM OldRecipe r", OldRecipeImpl.class)
        .getResultList()
        .forEach(manager::remove);

    manager
        .createQuery("SELECT u FROM Usr u", UserImpl.class)
        .getResultList()
        .forEach(manager::remove);
    manager.getTransaction().commit();
  }

  @AfterClass
  public void shutdown() throws Exception {
    h2JpaCleaner.clean();
  }

  @Test
  public void shouldGetRecipePermissionByInstanceIdAndWildcard() throws Exception {
    manager.getTransaction().begin();
    manager.persist(new RecipePermissionsImpl(null, "recipe1", asList("read", "use", "run")));
    manager.getTransaction().commit();

    RecipePermissionsImpl result = dao.get("*", "recipe1");
    assertEquals(result.getInstanceId(), "recipe1");
    assertEquals(result.getUserId(), null);
  }

  @Test
  public void
      shouldGetRecipePermissionByInstanceIdAndUserIdIfPublicPermissionExistsWithSameInstanceId()
          throws Exception {
    manager.getTransaction().begin();
    manager.persist(new RecipePermissionsImpl(null, "recipe1", asList("read", "use", "run")));
    manager.getTransaction().commit();

    RecipePermissionsImpl result = dao.get("user1", "recipe1");
    assertEquals(result.getInstanceId(), "recipe1");
    assertEquals(result.getUserId(), "user1");
  }

  @Test
  public void shouldStoreRecipePublicPermission() throws Exception {
    //given
    final RecipePermissionsImpl publicPermission =
        new RecipePermissionsImpl("*", "recipe1", asList("read", "use", "run"));

    //when
    dao.store(publicPermission);

    //then
    assertTrue(
        dao.getByInstance(publicPermission.getInstanceId(), 3, 0)
            .getItems()
            .contains(new RecipePermissionsImpl(publicPermission)));
  }

  @Test
  public void shouldUpdateExistingRecipePublicPermissions() throws Exception {
    final RecipePermissionsImpl publicPermission =
        new RecipePermissionsImpl("*", "recipe1", asList("read", "use", "run"));
    dao.store(publicPermission);
    dao.store(publicPermission);

    final List<RecipePermissionsImpl> storedPermissions =
        dao.getByInstance(publicPermission.getInstanceId(), 30, 0).getItems();
    assertTrue(storedPermissions.contains(new RecipePermissionsImpl(publicPermission)));
    assertTrue(storedPermissions.stream().filter(p -> "*".equals(p.getUserId())).count() == 1);
  }

  @Test
  public void shouldRemoveRecipePublicPermission() throws Exception {
    final RecipePermissionsImpl publicPermission =
        new RecipePermissionsImpl("*", "recipe1", asList("read", "use", "run"));
    dao.store(publicPermission);
    dao.remove(publicPermission.getUserId(), publicPermission.getInstanceId());

    final List<RecipePermissionsImpl> storePermissions =
        dao.getByInstance(publicPermission.getInstanceId(), 30, 0).getItems();
    assertTrue(storePermissions.stream().filter(p -> "*".equals(p.getUserId())).count() == 0);
  }
}
