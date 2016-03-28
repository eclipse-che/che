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
package org.eclipse.che.api.factory.server.impl;

import org.eclipse.che.api.account.server.dao.AccountDao;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.factory.shared.dto.Factory;
import org.eclipse.che.api.user.server.dao.PreferenceDao;
import org.eclipse.che.api.user.server.dao.UserDao;

import org.eclipse.che.api.workspace.server.WorkspaceConfigValidator;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link org.eclipse.che.api.factory.server.impl.FactoryAcceptValidatorImpl} and {@link FactoryCreateValidatorImpl}
 */
@Listeners(value = {MockitoTestNGListener.class})
public class FactoryCreateAndAcceptValidatorsImplsTest {

    @Mock
    private AccountDao accountDao;

    @Mock
    private UserDao userDao;

    @Mock
    private PreferenceDao preferenceDao;

    @Mock
    private Factory factory;

    @Mock
    private WorkspaceConfigValidator workspaceConfigValidator;

    private FactoryAcceptValidatorImpl acceptValidator;

    private FactoryCreateValidatorImpl createValidator;


    @BeforeMethod
    public void setUp() throws Exception {

        acceptValidator = new FactoryAcceptValidatorImpl(accountDao, preferenceDao);
        createValidator = new FactoryCreateValidatorImpl(accountDao, preferenceDao, workspaceConfigValidator);
    }

    @Test
    public void testValidateOnCreate() throws ApiException {
        FactoryCreateValidatorImpl spy = spy(createValidator);
        doNothing().when(spy)
                   .validateProjects(any(Factory.class));
        doNothing().when(spy)
                   .validateAccountId(any(Factory.class));
        doNothing().when(spy)
                   .validateCurrentTimeAfterSinceUntil(any(Factory.class));
        doNothing().when(spy)
                   .validateProjectActions(any(Factory.class));
        doNothing().when(workspaceConfigValidator)
                   .validate(any(WorkspaceConfig.class));

        //main invoke
        spy.validateOnCreate(factory);

        verify(spy).validateProjects(any(Factory.class));
        verify(spy).validateAccountId(any(Factory.class));
        verify(spy).validateCurrentTimeAfterSinceUntil(any(Factory.class));
        verify(spy).validateOnCreate(any(Factory.class));
        verify(spy).validateProjectActions(any(Factory.class));
        verifyNoMoreInteractions(spy);
    }



    @Test
    public void testOnAcceptEncoded() throws ApiException {
        FactoryAcceptValidatorImpl spy = spy(acceptValidator);
        doNothing().when(spy).validateCurrentTimeBetweenSinceUntil(any(Factory.class));
        doNothing().when(spy).validateProjectActions(any(Factory.class));

        //main invoke
        spy.validateOnAccept(factory);

        verify(spy).validateCurrentTimeBetweenSinceUntil(any(Factory.class));
        verify(spy).validateOnAccept(any(Factory.class));
        verify(spy).validateProjectActions(any(Factory.class));
        verifyNoMoreInteractions(spy);
    }



}
