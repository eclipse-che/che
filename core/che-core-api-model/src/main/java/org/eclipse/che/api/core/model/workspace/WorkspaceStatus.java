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
package org.eclipse.che.api.core.model.workspace;

/**
 * Defines the contract between workspace and its active environment.
 *
 * <p>Workspace is rather part of the {@link Workspace} than {@link Runtime} or {@link
 * WorkspaceConfig}, as it shows the state of <b>certain</b> user's workspace and exists
 * <b>earlier</b> than runtime workspace instance e.g. UsersWorkspace may be considered as
 * 'STARTING' before it becomes runtime('RUNNING').
 *
 * @author Alexander Garagatyi
 * @author Yevhenii Voevodin
 */
public enum WorkspaceStatus {

  /**
   * Workspace considered as starting if and only if its active environment is booting.
   *
   * <p>Workspace becomes starting only if it was {@link #STOPPED}. The status map:
   *
   * <pre>
   *  STOPPED -> <b>STARTING</b> -> RUNNING  (normal behaviour)
   *  STOPPED -> <b>STARTING</b> -> STOPPED  (failed to start)
   *  STOPPED -> <b>STARTING</b> -> STOPPING (explicitly stopped)
   * </pre>
   */
  STARTING,

  /**
   * Workspace considered as running if and only if its environment is running.
   *
   * <p>Workspace becomes running after it was {@link #STARTING}. The status map:
   *
   * <pre>
   *  STARTING -> <b>RUNNING</b> -> STOPPING (normal behaviour)
   *  STARTING -> <b>RUNNING</b> -> STOPPED (environment start was interrupted)
   * </pre>
   */
  RUNNING,

  /**
   * Workspace considered as stopping if and only if its active environment is shutting down.
   *
   * <p>Workspace is in stopping status only if it was in {@link #RUNNING} or {@link #STARTING}
   * status before. The status map:
   *
   * <pre>
   *  RUNNING  -> <b>STOPPING</b> -> STOPPED (normal behaviour)/(error while stopping)
   *  STARTING -> <b>STOPPING</b> -> STOPPED (stopped while starting)
   * </pre>
   */
  STOPPING,

  /**
   * Workspace considered as stopped when:
   *
   * <ul>
   *   <li>Environment was successfully stopped
   *   <li>Error occurred while environment was stopping
   *   <li>Dev-machine failed to start
   *   <li>Running environment machine was stopped by internal problem(e.g. OOM of a machine)
   *   <li>Workspace hasn't been started yet(e.g stopped is the status of the user's workspace
   *       instance without its runtime)
   * </ul>
   *
   * <p>The status map:
   *
   * <pre>
   *  STOPPING -> <b>STOPPED</b> (normal behaviour)/(error while stopping)
   *  STARTING -> <b>STOPPED</b> (failed to start)
   *  RUNNING  -> <b>STOPPED</b> (environment machine was interrupted)
   * </pre>
   */
  STOPPED
}
