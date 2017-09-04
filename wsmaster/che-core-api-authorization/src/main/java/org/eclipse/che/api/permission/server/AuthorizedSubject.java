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
package org.eclipse.che.api.permission.server;

import static java.lang.String.format;

import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.commons.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sets up implementation of {@link Subject} that can check permissions by {@link PermissionChecker}
 *
 * @author Sergii Leschenko
 */
public class AuthorizedSubject implements Subject {
    private final Subject baseSubject;

    public AuthorizedSubject(Subject baseSubject) {
      this.baseSubject = baseSubject;
    }

    @Override
    public String getUserName() {
      return baseSubject.getUserName();
    }

    @Override
    public boolean hasPermission(String domain, String instance, String action) {
      try {
        return permissionChecker.hasPermission(getUserId(), domain, instance, action);
      } catch (NotFoundException nfe) {
        return false;
      } catch (ServerException | ConflictException e) {
        LOG.error(
            format(
                "Can't check permissions for user '%s' and instance '%s' of domain '%s'",
                getUserId(), domain, instance),
            e);
        throw new RuntimeException("Can't check user's permissions", e);
      }
    }

    @Override
    public void checkPermission(String domain, String instance, String action)
        throws ForbiddenException {
      if (!hasPermission(domain, instance, action)) {
        String message = "User is not authorized to perform " + action + " of " + domain;
        if (instance != null) {
          message += " with id '" + instance + "'";
        }
        throw new ForbiddenException(message);
      }
    }

    @Override
    public String getToken() {
      return baseSubject.getToken();
    }

    @Override
    public String getUserId() {
      return baseSubject.getUserId();
    }

    @Override
    public boolean isTemporary() {
      return baseSubject.isTemporary();
    }
  }
}
