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
package org.eclipse.che.selenium.core.client;

import com.google.inject.assistedinject.Assisted;

/** @author Dmytro Nochevnov */
public interface TestUserServiceClientFactory {
  TestUserServiceClientImpl create(
      @Assisted("name") String name,
      @Assisted("password") String password,
      @Assisted("offlineToken") String offlineToken);
}
