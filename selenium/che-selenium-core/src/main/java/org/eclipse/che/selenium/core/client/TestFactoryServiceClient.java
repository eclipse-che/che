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
package org.eclipse.che.selenium.core.client;

import static java.util.Optional.ofNullable;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonResponse;
import org.eclipse.che.api.factory.shared.dto.FactoryDto;
import org.eclipse.che.selenium.core.provider.TestApiEndpointUrlProvider;
import org.eclipse.che.selenium.core.provider.TestIdeUrlProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Musienko Maxim */
@Singleton
public class TestFactoryServiceClient {
  private static final Logger LOG = LoggerFactory.getLogger(TestFactoryServiceClient.class);

  private final String factoryApiEndpoint;
  private final String ideUrl;
  private final HttpJsonRequestFactory requestFactory;

  @Inject
  public TestFactoryServiceClient(
      TestApiEndpointUrlProvider testApiEndpointUrlProvider,
      TestIdeUrlProvider ideUrlProvider,
      HttpJsonRequestFactory requestFactory)
      throws Exception {
    this.factoryApiEndpoint = testApiEndpointUrlProvider.get() + "factory/";
    this.ideUrl = ideUrlProvider.get().toString();
    this.requestFactory = requestFactory;
  }

  /**
   * Creates factory
   *
   * @param createFactoryDto DTO object to create the factory
   * @return URL for the saved factory
   */
  public String createFactory(FactoryDto createFactoryDto) throws Exception {
    HttpJsonResponse request =
        requestFactory
            .fromUrl(factoryApiEndpoint)
            .usePostMethod()
            .setBody(createFactoryDto)
            .request();

    FactoryDto responseDto =
        ofNullable(request.asDto(FactoryDto.class))
            .orElseThrow(() -> new RuntimeException("There is a problem creation of factory."));

    LOG.debug(
        "Factory with name='{}' and id='{}' has been created without errors",
        responseDto.getName(),
        responseDto.getId());

    return String.format("%sf?id=%s", ideUrl, responseDto.getId());
  }

  public String findFactoryIdByName(String name) throws Exception {
    String queryParamPrefix = "find?name=" + name;
    HttpJsonResponse request =
        requestFactory.fromUrl(factoryApiEndpoint + queryParamPrefix).request();
    List<FactoryDto> dtos = request.asList(FactoryDto.class);
    return dtos.isEmpty() ? null : dtos.get(0).getId();
  }

  public void deleteFactoryByName(String name) throws Exception {
    String id = findFactoryIdByName(name);
    if (id != null) {
      requestFactory.fromUrl(factoryApiEndpoint + id).useDeleteMethod().request();
    }
  }
}
