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
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import javax.annotation.PostConstruct;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.machine.DevMachine;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStartedEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStoppedEvent;
import org.eclipse.che.ide.commons.exception.UnmarshallerException;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.rest.AsyncRequest;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.Unmarshallable;
import org.eclipse.che.multiuser.machine.authentication.shared.dto.MachineTokenDto;

/**
 * Looks at the request and substitutes an appropriate implementation.
 *
 * @author Anton Korneta
 */
@Singleton
public class MachineAsyncRequestFactory extends AsyncRequestFactory
    implements WorkspaceStoppedEvent.Handler, WorkspaceStartedEvent.Handler {
  private static final String CSRF_TOKEN_HEADER_NAME = "X-CSRF-Token";

  private final Provider<MachineTokenServiceClient> machineTokenServiceProvider;
  private final AppContext appContext;

  private String machineToken;
  private String wsAgentBaseUrl;
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
    eventBus.addHandler(WorkspaceStartedEvent.TYPE, this);
    eventBus.addHandler(WorkspaceStoppedEvent.TYPE, this);
  }

  @PostConstruct
  private void init() {
    requestCsrfToken();
  }

  @Override
  protected AsyncRequest newAsyncRequest(RequestBuilder.Method method, String url, boolean async) {
    if (isWsAgentRequest(url)) {
      return new MachineAsyncRequest(method, url, async, getMachineToken());
    }
    if (isModifyingMethod(method)) {
      return new CsrfPreventingAsyncModifyingRequest(method, url, async);
    }
    return super.newAsyncRequest(method, url, async);
  }

  private Promise<String> getMachineToken() {
    if (!isNullOrEmpty(machineToken)) {
      return Promises.resolve(machineToken);
    } else {
      return machineTokenServiceProvider
          .get()
          .getMachineToken()
          .then(
              (Function<MachineTokenDto, String>)
                  tokenDto -> {
                    machineToken = tokenDto.getMachineToken();
                    return machineToken;
                  });
    }
  }

  @Override
  public void onWorkspaceStarted(WorkspaceStartedEvent event) {
    getMachineToken()
        .then(
            machineToken -> {
              if (!isNullOrEmpty(machineToken)) {
                appContext.getProperties().put("machineToken", machineToken);
              }
            });
  }

  // since the machine token lives with the workspace runtime,
  // we need to invalidate it on stopping workspace.
  @Override
  public void onWorkspaceStopped(WorkspaceStoppedEvent event) {
    machineToken = null;
    wsAgentBaseUrl = null;
  }

  /**
   * Going to check is this request goes to WsAgent
   *
   * @param url
   * @return
   */
  protected boolean isWsAgentRequest(String url) {
    if (appContext.getWorkspace() == null
        || !RUNNING.equals(appContext.getWorkspace().getStatus())) {
      return false; // ws-agent not started
    }
    if (isNullOrEmpty(wsAgentBaseUrl)) {
      final DevMachine devMachine = appContext.getDevMachine();
      if (devMachine != null) {
        wsAgentBaseUrl = devMachine.getWsAgentBaseUrl();
      } else {
        return false;
      }
    }
    return url.contains(nullToEmpty(wsAgentBaseUrl));
  }

  private Promise<String> requestCsrfToken() {
    if (csrfToken != null) {
      return Promises.resolve(csrfToken);
    }
    return createGetRequest(appContext.getMasterEndpoint() + "/profile")
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
