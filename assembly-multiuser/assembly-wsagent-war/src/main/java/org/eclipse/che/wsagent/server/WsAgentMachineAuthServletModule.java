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
package org.eclipse.che.wsagent.server;

import com.google.inject.servlet.ServletModule;
import org.eclipse.che.api.core.cors.CheCorsFilter;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.multiuser.machine.authentication.agent.MachineLoginFilter;
import org.everrest.guice.servlet.GuiceEverrestServlet;

/** Provide bindings of security && authentication filters necessary for multi-user Che */
@DynaModule
public class WsAgentMachineAuthServletModule extends ServletModule {
  @Override
  protected void configureServlets() {
    filter("/*").through(CheCorsFilter.class);
    filter("/*").through(MachineLoginFilter.class);
    serveRegex("^/api((?!(/(ws|eventbus)($|/.*)))/.*)").with(GuiceEverrestServlet.class);
  }
}
