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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.wsplugins.model.ChePlugin;
import org.eclipse.che.commons.tracing.TracerUtil;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.BrokersResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wait until Che plugin broker future finishes and returns resulting workspace tooling or error.
 * Also calls next {@link BrokerPhase}.
 *
 * <p>This API is in <b>Beta</b> and is subject to changes or removal.
 *
 * @author Oleksandr Garagatyi
 */
@Beta
public class WaitBrokerResult extends BrokerPhase {

  private static final Logger LOG = LoggerFactory.getLogger(WaitBrokerResult.class);
  private static final String SPAN_NAME = "WaitBrokerResult";

  private final BrokersResult brokersResult;
  private final int resultWaitingTimeout;
  private final TracerUtil tracerUtil;

  public WaitBrokerResult(
      String workspaceId,
      BrokersResult brokersResult,
      int resultWaitingTimeout,
      TracerUtil tracerUtil) {
    this.workspaceId = workspaceId;
    this.brokersResult = brokersResult;
    this.resultWaitingTimeout = resultWaitingTimeout;
    this.tracerUtil = tracerUtil;
  }

  @Override
  public List<ChePlugin> execute() throws InfrastructureException {
    Span tracingSpan = tracerUtil.buildSpan(SPAN_NAME, null, workspaceId, null);
    try {
      LOG.debug("Trying to get brokers result for workspace '{}'", workspaceId);
      return brokersResult.get(resultWaitingTimeout, TimeUnit.MINUTES);
    } catch (InterruptedException e) {
      tracerUtil.finishSpanAsFailure(tracingSpan, e.getMessage());
      throw new InfrastructureException(
          "Plugins installation process was interrupted. Error: " + e.getMessage(), e);
    } catch (ExecutionException e) {
      tracerUtil.finishSpanAsFailure(tracingSpan, e.getMessage());
      throw new InfrastructureException(
          "Plugins installation process failed. Error: " + e.getCause().getMessage(), e.getCause());
    } catch (TimeoutException e) {
      tracerUtil.finishSpanAsFailure(tracingSpan, e.getMessage());
      throw new InfrastructureException("Plugins installation process timed out");
    } finally {
      tracerUtil.finishSpan(tracingSpan);
    }
  }
}
