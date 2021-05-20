/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
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

import static org.eclipse.che.workspace.infrastructure.kubernetes.util.TracingSpanConstants.WAIT_BROKERS_RESULT_PHASE;

import com.google.common.annotations.Beta;
import io.opentracing.Span;
import io.opentracing.Tracer;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.wsplugins.model.ChePlugin;
import org.eclipse.che.commons.tracing.TracingTags;
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

  private final BrokersResult brokersResult;
  private final String workspaceId;
  private final Tracer tracer;

  private final int resultWaitingTimeout;

  public WaitBrokerResult(
      String workspaceId, BrokersResult brokersResult, int resultWaitingTimeout, Tracer tracer) {
    this.workspaceId = workspaceId;
    this.brokersResult = brokersResult;
    this.resultWaitingTimeout = resultWaitingTimeout;
    this.tracer = tracer;
  }

  @Override
  public List<ChePlugin> execute() throws InfrastructureException {
    Span tracingSpan = tracer.buildSpan(WAIT_BROKERS_RESULT_PHASE).start();
    TracingTags.WORKSPACE_ID.set(tracingSpan, workspaceId);

    try {
      LOG.debug("Trying to get brokers result for workspace '{}'", workspaceId);
      return brokersResult.get(resultWaitingTimeout, TimeUnit.MINUTES);
    } catch (InterruptedException e) {
      TracingTags.setErrorStatus(tracingSpan, e);
      throw new InfrastructureException(
          "Plugins installation process was interrupted. Error: " + e.getMessage(), e);
    } catch (ExecutionException e) {
      TracingTags.setErrorStatus(tracingSpan, e);
      throw new InfrastructureException(
          "Plugins installation process failed. Error: " + e.getCause().getMessage(), e.getCause());
    } catch (TimeoutException e) {
      TracingTags.setErrorStatus(tracingSpan, e);
      throw new InfrastructureException("Plugins installation process timed out");
    } finally {
      tracingSpan.finish();
    }
  }
}
