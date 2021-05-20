/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.permission.workspace.infra.kubernetes;

import static org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.events.BrokerService.BROKER_RESULT_METHOD;
import static org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.events.BrokerService.BROKER_STATUS_CHANGED_METHOD;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerManager;
import org.eclipse.che.api.workspace.shared.dto.RuntimeIdentityDto;
import org.eclipse.che.api.workspace.shared.dto.event.BrokerStatusChangedEvent;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.multiuser.permission.workspace.server.WorkspaceDomain;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link BrokerServicePermissionFilter}
 *
 * @author Sergii Leshchenko
 */
@Listeners(MockitoTestNGListener.class)
public class BrokerServicePermissionFilterTest {

  @Mock private RequestHandlerManager requestHandlerManager;

  @Mock private Subject subject;

  private BrokerServicePermissionFilter permissionFilter;

  @BeforeMethod
  public void setUp() {
    EnvironmentContext.getCurrent().setSubject(subject);
    permissionFilter = new BrokerServicePermissionFilter();
  }

  @AfterMethod
  public void tearDown() {
    EnvironmentContext.reset();
  }

  @Test
  public void shouldRegisterItself() {
    // when
    permissionFilter.register(requestHandlerManager);

    // then
    requestHandlerManager.registerMethodInvokerFilter(
        permissionFilter, BROKER_STATUS_CHANGED_METHOD, BROKER_RESULT_METHOD);
  }

  @Test(
      dataProvider = "coveredMethods",
      expectedExceptions = ForbiddenException.class,
      expectedExceptionsMessageRegExp =
          "User doesn't have the required permissions to the specified workspace")
  public void shouldThrowExceptionIfUserDoesNotHaveRunPermission(String method) throws Exception {
    // given
    when(subject.hasPermission(eq(WorkspaceDomain.DOMAIN_ID), eq("ws123"), eq(WorkspaceDomain.RUN)))
        .thenReturn(false);

    // when
    permissionFilter.doAccept(
        method,
        DtoFactory.newDto(BrokerStatusChangedEvent.class)
            .withRuntimeId(DtoFactory.newDto(RuntimeIdentityDto.class).withWorkspaceId("ws123")));
  }

  @Test(dataProvider = "coveredMethods")
  public void shouldDoNothingIfUserHasRunPermissions(String method) throws Exception {
    // given
    when(subject.hasPermission(WorkspaceDomain.DOMAIN_ID, "ws123", WorkspaceDomain.RUN))
        .thenReturn(true);

    // when
    permissionFilter.doAccept(
        method,
        DtoFactory.newDto(BrokerStatusChangedEvent.class)
            .withRuntimeId(DtoFactory.newDto(RuntimeIdentityDto.class).withWorkspaceId("ws123")));
  }

  @Test(
      expectedExceptions = ForbiddenException.class,
      expectedExceptionsMessageRegExp = "Unknown method is configured to be filtered\\.")
  public void shouldThrowExceptionIfUnknownMethodIsInvoking() throws Exception {
    // when
    permissionFilter.doAccept(
        "unknown",
        DtoFactory.newDto(BrokerStatusChangedEvent.class)
            .withRuntimeId(DtoFactory.newDto(RuntimeIdentityDto.class).withWorkspaceId("ws123")));
  }

  @DataProvider
  public Object[][] coveredMethods() {
    return new Object[][] {{BROKER_STATUS_CHANGED_METHOD}, {BROKER_RESULT_METHOD}};
  }
}
