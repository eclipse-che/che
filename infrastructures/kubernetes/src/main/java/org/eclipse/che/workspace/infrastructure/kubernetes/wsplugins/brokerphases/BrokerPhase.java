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
import java.util.List;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.wsplugins.model.ChePlugin;

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
}
