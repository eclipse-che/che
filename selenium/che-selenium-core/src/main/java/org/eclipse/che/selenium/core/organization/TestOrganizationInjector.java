/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.core.organization;

import static java.lang.String.format;
import static org.eclipse.che.commons.lang.NameGenerator.generate;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.MembersInjector;
import com.google.inject.Provider;
import java.lang.reflect.Field;
import org.eclipse.che.selenium.core.client.TestOrganizationServiceClient;
import org.eclipse.che.selenium.core.client.TestOrganizationServiceClientFactory;
import org.eclipse.che.selenium.core.user.AdminTestUser;

/**
 * Injector for custom annotation {@link InjectTestOrganization}.
 *
 * @author Dmytro Nochevnov
 */
public class TestOrganizationInjector<T> implements MembersInjector<T> {

  private final Field field;
  private final InjectTestOrganization injectTestOrganization;
  private final Provider<Injector> injectorProvider;

  public TestOrganizationInjector(
      Field field,
      InjectTestOrganization injectTestOrganization,
      Provider<Injector> injectorProvider) {
    this.field = field;
    this.injectTestOrganization = injectTestOrganization;
    this.injectorProvider = injectorProvider;
  }

  @Override
  public void injectMembers(T instance) {
    Injector injector = injectorProvider.get();

    String name = generateName();
    String parentPrefix = injectTestOrganization.parentPrefix();

    AdminTestUser adminTestUser = injector.getInstance(Key.get(AdminTestUser.class));

    TestOrganizationServiceClientFactory testOrganizationServiceClient =
        injector.getInstance(Key.get(TestOrganizationServiceClientFactory.class));

    TestOrganizationServiceClient adminOrganizationServiceClient =
        testOrganizationServiceClient.create(adminTestUser);

    TestOrganization testOrganization;
    try {
      if (parentPrefix.isEmpty()) {
        testOrganization = new TestOrganization(name, adminOrganizationServiceClient);
      } else {
        String parentId = findInjectedOrganization(instance, parentPrefix);
        testOrganization = new TestOrganization(name, parentId, adminOrganizationServiceClient);
      }

      field.setAccessible(true);
      field.set(instance, testOrganization);
    } catch (Exception e) {
      throw new RuntimeException(
          format(
              "Failed to instantiate organization with name '%s' in class '%s'",
              injectTestOrganization.prefix(), instance.getClass().getName()),
          e);
    }
  }

  private String generateName() {
    String fieldNamePrefix = field.getName().substring(0, Math.min(field.getName().length(), 12));
    String annotationPrefix = injectTestOrganization.prefix();

    String prefix = annotationPrefix.isEmpty() ? fieldNamePrefix : annotationPrefix;

    return generate(prefix + "-", 5);
  }

  /**
   * @return id of organization among the fields of instance with name which starts from
   *     orgNamePrefix
   */
  private String findInjectedOrganization(T instance, String orgNamePrefix) {
    for (Field field : instance.getClass().getDeclaredFields()) {
      if (field.getType() == TestOrganization.class) {
        field.setAccessible(true);

        TestOrganization org;
        try {
          org = (TestOrganization) field.get(instance);
        } catch (IllegalAccessException e) {
          throw new IllegalArgumentException(e);
        }

        if (org != null && org.getName().startsWith(orgNamePrefix)) {
          return org.getId();
        }
      }
    }

    throw new IllegalArgumentException(
        format(
            "Organization with name which starts from '%s' not found or isn't instantiated in class '%s'",
            orgNamePrefix, instance.getClass().getName()));
  }
}
