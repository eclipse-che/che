/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.api.authentication.commons;

import java.security.Principal;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import org.eclipse.che.commons.subject.Subject;

/**
 * Wraps {@link HttpServletRequest} and overrides {@link HttpServletRequest#getRemoteUser} and
 * {@link HttpServletRequest#getUserPrincipal} to return an values based on current {@link Subject}
 * which is calculated during request authentication process.
 */
public class SubjectHttpRequestWrapper extends HttpServletRequestWrapper {

  private final Principal principal;

  public SubjectHttpRequestWrapper(HttpServletRequest request, Subject subject) {
    super(request);
    this.subject = subject;
    this.principal = subject::getUserName;
  }

  private final Subject subject;

  @Override
  public String getRemoteUser() {
    return subject.getUserName();
  }

  @Override
  public Principal getUserPrincipal() {
    return principal;
  }
}
