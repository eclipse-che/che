/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.api.debug;

import com.google.gwt.http.client.URL;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.debugger.shared.dto.BreakpointDto;
import org.eclipse.che.ide.api.debugger.shared.dto.DebugSessionDto;
import org.eclipse.che.ide.api.debugger.shared.dto.LocationDto;
import org.eclipse.che.ide.api.debugger.shared.dto.StackFrameDumpDto;
import org.eclipse.che.ide.api.debugger.shared.dto.ValueDto;
import org.eclipse.che.ide.api.debugger.shared.dto.VariableDto;
import org.eclipse.che.ide.api.debugger.shared.dto.action.ActionDto;
import org.eclipse.che.ide.api.debugger.shared.dto.action.ResumeActionDto;
import org.eclipse.che.ide.api.debugger.shared.dto.action.StartActionDto;
import org.eclipse.che.ide.api.debugger.shared.dto.action.StepIntoActionDto;
import org.eclipse.che.ide.api.debugger.shared.dto.action.StepOutActionDto;
import org.eclipse.che.ide.api.debugger.shared.dto.action.StepOverActionDto;
import org.eclipse.che.api.machine.gwt.client.DevMachine;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.json.JsonHelper;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.StringUnmarshaller;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;

import java.util.List;
import java.util.Map;

import static org.eclipse.che.ide.MimeType.APPLICATION_JSON;
import static org.eclipse.che.ide.rest.HTTPHeader.CONTENT_TYPE;

/**
 * The implementation of {@link DebuggerServiceClient}.
 *
 * @author Anatoliy Bazko
 */
@Singleton
public class DebuggerServiceClientImpl implements DebuggerServiceClient {
    private final LoaderFactory          loaderFactory;
    private final AsyncRequestFactory    asyncRequestFactory;
    private final DtoUnmarshallerFactory dtoUnmarshallerFactory;
    private final AppContext             appContext;

    @Inject
    protected DebuggerServiceClientImpl(AppContext appContext,
                                        LoaderFactory loaderFactory,
                                        AsyncRequestFactory asyncRequestFactory,
                                        DtoUnmarshallerFactory dtoUnmarshallerFactory) {
        this.loaderFactory = loaderFactory;
        this.asyncRequestFactory = asyncRequestFactory;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.appContext = appContext;
    }

    @Override
    public Promise<DebugSessionDto> connect(String debuggerType, Map<String, String> connectionProperties) {
        final String requestUrl = getBaseUrl() + "?type=" + debuggerType;
        return asyncRequestFactory.createPostRequest(requestUrl, null)
                                  .header(CONTENT_TYPE, APPLICATION_JSON)
                                  .data(JsonHelper.toJson(connectionProperties))
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(DebugSessionDto.class));
    }

    @Override
    public Promise<Void> disconnect(String id) {
        final String requestUrl = getBaseUrl() + "/" + id;
        return asyncRequestFactory.createDeleteRequest(requestUrl)
                                  .loader(loaderFactory.newLoader())
                                  .send();
    }

    @Override
    public Promise<DebugSessionDto> getSessionInfo(String id) {
        final String requestUrl = getBaseUrl() + "/" + id;
        return asyncRequestFactory.createGetRequest(requestUrl)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(DebugSessionDto.class));
    }

    @Override
    public Promise<Void> start(String id, StartActionDto action) {
        return performAction(id, action);
    }

    @Override
    public Promise<Void> addBreakpoint(String id, BreakpointDto breakpointDto) {
        final String requestUrl = getBaseUrl() + "/" + id + "/breakpoint";
        return asyncRequestFactory.createPostRequest(requestUrl, breakpointDto)
                                  .loader(loaderFactory.newLoader())
                                  .send();
    }

    @Override
    public Promise<List<BreakpointDto>> getAllBreakpoints(String id) {
        final String requestUrl = getBaseUrl() + "/" + id + "/breakpoint";
        return asyncRequestFactory.createGetRequest(requestUrl)
                                  .loader(loaderFactory.newLoader())
                                  .send(dtoUnmarshallerFactory.newListUnmarshaller(BreakpointDto.class));
    }

    @Override
    public Promise<Void> deleteBreakpoint(String id, LocationDto locationDto) {
        final String requestUrl = getBaseUrl() + "/" + id + "/breakpoint";
        final String params = "?target=" + locationDto.getTarget() + "&line=" + locationDto.getLineNumber();
        return asyncRequestFactory.createDeleteRequest(requestUrl + params).send();
    }

    @Override
    public Promise<Void> deleteAllBreakpoints(String id) {
        final String requestUrl = getBaseUrl() + "/" + id + "/breakpoint";
        return asyncRequestFactory.createDeleteRequest(requestUrl).send();
    }

    @Override
    public Promise<StackFrameDumpDto> getStackFrameDump(String id) {
        final String requestUrl = getBaseUrl() + "/" + id + "/dump";
        return asyncRequestFactory.createGetRequest(requestUrl)
                                  .loader(loaderFactory.newLoader())
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(StackFrameDumpDto.class));
    }

    @Override
    public Promise<Void> resume(String id, ResumeActionDto action) {
        return performAction(id, action);
    }

    @Override
    public Promise<ValueDto> getValue(String id, VariableDto variableDto) {
        final String requestUrl = getBaseUrl() + "/" + id + "/value";
        List<String> path = variableDto.getVariablePath().getPath();

        StringBuilder params = new StringBuilder();
        for (int i = 0; i < path.size(); i++) {
            params.append(i == 0 ? "?" : "&");
            params.append("path");
            params.append(i);
            params.append("=");
            params.append(path.get(i));
        }

        return asyncRequestFactory.createGetRequest(requestUrl + params)
                                  .loader(loaderFactory.newLoader())
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(ValueDto.class));
    }

    @Override
    public Promise<Void> setValue(String id, VariableDto variableDto) {
        final String requestUrl = getBaseUrl() + "/" + id + "/value";
        return asyncRequestFactory.createPutRequest(requestUrl, variableDto)
                                  .loader(loaderFactory.newLoader())
                                  .send();
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
    public Promise<String> evaluate(String id, String expression) {
        String requestUrl = getBaseUrl() + "/" + id + "/evaluation";
        String params = "?expression=" + URL.encodeQueryString(expression);
        return asyncRequestFactory.createGetRequest(requestUrl + params)
                                  .loader(loaderFactory.newLoader())
                                  .send(new StringUnmarshaller());
    }

    private String getBaseUrl() {
        DevMachine devMachine = appContext.getDevMachine();
        return devMachine.getWsAgentBaseUrl() + "/debugger/" + devMachine.getWorkspace();
    }

    protected Promise<Void> performAction(String id, ActionDto actionDto) {
        final String requestUrl = getBaseUrl() + "/" + id;
        return asyncRequestFactory.createPostRequest(requestUrl, actionDto)
                                  .loader(loaderFactory.newLoader())
                                  .send();
    }
}
