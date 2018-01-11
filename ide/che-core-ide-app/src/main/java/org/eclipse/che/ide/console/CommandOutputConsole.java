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
package org.eclipse.che.ide.console;

import java.util.function.Consumer;
import org.eclipse.che.agent.exec.shared.dto.ProcessSubscribeResponseDto;
import org.eclipse.che.agent.exec.shared.dto.event.ProcessDiedEventDto;
import org.eclipse.che.agent.exec.shared.dto.event.ProcessStartedEventDto;
import org.eclipse.che.agent.exec.shared.dto.event.ProcessStdErrEventDto;
import org.eclipse.che.agent.exec.shared.dto.event.ProcessStdOutEventDto;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.outputconsole.OutputConsole;

/** Describes requirements for the console for command output. */
public interface CommandOutputConsole extends OutputConsole {

  /**
   * Get command that this output console is associated with
   *
   * @return corresponding command
   */
  CommandImpl getCommand();

  /** Returns PID of the associated process or 0 if none was associated. */
  int getPid();

  /** Start listening to the output on the given WebSocket channel. */
  @Deprecated
  void listenToOutput(String wsChannel);

  /**
   * Get an output console related operations that should be performed when an standard error
   * message received
   *
   * @return operation
   */
  Consumer<ProcessStdErrEventDto> getStdErrConsumer();

  /**
   * Get an output console related operations that should be performed when an standard output
   * message received
   *
   * @return operation
   */
  Consumer<ProcessStdOutEventDto> getStdOutConsumer();

  /**
   * Get an output console related operations that should be performed when a process started event
   * caught
   *
   * @return operation
   */
  Consumer<ProcessStartedEventDto> getProcessStartedConsumer();

  /**
   * Get an output console related operations that should be performed when a process died event
   * caught
   *
   * @return operation
   */
  Consumer<ProcessDiedEventDto> getProcessDiedConsumer();

  /**
   * Get an output console related operations that should be performed when a subscription to a
   * process is performed
   *
   * @return operation
   */
  Consumer<ProcessSubscribeResponseDto> getProcessSubscribeConsumer();

  /**
   * Print raw string data inside the output console
   *
   * @param output output string
   */
  void printOutput(String output);
}
