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
package org.eclipse.che.api.core.rest.shared.dto;

import java.util.List;
import org.eclipse.che.dto.shared.DTO;
import org.eclipse.che.dto.shared.DelegateRule;
import org.eclipse.che.dto.shared.DelegateTo;

/**
 * Describes resource.
 *
 * @author andrew00x
 */
@DTO
public interface Link {
  /**
   * Get URL of resource link.
   *
   * @return URL of resource link.
   */
  String getHref();

  Link withHref(String href);

  /**
   * Set URL of resource link.
   *
   * @param href URL of resource link.
   */
  void setHref(String href);

  /**
   * Get short description of resource link.
   *
   * @return short description of resource link
   */
  String getRel();

  Link withRel(String rel);

  /**
   * Set short description of resource link.
   *
   * @param rel short description of resource link
   */
  void setRel(String rel);

  /**
   * Get HTTP method to use with resource.
   *
   * @return HTTP method to use with resource
   */
  String getMethod();

  Link withMethod(String method);

  /**
   * Set HTTP method to use with resource.
   *
   * @param method HTTP method to use with resource
   */
  void setMethod(String method);

  /**
   * Get media type produced by resource.
   *
   * @return media type produced by resource
   */
  String getProduces();

  Link withProduces(String produces);

  /**
   * Set media type produced by resource.
   *
   * @param produces media type produced by resource
   */
  void setProduces(String produces);

  /**
   * Get media type consumed by resource.
   *
   * @return media type consumed by resource
   */
  String getConsumes();

  Link withConsumes(String consumes);

  /**
   * Set media type consumed by resource.
   *
   * @param consumes media type consumed by resource
   */
  void setConsumes(String consumes);

  /**
   * Get description of the query parameters (if any) of request.
   *
   * @return description of the query parameters (if any) of request
   */
  List<LinkParameter> getParameters();

  @DelegateTo(
    client = @DelegateRule(type = LinkParameterResolver.class, method = "getParameter"),
    server = @DelegateRule(type = LinkParameterResolver.class, method = "getParameter")
  )
  LinkParameter getParameter(String parameterName);

  Link withParameters(List<LinkParameter> parameters);

  /**
   * Set description of the query parameters (if any) of request.
   *
   * @param parameters description of the query parameters (if any) of request
   */
  void setParameters(List<LinkParameter> parameters);

  /**
   * Get request body description.
   *
   * @return request body description
   */
  RequestBodyDescriptor getRequestBody();

  Link withRequestBody(RequestBodyDescriptor requestBody);

  /**
   * Set request body description.
   *
   * @param requestBody request body description
   */
  void setRequestBody(RequestBodyDescriptor requestBody);

  class LinkParameterResolver {
    public static LinkParameter getParameter(Link link, String parameterName) {
      for (LinkParameter linkParameter : link.getParameters()) {
        if (linkParameter.getName().equals(parameterName)) {
          return linkParameter;
        }
      }
      return null;
    }
  }
}
