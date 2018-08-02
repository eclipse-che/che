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
package org.eclipse.che.account.spi;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;

import org.eclipse.che.account.api.AccountManager;
import org.eclipse.che.api.core.NotFoundException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests of {@link AccountValidator}.
 *
 * @author Mihail Kuznyetsov
 * @author Yevhenii Voevodin
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class AccountValidatorTest {

  @Mock private AccountManager accountManager;

  @InjectMocks private AccountValidator accountValidator;

  @Test(dataProvider = "namesToNormalize")
  public void testNormalizeAccountName(String input, String expected) throws Exception {
    doThrow(NotFoundException.class).when(accountManager).getByName(anyString());

    Assert.assertEquals(accountValidator.normalizeAccountName(input, "account"), expected);
  }

  @Test
  public void testNormalizeAccountNameWhenInputDoesNotContainAnyValidCharacter() throws Exception {
    doThrow(NotFoundException.class).when(accountManager).getByName(anyString());

    Assert.assertTrue(accountValidator.normalizeAccountName("#", "name").startsWith("name"));
  }

  @Test(dataProvider = "namesToValidate")
  public void testValidUserName(String input, boolean expected) throws Exception {
    doThrow(NotFoundException.class).when(accountManager).getByName(anyString());

    Assert.assertEquals(accountValidator.isValidName(input), expected);
  }

  @DataProvider
  public Object[][] namesToNormalize() {
    return new Object[][] {
      {"test", "test"},
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
      {"t-----e--s-t", "t-e-s-t"}
    };
  }

  @DataProvider
  public Object[][] namesToValidate() {
    return new Object[][] {
      {"test", true},
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
