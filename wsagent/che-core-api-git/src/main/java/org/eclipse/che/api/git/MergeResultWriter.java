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
import org.eclipse.che.api.git.shared.MergeResult;

/** @author andrew00x */
@Singleton
@Provider
@Produces(MediaType.TEXT_PLAIN)
public final class MergeResultWriter implements MessageBodyWriter<MergeResult> {
  /**
   * @see MessageBodyWriter#isWriteable(Class, java.lang.reflect.Type,
   *     java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)
   */
  @Override
  public boolean isWriteable(
      Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return MergeResult.class.isAssignableFrom(type);
  }

  /**
   * @see MessageBodyWriter#getSize(Object, Class, java.lang.reflect.Type,
   *     java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)
   */
  @Override
  public long getSize(
      MergeResult mergeResult,
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
      MergeResult mergeResult,
      Class<?> type,
      Type genericType,
      Annotation[] annotations,
      MediaType mediaType,
      MultivaluedMap<String, Object> httpHeaders,
      OutputStream entityStream)
      throws IOException, WebApplicationException {
    Writer writer = new OutputStreamWriter(entityStream);
    MergeResult.MergeStatus status = mergeResult.getMergeStatus();
    switch (mergeResult.getMergeStatus()) {
      case FAST_FORWARD:
      case ALREADY_UP_TO_DATE:
      case MERGED:
        writer.write(status.toString());
        writer.write('\n');
        break;
      case FAILED:
        writer.write("error: Failed to merge:");
        for (String failed : mergeResult.getFailed()) {
          writer.write("        ");
          writer.write(failed);
          writer.write('\n');
        }
        break;
      case CONFLICTING:
        for (String conflict : mergeResult.getConflicts()) {
          writer.write("CONFLICT(content): Merge conflict in: " + conflict);
          writer.write('\n');
        }
        writer.write("Automatic merge failed; fix conflicts and then commit the result");
        writer.write('\n');
        break;
      case NOT_SUPPORTED:
        writer.write("Operation not supported");
        writer.write('\n');
        break;
      default:
        break;
    }
    writer.flush();
  }
}
