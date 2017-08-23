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
package org.eclipse.che.ide.api.command.exec;

import java.util.List;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcPromise;
import org.eclipse.che.api.core.model.workspace.config.Command;
import org.eclipse.che.ide.api.command.exec.dto.GetProcessLogsResponseDto;
import org.eclipse.che.ide.api.command.exec.dto.GetProcessResponseDto;
import org.eclipse.che.ide.api.command.exec.dto.GetProcessesResponseDto;
import org.eclipse.che.ide.api.command.exec.dto.ProcessKillResponseDto;
import org.eclipse.che.ide.api.command.exec.dto.ProcessStartResponseDto;
import org.eclipse.che.ide.api.command.exec.dto.ProcessSubscribeResponseDto;
import org.eclipse.che.ide.api.command.exec.dto.ProcessUnSubscribeResponseDto;
import org.eclipse.che.ide.api.command.exec.dto.UpdateSubscriptionResponseDto;

/**
 * Manages calls to exec agent that are related to processes, subscription, etc.
 *
 * @author Dmitry Kuleshov
 */
public interface ExecAgentCommandManager {
  /**
   * Call to exec agent to start a process with specified command parameters
   *
   * @param endpointId endpoint identifier
   * @param command command
   * @return exec agent promise with appropriate dto
   */
  ExecAgentConsumer<ProcessStartResponseDto> startProcess(String endpointId, Command command);

  /**
   * Call exec agent to kill a process with specified identifier
   *
   * @param endpointId endpoint identifier
   * @param pid process identifier
   * @return promise with appropriate dto
   */
  JsonRpcPromise<ProcessKillResponseDto> killProcess(String endpointId, int pid);

  /**
   * Call for a subscription to events related to a specified process after defined timestamp
   * represented by a corresponding string (RFC3339Nano e.g. "2016-07-26T09:36:44.920890113+03:00").
   *
   * @param endpointId endpoint identifier
   * @param pid process identifier
   * @param eventTypes event types (e.g. stderr, stdout)
   * @param after after timestamp
   * @return exec agent promise with appropriate dto
   */
  ExecAgentConsumer<ProcessSubscribeResponseDto> subscribe(
      String endpointId, int pid, List<String> eventTypes, String after);

  /**
   * Call for a cancellation of a subscription to events related to a specific process after defined
   * timestamp represented by a corresponding string (RFC3339Nano e.g.
   * "2016-07-26T09:36:44.920890113+03:00").
   *
   * @param endpointId endpoint identifier
   * @param pid process identifier
   * @param eventTypes event types (e.g. stderr, stdout)
   * @param after after timestamp
   * @return promise with appropriate dto
   */
  JsonRpcPromise<ProcessUnSubscribeResponseDto> unsubscribe(
      String endpointId, int pid, List<String> eventTypes, String after);

  /**
   * Call for an update of a subscription to events related to a specific process.
   *
   * @param endpointId endpoint identifier
   * @param pid process identifier
   * @param eventTypes event types (e.g. stderr, stdout)
   * @return promise with appropriate dto
   */
  JsonRpcPromise<UpdateSubscriptionResponseDto> updateSubscription(
      String endpointId, int pid, List<String> eventTypes);

  /**
   * Call for a report on proess logs of a specific process.
   *
   * @param endpointId endpoint identifier
   * @param pid process identifier
   * @param from string represented timestamp the beginning of a time segment (RFC3339Nano e.g.
   *     "2016-07-26T09:36:44.920890113+03:00")
   * @param till string represented timestamp the ending of a time segment (RFC3339Nano e.g.
   *     "2016-07-26T09:36:44.920890113+03:00")
   * @param limit the limit of logs in result, the default value is 50
   * @param skip the logs to skip, default value is 0
   * @return promise with appropriate dto
   */
  JsonRpcPromise<List<GetProcessLogsResponseDto>> getProcessLogs(
      String endpointId, int pid, String from, String till, int limit, int skip);

  /**
   * Call for a process info
   *
   * @param endpointId endpoint identifier
   * @param pid process identifier
   * @return promise with appropriate dto
   */
  JsonRpcPromise<GetProcessResponseDto> getProcess(String endpointId, int pid);

  /**
   * Call for a process info
   *
   * @param endpointId endpoint identifier
   * @param all defines if include already stopped processes, true for all, processes and false for
   *     running processes
   * @return promise with appropriate dto
   */
  JsonRpcPromise<List<GetProcessesResponseDto>> getProcesses(String endpointId, boolean all);
}
