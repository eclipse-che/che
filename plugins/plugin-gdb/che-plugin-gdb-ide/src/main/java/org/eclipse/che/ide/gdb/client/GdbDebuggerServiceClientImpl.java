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
package org.eclipse.che.ide.gdb.client;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.ext.debugger.client.debug.DebuggerServiceClient;
import org.eclipse.che.ide.ext.debugger.shared.Breakpoint;
import org.eclipse.che.ide.ext.debugger.shared.BreakpointList;
import org.eclipse.che.ide.ext.debugger.shared.DebuggerInfo;
import org.eclipse.che.ide.ext.debugger.shared.StackFrameDump;
import org.eclipse.che.ide.ext.debugger.shared.UpdateVariableRequest;
import org.eclipse.che.ide.ext.debugger.shared.Value;
import org.eclipse.che.ide.ext.debugger.shared.Variable;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.StringUnmarshaller;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;

import javax.validation.constraints.NotNull;
import java.util.Map;

import static org.eclipse.che.ide.MimeType.TEXT_PLAIN;
import static org.eclipse.che.ide.gdb.client.GdbDebuggerClient.ConnectionProperties.FILE;
import static org.eclipse.che.ide.gdb.client.GdbDebuggerClient.ConnectionProperties.HOST;
import static org.eclipse.che.ide.gdb.client.GdbDebuggerClient.ConnectionProperties.PORT;
import static org.eclipse.che.ide.rest.HTTPHeader.ACCEPT;
import static org.eclipse.che.ide.rest.HTTPHeader.CONTENTTYPE;

/**
 * The implementation of {@link DebuggerServiceClient}.
 *
 * @author Anatoliy Bazko
 */
@Singleton
public class GdbDebuggerServiceClientImpl implements DebuggerServiceClient {
    private final String                          baseUrl;
    private final LoaderFactory                   loaderFactory;
    private final AsyncRequestFactory             asyncRequestFactory;
    private final DtoUnmarshallerFactory          dtoUnmarshallerFactory;

    @Inject
    protected GdbDebuggerServiceClientImpl(@Named("cheExtensionPath") String extPath,
                                           AppContext appContext,
                                           LoaderFactory loaderFactory,
                                           AsyncRequestFactory asyncRequestFactory,
                                           DtoUnmarshallerFactory dtoUnmarshallerFactory) {
        this.loaderFactory = loaderFactory;
        this.asyncRequestFactory = asyncRequestFactory;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.baseUrl = extPath + "/gdb/" + appContext.getWorkspace().getId();
    }

    @Override
    public Promise<DebuggerInfo> connect(@NotNull Map<String, String> connectionProperties) {
        final String requestUrl = baseUrl + "/connect";
        final String params = "?host=" + connectionProperties.get(HOST.toString())
                              + "&port=" + connectionProperties.get(PORT.toString())
                              + "&file=" + connectionProperties.get(FILE.toString());

        return asyncRequestFactory.createGetRequest(requestUrl + params)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(DebuggerInfo.class));
    }

    @Override
    public Promise<Void> disconnect(@NotNull String id) {
        final String requestUrl = baseUrl + "/disconnect/" + id;
        return asyncRequestFactory.createGetRequest(requestUrl)
                                  .loader(loaderFactory.newLoader())
                                  .send();
    }

    @Override
    public Promise<Void> start(@NotNull String id) {
        final String requestUrl = baseUrl + "/start/" + id;
        return asyncRequestFactory.createGetRequest(requestUrl)
                                  .loader(loaderFactory.newLoader())
                                  .send();
    }

    @Override
    public Promise<DebuggerInfo> getInfo(@NotNull String id) {
        final String requestUrl = baseUrl + "/" + id;
        return asyncRequestFactory.createGetRequest(requestUrl)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(DebuggerInfo.class));
    }

    @Override
    public Promise<Void> addBreakpoint(@NotNull String id, @NotNull Breakpoint breakpoint) {
        final String requestUrl = baseUrl + "/breakpoints/add/" + id;
        return asyncRequestFactory.createPostRequest(requestUrl, breakpoint)
                                  .loader(loaderFactory.newLoader())
                                  .send();
    }

    @Override
    public Promise<Void> deleteBreakpoint(@NotNull String id, @NotNull Breakpoint breakpoint) {
        final String requestUrl = baseUrl + "/breakpoints/delete/" + id;
        return asyncRequestFactory.createPostRequest(requestUrl, breakpoint)
                                  .send();
    }

    @Override
    public Promise<BreakpointList> getAllBreakpoints(@NotNull String id) {
        final String requestUrl = baseUrl + "/breakpoints/" + id;
        return asyncRequestFactory.createGetRequest(requestUrl)
                                  .loader(loaderFactory.newLoader())
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(BreakpointList.class));
    }

    @Override
    public Promise<Void> deleteAllBreakpoints(@NotNull String id) {
        final String requestUrl = baseUrl + "/breakpoints/delete_all/" + id;
        return asyncRequestFactory.createGetRequest(requestUrl).loader(loaderFactory.newLoader()).send();
    }

    @Override
    public Promise<StackFrameDump> getStackFrameDump(@NotNull String id) {
        final String requestUrl = baseUrl + "/dump/" + id;
        return asyncRequestFactory.createGetRequest(requestUrl)
                                  .loader(loaderFactory.newLoader())
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(StackFrameDump.class));
    }

    @Override
    public Promise<Void> resume(@NotNull String id) {
        final String requestUrl = baseUrl + "/resume/" + id;
        return asyncRequestFactory.createGetRequest(requestUrl).loader(loaderFactory.newLoader()).send();
    }

    @Override
    public Promise<Value> getValue(@NotNull String id, @NotNull Variable variable) {
        final String requestUrl = baseUrl + "/value/get/" + id;
        return asyncRequestFactory.createPostRequest(requestUrl, variable)
                                  .loader(loaderFactory.newLoader())
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(Value.class));
    }

    @Override
    public Promise<Void> setValue(@NotNull String id, @NotNull UpdateVariableRequest updateVariableRequest) {
        final String requestUrl = baseUrl + "/value/set/" + id;
        return asyncRequestFactory.createPostRequest(requestUrl, updateVariableRequest)
                                  .loader(loaderFactory.newLoader())
                                  .send();
    }

    @Override
    public Promise<Void> stepInto(@NotNull String id) {
        final String requestUrl = baseUrl + "/step/into/" + id;
        return asyncRequestFactory.createGetRequest(requestUrl).loader(loaderFactory.newLoader()).send();
    }

    @Override
    public Promise<Void> stepOver(@NotNull String id) {
        final String requestUrl = baseUrl + "/step/over/" + id;
        return asyncRequestFactory.createGetRequest(requestUrl).loader(loaderFactory.newLoader()).send();
    }

    @Override
    public Promise<Void> stepOut(@NotNull String id) {
        final String requestUrl = baseUrl + "/step/out/" + id;
        return asyncRequestFactory.createGetRequest(requestUrl).loader(loaderFactory.newLoader()).send();
    }

    @Override
    public Promise<String> evaluateExpression(@NotNull String id, @NotNull String expression) {
        final String requestUrl = baseUrl + "/expression/" + id;
        return asyncRequestFactory.createPostRequest(requestUrl, null)
                                  .data(expression)
                                  .header(ACCEPT, TEXT_PLAIN)
                                  .header(CONTENTTYPE, TEXT_PLAIN)
                                  .send(new StringUnmarshaller());
    }
}
