/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.machine.authentication.server;

import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.everrest.assured.EverrestJetty;
import org.everrest.core.Filter;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.RequestFilter;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;

/**
 * Tests for {@link MachineTokenPermissionsFilter}.
 *
 * @author Max Shaposhnik
 */
@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class MachineTokenPermissionsFilterTest {

    @SuppressWarnings("unused")
    private static final EnvironmentFilter FILTER = new EnvironmentFilter();

    @SuppressWarnings("unused")
    @InjectMocks
    MachineTokenPermissionsFilter permissionsFilter;

    @Mock
    private static Subject subject;

    @Mock
    MachineTokenService service;

    /*
    @Test
    public void shouldCheckPermissionsOnGettingMachineById() throws Exception {

        when(subject.hasPermission(eq(DOMAIN_ID), eq("workspace123"), eq(USE))).thenReturn(true);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .when()
                                         .get(SECURE_PATH + "/machine/token/workspace123");

        assertEquals(response.getStatusCode(), 204);
        verify(service).getMachineToken(eq("workspace123"));
        verify(subject).checkPermission(DOMAIN_ID, "workspace123", USE);
    }

    @Test
    public void shouldSkipGetUserMethod() throws Exception {

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .when()
                                         .get(SECURE_PATH + "/machine/token/user/user123");

        verify(subject, never()).checkPermission(anyString(), anyString(), anyString());
    }

    @Test
    public void shouldThrowExceptionWhenUpdatingNotOwnedWorkspace() throws Exception {

        when(subject.hasPermission(eq(DOMAIN_ID), eq("workspace123"), eq(USE))).thenReturn(false);
        doThrow(new ForbiddenException("The user does not have permission to " + USE + " workspace with id 'workspace123'"))
                .when(subject).checkPermission(anyString(), anyString(), anyString());

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .when()
                                         .get(SECURE_PATH + "/machine/token/workspace123");

        assertEquals(response.getStatusCode(), 403);
    }



    @Test(expectedExceptions = ForbiddenException.class)
    public void shouldThrowExceptionWhenCallingUnlistedMethod() throws Exception {

        GenericResourceMethod genericResourceMethod = Mockito.mock(GenericResourceMethod.class);
        when(genericResourceMethod.getMethod()).thenReturn(this.getClass().getDeclaredMethod("shouldThrowExceptionWhenCallingUnlistedMethod"));
        Object[] argument = new Object[0];
        permissionsFilter.filter(genericResourceMethod, argument);
    }
    */
    @Filter
    public static class EnvironmentFilter implements RequestFilter {
        public void doFilter(GenericContainerRequest request) {
            EnvironmentContext.getCurrent().setSubject(subject);
        }
    }
}
