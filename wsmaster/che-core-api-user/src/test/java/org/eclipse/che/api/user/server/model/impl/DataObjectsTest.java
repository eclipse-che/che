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
package org.eclipse.che.api.user.server.model.impl;

import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.HashMap;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Tests for {@link UserImpl} and {@link ProfileImpl}.
 *
 * @author Yevhenii Voevodin
 */
public class DataObjectsTest {

  @Test
  public void testUserCreation() {
    final UserImpl user =
        new UserImpl(
            "user123", "user@company.com", "user_name", "password", singletonList("google:id"));

    assertEquals(user.getId(), "user123");
    assertEquals(user.getEmail(), "user@company.com");
    assertEquals(user.getName(), "user_name");
    assertEquals(user.getPassword(), "password");
    assertEquals(user.getAliases(), singletonList("google:id"));
  }

  @Test
  public void testUserModification() throws Exception {
    final UserImpl user =
        new UserImpl(
            "user123", "user@company.com", "user_name", "password", singletonList("google:id"));

    user.setName("new_name");
    user.setEmail("new_email@company.com");
    user.setPassword("new-password");
    user.setAliases(singletonList("new-alias"));

    assertEquals(user.getName(), "new_name");
    assertEquals(user.getEmail(), "new_email@company.com");
    assertEquals(user.getPassword(), "new-password");
    assertEquals(user.getAliases(), singletonList("new-alias"));
  }

  @Test
  public void testUserCopyConstructor() throws Exception {
    final UserImpl user =
        new UserImpl(
            "user123",
            "user@company.com",
            "user_name",
            "password",
            new ArrayList<>(singletonList("google:id")));
    final UserImpl copy = new UserImpl(user);

    user.getAliases().add("new-alias");

    assertEquals(copy.getName(), "user_name");
    assertEquals(copy.getEmail(), "user@company.com");
    assertEquals(copy.getPassword(), "password");
    assertEquals(copy.getAliases(), singletonList("google:id"));
    assertFalse(copy.getAliases().contains("new-alias"));
  }

  @Test
  public void testProfileCreation() {
    final ProfileImpl profile =
        new ProfileImpl(
            "user123",
            ImmutableMap.of(
                "attribute1", "value1",
                "attribute2", "value2",
                "attribute3", "value3"));

    assertEquals(profile.getUserId(), "user123");
    assertEquals(
        profile.getAttributes(),
        ImmutableMap.of(
            "attribute1", "value1",
            "attribute2", "value2",
            "attribute3", "value3"));
  }

  @Test
  public void testProfileModification() throws Exception {
    final ProfileImpl profile =
        new ProfileImpl(
            "user123",
            ImmutableMap.of(
                "attribute1", "value1",
                "attribute2", "value2",
                "attribute3", "value3"));

    profile.setAttributes(ImmutableMap.of("attribute1", "value1"));

    assertEquals(profile.getAttributes(), ImmutableMap.of("attribute1", "value1"));
  }

  @Test
  public void testProfileCopyConstructor() throws Exception {
    final ProfileImpl profile =
        new ProfileImpl(
            "user123",
            new HashMap<>(
                ImmutableMap.of(
                    "attribute1", "value1",
                    "attribute2", "value2",
                    "attribute3", "value3")));

    final ProfileImpl copy = new ProfileImpl(profile);
    profile.getAttributes().put("new-attribute", "new-value");

    assertEquals(copy.getUserId(), "user123");
    assertEquals(
        copy.getAttributes(),
        ImmutableMap.of(
            "attribute1", "value1",
            "attribute2", "value2",
            "attribute3", "value3"));
    assertFalse(copy.getAttributes().containsKey("new-attribute"));
  }

  @Test(dataProvider = "reflexivenessProvider")
  @SuppressWarnings("all")
  public void testReflexiveness(Object obj) throws Exception {
    assertTrue(obj.equals(obj));
  }

  @Test(dataProvider = "symmetryDataProvider")
  public void testSymmetry(Object object1, Object object2) throws Exception {
    assertTrue(object1.equals(object2));
    assertTrue(object2.equals(object1));
  }

  @Test(dataProvider = "transitivityDataProvider")
  public void testTransitivity(Object object1, Object object2, Object object3) {
    assertTrue(object1.equals(object2));
    assertTrue(object2.equals(object3));
    assertTrue(object3.equals(object1));
  }

  @Test(dataProvider = "consistencyDataProvider")
  public void testConsistency(Object object1, Object object2) {
    assertTrue(object1.equals(object2));
  }

  @Test(dataProvider = "reflexivenessProvider")
  @SuppressWarnings("all")
  public void testNotEqualityToNull(Object object) throws Exception {
    assertFalse(object.equals(null));
  }

  @Test(
    dependsOnMethods = {
      "testReflexiveness",
      "testSymmetry",
      "testTransitivity",
      "testConsistency",
      "testNotEqualityToNull"
    }
  )
  public void testHashCodeContract() throws Exception {
    final UserImpl user1 =
        new UserImpl("user123", "user@company.com", "user_name", "password", null);
    final UserImpl user2 =
        new UserImpl("user123", "user@company.com", "user_name", "password", new ArrayList<>());

    assertEquals(user1.hashCode(), user2.hashCode());
  }

  @DataProvider(name = "reflexivenessProvider")
  public Object[][] singleObjectProvider() {
    return new Object[][] {
      {
        new UserImpl(
            "user123", "user@company.com", "user_name", "password", singletonList("google:id"))
      },
      {
        new ProfileImpl(
            "user123",
            ImmutableMap.of("attribute1", "value1", "attribute2", "value2", "attribute3", "value3"))
      }
    };
  }

  @DataProvider(name = "symmetryDataProvider")
  public Object[][] symmetryDataProvider() {
    return new Object[][] {
      {
        new UserImpl(
            "user123", "user@company.com", "user_name", "password", singletonList("google:id")),
        new UserImpl(
            "user123", "user@company.com", "user_name", "password", singletonList("google:id"))
      },
      {
        new ProfileImpl("user123", ImmutableMap.of("attribute1", "value1")),
        new ProfileImpl("user123", ImmutableMap.of("attribute1", "value1"))
      }
    };
  }

  @DataProvider(name = "transitivityDataProvider")
  public Object[][] transitivityDataProvider() {
    return new Object[][] {
      {
        new UserImpl("user123", "user@company.com", "user_name", "password", null),
        new UserImpl("user123", "user@company.com", "user_name", "password", new ArrayList<>()),
        new UserImpl("user123", "user@company.com", "user_name", "password", null)
      },
      {
        new ProfileImpl("user123", ImmutableMap.of("attribute1", "value1")),
        new ProfileImpl("user123", ImmutableMap.of("attribute1", "value1")),
        new ProfileImpl("user123", ImmutableMap.of("attribute1", "value1"))
      }
    };
  }

  @DataProvider(name = "consistencyDataProvider")
  public Object[][] consistencyDatProvider() {
    return new Object[][] {
      {
        new UserImpl("user123", "user@company.com", "user_name", "password", null),
        new UserImpl("user123", "user@company.com", "user_name", "password", new ArrayList<>())
      },
      {new ProfileImpl("user123", null), new ProfileImpl("user123", new HashMap<>())}
    };
  }
}
