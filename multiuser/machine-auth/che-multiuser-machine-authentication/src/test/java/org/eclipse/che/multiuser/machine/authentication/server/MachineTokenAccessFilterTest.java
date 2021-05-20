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
package org.eclipse.che.multiuser.machine.authentication.server;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Collections;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.workspace.server.WorkspaceService;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.multiuser.api.permission.server.AuthorizedSubject;
import org.everrest.core.impl.resource.PathValue;
import org.everrest.core.resource.GenericResourceMethod;
import org.everrest.core.resource.ResourceDescriptor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class MachineTokenAccessFilterTest {

  @Mock EnvironmentContext environmentContext;

  @Mock GenericResourceMethod genericMethodResource;

  @Mock MachineTokenAuthorizedSubject machineTokenAuthorizedSubject;

  @Mock AuthorizedSubject authorizedSubject;

  MachineTokenAccessFilter filter;

  @BeforeMethod
  private void setUp() {
    filter =
        new MachineTokenAccessFilter(
            Collections.singleton(new MachineAuthenticatedResource("/workspace", "getByKey")));
  }

  @Test
  public void shouldNotLimitAccessIfSubjectIsNotMachineAuthorized() throws Exception {
    when(environmentContext.getSubject()).thenReturn(authorizedSubject);
    EnvironmentContext.setCurrent(environmentContext);
    filter.filter(genericMethodResource, new Object[] {});
    verifyZeroInteractions(genericMethodResource);
  }

  @Test(expectedExceptions = ForbiddenException.class)
  public void shouldLimitAccessIfMethodIsNotAllowed() throws Exception {
    when(environmentContext.getSubject()).thenReturn(machineTokenAuthorizedSubject);
    EnvironmentContext.setCurrent(environmentContext);
    Method method = WorkspaceService.class.getMethod("getServiceDescriptor");
    ResourceDescriptor descriptor = mock(ResourceDescriptor.class);
    PathValue pathValue = mock(PathValue.class);

    when(genericMethodResource.getMethod()).thenReturn(method);
    when(descriptor.getPathValue()).thenReturn(pathValue);
    when(genericMethodResource.getParentResource()).thenReturn(descriptor);
    when(pathValue.getPath()).thenReturn("/workspace");

    filter.filter(genericMethodResource, new Object[] {});
  }
}
