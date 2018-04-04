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
import org.eclipse.che.api.core.cors.CheCorsFilter;
import org.eclipse.che.inject.DynaModule;
import org.everrest.guice.servlet.GuiceEverrestServlet;

/**
 * Add che specific servlet binding. General binding that may be reused by other basic assembly
 * should go into @{@link WsAgentServletModule}
 *
 * <p>Other basic assembly may override this file by excluding it from packaging.
 *
 * @author Sergii Kabashniuk
 */
@DynaModule
public class CheWsAgentServletModule extends ServletModule {
  @Override
  protected void configureServlets() {
    filter("/*").through(CheCorsFilter.class);
    serveRegex("^/api((?!(/(ws|eventbus)($|/.*)))/.*)").with(GuiceEverrestServlet.class);
  }
}
