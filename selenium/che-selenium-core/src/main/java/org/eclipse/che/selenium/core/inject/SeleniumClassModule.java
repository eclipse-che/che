/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.core.inject;

import static com.google.inject.matcher.Matchers.any;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import java.lang.reflect.Field;
import java.util.function.Consumer;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.organization.InjectTestOrganization;
import org.eclipse.che.selenium.core.organization.TestOrganization;
import org.eclipse.che.selenium.core.organization.TestOrganizationInjector;
import org.eclipse.che.selenium.core.workspace.InjectTestWorkspace;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.core.workspace.TestWorkspaceInjector;

/**
 * Guice module per test class.
 *
 * @author Anatolii Bazko
 */
public class SeleniumClassModule extends AbstractModule {
  @Override
  public void configure() {
    bind(SeleniumWebDriver.class);

    bindListener(any(), new WorkspaceTypeListener(binder().getProvider(Injector.class)));
    bindListener(any(), new OrganizationTypeListener(binder().getProvider(Injector.class)));
  }

  private class WorkspaceTypeListener implements TypeListener {
    private final Provider<Injector> injectorProvider;

    public WorkspaceTypeListener(Provider<Injector> injector) {
      this.injectorProvider = injector;
    }

    @Override
    public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
      traverseClassHierarchy(
          type.getRawType(),
          (clazz) -> {
            for (Field field : clazz.getDeclaredFields()) {
              if (field.getType() == TestWorkspace.class
                  && field.isAnnotationPresent(InjectTestWorkspace.class)) {
                encounter.register(
                    new TestWorkspaceInjector<>(
                        field,
                        field.getAnnotation(InjectTestWorkspace.class),
                        injectorProvider.get()));
              }
            }
          });
    }

    private void traverseClassHierarchy(Class clazz, Consumer<Class> handler) {
      if (clazz == null || clazz.equals(Object.class)) {
        return;
      }

      traverseClassHierarchy(clazz.getSuperclass(), handler);
      handler.accept(clazz);
    }
  }

  private class OrganizationTypeListener implements TypeListener {
    private final Provider<Injector> injectorProvider;

    public OrganizationTypeListener(Provider<Injector> injector) {
      this.injectorProvider = injector;
    }

    @Override
    public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
      Class<?> clazz = type.getRawType();
      for (Field field : clazz.getDeclaredFields()) {
        if (field.getType() == TestOrganization.class
            && field.isAnnotationPresent(InjectTestOrganization.class)) {
          encounter.register(
              new TestOrganizationInjector<>(
                  field, field.getAnnotation(InjectTestOrganization.class), injectorProvider));
        }
      }
    }
  }
}
