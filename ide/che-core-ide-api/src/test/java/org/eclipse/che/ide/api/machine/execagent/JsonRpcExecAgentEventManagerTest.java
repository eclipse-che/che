/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.api.machine.execagent;

import org.eclipse.che.api.machine.shared.dto.execagent.event.ProcessDiedEventDto;
import org.eclipse.che.api.machine.shared.dto.execagent.event.ProcessStartedEventDto;
import org.eclipse.che.api.machine.shared.dto.execagent.event.ProcessStdErrEventDto;
import org.eclipse.che.api.machine.shared.dto.execagent.event.ProcessStdOutEventDto;
import org.eclipse.che.api.promises.client.Operation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Tests for {@link JsonRpcExecAgentEventManager}
 *
 * @author Dmitry Kuleshov
 */
@RunWith(MockitoJUnitRunner.class)
public class JsonRpcExecAgentEventManagerTest {
    @Mock
    private ProcessDiedEventHandler      processDiedEventHandler;
    @Mock
    private ProcessStartedEventHandler   processStartedEventHandler;
    @Mock
    private ProcessStdErrEventHandler    processStdErrEventHandler;
    @Mock
    private ProcessStdOutEventHandler    processStdOutEventHandler;
    @InjectMocks
    private JsonRpcExecAgentEventManager eventManager;

    @Mock
    Operation<ProcessDiedEventDto>    processDiedOperation;
    @Mock
    Operation<ProcessStartedEventDto> processStartedOperation;
    @Mock
    Operation<ProcessStdErrEventDto>  processStdErrOperation;
    @Mock
    Operation<ProcessStdOutEventDto>  processStdOutOperation;

    @Test
    public void shouldProperlyRegisterProcessDiedOperation(){
        eventManager.registerProcessDiedOperation("endpoint", 0, processDiedOperation);

        Mockito.verify(processDiedEventHandler).registerOperation("endpoint", 0, processDiedOperation);
    }

    @Test
    public void shouldProperlyRegisterProcessStartedOperation(){
        eventManager.registerProcessStartedOperation("endpoint", 0, processStartedOperation);

        Mockito.verify(processStartedEventHandler).registerOperation("endpoint", 0, processStartedOperation);
    }

    @Test
    public void shouldProperlyRegisterProcessStdErrOperation(){
        eventManager.registerProcessStdErrOperation("endpoint", 0, processStdErrOperation);

        Mockito.verify(processStdErrEventHandler).registerOperation("endpoint", 0, processStdErrOperation);
    }

    @Test
    public void shouldProperlyRegisterProcessStdOutOperation() {
        eventManager.registerProcessStdOutOperation("endpoint", 0, processStdOutOperation);

        Mockito.verify(processStdOutEventHandler).registerOperation("endpoint", 0, processStdOutOperation);
    }
}
