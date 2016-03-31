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
import com.google.inject.name.Named;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.ext.java.jdi.client.JavaRuntimeLocalizationConstant;
import org.eclipse.che.ide.ext.java.jdi.shared.BreakPoint;
import org.eclipse.che.ide.ext.java.jdi.shared.BreakPointList;
import org.eclipse.che.ide.ext.java.jdi.shared.DebuggerEventList;
import org.eclipse.che.ide.ext.java.jdi.shared.JavaDebuggerInfo;
import org.eclipse.che.ide.ext.java.jdi.shared.StackFrameDump;
import org.eclipse.che.ide.ext.java.jdi.shared.UpdateVariableRequest;
import org.eclipse.che.ide.ext.java.jdi.shared.Value;
import org.eclipse.che.ide.ext.java.jdi.shared.Variable;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.StringUnmarshaller;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;

import javax.validation.constraints.NotNull;
import java.util.Map;

import static org.eclipse.che.ide.MimeType.TEXT_PLAIN;
import static org.eclipse.che.ide.ext.java.jdi.client.debug.JavaDebugger.JavaConnectionProperties.HOST;
import static org.eclipse.che.ide.ext.java.jdi.client.debug.JavaDebugger.JavaConnectionProperties.PORT;
import static org.eclipse.che.ide.rest.HTTPHeader.ACCEPT;
import static org.eclipse.che.ide.rest.HTTPHeader.CONTENTTYPE;

/**
 * The implementation of {@link DebuggerServiceClient}.
 *
 * @author Vitaly Parfonov
 * @author Artem Zatsarynnyi
 * @author Anatoliy Bazko
 */
@Singleton
public class JavaDebuggerServiceClientImpl implements DebuggerServiceClient {
    private final String                          baseUrl;
    private final LoaderFactory                   loaderFactory;
    private final AsyncRequestFactory             asyncRequestFactory;
    private final JavaRuntimeLocalizationConstant localizationConstant;
    private final DtoUnmarshallerFactory          dtoUnmarshallerFactory;

    @Inject
    protected JavaDebuggerServiceClientImpl(@Named("cheExtensionPath") String extPath,
                                            AppContext appContext,
                                            LoaderFactory loaderFactory,
                                            AsyncRequestFactory asyncRequestFactory,
                                            JavaRuntimeLocalizationConstant localizationConstant,
                                            DtoUnmarshallerFactory dtoUnmarshallerFactory) {
        this.loaderFactory = loaderFactory;
        this.asyncRequestFactory = asyncRequestFactory;
        this.localizationConstant = localizationConstant;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.baseUrl = extPath + "/debug-java/" + appContext.getWorkspace().getId();
    }

    @Override
    public Promise<JavaDebuggerInfo> connect(Map<String, String> connectionProperties) {
        final String requestUrl = baseUrl + "/connect";
        final String params = "?host=" + connectionProperties.get(HOST.toString()) + "&port=" + connectionProperties.get(PORT.toString());

        return asyncRequestFactory.createGetRequest(requestUrl + params)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(JavaDebuggerInfo.class));
    }

    @Override
    public Promise<Void> disconnect(@NotNull String id) {
        final String requestUrl = baseUrl + "/disconnect/" + id;
        return asyncRequestFactory.createGetRequest(requestUrl)
                                  .loader(loaderFactory.newLoader(localizationConstant.debuggerDisconnectingTitle()))
                                  .send();
    }

    @Override
    public Promise<Void> addBreakpoint(@NotNull String id, @NotNull BreakPoint breakPoint) {
        final String requestUrl = baseUrl + "/breakpoints/add/" + id;
        return asyncRequestFactory.createPostRequest(requestUrl, breakPoint)
                                  .loader(loaderFactory.newLoader())
                                  .send();
    }

    @Override
    public Promise<Void> deleteBreakpoint(@NotNull String id, @NotNull BreakPoint breakPoint) {
        final String requestUrl = baseUrl + "/breakpoints/delete/" + id;
        return asyncRequestFactory.createPostRequest(requestUrl, breakPoint)
                                  .send();
    }

    @Override
    public Promise<BreakPointList> getAllBreakpoints(@NotNull String id) {
        final String requestUrl = baseUrl + "/breakpoints/" + id;
        return asyncRequestFactory.createGetRequest(requestUrl)
                                  .loader(loaderFactory.newLoader())
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(BreakPointList.class));
    }

    @Override
    public Promise<Void> deleteAllBreakpoints(@NotNull String id) {
        final String requestUrl = baseUrl + "/breakpoints/delete_all/" + id;
        return asyncRequestFactory.createGetRequest(requestUrl).loader(loaderFactory.newLoader()).send();
    }

    @Override
    public Promise<DebuggerEventList> checkEvents(@NotNull String id) {
        final String requestUrl = baseUrl + "/events/" + id;
        return asyncRequestFactory.createGetRequest(requestUrl).send(dtoUnmarshallerFactory.newUnmarshaller(DebuggerEventList.class));
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
    public Promise<Value> getValue(@NotNull String id, @NotNull Variable var) {
        final String requestUrl = baseUrl + "/value/get/" + id;
        return asyncRequestFactory.createPostRequest(requestUrl, var.getVariablePath())
                                  .loader(loaderFactory.newLoader())
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(Value.class));
    }

    @Override
    public Promise<Void> setValue(@NotNull String id, @NotNull UpdateVariableRequest request) {
        final String requestUrl = baseUrl + "/value/set/" + id;
        return asyncRequestFactory.createPostRequest(requestUrl, request).loader(loaderFactory.newLoader()).send();
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
