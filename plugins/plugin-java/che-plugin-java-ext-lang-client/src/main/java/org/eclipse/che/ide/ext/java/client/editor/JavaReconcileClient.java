/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.java.client.editor;

import static org.eclipse.che.ide.api.jsonrpc.Constants.WS_AGENT_JSON_RPC_ENDPOINT_ID;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcPromise;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.java.shared.dto.JavaClassInfo;
import org.eclipse.che.ide.ext.java.shared.dto.ReconcileResult;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.util.loging.Log;

/** @author Evgen Vidolob */
@Singleton
public class JavaReconcileClient {
  private static final String OUTCOMING_METHOD = "request:java-reconcile";

  private final RequestTransmitter requestTransmitter;
  private final DtoUnmarshallerFactory dtoUnmarshallerFactory;
  private final AsyncRequestFactory asyncRequestFactory;
  private final AppContext appContext;
  private DtoFactory dtoFactory;

  @Inject
  public JavaReconcileClient(
      DtoUnmarshallerFactory dtoUnmarshallerFactory,
      AppContext appContext,
      RequestTransmitter requestTransmitter,
      AsyncRequestFactory asyncRequestFactory,
      DtoFactory dtoFactory) {
    this.appContext = appContext;
    this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
    this.asyncRequestFactory = asyncRequestFactory;
    this.requestTransmitter = requestTransmitter;
    this.dtoFactory = dtoFactory;
  }

  /**
   * Sends request on server side to reconcile entity by given fqn
   *
   * @param fqn fully qualified name of entity to reconcile
   * @param projectPath path to the project which contains the entity to reconcile
   * @return promise which represents result of reconcile operation
   */
  public JsonRpcPromise<ReconcileResult> reconcile(String fqn, String projectPath) {
    JavaClassInfo javaClassInfo =
        dtoFactory.createDto(JavaClassInfo.class).withFQN(fqn).withProjectPath(projectPath);
    return requestTransmitter
        .newRequest()
        .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
        .methodName(OUTCOMING_METHOD)
        .paramsAsDto(javaClassInfo)
        .sendAndReceiveResultAsDto(ReconcileResult.class);
  }

  /** @deprecated in favor of {@link #reconcile(String, String)} */
  @Deprecated
  public void reconcile(String projectPath, String fqn, final ReconcileCallback callback) {
    String url =
        appContext.getWsAgentServerApiEndpoint()
            + "/java/reconcile/?projectpath="
            + projectPath
            + "&fqn="
            + fqn;
    asyncRequestFactory
        .createGetRequest(url)
        .send(
            new AsyncRequestCallback<ReconcileResult>(
                dtoUnmarshallerFactory.newUnmarshaller(ReconcileResult.class)) {
              @Override
              protected void onSuccess(ReconcileResult result) {
                callback.onReconcile(result);
              }

              @Override
              protected void onFailure(Throwable exception) {
                Log.error(JavaReconcileClient.class, exception);
              }
            });
  }

  /** @deprecated use {@link #reconcile(String, String)} which does not use callback */
  @Deprecated
  public interface ReconcileCallback {
    void onReconcile(ReconcileResult result);
  }
}
