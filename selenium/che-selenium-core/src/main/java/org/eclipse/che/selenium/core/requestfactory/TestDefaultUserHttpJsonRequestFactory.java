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
package org.eclipse.che.selenium.core.requestfactory;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.user.DefaultTestUser;

/** @author Dmytro Nochevnov */
@Singleton
public class TestDefaultUserHttpJsonRequestFactory extends TestHttpJsonRequestFactory {
  private Provider<DefaultTestUser> testUserProvider;

  @Inject
  public TestDefaultUserHttpJsonRequestFactory(Provider<DefaultTestUser> testUserProvider) {
    this.testUserProvider = testUserProvider;
  }

  protected String getAuthToken() {
    return testUserProvider.get().getAuthToken();
  }
}
