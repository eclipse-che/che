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

import org.eclipse.che.api.machine.shared.dto.execagent.event.ProcessDiedEventWithPidDto;
import org.eclipse.che.api.machine.shared.dto.execagent.event.ProcessStartedEventWithPidDto;
import org.eclipse.che.api.machine.shared.dto.execagent.event.ProcessStdErrEventWithPidDto;
import org.eclipse.che.api.machine.shared.dto.execagent.event.ProcessStdOutEventWithPidDto;
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
    Operation<ProcessDiedEventWithPidDto> processDiedOperation;
    @Mock
    Operation<ProcessStartedEventWithPidDto> processStartedOperation;
    @Mock
    Operation<ProcessStdErrEventWithPidDto> processStdErrOperation;
    @Mock
    Operation<ProcessStdOutEventWithPidDto> processStdOutOperation;

    @Test
    public void shouldProperlyRegisterProcessDiedOperation(){
        eventManager.registerProcessDiedOperation(0, processDiedOperation);

        Mockito.verify(processDiedEventHandler).registerOperation(0, processDiedOperation);
    }

    @Test
    public void shouldProperlyRegisterProcessStartedOperation(){
        eventManager.registerProcessStartedOperation(0, processStartedOperation);

        Mockito.verify(processStartedEventHandler).registerOperation(0, processStartedOperation);
    }

    @Test
    public void shouldProperlyRegisterProcessStdErrOperation(){
        eventManager.registerProcessStdErrOperation(0, processStdErrOperation);

        Mockito.verify(processStdErrEventHandler).registerOperation(0, processStdErrOperation);
    }

    @Test
    public void shouldProperlyRegisterProcessStdOutOperation() {
        eventManager.registerProcessStdOutOperation(0, processStdOutOperation);

        Mockito.verify(processStdOutEventHandler).registerOperation(0, processStdOutOperation);
    }
}
