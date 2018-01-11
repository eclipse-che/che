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

import com.google.inject.assistedinject.Assisted;

/**
 * @author Anton Korneta
 * @author Dmytro Nochevnov
 */
public interface TestUserFactory {

  /** Creates new test user with generated password */
  TestUserImpl create(@Assisted("email") String email);

  /** Creates new test user with given e-mail and password */
  TestUserImpl create(@Assisted("email") String email, @Assisted("password") String password);
}
