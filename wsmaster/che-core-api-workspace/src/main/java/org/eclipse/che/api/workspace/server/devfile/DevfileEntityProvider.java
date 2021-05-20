/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server.devfile;

import static com.google.common.base.Strings.isNullOrEmpty;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.eclipse.che.api.workspace.server.DtoConverter.asDto;

import com.google.common.io.CharStreams;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.NotSupportedException;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileFormatException;
import org.eclipse.che.api.workspace.shared.dto.devfile.DevfileDto;
import org.eclipse.che.dto.server.DtoFactory;

/**
 * Parses {@link DevfileDto} either from Json or yaml content, and performs schema validation before
 * the actual DTO created.
 *
 * @author Max Shaposhnyk
 */
@Singleton
@Provider
@Produces({APPLICATION_JSON})
@Consumes({APPLICATION_JSON, "text/yaml", "text/x-yaml"})
public class DevfileEntityProvider
    implements MessageBodyReader<DevfileDto>, MessageBodyWriter<DevfileDto> {

  private DevfileParser devfileParser;

  @Inject
  public DevfileEntityProvider(DevfileParser devfileParser) {
    this.devfileParser = devfileParser;
  }

  @Override
  public boolean isReadable(
      Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return type == DevfileDto.class;
  }

  @Override
  public DevfileDto readFrom(
      Class<DevfileDto> type,
      Type genericType,
      Annotation[] annotations,
      MediaType mediaType,
      MultivaluedMap<String, String> httpHeaders,
      InputStream entityStream)
      throws IOException, WebApplicationException {

    try {
      if (mediaType.isCompatible(MediaType.APPLICATION_JSON_TYPE)) {
        return asDto(
            devfileParser.parseJson(
                CharStreams.toString(
                    new InputStreamReader(entityStream, getCharsetOrUtf8(mediaType)))));
      } else if (mediaType.isCompatible(MediaType.valueOf("text/yaml"))
          || mediaType.isCompatible(MediaType.valueOf("text/x-yaml"))) {
        return asDto(
            devfileParser.parseYaml(
                CharStreams.toString(
                    new InputStreamReader(entityStream, getCharsetOrUtf8(mediaType)))));
      }
    } catch (DevfileFormatException e) {
      throw new BadRequestException(e.getMessage());
    }
    throw new NotSupportedException("Unknown media type " + mediaType.toString());
  }

  @Override
  public boolean isWriteable(
      Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return DevfileDto.class.isAssignableFrom(type);
  }

  @Override
  public long getSize(
      DevfileDto devfileDto,
      Class<?> type,
      Type genericType,
      Annotation[] annotations,
      MediaType mediaType) {
    return -1;
  }

  @Override
  public void writeTo(
      DevfileDto devfileDto,
      Class<?> type,
      Type genericType,
      Annotation[] annotations,
      MediaType mediaType,
      MultivaluedMap<String, Object> httpHeaders,
      OutputStream entityStream)
      throws IOException, WebApplicationException {
    httpHeaders.putSingle(HttpHeaders.CACHE_CONTROL, "public, no-cache, no-store, no-transform");
    try (Writer w = new OutputStreamWriter(entityStream, StandardCharsets.UTF_8)) {
      w.write(DtoFactory.getInstance().toJson(devfileDto));
      w.flush();
    }
  }

  private String getCharsetOrUtf8(MediaType mediaType) {
    String charset = mediaType == null ? null : mediaType.getParameters().get("charset");
    if (isNullOrEmpty(charset)) {
      charset = "UTF-8";
    }
    return charset;
  }
}
