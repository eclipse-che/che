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
package org.eclipse.che.api.core.rest;

import java.io.InputStream;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import org.eclipse.che.api.core.rest.shared.dto.ApiInfo;
import org.eclipse.che.dto.server.DtoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides api info by reading it from war manifest.
 *
 * @author Max Shaposhnyk
 */
@Singleton
public class ApiInfoProvider implements Provider<ApiInfo> {

  private static final Logger LOG = LoggerFactory.getLogger(ApiInfoProvider.class);

  private ApiInfo apiInfo;

  @Inject
  public ApiInfoProvider(@Context ServletContext context) {
    this.apiInfo = readApiInfo(context);
  }

  @Override
  public ApiInfo get() {
    return apiInfo;
  }

  private ApiInfo readApiInfo(ServletContext context) {
    try {
      try (InputStream inputStream = context.getResourceAsStream("/META-INF/MANIFEST.MF")) {
        final Manifest manifest = new Manifest(inputStream);
        final Attributes mainAttributes = manifest.getMainAttributes();
        final DtoFactory dtoFactory = DtoFactory.getInstance();
        return dtoFactory
            .createDto(ApiInfo.class)
            .withSpecificationVendor(mainAttributes.getValue("Specification-Vendor"))
            .withImplementationVendor(mainAttributes.getValue("Implementation-Vendor"))
            .withSpecificationTitle("Che REST API")
            .withSpecificationVersion(mainAttributes.getValue("Specification-Version"))
            .withImplementationVersion(mainAttributes.getValue("Implementation-Version"))
            .withScmRevision(mainAttributes.getValue("SCM-Revision"));
      }
    } catch (Exception e) {
      LOG.error("Unable to read API info. Error: " + e.getMessage(), e);
      throw new RuntimeException("Unable to read API information", e);
    }
  }
}
