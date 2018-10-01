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
package org.eclipse.che.multiuser.machine.authentication.server;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import java.util.Set;
import javax.inject.Inject;
import javax.ws.rs.Path;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.everrest.CheMethodInvokerFilter;
import org.everrest.core.Filter;
import org.everrest.core.resource.GenericResourceMethod;

/**
 * Limits set of methods which can be invoked using machine token signed requests.
 *
 * @author Max Shaposhnik (mshaposh@redhat.com)
 */
@Filter
@Path("/{path:.*}")
public class MachineTokenAccessFilter extends CheMethodInvokerFilter {

  private final SetMultimap<String, String> allowedMethodsByPath = HashMultimap.create();

  @Inject
  public MachineTokenAccessFilter(Set<MachineAuthenticatedResource> resources) {
    for (MachineAuthenticatedResource resource : resources) {
      allowedMethodsByPath.putAll(resource.getServicePath(), resource.getMethodNames());
    }
  }

  @Override
  protected void filter(GenericResourceMethod genericMethodResource, Object[] arguments)
      throws ForbiddenException {
    if (!(EnvironmentContext.getCurrent().getSubject() instanceof MachineTokenAuthorizedSubject)) {
      return;
    }
    if (!allowedMethodsByPath
        .get(genericMethodResource.getParentResource().getPathValue().getPath())
        .contains(genericMethodResource.getMethod().getName())) {
      throw new ForbiddenException("This operation cannot be performed using machine token.");
    }
  }
}
