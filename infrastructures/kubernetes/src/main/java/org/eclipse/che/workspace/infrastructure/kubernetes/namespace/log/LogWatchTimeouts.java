/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.namespace.log;

/** Various timeouts used in watching workspace logs logic. All values are in milliseconds. */
public class LogWatchTimeouts {

  /** Standard timeouts gentle to io resources. Use this unless you need something more eager. */
  public static final LogWatchTimeouts DEFAULT = new LogWatchTimeouts(30_000, 2_000, 5_000);

  /** Aggressive timeouts for more short-lived tasks. */
  public static final LogWatchTimeouts AGGRESSIVE = new LogWatchTimeouts(5_000, 100, 2_500);

  private final long watchTimeoutMs;
  private final long waitBetweenTriesMs;
  private final long waitBeforeCleanupMs;

  public LogWatchTimeouts(long watchTimeoutMs, long waitBetweenTriesMs, long waitBeforeCleanupMs) {
    this.watchTimeoutMs = watchTimeoutMs;
    this.waitBetweenTriesMs = waitBetweenTriesMs;
    this.waitBeforeCleanupMs = waitBeforeCleanupMs;
  }

  /**
   * How long we should try watch the logs.
   *
   * @return timeout in ms
   */
  public long getWatchTimeoutMs() {
    return watchTimeoutMs;
  }

  /**
   * How long to block cleanup to get all container logs.
   *
   * @return timeout in ms
   */
  public long getWaitBeforeCleanupMs() {
    return waitBeforeCleanupMs;
  }

  /**
   * How long to wait between individual tries to get container logs.
   *
   * @return timeout in ms
   */
  public long getWaitBeforeNextTry() {
    return waitBetweenTriesMs;
  }
}
