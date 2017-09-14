/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.api.debug;

import static org.eclipse.che.ide.MimeType.APPLICATION_JSON;
import static org.eclipse.che.ide.rest.HTTPHeader.CONTENT_TYPE;

import com.google.gwt.http.client.URL;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.debug.shared.dto.BreakpointDto;
import org.eclipse.che.api.debug.shared.dto.DebugSessionDto;
import org.eclipse.che.api.debug.shared.dto.LocationDto;
import org.eclipse.che.api.debug.shared.dto.SimpleValueDto;
import org.eclipse.che.api.debug.shared.dto.StackFrameDumpDto;
import org.eclipse.che.api.debug.shared.dto.ThreadStateDto;
import org.eclipse.che.api.debug.shared.dto.VariableDto;
import org.eclipse.che.api.debug.shared.dto.action.ActionDto;
import org.eclipse.che.api.debug.shared.dto.action.ResumeActionDto;
import org.eclipse.che.api.debug.shared.dto.action.StartActionDto;
import org.eclipse.che.api.debug.shared.dto.action.StepIntoActionDto;
import org.eclipse.che.api.debug.shared.dto.action.StepOutActionDto;
import org.eclipse.che.api.debug.shared.dto.action.StepOverActionDto;
import org.eclipse.che.api.debug.shared.dto.action.SuspendActionDto;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.json.JsonHelper;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.StringUnmarshaller;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;

/**
 * The implementation of {@link DebuggerServiceClient}.
 *
 * @author Anatoliy Bazko
 */
@Singleton
public class DebuggerServiceClientImpl implements DebuggerServiceClient {
  private final LoaderFactory loaderFactory;
  private final AsyncRequestFactory asyncRequestFactory;
  private final DtoUnmarshallerFactory dtoUnmarshallerFactory;
  private final AppContext appContext;

  @Inject
  protected DebuggerServiceClientImpl(
      AppContext appContext,
      LoaderFactory loaderFactory,
      AsyncRequestFactory asyncRequestFactory,
      DtoUnmarshallerFactory dtoUnmarshallerFactory) {
    this.loaderFactory = loaderFactory;
    this.asyncRequestFactory = asyncRequestFactory;
    this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
    this.appContext = appContext;
  }

  @Override
  public Promise<DebugSessionDto> connect(
      String debuggerType, Map<String, String> connectionProperties) {
    final String requestUrl = getBaseUrl(null) + "?type=" + debuggerType;
    return asyncRequestFactory
        .createPostRequest(requestUrl, null)
        .header(CONTENT_TYPE, APPLICATION_JSON)
        .data(JsonHelper.toJson(connectionProperties))
        .send(dtoUnmarshallerFactory.newUnmarshaller(DebugSessionDto.class));
  }

  @Override
  public Promise<Void> disconnect(String id) {
    final String requestUrl = getBaseUrl(id);
    return asyncRequestFactory
        .createDeleteRequest(requestUrl)
        .loader(loaderFactory.newLoader())
        .send();
  }

  @Override
  public Promise<Void> suspend(String id, SuspendActionDto action) {
    return performAction(id, action);
  }

  @Override
  public Promise<DebugSessionDto> getSessionInfo(String id) {
    final String requestUrl = getBaseUrl(id);
    return asyncRequestFactory
        .createGetRequest(requestUrl)
        .send(dtoUnmarshallerFactory.newUnmarshaller(DebugSessionDto.class));
  }

  @Override
  public Promise<Void> start(String id, StartActionDto action) {
    return performAction(id, action);
  }

  @Override
  public Promise<Void> addBreakpoint(String id, BreakpointDto breakpointDto) {
    final String requestUrl = getBaseUrl(id) + "/breakpoint";
    return asyncRequestFactory.createPostRequest(requestUrl, breakpointDto).send();
  }

  @Override
  public Promise<List<BreakpointDto>> getAllBreakpoints(String id) {
    final String requestUrl = getBaseUrl(id) + "/breakpoint";
    return asyncRequestFactory
        .createGetRequest(requestUrl)
        .send(dtoUnmarshallerFactory.newListUnmarshaller(BreakpointDto.class));
  }

  @Override
  public Promise<Void> deleteBreakpoint(String id, LocationDto locationDto) {
    final String requestUrl = getBaseUrl(id) + "/breakpoint";
    final String params =
        "?target="
            + locationDto.getTarget()
            + "&line="
            + locationDto.getLineNumber()
            + "&project="
            + locationDto.getResourceProjectPath();
    return asyncRequestFactory.createDeleteRequest(requestUrl + params).send();
  }

  @Override
  public Promise<Void> deleteAllBreakpoints(String id) {
    final String requestUrl = getBaseUrl(id) + "/breakpoint";
    return asyncRequestFactory.createDeleteRequest(requestUrl).send();
  }

  @Override
  public Promise<StackFrameDumpDto> getStackFrameDump(String id, long threadId, int frameIndex) {
    final String requestUrl =
        getBaseUrl(id) + "/stackframedump?thread=" + threadId + "&frame=" + frameIndex;
    return asyncRequestFactory
        .createGetRequest(requestUrl)
        .send(dtoUnmarshallerFactory.newUnmarshaller(StackFrameDumpDto.class));
  }

  @Override
  public Promise<List<ThreadStateDto>> getThreadDump(String id) {
    final String requestUrl = getBaseUrl(id) + "/threaddump";
    return asyncRequestFactory
        .createGetRequest(requestUrl)
        .send(dtoUnmarshallerFactory.newListUnmarshaller(ThreadStateDto.class));
  }

  @Override
  public Promise<Void> resume(String id, ResumeActionDto action) {
    return performAction(id, action);
  }

  @Override
  public Promise<SimpleValueDto> getValue(
      String id, VariableDto variableDto, long threadId, int frameIndex) {
    final String requestUrl = getBaseUrl(id) + "/value?thread=" + threadId + "&frame=" + frameIndex;
    List<String> path = variableDto.getVariablePath().getPath();

    StringBuilder params = new StringBuilder();
    for (int i = 0; i < path.size(); i++) {

      params.append("&path");
      params.append(i);
      params.append("=");
      params.append(path.get(i));
    }

    return asyncRequestFactory
        .createGetRequest(requestUrl + params)
        .send(dtoUnmarshallerFactory.newUnmarshaller(SimpleValueDto.class));
  }

  @Override
  public Promise<Void> setValue(String id, VariableDto variableDto, long threadId, int frameIndex) {
    final String requestUrl = getBaseUrl(id) + "/value?thread=" + threadId + "&frame=" + frameIndex;
    return asyncRequestFactory.createPutRequest(requestUrl, variableDto).send();
  }

  @Override
  public Promise<Void> stepInto(String id, StepIntoActionDto action) {
    return performAction(id, action);
  }

  @Override
  public Promise<Void> stepOver(String id, StepOverActionDto action) {
    return performAction(id, action);
  }

  @Override
  public Promise<Void> stepOut(String id, StepOutActionDto action) {
    return performAction(id, action);
  }

  @Override
  public Promise<String> evaluate(String id, String expression, long threadId, int frameIndex) {
    String requestUrl = getBaseUrl(id) + "/evaluation?thread=" + threadId + "&frame=" + frameIndex;
    String params = "&expression=" + URL.encodeQueryString(expression);
    return asyncRequestFactory
        .createGetRequest(requestUrl + params)
        .loader(loaderFactory.newLoader())
        .send(new StringUnmarshaller());
  }

  private String getBaseUrl(String id) {
    final String url = appContext.getWsAgentServerApiEndpoint() + "/debugger";
    if (id != null) {
      return url + "/" + id;
    }
    return url;
  }

  protected Promise<Void> performAction(String id, ActionDto actionDto) {
    final String requestUrl = getBaseUrl(id);
    return asyncRequestFactory.createPostRequest(requestUrl, actionDto).send();
  }
}
