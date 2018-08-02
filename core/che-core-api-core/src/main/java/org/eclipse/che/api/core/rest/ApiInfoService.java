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

import static java.util.Objects.requireNonNull;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import java.io.File;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.annotations.OPTIONS;
import org.eclipse.che.api.core.rest.shared.dto.ApiInfo;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.dto.server.DtoFactory;
import org.everrest.core.ObjectFactory;
import org.everrest.core.ResourceBinder;
import org.everrest.core.resource.ResourceDescriptor;
import org.everrest.services.RestServicesList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author andrew00x */
@Path("/")
@Singleton
public class ApiInfoService {
  private static final Logger LOG = LoggerFactory.getLogger(ApiInfoService.class);

  private volatile ApiInfo apiInfo;

  @OPTIONS
  public ApiInfo info() throws ServerException {
    ApiInfo myApiInfo = apiInfo;
    if (myApiInfo == null) {
      apiInfo = myApiInfo = readApiInfo();
    }
    return myApiInfo;
  }

  private ApiInfo readApiInfo() throws ServerException {
    try {
      URL url = ApiInfoService.class.getProtectionDomain().getCodeSource().getLocation();
      try (JarFile jar = new JarFile(new File(url.toURI()))) {
        final Manifest manifest = requireNonNull(jar.getManifest(), "Manifest must not be null");
        final Attributes mainAttributes = manifest.getMainAttributes();
        final DtoFactory dtoFactory = DtoFactory.getInstance();
        return dtoFactory
            .createDto(ApiInfo.class)
            .withSpecificationVendor(mainAttributes.getValue("Specification-Vendor"))
            .withImplementationVendor(mainAttributes.getValue("Implementation-Vendor"))
            .withSpecificationTitle("Codenvy REST API")
            .withSpecificationVersion(mainAttributes.getValue("Specification-Version"))
            .withImplementationVersion(mainAttributes.getValue("Implementation-Version"))
            .withScmRevision(mainAttributes.getValue("SCM-Revision"));
      }
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
      throw new ServerException("Unable read info about API. Contact support for assistance.");
    }
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  public RestServicesList.RootResourcesList listJSON(@Context ServletContext context) {
    ResourceBinder binder = (ResourceBinder) context.getAttribute(ResourceBinder.class.getName());
    return new RestServicesList.RootResourcesList(
        FluentIterable.from(binder.getResources())
            .transform(
                new Function<ObjectFactory<ResourceDescriptor>, RestServicesList.RootResource>() {
                  @Nullable
                  @Override
                  public RestServicesList.RootResource apply(
                      ObjectFactory<ResourceDescriptor> input) {
                    ResourceDescriptor descriptor = input.getObjectModel();
                    return new RestServicesList.RootResource(
                        descriptor.getObjectClass().getName(), //
                        descriptor.getPathValue().getPath(), //
                        descriptor.getUriPattern().getRegex());
                  }
                })
            .toList());
  }
}
