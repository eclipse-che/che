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
package org.eclipse.che.selenium.core.workspace;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.MembersInjector;
import com.google.inject.name.Names;
import java.lang.reflect.Field;
import org.eclipse.che.selenium.core.user.DefaultTestUser;

/**
 * Injector for custom annotation {@link InjectTestWorkspace}.
 *
 * @author Anatolii Bazko
 */
public abstract class AbstractTestWorkspaceInjector<T> implements MembersInjector<T> {
  protected final Field field;
  protected final InjectTestWorkspace injectTestWorkspace;
  protected final Injector injector;

  public AbstractTestWorkspaceInjector(
      Field field, InjectTestWorkspace injectTestWorkspace, Injector injector) {
    this.field = field;
    this.injectTestWorkspace = injectTestWorkspace;
    this.injector = injector;
  }

  @Override
  public void injectMembers(T instance) throws RuntimeException {
    try {
      TestWorkspace testWorkspace =
          getTestWorkspaceProvider()
              .createWorkspace(
                  getUser(instance),
                  getMemory(),
                  injectTestWorkspace.template(),
                  injectTestWorkspace.startAfterCreation());
      testWorkspace.await();
      field.setAccessible(true);
      field.set(instance, testWorkspace);
    } catch (Exception e) {
      throw new RuntimeException("Could not inject workspace. ", e);
    }
  }

  protected abstract TestWorkspaceProvider getTestWorkspaceProvider();

  protected int getMemory() {
    int workspaceDefaultMemoryGb =
        injector.getInstance(Key.get(int.class, Names.named("workspace.default_memory_gb")));
    return injectTestWorkspace.memoryGb() <= 0
        ? workspaceDefaultMemoryGb
        : injectTestWorkspace.memoryGb();
  }

  protected DefaultTestUser getUser(T instance) {
    return isNullOrEmpty(injectTestWorkspace.user())
        ? injector.getInstance(DefaultTestUser.class)
        : findInjectedUser(instance, injectTestWorkspace.user());
  }

  protected DefaultTestUser findInjectedUser(T instance, String userEmail) {
    for (Field field : instance.getClass().getDeclaredFields()) {
      if (field.getType() == DefaultTestUser.class) {
        field.setAccessible(true);

        DefaultTestUser testUser;
        try {
          testUser = (DefaultTestUser) field.get(instance);
        } catch (IllegalAccessException e) {
          throw new IllegalArgumentException(e);
        }

        if (testUser != null && testUser.getEmail().equals(userEmail)) {
          return testUser;
        }
      }
    }

    throw new IllegalArgumentException(
        format(
            "User %s not found or isn't instantiated in %s",
            userEmail, instance.getClass().getName()));
  }
}
