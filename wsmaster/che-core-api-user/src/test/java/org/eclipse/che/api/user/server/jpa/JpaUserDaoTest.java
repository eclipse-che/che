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
package org.eclipse.che.api.user.server.jpa;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.Collections;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link JpaUserDao}.
 *
 * @author Igor Vinokur
 */
@Listeners(MockitoTestNGListener.class)
public class JpaUserDaoTest {

    @Mock
    Provider   managerProvider;
    @Mock
    TypedQuery typedQuery;

    @Spy
    @InjectMocks
    JpaUserDao userDao;

    @BeforeMethod
    public void setup() throws Exception {
        EntityManager entityManager = mock(EntityManager.class);
        when(entityManager.createNamedQuery(anyString(), anyObject())).thenReturn(typedQuery);
        when(managerProvider.get()).thenReturn(entityManager);
        when(typedQuery.setMaxResults(anyInt())).thenReturn(typedQuery);
        when(typedQuery.setFirstResult(anyInt())).thenReturn(typedQuery);
        when(typedQuery.setFirstResult(anyInt())).thenReturn(typedQuery);
    }

    @Test
    public void shouldNotThrowExceptionOnGetAllWithMaximumIntegerValueAsSkipCountParameter() throws Exception {
        when(typedQuery.getResultList()).thenReturn(Collections.emptyList());
        doReturn(1L).when(userDao).getTotalCount();

        userDao.getAll(30, Integer.MAX_VALUE);

        verify(typedQuery).setMaxResults(eq(30));
        verify(typedQuery).setFirstResult(eq(Integer.MAX_VALUE));
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "The number of items to skip can't be negative or greater than 2147483647")
    public void shouldThrowExceptionOnGetAllWithGraterThanMaximumIntegerValueAsSkipCountParameter() throws Exception {
        userDao.getAll(30, Integer.MAX_VALUE + 1L);
    }
}
