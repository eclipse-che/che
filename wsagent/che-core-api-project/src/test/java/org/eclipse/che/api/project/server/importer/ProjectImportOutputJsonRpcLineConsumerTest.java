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

import org.eclipse.che.api.core.jsonrpc.RequestTransmitter;
import org.eclipse.che.api.project.shared.dto.ImportProgressRecordDto;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for the {@link ProjectImportOutputJsonRpcLineConsumer}.
 *
 * @author Vlad Zhukovskyi
 */
@RunWith(MockitoJUnitRunner.class)
public class ProjectImportOutputJsonRpcLineConsumerTest {

    @Mock
    RequestTransmitter                  requestTransmitter;
    @Mock
    ProjectImportOutputJsonRpcRegistrar registrar;

    private ProjectImportOutputJsonRpcLineConsumer consumer;

    @Before
    public void setUp() throws Exception {
        consumer = new ProjectImportOutputJsonRpcLineConsumer("project", requestTransmitter, registrar, 100);
    }

    @Test
    public void testShouldSendOutputLineThroughJsonRpcToEndpoint() throws Exception {
        //given
        when(registrar.getRegisteredEndpoints()).thenReturn(Collections.singleton("endpointId"));

        //when
        consumer.sendOutputLine("message");

        //then
        verify(requestTransmitter).transmitOneToNone(eq("endpointId"),
                                                     eq("event:import-project:progress"),
                                                     eq(newDto(ImportProgressRecordDto.class).withNum(1)
                                                                                             .withLine("message")
                                                                                             .withProjectName("project")));
    }
}