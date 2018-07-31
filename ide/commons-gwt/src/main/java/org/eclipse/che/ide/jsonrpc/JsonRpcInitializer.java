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
package org.eclipse.che.ide.jsonrpc;

import java.util.Map;
import java.util.Set;
import javax.inject.Singleton;

/** Performs all needed preparations to initialize and terminate the json rpc service. */
@Singleton
public interface JsonRpcInitializer {
  /**
   * Initialize json rpc service for an endpoint defined by a high level identifier with
   * implementation defined properties.
   *
   * @param endpointId high level endpoint identifier (e.g. "exec-agent")
   * @param initProperties map of implementation dependent properties (e.g. URL, etc.)
   */
  void initialize(String endpointId, Map<String, String> initProperties);

  /**
   * Initialize json rpc service for an endpoint defined by a high level identifier with
   * implementation defined properties and actions.
   *
   * @param endpointId high level endpoint identifier (e.g. "exec-agent")
   * @param initProperties map of implementation dependent properties (e.g. URL, etc.)
   * @param initActions actions to be performed each time the connection is initialized or
   *     reinitialized.
   */
  void initialize(String endpointId, Map<String, String> initProperties, Set<Runnable> initActions);

  /**
   * Terminate json rpc service defined by high level identifier
   *
   * @param endpointId high level endpoint identifier (e.g. "exec-agent")
   */
  void terminate(String endpointId);

  /**
   * Terminate json rpc service defined by high level identifier
   *
   * @param endpointId high level endpoint identifier (e.g. "exec-agent")
   * @param terminateProperties pam of implementation specific properties for the termination
   *     process
   * @param terminateActions actions to be performed after connection is terminated
   */
  void terminate(
      String endpointId, Map<String, String> terminateProperties, Set<Runnable> terminateActions);
}
