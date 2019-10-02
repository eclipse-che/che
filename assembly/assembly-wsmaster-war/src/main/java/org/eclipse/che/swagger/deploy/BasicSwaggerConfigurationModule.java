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
package org.eclipse.che.swagger.deploy;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import com.google.inject.servlet.ServletModule;
import java.io.InputStream;
import java.util.jar.Manifest;
import org.slf4j.LoggerFactory;

/**
 * Module provide basic swagger configuration
 *
 * @author Sergii Kabashnyuk
 */
public class BasicSwaggerConfigurationModule extends ServletModule {
  private static final org.slf4j.Logger LOG =
      LoggerFactory.getLogger(BasicSwaggerConfigurationModule.class);

  @Override
  protected void configureServlets() {
    bind(io.swagger.jaxrs.config.DefaultJaxrsConfig.class).asEagerSingleton();

    serve("/swaggerinit")
        .with(
            io.swagger.jaxrs.config.DefaultJaxrsConfig.class,
            ImmutableMap.of(
                "api.version", getCheVersion(),
                "swagger.api.title", "Eclipse Che",
                "swagger.api.basepath", "/api"));
  }

  private String getCheVersion() {
    try {;
      try (InputStream manifestInputStream =
          Resources.asByteSource(Resources.getResource("/META-INF/MANIFEST.MF")).openStream()) {
        final Manifest manifest = new Manifest(manifestInputStream);
        return manifest.getMainAttributes().getValue("Specification-Version");
      }
    } catch (Exception e) {
      LOG.error("Unable to retrieve implementation version from manifest file", e);
      return "unknown";
    }
  }
}
