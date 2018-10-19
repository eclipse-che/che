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
package org.eclipse.che.plugin.java.languageserver;

import static com.google.common.collect.Sets.newConcurrentHashSet;
import static org.eclipse.che.ide.ext.java.shared.Constants.PROGRESS_OUTPUT_SUBSCRIBE;
import static org.eclipse.che.ide.ext.java.shared.Constants.PROGRESS_OUTPUT_UNSUBSCRIBE;
import static org.eclipse.che.ide.ext.java.shared.Constants.PROGRESS_REPORT_METHOD;

import com.google.inject.Singleton;
import java.util.Set;
import javax.inject.Inject;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.jdt.ls.extension.api.dto.ProgressReport;

/** Send jdt.ls events using JSON RPC to the clients. */
@Singleton
public class ProcessorJsonRpcCommunication {
  private final Set<String> endpointIds = newConcurrentHashSet();
  private RequestTransmitter transmitter;

  @Inject
  public ProcessorJsonRpcCommunication(RequestTransmitter transmitter) {
    this.transmitter = transmitter;
  }

  @Inject
  private void configureHandlers(RequestHandlerConfigurator configurator) {
    configurator
        .newConfiguration()
        .methodName(PROGRESS_OUTPUT_SUBSCRIBE)
        .noParams()
        .noResult()
        .withConsumer(endpointIds::add);

    configurator
        .newConfiguration()
        .methodName(PROGRESS_OUTPUT_UNSUBSCRIBE)
        .noParams()
        .noResult()
        .withConsumer(endpointIds::remove);
  }

  public void sendProgressNotification(ProgressReport report) {
    ProgressReport reportDto = DtoFactory.newDto(ProgressReport.class);
    reportDto.setComplete(report.isComplete());
    reportDto.setStatus(report.getStatus());
    reportDto.setTask(report.getTask());
    reportDto.setTotalWork(report.getTotalWork());
    reportDto.setWorkDone(report.getWorkDone());
    reportDto.setSubTask(report.getSubTask());

    endpointIds.forEach(
        it ->
            transmitter
                .newRequest()
                .endpointId(it)
                .methodName(PROGRESS_REPORT_METHOD)
                .paramsAsDto(report)
                .sendAndSkipResult());
  }
}
