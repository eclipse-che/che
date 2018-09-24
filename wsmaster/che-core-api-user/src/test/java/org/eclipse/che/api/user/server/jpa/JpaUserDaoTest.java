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
package org.eclipse.che.api.user.server.jpa;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link JpaUserDao}.
 *
 * @author Igor Vinokur
 */
@Listeners(MockitoTestNGListener.class)
public class JpaUserDaoTest {

  @Mock Provider managerProvider;
  @Mock TypedQuery typedQuery;

  @Spy @InjectMocks JpaUserDao userDao;

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
  public void shouldNotThrowExceptionOnGetAllWithMaximumIntegerValueAsSkipCountParameter()
      throws Exception {
    when(typedQuery.getResultList()).thenReturn(Collections.emptyList());
    doReturn(1L).when(userDao).getTotalCount();

    userDao.getAll(30, Integer.MAX_VALUE);

    verify(typedQuery).setMaxResults(eq(30));
    verify(typedQuery).setFirstResult(eq(Integer.MAX_VALUE));
  }

  @Test(
      expectedExceptions = IllegalArgumentException.class,
      expectedExceptionsMessageRegExp =
          "The number of items to skip can't be negative or greater than 2147483647")
  public void shouldThrowExceptionOnGetAllWithGraterThanMaximumIntegerValueAsSkipCountParameter()
      throws Exception {
    userDao.getAll(30, Integer.MAX_VALUE + 1L);
  }
}
