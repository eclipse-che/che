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
package org.eclipse.che.ide.api.machine.execagent;

import org.eclipse.che.api.machine.shared.dto.execagent.event.ProcessDiedEventDto;
import org.eclipse.che.api.machine.shared.dto.execagent.event.ProcessStartedEventDto;
import org.eclipse.che.api.machine.shared.dto.execagent.event.ProcessStdErrEventDto;
import org.eclipse.che.api.machine.shared.dto.execagent.event.ProcessStdOutEventDto;
import org.eclipse.che.api.promises.client.Operation;

/**
 * Exec agent promise wrapper provides means to register operation for several
 * exec agent events (e.g. process started, process died, etc.). Besides that
 * allows to register custom operation with DTO instances of type defined in
 * generic parameter T similar to how it is done in classical javascript
 * promises.
 *
 * @author Dmitry Kuleshov
 */
public class ExecAgentPromise<T> {
    private Operation<ProcessDiedEventDto>    processDiedEventDtoOperation;
    private Operation<ProcessStartedEventDto> processStartedEventDtoOperation;
    private Operation<ProcessStdErrEventDto>  processStdErrEventDtoOperation;
    private Operation<ProcessStdOutEventDto>  processStdOutEventDtoOperation;
    private Operation<T>                      operation;

    /**
     * Register an operation which will be performed when a process generates
     * process died event.
     *
     * @param operation
     *         operation to be performed
     *
     * @return this instance
     */
    public ExecAgentPromise<T> thenIfProcessDiedEvent(Operation<ProcessDiedEventDto> operation) {
        processDiedEventDtoOperation = operation;
        return this;
    }

    /**
     * Register an operation which will be performed when a process generates
     * process started event.
     *
     * @param operation
     *         operation to be performed
     *
     * @return this instance
     */
    public ExecAgentPromise<T> thenIfProcessStartedEvent(Operation<ProcessStartedEventDto> operation) {
        processStartedEventDtoOperation = operation;
        return this;
    }

    /**
     * Register an operation which will be performed when a process generates
     * process standard output event.
     *
     * @param operation
     *         operation to be performed
     *
     * @return this instance
     */
    public ExecAgentPromise<T> thenIfProcessStdOutEvent(Operation<ProcessStdOutEventDto> operation) {
        processStdOutEventDtoOperation = operation;
        return this;
    }

    /**
     * Register an operation which will be performed when a process generates
     * process standard error event.
     *
     * @param operation
     *         operation to be performed
     *
     * @return this instance
     */
    public ExecAgentPromise<T> thenIfProcessStdErrEvent(Operation<ProcessStdErrEventDto> operation) {
        processStdErrEventDtoOperation = operation;
        return this;
    }

    /**
     * Register an operation which will be performed when a request is accepted
     * and response is received.
     * process died event.
     *
     * @param operation
     *         operation to be performed
     *
     * @return this instance
     */
    public ExecAgentPromise<T> then(Operation<T> operation) {
        this.operation = operation;
        return this;
    }

    /**
     * Checks if process died event is associated with an operation
     *
     * @return true if there is an operation, otherwise - false
     */
    public boolean hasProcessDiedEventOperation() {
        return processDiedEventDtoOperation != null;
    }

    /**
     * Checks if process started event is associated with an operation
     *
     * @return true if there is an operation, otherwise - false
     */
    public boolean hasProcessStartedEventOperation() {
        return processStartedEventDtoOperation != null;
    }

    /**
     * Checks if process standard error event is associated with an operation
     *
     * @return true if there is an operation, otherwise - false
     */
    public boolean hasProcessStdErrEventOperation() {
        return processStdErrEventDtoOperation != null;
    }

    /**
     * Checks if process standard output event is associated with an operation
     *
     * @return true if there is an operation, otherwise - false
     */
    public boolean hasProcessStdOutEventOperation() {
        return processStdOutEventDtoOperation != null;
    }

    /**
     * Checks if response associated with an operation
     *
     * @return true if there is an operation, otherwise - false
     */
    public boolean hasOperation() {
        return operation != null;
    }

    public Operation<ProcessDiedEventDto> getProcessDiedEventDtoOperation() {
        return processDiedEventDtoOperation;
    }

    public Operation<ProcessStartedEventDto> getProcessStartedEventDtoOperation() {
        return processStartedEventDtoOperation;
    }

    public Operation<ProcessStdErrEventDto> getProcessStdErrEventDtoOperation() {
        return processStdErrEventDtoOperation;
    }

    public Operation<ProcessStdOutEventDto> getProcessStdOutEventDtoOperation() {
        return processStdOutEventDtoOperation;
    }

    public Operation<T> getOperation() {
        return operation;
    }
}
