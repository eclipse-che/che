/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.core.model.workspace.runtime;

import java.util.Map;

/**
 * Runtime information about machine.
 *
 * @author Alexander Garagatyi
 */
public interface Machine {

  /** Returns machine specific attributes. */
  Map<String, String> getAttributes();

  /**
   * Returns mapping of exposed ports to {@link Server}.
   *
   * <p>Key is a symbolic server name <br>
   * Example:
   *
   * <pre>
   * {
   *     server1 : {
   *         "url" : "http://server-with-machines.com:8080"
   *     }
   * }
   * </pre>
   */
  Map<String, ? extends Server> getServers();

  MachineStatus getStatus();
}
