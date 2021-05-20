/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.security.Principal;
import javax.servlet.http.HttpServletRequest;
import org.eclipse.che.commons.subject.Subject;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(value = MockitoTestNGListener.class)
public class SubjectHttpRequestWrapperTest {

  private final String userName = "userName";
  @Mock private Subject subject;

  @Mock private HttpServletRequest servletRequest;

  private SubjectHttpRequestWrapper subjectHttpRequestWrapper;

  @BeforeMethod
  public void setUp() throws Exception {
    when(subject.getUserName()).thenReturn(userName);
    this.subjectHttpRequestWrapper = new SubjectHttpRequestWrapper(servletRequest, subject);
  }

  @Test
  public void shouldReturnRemoteUserFromSubject() {
    String user = subjectHttpRequestWrapper.getRemoteUser();
    verify(subject).getUserName();
    verifyNoMoreInteractions(subject);
    assertEquals(user, userName);
  }

  @Test
  public void shouldConstructPrincipalFromSubject() {
    Principal principal = subjectHttpRequestWrapper.getUserPrincipal();
    assertEquals(principal.getName(), userName);
    verify(subject).getUserName();
    verifyNoMoreInteractions(subject);
  }
}
