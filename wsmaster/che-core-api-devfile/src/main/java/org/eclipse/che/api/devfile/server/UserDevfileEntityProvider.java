/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.devfile.server;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
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
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import org.eclipse.che.api.devfile.shared.dto.UserDevfileDto;
import org.eclipse.che.api.workspace.server.devfile.DevfileParser;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileFormatException;
import org.eclipse.che.dto.server.DtoFactory;

/**
 * Entity provider for {@link UserDevfileDto}. Performs schema validation of devfile part of the
 * user devfile before actual {@link UserDevfileDto} creation.
 */
@Singleton
@Provider
@Produces({APPLICATION_JSON})
@Consumes({APPLICATION_JSON})
public class UserDevfileEntityProvider
    implements MessageBodyReader<UserDevfileDto>, MessageBodyWriter<UserDevfileDto> {

  private final DevfileParser devfileParser;
  private final ObjectMapper mapper = new ObjectMapper();

  @Inject
  public UserDevfileEntityProvider(DevfileParser devfileParser) {
    this.devfileParser = devfileParser;
  }

  @Override
  public boolean isReadable(
      Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return type == UserDevfileDto.class;
  }

  @Override
  public UserDevfileDto readFrom(
      Class<UserDevfileDto> type,
      Type genericType,
      Annotation[] annotations,
      MediaType mediaType,
      MultivaluedMap<String, String> httpHeaders,
      InputStream entityStream)
      throws IOException, WebApplicationException {
    try {
      JsonNode wsNode = mapper.readTree(entityStream);
      JsonNode devfileNode = wsNode.path("devfile");
      if (!devfileNode.isNull() && !devfileNode.isMissingNode()) {
        devfileParser.parseJson(devfileNode.toString());
      } else {
        throw new BadRequestException("Mandatory field `devfile` is not defined.");
      }
      return DtoFactory.getInstance().createDtoFromJson(wsNode.toString(), UserDevfileDto.class);
    } catch (DevfileFormatException e) {
      throw new BadRequestException(e.getMessage());
    } catch (IOException e) {
      throw new WebApplicationException(e.getMessage(), e);
    }
  }

  @Override
  public boolean isWriteable(
      Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return UserDevfileDto.class.isAssignableFrom(type);
  }

  @Override
  public long getSize(
      UserDevfileDto userDevfileDto,
      Class<?> type,
      Type genericType,
      Annotation[] annotations,
      MediaType mediaType) {
    return -1;
  }

  @Override
  public void writeTo(
      UserDevfileDto userDevfileDto,
      Class<?> type,
      Type genericType,
      Annotation[] annotations,
      MediaType mediaType,
      MultivaluedMap<String, Object> httpHeaders,
      OutputStream entityStream)
      throws IOException, WebApplicationException {
    httpHeaders.putSingle(HttpHeaders.CACHE_CONTROL, "public, no-cache, no-store, no-transform");
    try (Writer w = new OutputStreamWriter(entityStream, StandardCharsets.UTF_8)) {
      w.write(DtoFactory.getInstance().toJson(userDevfileDto));
      w.flush();
    }
  }
}
