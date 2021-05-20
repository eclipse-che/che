/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.util;

/**
 * Constants used as names for spans when tracing kubernetes infra events.
 *
 * @author amisevsk
 */
public class TracingSpanConstants {

  /** Span name for the wait phase after objects are created until they are ready */
  public static final String WAIT_MACHINES_START = "WaitMachinesStart";

  /** Span name for async wait for pods to be running */
  public static final String WAIT_RUNNING_ASYNC = "WaitRunningAsync";

  /** Span name for async wait for servers to be running */
  public static final String CHECK_SERVERS = "CheckServers";

  /** Span name for wait for broker storage to be ready */
  public static final String PREPARE_STORAGE_PHASE = "PrepareStorage";

  /** Span name for wait for plugin broker to be deployed */
  public static final String DEPLOY_BROKER_PHASE = "DeployBroker";

  /** Span name for wait for plugin broker's results */
  public static final String WAIT_BROKERS_RESULT_PHASE = "WaitBrokerResult";
}
