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
package org.eclipse.che.api.git;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import javax.inject.Singleton;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import org.eclipse.che.api.git.shared.Tag;

/**
 * Writer to serialize list of git tags to plain text in form as command line git does.
 *
 * @author andrew00x
 */
@Singleton
@Provider
@Produces(MediaType.TEXT_PLAIN)
public final class TagListWriter implements MessageBodyWriter<Iterable<Tag>> {
  /**
   * @see MessageBodyWriter#isWriteable(Class, java.lang.reflect.Type,
   *     java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)
   */
  @Override
  public boolean isWriteable(
      Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    if (Iterable.class.isAssignableFrom(type) && (genericType instanceof ParameterizedType)) {
      Type[] types = ((ParameterizedType) genericType).getActualTypeArguments();
      return types.length == 1 && types[0] == Tag.class;
    }
    return false;
  }

  /**
   * @see MessageBodyWriter#getSize(Object, Class, java.lang.reflect.Type,
   *     java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)
   */
  @Override
  public long getSize(
      Iterable<Tag> tags,
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
      Iterable<Tag> tags,
      Class<?> type,
      Type genericType,
      Annotation[] annotations,
      MediaType mediaType,
      MultivaluedMap<String, Object> httpHeaders,
      OutputStream entityStream)
      throws IOException, WebApplicationException {
    Writer writer = new OutputStreamWriter(entityStream);
    for (Tag tag : tags) {
      writer.write(tag.getName());
      writer.write('\n');
    }
    writer.flush();
  }
}
