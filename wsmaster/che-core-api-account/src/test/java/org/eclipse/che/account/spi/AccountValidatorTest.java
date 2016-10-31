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
package org.eclipse.che.account.spi;

import org.eclipse.che.account.api.AccountManager;
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
 * Tests of {@link AccountValidator}.
 *
 * @author Mihail Kuznyetsov
 * @author Yevhenii Voevodin
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class AccountValidatorTest {

    @Mock
    private AccountManager accountManager;

    @InjectMocks
    private AccountValidator accountValidator;

    @Test(dataProvider = "normalizeNames")
    public void testNormalizeAccountName(String input, String expected) throws Exception {
        doThrow(NotFoundException.class).when(accountManager).getByName(anyString());

        Assert.assertEquals(accountValidator.normalizeAccountName(input, "account"), expected);
    }

    @Test
    public void testNormalizeAccountNameWhenInputDoesNotContainAnyValidCharacter() throws Exception {
        doThrow(NotFoundException.class).when(accountManager).getByName(anyString());

        Assert.assertTrue(accountValidator.normalizeAccountName("#", "name").startsWith("name"));
    }

    @Test(dataProvider = "validNames")
    public void testValidUserName(String input, boolean expected) throws Exception {
        doThrow(NotFoundException.class).when(accountManager).getByName(anyString());

        Assert.assertEquals(accountValidator.isValidName(input), expected);
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
