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
package org.eclipse.che.api.git;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.inject.Singleton;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

/** @author andrew00x */
@Singleton
@Provider
@Produces(MediaType.TEXT_PLAIN)
public final class StatusPageWriter implements MessageBodyWriter<InfoPage> {
  /**
   * @see MessageBodyWriter#isWriteable(Class, java.lang.reflect.Type,
   *     java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)
   */
  @Override
  public boolean isWriteable(
      Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return InfoPage.class.isAssignableFrom(type);
  }

  /**
   * @see MessageBodyWriter#getSize(Object, Class, java.lang.reflect.Type,
   *     java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)
   */
  @Override
  public long getSize(
      InfoPage infoPage,
      Class<?> type,
      Type genericType,
      Annotation[] annotations,
      MediaType mediaType) {
    return -1;
  }

  /**
   * @see MessageBodyWriter#writeTo(Object, Class, java.lang.reflect.Type,
   *     java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType,
   *     javax.ws.rs.core.MultivaluedMap, java.io.OutputStream)
   */
  @Override
  public void writeTo(
      InfoPage infoPage,
      Class<?> type,
      Type genericType,
      Annotation[] annotations,
      MediaType mediaType,
      MultivaluedMap<String, Object> httpHeaders,
      OutputStream entityStream)
      throws IOException, WebApplicationException {
    infoPage.writeTo(entityStream);
  }
}
