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
package org.eclipse.che.api.user.server;

import org.eclipse.che.account.spi.AccountValidator;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Tests of {@link UserValidator}.
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class UserValidatorTest {

    @Mock
    private AccountValidator accountValidator;

    @InjectMocks
    private UserValidator userNameValidator;

    @Test
    public void shouldReturnNameNormalizedByAccountValidator() throws Exception {
        when(accountValidator.normalizeAccountName(anyString(), anyString())).thenReturn("testname");

        assertEquals(userNameValidator.normalizeUserName("toNormalize"), "testname");
        verify(accountValidator).normalizeAccountName("toNormalize", UserValidator.GENERATED_NAME_PREFIX);
    }

    @Test
    public void shouldReturnTrueWhenInputIsValidAccountName() throws Exception {
        when(accountValidator.isValidName(any())).thenReturn(true);

        assertEquals(userNameValidator.isValidName("toTest"), true);
        verify(accountValidator).isValidName("toTest");
    }

    @Test
    public void shouldReturnFalseWhenInputIsInvalidAccountName() throws Exception {
        when(accountValidator.isValidName(any())).thenReturn(false);

        assertEquals(userNameValidator.isValidName("toTest"), false);
        verify(accountValidator).isValidName("toTest");
    }
}
