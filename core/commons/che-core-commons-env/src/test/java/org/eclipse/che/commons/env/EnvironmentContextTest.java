/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.commons.env;

import org.eclipse.che.commons.user.User;
import org.eclipse.che.commons.user.UserImpl;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class EnvironmentContextTest {

    @Test
    public void shouldBeAbleToSetEnvContextInSameThread() {
        //given
        EnvironmentContext expected = EnvironmentContext.getCurrent();
        expected.setWorkspaceId("ws1");
        expected.setWorkspaceTemporary(true);
        expected.setUser(new UserImpl("user", "id", "token", Collections.singleton("role"), false));

        EnvironmentContext actual = EnvironmentContext.getCurrent();
        assertEquals(actual.getWorkspaceId(), "ws1");
        assertTrue(actual.isWorkspaceTemporary());
        User actualUser = actual.getUser();
        assertEquals(actualUser.getName(), "user");
        assertEquals(actualUser.getId(), "id");
        assertEquals(actualUser.getToken(), "token");
        assertTrue(actualUser.isMemberOf("role"));
        assertFalse(actualUser.isTemporary());
    }

    @Test(enabled = false)
    public void shouldNotBeAbleToSeeContextInOtherThread() {
        //given
        final EnvironmentContext expected = EnvironmentContext.getCurrent();
        expected.setWorkspaceId("ws1");
        expected.setWorkspaceTemporary(true);
        expected.setUser(new UserImpl("user", "id", "token", Collections.singleton("role"), false));



        Thread otherThread = new Thread(){
            @Override
            public void run() {
                EnvironmentContext.getCurrent();
            }
        };

    }

}
