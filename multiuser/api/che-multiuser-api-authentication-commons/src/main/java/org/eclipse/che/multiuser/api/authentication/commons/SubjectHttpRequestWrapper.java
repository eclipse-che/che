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
package org.eclipse.che.multiuser.api.authentication.commons;

import java.security.Principal;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import org.eclipse.che.commons.subject.Subject;

public class SubjectHttpRequestWrapper extends HttpServletRequestWrapper {

  public SubjectHttpRequestWrapper(HttpServletRequest request, Subject subject) {
    super(request);
    this.subject = subject;
  }

  private final Subject subject;

  @Override
  public String getRemoteUser() {
    return subject.getUserName();
  }

  @Override
  public Principal getUserPrincipal() {
    return subject::getUserName;
  }
}
