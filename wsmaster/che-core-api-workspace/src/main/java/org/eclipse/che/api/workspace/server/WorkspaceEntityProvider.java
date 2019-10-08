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
package org.eclipse.che.api.workspace.server;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.eclipse.che.api.workspace.server.DtoConverter.asDto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
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
import org.eclipse.che.api.workspace.server.devfile.DevfileManager;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileFormatException;
import org.eclipse.che.api.workspace.server.dto.DtoServerImpls.WorkspaceDtoImpl;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.api.workspace.shared.dto.devfile.DevfileDto;
import org.eclipse.che.dto.server.DtoFactory;

/**
 * Entity provider for {@link WorkspaceDto}. Performs schema validation of devfile part of the
 * workspace before actual {@link DevfileDto} creation.
 *
 * @author Max Shaposhnyk
 */
@Singleton
@Provider
@Produces({APPLICATION_JSON})
@Consumes({APPLICATION_JSON})
public class WorkspaceEntityProvider
    implements MessageBodyReader<WorkspaceDto>, MessageBodyWriter<WorkspaceDto> {

  private DevfileManager devfileManager;
  private ObjectMapper mapper = new ObjectMapper();

  @Inject
  public WorkspaceEntityProvider(DevfileManager devfileManager) {
    this.devfileManager = devfileManager;
    SimpleModule module = new SimpleModule();
    module.addDeserializer(DevfileDto.class, new DevfileDtoDeserializer());
    mapper.registerModule(module);
  }

  @Override
  public boolean isReadable(
      Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return type == WorkspaceDto.class;
  }

  @Override
  public WorkspaceDto readFrom(
      Class<WorkspaceDto> type,
      Type genericType,
      Annotation[] annotations,
      MediaType mediaType,
      MultivaluedMap<String, String> httpHeaders,
      InputStream entityStream)
      throws IOException, WebApplicationException {
    return mapper
        .readerFor(WorkspaceDtoImpl.class)
        .without(DeserializationFeature.WRAP_EXCEPTIONS)
        .readValue(entityStream);
  }

  @Override
  public boolean isWriteable(
      Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return WorkspaceDto.class.isAssignableFrom(type);
  }

  @Override
  public long getSize(
      WorkspaceDto workspaceDto,
      Class<?> type,
      Type genericType,
      Annotation[] annotations,
      MediaType mediaType) {
    return -1;
  }

  @Override
  public void writeTo(
      WorkspaceDto workspaceDto,
      Class<?> type,
      Type genericType,
      Annotation[] annotations,
      MediaType mediaType,
      MultivaluedMap<String, Object> httpHeaders,
      OutputStream entityStream)
      throws IOException, WebApplicationException {
    httpHeaders.putSingle(HttpHeaders.CACHE_CONTROL, "public, no-cache, no-store, no-transform");
    try (Writer w = new OutputStreamWriter(entityStream, StandardCharsets.UTF_8)) {
      w.write(DtoFactory.getInstance().toJson(workspaceDto));
      w.flush();
    }
  }

  class DevfileDtoDeserializer extends JsonDeserializer<DevfileDto> {
    @Override
    public DevfileDto deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
      try {
        return asDto(devfileManager.parseJson(p.readValueAsTree().toString()));
      } catch (DevfileFormatException e) {
        throw new BadRequestException(e.getMessage());
      }
    }
  }
}
