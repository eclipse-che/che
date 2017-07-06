/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.project.server.importer;

import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.project.shared.dto.ImportProgressRecordDto;

import java.util.concurrent.atomic.AtomicInteger;

import static org.eclipse.che.api.project.shared.Constants.EVENT_IMPORT_OUTPUT_PROGRESS;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * Importer output line consumer that perform broadcasting consumed output through the json rpc protocol to the specific method.
 *
 * @author Vlad Zhukovskyi
 * @since 5.9.0
 */
public class ProjectImportOutputJsonRpcLineConsumer extends BaseProjectImportOutputLineConsumer {

    private final AtomicInteger                       lineCounter;
    private final RequestTransmitter                  transmitter;
    private final ProjectImportOutputJsonRpcRegistrar endpointIdRegistrar;

    public ProjectImportOutputJsonRpcLineConsumer(String projectName,
                                                  RequestTransmitter transmitter,
                                                  ProjectImportOutputJsonRpcRegistrar endpointIdRegistrar,
                                                  int delayBetweenMessages) {
        super(projectName, delayBetweenMessages);
        this.transmitter = transmitter;
        this.endpointIdRegistrar = endpointIdRegistrar;

        lineCounter = new AtomicInteger(1);
    }

    @Override
    protected void sendOutputLine(String outputLine) {
        final ImportProgressRecordDto progressRecord = newDto(ImportProgressRecordDto.class).withNum(lineCounter.getAndIncrement())
                                                                                            .withLine(outputLine)
                                                                                            .withProjectName(projectName);

        endpointIdRegistrar.getRegisteredEndpoints()
                           .forEach(it -> transmitter.newRequest().endpointId(it).methodName(EVENT_IMPORT_OUTPUT_PROGRESS + "/" + projectName).paramsAsDto(progressRecord).sendAndSkipResult());
    }
}
