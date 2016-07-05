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
package org.eclipse.che.api.user.server;

import org.eclipse.che.api.core.NotFoundException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;

/**
 * Tests of {@link UserValidator}.
 *
 * @author Mihail Kuznyetsov
 * @author Yevhenii Voevodin
 */
@Listeners(MockitoTestNGListener.class)
public class UserValidatorTest {

    @Mock
    private UserManager userManager;

    @InjectMocks
    private UserValidator userNameValidator;

    @Test(dataProvider = "normalizeNames")
    public void testNormalizeUserName(String input, String expected) throws Exception {
        doThrow(NotFoundException.class).when(userManager).getByName(anyString());

        Assert.assertEquals(userNameValidator.normalizeUserName(input), expected);
    }


    @Test(dataProvider = "validNames")
    public void testValidUserName(String input, boolean expected) throws Exception {
        doThrow(NotFoundException.class).when(userManager).getByName(anyString());

        Assert.assertEquals(userNameValidator.isValidName(input), expected);
    }

    @DataProvider(name = "normalizeNames")
    public Object[][] normalizeNames() {
        return new Object[][] {{"test", "test"},
                               {"test123", "test123"},
                               {"test 123", "test123"},
                               {"test@gmail.com", "testgmailcom"},
                               {"TEST", "TEST"},
                               {"test-", "test"},
                               {"te-st", "test"},
                               {"-test", "test"},
                               {"te_st", "test"},
                               {"te#st", "test"}
        };
    }

    @DataProvider(name = "validNames")
    public Object[][] validNames() {
        return new Object[][] {{"test", true},
                               {"test123", true},
                               {"test 123", false},
                               {"test@gmail.com", false},
                               {"TEST", true},
                               {"test-", false},
                               {"te-st", false},
                               {"-test", false},
                               {"te_st", false},
                               {"te#st", false}
        };
    }
}
