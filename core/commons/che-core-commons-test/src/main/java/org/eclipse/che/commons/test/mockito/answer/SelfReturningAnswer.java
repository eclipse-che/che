/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.commons.test.mockito.answer;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Allows create mocks for builder-like classes easily
 *
 * <p>If builder-like classes needs to be mocked then:
 * <ul>
 * <li>methods of that class returns the same object</li>
 * <li>not all methods can be called</li>
 * <li>methods can be called in different order</li>
 * </ul>
 * In that case it is hard to mock that class.
 * With the help of this class it can be achieved as:
 * <pre class="code"><code class="java">
 *     HttpJsonRequest httpJsonRequest = mock(HttpJsonRequest.class, new SelfReturningAnswer());
 * </code></pre>
 * In that case all methods of httpJsonRequest that returns HttpJsonRequest will return httpJsonRequest;
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
