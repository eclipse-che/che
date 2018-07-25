/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.api.command.exec;

import java.util.function.Consumer;
import org.eclipse.che.agent.exec.shared.dto.event.ProcessDiedEventDto;
import org.eclipse.che.agent.exec.shared.dto.event.ProcessStartedEventDto;
import org.eclipse.che.agent.exec.shared.dto.event.ProcessStdErrEventDto;
import org.eclipse.che.agent.exec.shared.dto.event.ProcessStdOutEventDto;

/**
 * Exec agent promise wrapper provides means to register operation for several exec agent events
 * (e.g. process started, process died, etc.). Besides that allows to register custom operation with
 * DTO instances of type defined in generic parameter T similar to how it is done in classical
 * javascript promises.
 *
 * @author Dmitry Kuleshov
 */
public class ExecAgentConsumer<T> {
  private Consumer<ProcessDiedEventDto> processDiedEventDtoConsumer;
  private Consumer<ProcessStartedEventDto> processStartedEventDtoConsumer;
  private Consumer<ProcessStdErrEventDto> processStdErrEventDtoConsumer;
  private Consumer<ProcessStdOutEventDto> processStdOutEventDtoConsumer;
  private Consumer<T> consumer;

  /**
   * Register an operation which will be performed when a process generates process died event.
   *
   * @param operation operation to be performed
   * @return this instance
   */
  public ExecAgentConsumer<T> thenIfProcessDiedEvent(Consumer<ProcessDiedEventDto> operation) {
    processDiedEventDtoConsumer = operation;
    return this;
  }

  /**
   * Register an operation which will be performed when a process generates process started event.
   *
   * @param consumer operation to be performed
   * @return this instance
   */
  public ExecAgentConsumer<T> thenIfProcessStartedEvent(Consumer<ProcessStartedEventDto> consumer) {
    processStartedEventDtoConsumer = consumer;
    return this;
  }

  /**
   * Register an operation which will be performed when a process generates process standard output
   * event.
   *
   * @param consumer operation to be performed
   * @return this instance
   */
  public ExecAgentConsumer<T> thenIfProcessStdOutEvent(Consumer<ProcessStdOutEventDto> consumer) {
    processStdOutEventDtoConsumer = consumer;
    return this;
  }

  /**
   * Register an operation which will be performed when a process generates process standard error
   * event.
   *
   * @param consumer operation to be performed
   * @return this instance
   */
  public ExecAgentConsumer<T> thenIfProcessStdErrEvent(Consumer<ProcessStdErrEventDto> consumer) {
    processStdErrEventDtoConsumer = consumer;
    return this;
  }

  /**
   * Register an operation which will be performed when a request is accepted and response is
   * received. process died event.
   *
   * @param consumer operation to be performed
   * @return this instance
   */
  public ExecAgentConsumer<T> then(Consumer<T> consumer) {
    this.consumer = consumer;
    return this;
  }

  /**
   * Checks if process died event is associated with an operation
   *
   * @return true if there is an operation, otherwise - false
   */
  public boolean hasProcessDiedEventConsumer() {
    return processDiedEventDtoConsumer != null;
  }

  /**
   * Checks if process started event is associated with an operation
   *
   * @return true if there is an operation, otherwise - false
   */
  public boolean hasProcessStartedEventConsumer() {
    return processStartedEventDtoConsumer != null;
  }

  /**
   * Checks if process standard error event is associated with an operation
   *
   * @return true if there is an operation, otherwise - false
   */
  public boolean hasProcessStdErrEventConsumer() {
    return processStdErrEventDtoConsumer != null;
  }

  /**
   * Checks if process standard output event is associated with an operation
   *
   * @return true if there is an operation, otherwise - false
   */
  public boolean hasProcessStdOutEventConsumer() {
    return processStdOutEventDtoConsumer != null;
  }

  /**
   * Checks if response associated with an operation
   *
   * @return true if there is an operation, otherwise - false
   */
  public boolean hasOperation() {
    return consumer != null;
  }

  public Consumer<ProcessDiedEventDto> getProcessDiedEventDtoConsumer() {
    return processDiedEventDtoConsumer;
  }

  public Consumer<ProcessStartedEventDto> getProcessStartedEventDtoConsumer() {
    return processStartedEventDtoConsumer;
  }

  public Consumer<ProcessStdErrEventDto> getProcessStdErrEventDtoConsumer() {
    return processStdErrEventDtoConsumer;
  }

  public Consumer<ProcessStdOutEventDto> getProcessStdOutEventDtoConsumer() {
    return processStdOutEventDtoConsumer;
  }

  public Consumer<T> getConsumer() {
    return consumer;
  }
}
