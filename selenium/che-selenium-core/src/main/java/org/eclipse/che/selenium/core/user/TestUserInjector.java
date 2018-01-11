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
package org.eclipse.che.selenium.core.user;

import com.google.inject.Injector;
import com.google.inject.MembersInjector;
import com.google.inject.Provider;
import java.lang.reflect.Field;

/**
 * Injector for custom annotation {@link InjectTestUser}.
 *
 * @author Anatolii Bazko
 */
public class TestUserInjector<T> implements MembersInjector<T> {
  private final Field field;
  private final InjectTestUser injectTestUser;
  private final Injector injector;

  public TestUserInjector(
      Field field, InjectTestUser injectTestUser, Provider<Injector> injectorProvider) {
    this.field = field;
    this.injectTestUser = injectTestUser;
    this.injector = injectorProvider.get();
  }

  @Override
  public void injectMembers(T t) {
    try {
      field.setAccessible(true);
      field.set(t, injector.getInstance(TestUserFactory.class).create(injectTestUser.value()));
    } catch (Exception e) {
      throw new RuntimeException("Failed to instantiate user in " + t.getClass().getName(), e);
    }
  }
}
