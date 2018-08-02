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
package org.eclipse.che.api.debugger.server;

import com.google.common.io.CharStreams;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import org.eclipse.che.api.debug.shared.dto.action.ActionDto;
import org.eclipse.che.api.debug.shared.dto.action.ResumeActionDto;
import org.eclipse.che.api.debug.shared.dto.action.StartActionDto;
import org.eclipse.che.api.debug.shared.dto.action.StepIntoActionDto;
import org.eclipse.che.api.debug.shared.dto.action.StepOutActionDto;
import org.eclipse.che.api.debug.shared.dto.action.StepOverActionDto;
import org.eclipse.che.api.debug.shared.dto.action.SuspendActionDto;
import org.eclipse.che.api.debug.shared.model.action.Action;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.dto.shared.DTO;

/**
 * Implementation of {@link MessageBodyReader} needed for binding JSON content to Java Objects.
 *
 * @author Anatoliy Bazko
 * @see DTO
 * @see DtoFactory
 */
@Singleton
@Provider
@Consumes({MediaType.APPLICATION_JSON})
public class DebuggerActionProvider implements MessageBodyReader<ActionDto> {

  @Override
  public boolean isReadable(
      Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return ActionDto.class.isAssignableFrom(type);
  }

  @Override
  public ActionDto readFrom(
      Class<ActionDto> type,
      Type genericType,
      Annotation[] annotations,
      MediaType mediaType,
      MultivaluedMap<String, String> httpHeaders,
      InputStream entityStream)
      throws IOException, WebApplicationException {
    String json = CharStreams.toString(new BufferedReader(new InputStreamReader(entityStream)));

    JsonParser jsonParser = new JsonParser();
    JsonElement jsonElement = jsonParser.parse(json);
    JsonObject jsonObject = jsonElement.getAsJsonObject();

    if (!jsonObject.has("type")) {
      throw new IOException("Json is broken. There is not type key in json object");
    }

    Action.TYPE actionType = Action.TYPE.valueOf(jsonObject.get("type").getAsString());
    switch (actionType) {
      case RESUME:
        return DtoFactory.getInstance().createDtoFromJson(json, ResumeActionDto.class);
      case START:
        return DtoFactory.getInstance().createDtoFromJson(json, StartActionDto.class);
      case STEP_INTO:
        return DtoFactory.getInstance().createDtoFromJson(json, StepIntoActionDto.class);
      case STEP_OUT:
        return DtoFactory.getInstance().createDtoFromJson(json, StepOutActionDto.class);
      case STEP_OVER:
        return DtoFactory.getInstance().createDtoFromJson(json, StepOverActionDto.class);
      case SUSPEND:
        return DtoFactory.getInstance().createDtoFromJson(json, SuspendActionDto.class);
      default:
        throw new IOException("Can't parse json. Unknown action type: " + actionType);
    }
  }
}
