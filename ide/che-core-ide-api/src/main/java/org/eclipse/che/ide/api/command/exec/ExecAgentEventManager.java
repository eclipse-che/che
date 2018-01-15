/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 * Manages registration of operations related to different kinds of even sent by exec agent
 *
 * @author Dmitry Kuleshov
 */
public interface ExecAgentEventManager {
  /**
   * Registers an consumer that is performed when 'process died' event is received
   *
   * @param endpointId endpoint identifier
   * @param pid process identifier
   * @param consumer consumer to be performed
   */
  void registerProcessDiedConsumer(
      String endpointId, int pid, Consumer<ProcessDiedEventDto> consumer);

  /**
   * Registers an consumer that is performed when 'process started' event is received
   *
   * @param endpointId endpoint identifier
   * @param pid process identifier
   * @param consumer consumer to be performed
   */
  void registerProcessStartedConsumer(
      String endpointId, int pid, Consumer<ProcessStartedEventDto> consumer);

  /**
   * Registers an consumer that is performed when 'process standard error' event is received
   *
   * @param endpointId endpoint identifier
   * @param pid process identifier
   * @param consumer consumer to be performed
   */
  void registerProcessStdErrConsumer(
      String endpointId, int pid, Consumer<ProcessStdErrEventDto> consumer);

  /**
   * Registers an consumer that is performed when 'process standard output' event is received
   *
   * @param endpointId endpoint identifier
   * @param pid process identifier
   * @param consumer consumer to be performed
   */
  void registerProcessStdOutConsumer(
      String endpointId, int pid, Consumer<ProcessStdOutEventDto> consumer);

  /**
   * Removes all registered event handler operations for the process associated with a PID
   *
   * @param endpointId endpoint identifier
   * @param pid process identifier
   */
  void cleanPidConsumer(String endpointId, int pid);
}
