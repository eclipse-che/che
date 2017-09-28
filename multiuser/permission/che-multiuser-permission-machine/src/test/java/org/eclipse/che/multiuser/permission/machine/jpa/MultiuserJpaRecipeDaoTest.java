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
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.inject.Guice;
import com.google.inject.Injector;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import org.eclipse.che.api.recipe.OldRecipeImpl;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.commons.test.db.H2TestHelper;
import org.eclipse.che.multiuser.permission.machine.recipe.RecipePermissionsImpl;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Max Shaposhnik */
public class MultiuserJpaRecipeDaoTest {
  private EntityManager manager;
  private MultiuserJpaRecipeDao dao;

  private RecipePermissionsImpl[] permissions;
  private UserImpl[] users;
  private OldRecipeImpl[] recipes;

  @BeforeClass
  public void setupEntities() throws Exception {
    permissions =
        new RecipePermissionsImpl[] {
          new RecipePermissionsImpl("user1", "recipe1", asList("read", "use", "search")),
          new RecipePermissionsImpl("user1", "recipe2", asList("read", "search")),
          new RecipePermissionsImpl("user1", "recipe3", asList("read", "search")),
          new RecipePermissionsImpl("user1", "recipe4", asList("read", "run")),
          new RecipePermissionsImpl("user2", "recipe1", asList("read", "use")),
          new RecipePermissionsImpl("*", "recipe_debian", singletonList("search")),
          new RecipePermissionsImpl("*", "recipe_ubuntu", singletonList("search"))
        };

    users =
        new UserImpl[] {
          new UserImpl("user1", "user1@com.com", "usr1"),
          new UserImpl("user2", "user2@com.com", "usr2")
        };

    recipes =
        new OldRecipeImpl[] {
          new OldRecipeImpl("recipe1", "rc1", null, null, null, asList("tag1", "tag2"), null),
          new OldRecipeImpl("recipe2", "rc2", null, "testType", null, null, null),
          new OldRecipeImpl("recipe3", "rc3", null, null, null, asList("tag1", "tag2"), null),
          new OldRecipeImpl("recipe4", "rc4", null, null, null, null, null),
          new OldRecipeImpl(
              "recipe_debian", "DEBIAN_JDK8", "test", "test", null, asList("debian", "tag1"), null),
          new OldRecipeImpl(
              "recipe_ubuntu", "DEBIAN_JDK8", "test", "test", null, asList("ubuntu", "tag1"), null)
        };

    Injector injector = Guice.createInjector(new JpaTestModule(), new MultiuserMachineJpaModule());
    manager = injector.getInstance(EntityManager.class);
    dao = injector.getInstance(MultiuserJpaRecipeDao.class);
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
    manager.getEntityManagerFactory().close();
    H2TestHelper.shutdownDefault();
  }

  @Test
  public void shouldFindRecipeByPermissionsAndType() throws Exception {
    List<OldRecipeImpl> results = dao.search(users[0].getId(), null, "testType", 0, 0);
    assertEquals(results.size(), 1);
    assertTrue(results.contains(recipes[1]));
  }

  @Test
  public void shouldFindRecipeByPermissionsAndTags() throws Exception {
    List<OldRecipeImpl> results = dao.search(users[0].getId(), singletonList("tag2"), null, 0, 0);
    assertEquals(results.size(), 2);
    assertTrue(results.contains(recipes[0]));
    assertTrue(results.contains(recipes[2]));
  }

  @Test
  public void shouldFindRecipeByUserIdAndPublicPermissions() throws Exception {
    final Set<OldRecipeImpl> results =
        new HashSet<>(dao.search(users[0].getId(), null, null, 0, 30));
    assertEquals(results.size(), 5);
    assertTrue(results.contains(recipes[0]));
    assertTrue(results.contains(recipes[1]));
    assertTrue(results.contains(recipes[2]));
    assertTrue(results.contains(recipes[4]));
    assertTrue(results.contains(recipes[5]));
  }

  @Test
  public void shouldNotFindRecipeNonexistentTags() throws Exception {
    List<OldRecipeImpl> results =
        dao.search(users[0].getId(), singletonList("unexisted_tag2"), null, 0, 0);
    assertTrue(results.isEmpty());
  }
}
