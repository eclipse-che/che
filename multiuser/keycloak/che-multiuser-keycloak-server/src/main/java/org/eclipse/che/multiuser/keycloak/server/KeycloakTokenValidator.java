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
package org.eclipse.che.multiuser.keycloak.server;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.user.server.TokenValidator;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;

/**
 * Dummy stub for {@link org.eclipse.che.api.user.server.UserService}.
 *
 * @author Max Shaposhnik (mshaposhnik@redhat.com)
 */
public class KeycloakTokenValidator implements TokenValidator {
  @Override
  public User validateToken(String token) throws ConflictException {
    final Subject subject = EnvironmentContext.getCurrent().getSubject();
    return new UserImpl(subject.getUserId(), "", subject.getUserName());
  }
}
