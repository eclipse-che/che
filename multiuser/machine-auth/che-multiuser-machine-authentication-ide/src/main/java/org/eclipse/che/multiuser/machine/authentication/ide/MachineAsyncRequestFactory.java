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
package org.eclipse.che.multiuser.machine.authentication.ide;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.base.Strings.nullToEmpty;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import java.util.Optional;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.workspace.model.MachineImpl;
import org.eclipse.che.ide.api.workspace.model.RuntimeImpl;
import org.eclipse.che.ide.api.workspace.model.WorkspaceImpl;
import org.eclipse.che.ide.commons.exception.UnmarshallerException;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.rest.AsyncRequest;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.Unmarshallable;
import org.eclipse.che.ide.util.loging.Log;

/**
 * Looks at the request and substitutes an appropriate implementation.
 *
 * @author Anton Korneta
 */
@Singleton
public class MachineAsyncRequestFactory extends AsyncRequestFactory {
  private static final String CSRF_TOKEN_HEADER_NAME = "X-CSRF-Token";

  private final Provider<MachineTokenServiceClient> machineTokenServiceProvider;
  private AppContext appContext;
  private String machineToken;

  private String csrfToken;


  @Inject
  public MachineAsyncRequestFactory(
      DtoFactory dtoFactory,
      Provider<MachineTokenServiceClient> machineTokenServiceProvider,
      AppContext appContext,
      EventBus eventBus) {
    super(dtoFactory);
    this.machineTokenServiceProvider = machineTokenServiceProvider;
    this.appContext = appContext;
  }

  @Override
  protected AsyncRequest newAsyncRequest(RequestBuilder.Method method, String url, boolean async) {
    String machineToken = appContext.getWorkspace().getRuntime().getMachineToken();
    if (isWsAgentRequest(url) && !isNullOrEmpty(machineToken)) {
      return new MachineAsyncRequest(method, url, false, machineToken);
    }
    if (isModifyingMethod(method)) {
      return new CsrfPreventingAsyncModifyingRequest(method, url, async);
    }
    return super.newAsyncRequest(method, url, async);
  }



  /**
   * Going to check is this request goes to WsAgent
   *
   * @param url
   * @return
   */
  protected boolean isWsAgentRequest(String url) {
    WorkspaceImpl currentWorkspace = appContext.getWorkspace();
    if (currentWorkspace == null || !isWsAgentStarted(currentWorkspace)) {
      Log.info(getClass(), "return false, URL:" + url);
      return false; // ws-agent not started
    }
    final boolean contains = url.contains(nullToEmpty(appContext.getWsAgentServerApiEndpoint()));
    Log.info(getClass(), contains + " : " + url + " >> " +  appContext.getWsAgentServerApiEndpoint());
    return contains;
  }

  private boolean isWsAgentStarted(WorkspaceImpl workspace) {
    if (workspace == null) {
      return false;
    }
    RuntimeImpl runtime = workspace.getRuntime();
    if (runtime == null) {
      return false;
    }

    Optional<MachineImpl> devMachine = runtime.getDevMachine();

    return devMachine.isPresent();
  }

  private Promise<String> requestCsrfToken() {
    if (csrfToken != null) {
      return Promises.resolve(csrfToken);
    }
    return createGetRequest(appContext.getMasterApiEndpoint() + "/profile")
        .header(CSRF_TOKEN_HEADER_NAME, "Fetch")
        .send(
            new Unmarshallable<String>() {
              @Override
              public void unmarshal(Response response) throws UnmarshallerException {
                csrfToken = response.getHeader(CSRF_TOKEN_HEADER_NAME);
                if (csrfToken != null) {
                  appContext.getProperties().put(CSRF_TOKEN_HEADER_NAME, csrfToken);
                }
              }

              @Override
              public String getPayload() {
                return csrfToken;
              }
            });
  }

  private class CsrfPreventingAsyncModifyingRequest extends AsyncRequest {

    protected CsrfPreventingAsyncModifyingRequest(
        RequestBuilder.Method method, String url, boolean async) {
      super(method, url, async);
    }

    @Override
    public void send(AsyncRequestCallback<?> callback) {
      requestCsrfToken()
          .then(
              token -> {
                super.header(CSRF_TOKEN_HEADER_NAME, token);
                super.send(callback);
              })
          .catchError(
              arg -> {
                super.send(callback);
              });
    }
  }

  private boolean isModifyingMethod(RequestBuilder.Method method) {
    return method == RequestBuilder.POST
        || method == RequestBuilder.PUT
        || method == RequestBuilder.DELETE;
  }
}
