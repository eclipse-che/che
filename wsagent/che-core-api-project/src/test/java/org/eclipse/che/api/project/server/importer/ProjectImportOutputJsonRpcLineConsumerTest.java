/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.project.server.importer;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.core.jsonrpc.commons.transmission.EndpointIdConfigurator;
import org.eclipse.che.api.core.jsonrpc.commons.transmission.MethodNameConfigurator;
import org.eclipse.che.api.core.jsonrpc.commons.transmission.ParamsConfigurator;
import org.eclipse.che.api.core.jsonrpc.commons.transmission.SendConfiguratorFromOne;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit test for the {@link ProjectImportOutputJsonRpcLineConsumer}.
 *
 * @author Vlad Zhukovskyi
 */
@RunWith(MockitoJUnitRunner.class)
public class ProjectImportOutputJsonRpcLineConsumerTest {

  @Mock RequestTransmitter requestTransmitter;
  @Mock ProjectImportOutputJsonRpcRegistrar registrar;

  private ProjectImportOutputJsonRpcLineConsumer consumer;

  @Before
  public void setUp() throws Exception {
    consumer =
        new ProjectImportOutputJsonRpcLineConsumer("project", requestTransmitter, registrar, 100);
  }

  @Test
  public void testShouldSendOutputLineThroughJsonRpcToEndpoint() throws Exception {
    //given
    when(registrar.getRegisteredEndpoints()).thenReturn(Collections.singleton("endpointId"));

    final EndpointIdConfigurator endpointIdConfigurator = mock(EndpointIdConfigurator.class);
    when(requestTransmitter.newRequest()).thenReturn(endpointIdConfigurator);

    final MethodNameConfigurator methodNameConfigurator = mock(MethodNameConfigurator.class);
    when(endpointIdConfigurator.endpointId(anyString())).thenReturn(methodNameConfigurator);

    final ParamsConfigurator paramsConfigurator = mock(ParamsConfigurator.class);
    when(methodNameConfigurator.methodName(anyString())).thenReturn(paramsConfigurator);

    final SendConfiguratorFromOne sendConfiguratorFromOne = mock(SendConfiguratorFromOne.class);
    when(paramsConfigurator.paramsAsDto(any())).thenReturn(sendConfiguratorFromOne);

    //when
    consumer.sendOutputLine("message");

    //then
    verify(sendConfiguratorFromOne).sendAndSkipResult();
  }
}
