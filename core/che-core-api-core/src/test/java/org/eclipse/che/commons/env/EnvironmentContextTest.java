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
package org.eclipse.che.commons.env;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.testng.annotations.Test;

public class EnvironmentContextTest {

  @Test
  public void shouldBeAbleToSetEnvContextInSameThread() {
    // given
    EnvironmentContext expected = EnvironmentContext.getCurrent();
    expected.setSubject(new SubjectImpl("user", "id", "token", false));

    EnvironmentContext actual = EnvironmentContext.getCurrent();
    Subject actualSubject = actual.getSubject();
    assertEquals(actualSubject.getUserName(), "user");
    assertEquals(actualSubject.getUserId(), "id");
    assertEquals(actualSubject.getToken(), "token");
    assertFalse(actualSubject.isTemporary());
  }

  @Test
  public void shouldReturnAnonymousSubjectWhenThereIsNoSubject() {
    // given
    EnvironmentContext expected = EnvironmentContext.getCurrent();
    expected.setSubject(null);

    // when
    Subject actualSubject = EnvironmentContext.getCurrent().getSubject();

    // then
    assertEquals(actualSubject.getUserName(), Subject.ANONYMOUS.getUserName());
    assertEquals(actualSubject.getUserId(), Subject.ANONYMOUS.getUserId());
    assertEquals(actualSubject.getToken(), Subject.ANONYMOUS.getToken());
    assertEquals(actualSubject.isTemporary(), Subject.ANONYMOUS.isTemporary());
    assertEquals(actualSubject.isAnonymous(), Subject.ANONYMOUS.isAnonymous());
  }

  @Test(enabled = false)
  public void shouldNotBeAbleToSeeContextInOtherThread() {
    // given
    final EnvironmentContext expected = EnvironmentContext.getCurrent();
    expected.setSubject(new SubjectImpl("user", "id", "token", false));

    Thread otherThread =
        new Thread() {
          @Override
          public void run() {
            EnvironmentContext.getCurrent();
          }
        };
  }
}
