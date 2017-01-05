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
package org.eclipse.che.commons.env;

import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

public class EnvironmentContextTest {

    @Test
    public void shouldBeAbleToSetEnvContextInSameThread() {
        //given
        EnvironmentContext expected = EnvironmentContext.getCurrent();
        expected.setSubject(new SubjectImpl("user", "id", "token", false));

        EnvironmentContext actual = EnvironmentContext.getCurrent();
        Subject actualSubject = actual.getSubject();
        assertEquals(actualSubject.getUserName(), "user");
        assertEquals(actualSubject.getUserId(), "id");
        assertEquals(actualSubject.getToken(), "token");
        assertFalse(actualSubject.isTemporary());
    }

    @Test(enabled = false)
    public void shouldNotBeAbleToSeeContextInOtherThread() {
        //given
        final EnvironmentContext expected = EnvironmentContext.getCurrent();
        expected.setSubject(new SubjectImpl("user", "id", "token", false));


        Thread otherThread = new Thread() {
            @Override
            public void run() {
                EnvironmentContext.getCurrent();
            }
        };

    }

}
