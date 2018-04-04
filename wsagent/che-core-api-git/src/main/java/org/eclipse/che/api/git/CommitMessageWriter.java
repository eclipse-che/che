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
package org.eclipse.che.api.git;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.inject.Singleton;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import org.eclipse.che.api.git.shared.Revision;

/** @author andrew00x */
@Singleton
@Provider
@Produces(MediaType.TEXT_PLAIN)
public final class CommitMessageWriter implements MessageBodyWriter<Revision> {
  /**
   * @see MessageBodyWriter#isWriteable(Class, java.lang.reflect.Type,
   *     java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)
   */
  @Override
  public boolean isWriteable(
      Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return type == Revision.class;
  }

  /**
   * @see MessageBodyWriter#getSize(Object, Class, java.lang.reflect.Type,
   *     java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)
   */
  @Override
  public long getSize(
      Revision revision,
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
      Revision revision,
      Class<?> type,
      Type genericType,
      Annotation[] annotations,
      MediaType mediaType,
      MultivaluedMap<String, Object> httpHeaders,
      OutputStream entityStream)
      throws IOException, WebApplicationException {
    Writer writer = new OutputStreamWriter(entityStream);
    writer.write('[');
    writer.write(revision.getBranch());
    writer.write(' ');
    writer.write(revision.getId());
    writer.write(']');
    writer.write(' ');
    writer.write(revision.getMessage());
    writer.write('\n');
    writer.flush();
  }
}
