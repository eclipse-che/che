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
package org.eclipse.che.multiuser.permission.factory;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.factory.server.FactoryManager;
import org.eclipse.che.api.factory.server.FactoryService;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.everrest.assured.EverrestJetty;
import org.everrest.core.Filter;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.RequestFilter;
import org.everrest.core.resource.GenericResourceMethod;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link FactoryPermissionsFilter}.
 *
 * @author Sergii Leschenko
 */
@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class FactoryPermissionsFilterTest {

  @SuppressWarnings("unused")
  private static final EnvironmentFilter FILTER = new EnvironmentFilter();

  @Mock private static Subject subject;

  @Mock private FactoryService service;

  @Mock private FactoryManager factoryManager;

  @InjectMocks private FactoryPermissionsFilter permissionsFilter;

  @Test(dataProvider = "publicMethods")
  public void shouldDoNothingWhenPublicMethodMethodIsCalled(String name, Class[] parameterTypes)
      throws Exception {
    GenericResourceMethod genericResourceMethod = mock(GenericResourceMethod.class);
    when(genericResourceMethod.getMethod())
        .thenReturn(FactoryService.class.getMethod(name, parameterTypes));

    permissionsFilter.filter(genericResourceMethod, new Object[0]);
  }

  @DataProvider(name = "publicMethods")
  public Object[][] publicMethods() {
    return new Object[][] {
      {"resolveFactory", new Class[] {Map.class, Boolean.class}},
    };
  }

  @Test(
      expectedExceptions = ForbiddenException.class,
      expectedExceptionsMessageRegExp =
          "The user does not have permission to perform this operation")
  public void shouldThrowForbiddenExceptionWhenUnlistedMethodIsCalled() throws Exception {
    GenericResourceMethod genericResourceMethod = mock(GenericResourceMethod.class);
    when(genericResourceMethod.getMethod())
        .thenReturn(FactoryService.class.getMethod("getServiceDescriptor"));

    permissionsFilter.filter(genericResourceMethod, new Object[0]);
  }

  @Filter
  public static class EnvironmentFilter implements RequestFilter {

    public void doFilter(GenericContainerRequest request) {
      EnvironmentContext.getCurrent().setSubject(subject);
    }
  }
}
