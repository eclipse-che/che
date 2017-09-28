/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.machine.authentication.server;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.hc.HttpConnectionServerChecker;
import org.eclipse.che.api.workspace.server.hc.ServerCheckerFactoryImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.env.EnvironmentContext;

/**
 * //TODO Doc //TODO Mb move it to another module
 *
 * @author Sergii Leshchenko
 */
public class AuthServerCheckerFactoryImpl extends ServerCheckerFactoryImpl {

  private final MachineTokenRegistry machineTokenRegistry;

  @Inject
  public AuthServerCheckerFactoryImpl(MachineTokenRegistry machineTokenRegistry) {
    this.machineTokenRegistry = machineTokenRegistry;
  }

  @Override
  public HttpConnectionServerChecker httpChecker(
      URL url, RuntimeIdentity runtimeIdentity, String machineName, String serverRef, Timer timer)
      throws InfrastructureException {

    try {
      URL newUrl =
          UriBuilder.fromUri(url.toString())
              .queryParam(
                  "token",
                  machineTokenRegistry.getOrCreateToken(
                      EnvironmentContext.getCurrent().getSubject().getUserId(),
                      runtimeIdentity.getWorkspaceId()))
              .build()
              .toURL();

      return super.httpChecker(newUrl, runtimeIdentity, machineName, serverRef, timer);
    } catch (NotFoundException | MalformedURLException e) {
      throw new InfrastructureException(e.getMessage());
    }
  }
}
