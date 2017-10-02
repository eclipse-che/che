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

import com.google.inject.assistedinject.Assisted;
import java.util.Objects;
import javax.inject.Inject;

/** @author Dmytro Nochevnov */
public class TestUserHttpJsonRequestFactory extends TestHttpJsonRequestFactory {

  private final String authToken;

  @Inject
  public TestUserHttpJsonRequestFactory(@Assisted String authToken) {
    Objects.requireNonNull(authToken);
    this.authToken = authToken;
  }

  protected String getAuthToken() {
    return this.authToken;
  }
}
