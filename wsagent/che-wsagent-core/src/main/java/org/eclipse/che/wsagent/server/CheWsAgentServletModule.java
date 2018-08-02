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
