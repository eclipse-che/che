/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.core.rest;

import static org.eclipse.che.api.core.rest.DownloadFileResponseFilter.EntityType.JSON_SERIALIZABLE;
import static org.eclipse.che.api.core.rest.DownloadFileResponseFilter.EntityType.STRING;
import static org.eclipse.che.api.core.rest.DownloadFileResponseFilter.EntityType.UNKNOWN;

import java.util.List;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.eclipse.che.dto.server.JsonSerializable;

/**
 * Abstract Filter used to provide the json result as a file download operation. It is applying on
 * GET method and JSON content type only.
 *
 * @author Florent Benoit
 */
public abstract class DownloadFileResponseFilter {

  /** Entity type that we will be able to handle. */
  public enum EntityType {
    JSON_SERIALIZABLE,
    STRING,
    UNKNOWN
  }
  /**
   * Query parameter used to ask to specify headers that will propose JSON object to be downloaded.
   */
  public static final String QUERY_DOWNLOAD_PARAMETER = "downloadAsFile";

  /**
   * Check if we need to apply a filter
   *
   * @param request
   * @return
   */
  protected String getFileName(
      Request request, MediaType mediaType, UriInfo uriInfo, int responseStatus) {

    // manage only GET requests
    if (!HttpMethod.GET.equals(request.getMethod())) {
      return null;
    }

    // manage only OK code
    if (Response.Status.OK.getStatusCode() != responseStatus) {
      return null;
    }

    // Only handle JSON content
    if (!MediaType.APPLICATION_JSON_TYPE.equals(mediaType)) {
      return null;
    }

    // check if parameter filename is given
    MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
    return queryParameters.getFirst(QUERY_DOWNLOAD_PARAMETER);
  }

  /**
   * Check if entity is compliant with our filter
   *
   * @param entity the entity embedded in response.
   * @return true if it's a type that we can handle
   */
  protected boolean hasCompliantEntity(Object entity) {
    // no entity, skip
    if (entity == null) {
      return false;
    }

    // Check entity type
    if (entity instanceof List) {
      List<?> entities = (List) entity;
      for (Object simpleEntity : entities) {
        if (getElementType(simpleEntity) == UNKNOWN) {
          return false;
        }
      }
    } else if (getElementType(entity) == UNKNOWN) {
      // unknown entity type, will not configure it as a download
      return false;
    }

    return true;
  }

  /**
   * Helper method for getting the type of the JSON entity
   *
   * @param entity the entity object
   * @return the type of the element
   */
  protected EntityType getElementType(Object entity) {
    if (JsonSerializable.class.isAssignableFrom(entity.getClass())) {
      return JSON_SERIALIZABLE;
    }

    if (String.class.isAssignableFrom(entity.getClass())) {
      return STRING;
    }

    return UNKNOWN;
  }
}
