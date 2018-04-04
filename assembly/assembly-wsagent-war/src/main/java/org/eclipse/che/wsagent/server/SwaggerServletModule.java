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

import com.google.common.collect.ImmutableMap;
import com.google.inject.servlet.ServletModule;
import org.eclipse.che.inject.DynaModule;

/** @author Sergii Kabashniuk */
@DynaModule
public class SwaggerServletModule extends ServletModule {
  @Override
  protected void configureServlets() {
    bind(io.swagger.jaxrs.config.DefaultJaxrsConfig.class).asEagerSingleton();
    install(new org.eclipse.che.swagger.deploy.DocsModule());
    serve("/swaggerinit")
        .with(
            io.swagger.jaxrs.config.DefaultJaxrsConfig.class,
            ImmutableMap.of(
                "api.version", "1.0",
                "swagger.api.title", "Eclipse Che",
                "swagger.api.basepath", "/api"));
  }
}
