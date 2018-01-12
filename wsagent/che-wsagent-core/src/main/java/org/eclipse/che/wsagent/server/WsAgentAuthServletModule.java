/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
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
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.multiuser.machine.authentication.agent.MachineLoginFilter;

/** Provide bindings of security && authentication filters necessary for multi-user Che */
@DynaModule
public class WsAgentAuthServletModule extends ServletModule {
  @Override
  protected void configureServlets() {
    if (Boolean.valueOf(System.getenv("CHE_AUTH_ENABLED"))) {
      configureMultiUserMode();
    } else {
      configureSingleUserMode();
    }
  }

  private void configureMultiUserMode() {
    filter("/*").through(MachineLoginFilter.class);
  }

  private void configureSingleUserMode() {
    filter("/*").through(org.eclipse.che.EnvironmentInitializationFilter.class);
  }
}
