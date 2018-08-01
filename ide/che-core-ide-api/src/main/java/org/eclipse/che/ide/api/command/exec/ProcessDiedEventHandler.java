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
package org.eclipse.che.ide.api.command.exec;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.agent.exec.shared.dto.event.ProcessDiedEventDto;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.ide.util.loging.Log;

/**
 * Handles event fired by exec agent when process died
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public class ProcessDiedEventHandler extends AbstractExecAgentEventHandler<ProcessDiedEventDto> {

  @Inject
  public void configureHandler(RequestHandlerConfigurator configurator) {
    configurator
        .newConfiguration()
        .methodName("process_died")
        .paramsAsDto(ProcessDiedEventDto.class)
        .noResult()
        .withBiConsumer(this);
  }

  @Override
  public void accept(String endpointId, ProcessDiedEventDto params) {
    Log.debug(getClass(), "Handling process died event. Params: " + params);
    handle(endpointId, params);
  }
}
