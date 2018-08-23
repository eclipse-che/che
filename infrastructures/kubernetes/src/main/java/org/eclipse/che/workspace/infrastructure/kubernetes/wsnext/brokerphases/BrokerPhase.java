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
package org.eclipse.che.workspace.infrastructure.kubernetes.wsnext.brokerphases;

import com.google.common.annotations.Beta;
import java.util.List;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.wsnext.model.ChePlugin;

/**
 * Phase of Che plugin broker lifecycle used to separate and simplify different stages on Che plugin
 * broker execution.
 *
 * <p>This API is in <b>Beta</b> and is subject to changes or removal.
 *
 * @author Oleksandr Garagatyi
 */
@Beta
public interface BrokerPhase {

  /**
   * Executes this phase. May involve nested call of another {@link BrokerPhase}.
   *
   * <p>This API is in <b>Beta</b> and is subject to changes or removal.
   */
  @Beta
  List<ChePlugin> execute() throws InfrastructureException;
}
