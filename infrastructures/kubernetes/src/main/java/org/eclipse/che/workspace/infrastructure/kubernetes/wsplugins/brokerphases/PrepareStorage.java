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
package org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.brokerphases;

import com.google.common.annotations.Beta;
import io.opentracing.Span;
import java.util.List;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.wsplugins.model.ChePlugin;
import org.eclipse.che.commons.tracing.TracerUtil;
import org.eclipse.che.workspace.infrastructure.kubernetes.StartSynchronizer;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.WorkspaceVolumesStrategy;

/**
 * Prepares PVC in a workspace and calls next {@link BrokerPhase}.
 *
 * <p>This API is in <b>Beta</b> and is subject to changes or removal.
 *
 * @author Oleksandr Garagatyi
 */
@Beta
public class PrepareStorage extends BrokerPhase {

  private static final String SPAN_NAME = "PrepareStorage";

  private final KubernetesEnvironment brokerEnvironment;
  private final WorkspaceVolumesStrategy volumesStrategy;
  private final StartSynchronizer startSynchronizer;
  private final TracerUtil tracerUtil;

  public PrepareStorage(
      String workspaceId,
      KubernetesEnvironment brokerEnvironment,
      WorkspaceVolumesStrategy volumesStrategy,
      StartSynchronizer startSynchronizer,
      TracerUtil tracerUtil) {
    this.workspaceId = workspaceId;
    this.brokerEnvironment = brokerEnvironment;
    this.volumesStrategy = volumesStrategy;
    this.startSynchronizer = startSynchronizer;
    this.tracerUtil = tracerUtil;
  }

  @Override
  public List<ChePlugin> execute() throws InfrastructureException {
    Span tracingSpan = tracerUtil.buildSpan(SPAN_NAME, null, workspaceId, null);
    try {
      volumesStrategy.prepare(
          brokerEnvironment, workspaceId, startSynchronizer.getStartTimeoutMillis());
    } catch (InfrastructureException e) {
      // Ensure span is finished with exception message
      tracerUtil.finishSpanAsFailure(tracingSpan, e.getMessage());
      throw e;
    }
    tracerUtil.finishSpan(tracingSpan);
    return nextPhase.execute();
  }
}
