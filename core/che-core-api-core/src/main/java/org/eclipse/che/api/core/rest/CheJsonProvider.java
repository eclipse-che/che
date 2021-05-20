/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
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

import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.dto.server.JsonSerializable;
import org.eclipse.che.dto.shared.DTO;
import org.everrest.core.impl.provider.JsonEntityProvider;

/**
 * Implementation of {@link MessageBodyReader} and {@link MessageBodyWriter} needed for binding JSON
 * content to and from Java Objects.
 *
 * @author andrew00x
 * @see DTO
 * @see DtoFactory
 */
@Singleton
@Provider
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
public class CheJsonProvider<T> implements MessageBodyReader<T>, MessageBodyWriter<T> {
  private Set<Class> ignoredClasses;
  private final JsonEntityProvider delegate = new JsonEntityProvider<>();
  private final Type listOfJsonSerializableType =
      new TypeToken<List<JsonSerializable>>() {}.getType();

  @Inject
  public CheJsonProvider(@Nullable @Named("che.json.ignored_classes") Set<Class> ignoredClasses) {
    this.ignoredClasses =
        ignoredClasses == null ? new LinkedHashSet<Class>() : new LinkedHashSet<>(ignoredClasses);
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean isWriteable(
      Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return !ignoredClasses.contains(type)
        && (type.isAnnotationPresent(DTO.class)
            || delegate.isWriteable(type, genericType, annotations, mediaType));
  }

  @Override
  public long getSize(
      T t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return -1;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void writeTo(
      T t,
      Class<?> type,
      Type genericType,
      Annotation[] annotations,
      MediaType mediaType,
      MultivaluedMap<String, Object> httpHeaders,
      OutputStream entityStream)
      throws IOException, WebApplicationException {
    // Add Cache-Control before start write body.
    httpHeaders.putSingle(HttpHeaders.CACHE_CONTROL, "public, no-cache, no-store, no-transform");
    if (t instanceof JsonSerializable) {
      try (Writer w = new OutputStreamWriter(entityStream, StandardCharsets.UTF_8)) {
        ((JsonSerializable) t).toJson(w);
      }
    } else if (isDtoList(type, genericType, t)) {
      try (Writer w = new OutputStreamWriter(entityStream, StandardCharsets.UTF_8)) {
        DtoFactory.getInstance().getGson().toJson(t, listOfJsonSerializableType, w);
      }
    } else {
      delegate.writeTo(t, type, genericType, annotations, mediaType, httpHeaders, entityStream);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean isReadable(
      Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return !ignoredClasses.contains(type)
        && (type.isAnnotationPresent(DTO.class)
            || delegate.isReadable(type, genericType, annotations, mediaType));
  }

  @SuppressWarnings("unchecked")
  @Override
  public T readFrom(
      Class<T> type,
      Type genericType,
      Annotation[] annotations,
      MediaType mediaType,
      MultivaluedMap<String, String> httpHeaders,
      InputStream entityStream)
      throws IOException, WebApplicationException {
    if (type.isAnnotationPresent(DTO.class)) {
      return DtoFactory.getInstance().createDtoFromJson(entityStream, type);
    } else if (isDtoList(type, genericType, null)) {
      ParameterizedType parameterizedType = (ParameterizedType) genericType;
      Type elementType = parameterizedType.getActualTypeArguments()[0];
      return (T) DtoFactory.getInstance().createListDtoFromJson(entityStream, (Class) elementType);
    }
    return (T)
        delegate.readFrom(type, genericType, annotations, mediaType, httpHeaders, entityStream);
  }

  /**
   * Get Set of classes that we never try to serialize or deserialize. Returned Set is mutable and
   * new classes may be added in ignored Set.
   */
  public Set<Class> getIgnoredClasses() {
    return ignoredClasses;
  }

  /** Checks if provided object is a list of DTO or serializable objects. */
  private static <T> boolean isDtoList(Class<?> type, Type genericType, T t) {
    if (!List.class.isAssignableFrom(type)) {
      return false;
    }
    if (genericType instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType) genericType;
      Type elementType = parameterizedType.getActualTypeArguments()[0];
      return elementType instanceof Class && ((Class) elementType).isAnnotationPresent(DTO.class);
    } else if (t instanceof List && type.equals(genericType)) {
      List list = (List) t;
      return !list.isEmpty() && list.iterator().next() instanceof JsonSerializable;
    }
    return false;
  }
}
