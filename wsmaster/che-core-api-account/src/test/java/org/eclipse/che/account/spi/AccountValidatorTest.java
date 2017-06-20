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

import static org.mockito.AdditionalMatchers.not;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

/**
 * Tests of {@link AccountValidator}.
 *
 * @author Mihail Kuznyetsov
 * @author Yevhenii Voevodin
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class AccountValidatorTest {

    private final static String LONG_ACCOUNT_NAME = "accountNameThatContainsMoreThan39Characters";

    @Mock
    private AccountManager accountManager;

    @InjectMocks
    private AccountValidator accountValidator;

    @Test(dataProvider = "namesToNormalize")
    public void testNormalizeAccountName(String input, String expected) throws Exception {
        doThrow(NotFoundException.class).when(accountManager).getByName(anyString());

        Assert.assertEquals(accountValidator.normalizeAccountName(input, "account"), expected);
    }

    @Test
    public void testNormalizeAccountNameWhenInputDoesNotContainAnyValidCharacter() throws Exception {
        doThrow(NotFoundException.class).when(accountManager).getByName(anyString());

        assertTrue(accountValidator.normalizeAccountName("#", "name").startsWith("name"));
    }

    @Test
    public void shouldChangeLast4CharactersInAccountNameIfAccountNameContains39CharactersAndAccountAlreadyExists() throws Exception {
        //given
        String accountName = LONG_ACCOUNT_NAME.substring(0, 39);
        doThrow(NotFoundException.class).when(accountManager).getByName(not(eq(accountName)));

        //when
        String normalizedAccountName = accountValidator.normalizeAccountName(accountName, "name");

        //then
        assertTrue(normalizedAccountName.length() == 39);
        assertNotEquals(normalizedAccountName, accountName);
        assertEquals(normalizedAccountName.substring(0, 35), accountName.substring(0, 35));
    }

    @Test(dataProvider = "namesToValidate")
    public void testValidUserName(String input, boolean expected) throws Exception {
        doThrow(NotFoundException.class).when(accountManager).getByName(anyString());

        Assert.assertEquals(accountValidator.isValidName(input), expected);
    }

    @DataProvider
    public Object[][] namesToNormalize() {
        return new Object[][]{{"test", "test"},
                              {"test123", "test123"},
                              {"test 123", "test123"},
                              {"test@gmail.com", "testgmailcom"},
                              {"TEST", "TEST"},
                              {"test-", "test"},
                              {"-test", "test"},
                              {"te_st", "test"},
                              {"te#st", "test"},
                              {"-test", "test"},
                              {"test-", "test"},
                              {"--test--", "test"},
                              {"t-----e--s-t", "t-e-s-t"},
                              {"testTestTestTestTestTestTestTestTestTestTest", "testTestTestTestTestTestTestTestTestTes"}
        };
    }

    @DataProvider
    public Object[][] namesToValidate() {
        return new Object[][]{{"test", true},
                              {"t-e-s-t", true},
                              {"test123", true},
                              {"TEST", true},
                              {"te-st", true},
                              {"test 123", false},
                              {"test@gmail.com", false},
                              {"test-", false},
                              {"-test", false},
                              {"te_st", false},
                              {"te#st", false},
                              {"-test", false},
                              {"test-", false},
                              {"--test--", false},
                              {"te--st", false}
        };
    }
}
