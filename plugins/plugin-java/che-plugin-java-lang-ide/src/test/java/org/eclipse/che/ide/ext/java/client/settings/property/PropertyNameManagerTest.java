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
package org.eclipse.che.ide.ext.java.client.settings.property;

import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.eclipse.che.ide.ext.java.client.settings.compiler.ErrorWarningsOptions.COMPILER_UNUSED_LOCAL;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(MockitoJUnitRunner.class)
public class PropertyNameManagerTest {

    private static final String SOME_TEXT = "someText";

    @Mock
    private JavaLocalizationConstant locale;

    @InjectMocks
    private PropertyNameManager nameManager;

    @Test
    public void constructorShouldBeVerified() {
        verify(locale).propertyUnusedLocal();
        verify(locale).propertyUnusedImport();
        verify(locale).propertyDeadCode();
        verify(locale).propertyWithConstructorName();
        verify(locale).propertyUnnecessaryElse();
        verify(locale).comparingIdenticalValues();
        verify(locale).noEffectAssignment();
        verify(locale).missingSerialVersionUid();
        verify(locale).typeParameterHideAnotherType();
        verify(locale).fieldHidesAnotherField();
        verify(locale).missingSwitchDefaultCase();
        verify(locale).unusedPrivateMember();
        verify(locale).uncheckedTypeOperation();
        verify(locale).usageRawType();
        verify(locale).missingOverrideAnnotation();
        verify(locale).nullPointerAccess();
        verify(locale).potentialNullPointerAccess();
        verify(locale).redundantNullCheck();
    }

    @Test
    public void parameterNameShouldBeReturned() {
        when(locale.propertyUnusedLocal()).thenReturn(SOME_TEXT);

        PropertyNameManager nameManager = new PropertyNameManager(locale);

        String name = nameManager.getName(COMPILER_UNUSED_LOCAL);

        assertThat(name, equalTo(SOME_TEXT));
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentExceptionShouldBeThrownWhenNameIsNotFound() {
        nameManager.getName(COMPILER_UNUSED_LOCAL);
    }

}