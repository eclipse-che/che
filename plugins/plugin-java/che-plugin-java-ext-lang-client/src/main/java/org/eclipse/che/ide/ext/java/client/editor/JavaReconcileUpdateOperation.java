/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.java.client.editor;

import com.google.web.bindery.event.shared.EventBus;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.project.shared.dto.ServerError;
import org.eclipse.che.ide.ext.java.shared.dto.ReconcileResult;
import org.eclipse.che.ide.util.loging.Log;

/**
 * Receives result of reconcile operations from server side and notifies interested client consumers
 * about it.
 *
 * @author Roman Nikitenko
 */
@Singleton
public class JavaReconcileUpdateOperation {
  private static final String JAVA_RECONCILE_ERROR_METHOD = "event:java-reconcile-error";
  private static final String JAVA_RECONCILE_STATE_CHANGED_METHOD =
      "event:java-reconcile-state-changed";

  private final EventBus eventBus;

  @Inject
  public JavaReconcileUpdateOperation(EventBus eventBus) {
    this.eventBus = eventBus;
  }

  @Inject
  public void configureHandler(RequestHandlerConfigurator configurator) {
    configurator
        .newConfiguration()
        .methodName(JAVA_RECONCILE_ERROR_METHOD)
        .paramsAsDto(ServerError.class)
        .noResult()
        .withConsumer(this::onError);

    configurator
        .newConfiguration()
        .methodName(JAVA_RECONCILE_STATE_CHANGED_METHOD)
        .paramsAsDto(ReconcileResult.class)
        .noResult()
        .withConsumer(this::onSuccess);
  }

  private void onSuccess(ReconcileResult reconcileResult) {
    eventBus.fireEvent(new ReconcileOperationEvent(reconcileResult));
  }

  private void onError(ServerError reconcileError) {
    Log.error(getClass(), reconcileError.getMessage());
  }
}
