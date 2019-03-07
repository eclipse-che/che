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
import io.opentracing.Tracer;
import java.util.List;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.wsplugins.model.ChePlugin;
import org.eclipse.che.commons.tracing.TracingTags;

/**
 * Phase of Che plugin broker lifecycle used to separate and simplify different stages on Che plugin
 * broker execution.
 *
 * <p>This API is in <b>Beta</b> and is subject to changes or removal.
 *
 * @author Oleksandr Garagatyi
 */
@Beta
public abstract class BrokerPhase {

  protected BrokerPhase nextPhase;
  protected String workspaceId;
  protected String spanName;
  protected Tracer tracer;

  @Beta
  public BrokerPhase then(BrokerPhase next) {
    this.nextPhase = next;
    return next;
  }

  /**
   * Executes this phase. Broker phase implementation should call next {@link BrokerPhase} if it is
   * set.
   *
   * <p>This API is in <b>Beta</b> and is subject to changes or removal.
   *
   * @throws InfrastructureException when an error occurs during the progressing of this stage
   */
  @Beta
  public abstract List<ChePlugin> execute() throws InfrastructureException;

  /**
   * Start a new span using provided tracer. If tracer is null, returns null.
   *
   * <p>Meant to be used with {@link BrokerPhase#finishSpanIfExists(Span)}
   */
  public Span startTracingPhase() {
    if (tracer == null) {
      return null;
    }

    return tracer
        .buildSpan(spanName)
        .asChildOf(tracer.activeSpan())
        .withTag(TracingTags.WORKSPACE_ID.getKey(), workspaceId)
        .start();
  }

  /**
   * Finish provided span if it is not null. Otherwise, do nothing.
   *
   * <p>Meant to be used with {@link #startTracingPhase(Tracer, String, String)}
   */
  public void finishSpanIfExists(Span span) {
    if (span != null) {
      span.finish();
    }
  }
}
