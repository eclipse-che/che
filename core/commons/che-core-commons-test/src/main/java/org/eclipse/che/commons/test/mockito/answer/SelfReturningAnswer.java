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
package org.eclipse.che.commons.test.mockito.answer;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Allows create mocks for builder-like classes easily
 *
 * <p>If builder-like classes needs to be mocked then:
 *
 * <ul>
 *   <li>methods of that class returns the same object
 *   <li>not all methods can be called
 *   <li>methods can be called in different order
 * </ul>
 *
 * In that case it is hard to mock that class. With the help of this class it can be achieved as:
 *
 * <pre class="code"><code class="java">
 *     HttpJsonRequest httpJsonRequest = mock(HttpJsonRequest.class, new SelfReturningAnswer());
 * </code></pre>
 *
 * In that case all methods of httpJsonRequest that returns HttpJsonRequest will return
 * httpJsonRequest;
 *
 * @author Alexander Garagatyi
 */
public class SelfReturningAnswer implements Answer<Object> {
  public Object answer(InvocationOnMock invocation) throws Throwable {
    Object mock = invocation.getMock();
    if (invocation.getMethod().getReturnType().isInstance(mock)) {
      return mock;
    } else {
      return Mockito.RETURNS_DEFAULTS.answer(invocation);
    }
  }
}
