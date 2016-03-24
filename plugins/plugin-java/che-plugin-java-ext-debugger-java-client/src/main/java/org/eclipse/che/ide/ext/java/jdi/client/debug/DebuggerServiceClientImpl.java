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
package org.eclipse.che.ide.ext.java.jdi.client.debug;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.machine.gwt.client.WsAgentUrlProvider;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.ext.java.jdi.client.JavaRuntimeLocalizationConstant;
import org.eclipse.che.ide.ext.java.jdi.shared.BreakPoint;
import org.eclipse.che.ide.ext.java.jdi.shared.DebuggerEventList;
import org.eclipse.che.ide.ext.java.jdi.shared.DebuggerInfo;
import org.eclipse.che.ide.ext.java.jdi.shared.StackFrameDump;
import org.eclipse.che.ide.ext.java.jdi.shared.UpdateVariableRequest;
import org.eclipse.che.ide.ext.java.jdi.shared.Value;
import org.eclipse.che.ide.ext.java.jdi.shared.Variable;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.StringUnmarshaller;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;

import javax.validation.constraints.NotNull;

import static org.eclipse.che.ide.MimeType.TEXT_PLAIN;
import static org.eclipse.che.ide.rest.HTTPHeader.ACCEPT;
import static org.eclipse.che.ide.rest.HTTPHeader.CONTENTTYPE;

/**
 * The implementation of {@link DebuggerServiceClient}.
 *
 * @author Vitaly Parfonov
 * @author Artem Zatsarynnyi
 */
@Singleton
public class DebuggerServiceClientImpl implements DebuggerServiceClient {
    private final String                          baseUrl;
    private final LoaderFactory                   loaderFactory;
    private final AsyncRequestFactory             asyncRequestFactory;
    private final JavaRuntimeLocalizationConstant localizationConstant;
    private final WsAgentUrlProvider              urlProvider;

    @Inject
    protected DebuggerServiceClientImpl(AppContext appContext,
                                        LoaderFactory loaderFactory,
                                        AsyncRequestFactory asyncRequestFactory,
                                        JavaRuntimeLocalizationConstant localizationConstant,
                                        WsAgentUrlProvider urlProvider) {
        this.loaderFactory = loaderFactory;
        this.asyncRequestFactory = asyncRequestFactory;
        this.localizationConstant = localizationConstant;
        this.baseUrl = "/debug-java/" + appContext.getWorkspaceId();
        this.urlProvider = urlProvider;
    }

    /** {@inheritDoc} */
    @Override
    public void connect(@NotNull String host, int port, @NotNull AsyncRequestCallback<DebuggerInfo> callback) {
        final String requestUrl = urlProvider.get() + baseUrl + "/connect";
        final String params = "?host=" + host + "&port=" + port;
        asyncRequestFactory.createGetRequest(requestUrl + params).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void disconnect(@NotNull String id, @NotNull AsyncRequestCallback<Void> callback) {
        final String requestUrl = urlProvider.get() + baseUrl + "/disconnect/" + id;
        asyncRequestFactory.createGetRequest(requestUrl).loader(loaderFactory.newLoader(localizationConstant.debuggerDisconnectingTitle()))
                           .send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void addBreakpoint(@NotNull String id, @NotNull BreakPoint breakPoint, @NotNull AsyncRequestCallback<Void> callback) {
        final String requestUrl = urlProvider.get() + baseUrl + "/breakpoints/add/" + id;
        asyncRequestFactory.createPostRequest(requestUrl, breakPoint).loader(loaderFactory.newLoader()).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void getAllBreakpoints(@NotNull String id, @NotNull AsyncRequestCallback<String> callback) {
        final String requestUrl = urlProvider.get() + baseUrl + "/breakpoints/" + id;
        asyncRequestFactory.createGetRequest(requestUrl).loader(loaderFactory.newLoader()).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void deleteBreakpoint(@NotNull String id, @NotNull BreakPoint breakPoint, @NotNull AsyncRequestCallback<Void> callback) {
        final String requestUrl = urlProvider.get() + baseUrl + "/breakpoints/delete/" + id;
        asyncRequestFactory.createPostRequest(requestUrl, breakPoint).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void deleteAllBreakpoints(@NotNull String id, @NotNull AsyncRequestCallback<String> callback) {
        final String requestUrl = urlProvider.get() + baseUrl + "/breakpoints/delete_all/" + id;
        asyncRequestFactory.createGetRequest(requestUrl).loader(loaderFactory.newLoader()).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void checkEvents(@NotNull String id, @NotNull AsyncRequestCallback<DebuggerEventList> callback) {
        final String requestUrl = urlProvider.get() + baseUrl + "/events/" + id;
        asyncRequestFactory.createGetRequest(requestUrl).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void getStackFrameDump(@NotNull String id, @NotNull AsyncRequestCallback<StackFrameDump> callback) {
        final String requestUrl = urlProvider.get() + baseUrl + "/dump/" + id;
        asyncRequestFactory.createGetRequest(requestUrl).loader(loaderFactory.newLoader()).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void resume(@NotNull String id, @NotNull AsyncRequestCallback<Void> callback) {
        final String requestUrl = urlProvider.get() + baseUrl + "/resume/" + id;
        asyncRequestFactory.createGetRequest(requestUrl).loader(loaderFactory.newLoader()).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void getValue(@NotNull String id, @NotNull Variable var, @NotNull AsyncRequestCallback<Value> callback) {
        final String requestUrl = urlProvider.get() + baseUrl + "/value/get/" + id;
        asyncRequestFactory.createPostRequest(requestUrl, var.getVariablePath()).loader(loaderFactory.newLoader()).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void setValue(@NotNull String id, @NotNull UpdateVariableRequest request, @NotNull AsyncRequestCallback<Void> callback) {
        final String requestUrl = urlProvider.get() + baseUrl + "/value/set/" + id;
        asyncRequestFactory.createPostRequest(requestUrl, request).loader(loaderFactory.newLoader()).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void stepInto(@NotNull String id, @NotNull AsyncRequestCallback<Void> callback) {
        final String requestUrl = urlProvider.get() + baseUrl + "/step/into/" + id;
        asyncRequestFactory.createGetRequest(requestUrl).loader(loaderFactory.newLoader()).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void stepOver(@NotNull String id, @NotNull AsyncRequestCallback<Void> callback) {
        final String requestUrl = urlProvider.get() + baseUrl + "/step/over/" + id;
        asyncRequestFactory.createGetRequest(requestUrl).loader(loaderFactory.newLoader()).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void stepOut(@NotNull String id, @NotNull AsyncRequestCallback<Void> callback) {
        final String requestUrl = urlProvider.get() + baseUrl + "/step/out/" + id;
        asyncRequestFactory.createGetRequest(requestUrl).loader(loaderFactory.newLoader()).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public Promise<String> evaluateExpression(@NotNull String id, @NotNull String expression) {
        final String requestUrl = urlProvider.get() + baseUrl + "/expression/" + id;
        return asyncRequestFactory.createPostRequest(requestUrl, null)
                                  .data(expression)
                                  .header(ACCEPT, TEXT_PLAIN)
                                  .header(CONTENTTYPE, TEXT_PLAIN)
                                  .send(new StringUnmarshaller());
    }
}
