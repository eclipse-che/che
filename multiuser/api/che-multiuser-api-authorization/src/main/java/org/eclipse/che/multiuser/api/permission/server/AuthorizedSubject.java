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
package org.eclipse.che.multiuser.api.permission.server;

import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;

import java.util.Map;
import java.util.Set;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.multiuser.api.permission.shared.model.PermissionsDomain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sets up implementation of {@link Subject} that can check permissions.
 *
 * @author Sergii Leschenko
 */
public class AuthorizedSubject implements Subject {

  private static final Logger LOG = LoggerFactory.getLogger(AuthorizedSubject.class);

  private final Subject baseSubject;
  private final PermissionChecker permissionChecker;
  /** An {@link PermissionsDomain} ID / instance ID set map to which current user auth scope limited to. */
  private final Map<String, Set<String>> limitingScopes;


  public AuthorizedSubject(Subject baseSubject, PermissionChecker permissionChecker) {
    this.baseSubject = baseSubject;
    this.permissionChecker = permissionChecker;
    this.limitingScopes = emptyMap();
  }

  public AuthorizedSubject(Subject baseSubject, PermissionChecker permissionChecker,
      Map<String, Set<String>> limitingScopes) {
    this.baseSubject = baseSubject;
    this.permissionChecker = permissionChecker;
    this.limitingScopes = limitingScopes !=null ? unmodifiableMap(limitingScopes) : emptyMap();
  }

  @Override
  public String getUserName() {
    return baseSubject.getUserName();
  }

  @Override
  public boolean hasPermission(String domain, String instance, String action) {
    if (limitingScopes.get(domain) != null && !limitingScopes.get(domain).contains(instance)) {
      return false;
    }
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
