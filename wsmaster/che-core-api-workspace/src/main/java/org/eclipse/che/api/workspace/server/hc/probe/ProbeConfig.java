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
package org.eclipse.che.api.workspace.server.hc.probe;

/** @author Alexander Garagatyi */
public abstract class ProbeConfig {
  private int successThreshold = 1;
  private int failureThreshold = 3;
  private int timeoutSeconds = 1;
  private int periodSeconds = 10;
  private int initialDelaySeconds;

  /**
   * Creates probe configuration.
   *
   * @param successThreshold minimum consecutive successes for the probe to be considered successful
   *     after having failed. Minimum value is 1. Defaults to 1.
   * @param failureThreshold consecutive failures of a probe needed to consider probe failed.
   *     Minimum value is 1. Defaults to 3.
   * @param timeoutSeconds number of seconds after which the probe times out. Defaults to 1 second.
   *     Minimum value is 1.
   * @param periodSeconds how often to perform the probe. Default to 10 seconds. Minimum value is 1.
   * @param initialDelaySeconds number of seconds after the probe is submitted for checks before
   *     probes are initiated.
   */
  public ProbeConfig(
      int successThreshold,
      int failureThreshold,
      int timeoutSeconds,
      int periodSeconds,
      int initialDelaySeconds) {
    // TODO check values
    this.successThreshold = successThreshold;
    this.failureThreshold = failureThreshold;
    this.timeoutSeconds = timeoutSeconds;
    this.periodSeconds = periodSeconds;
    this.initialDelaySeconds = initialDelaySeconds;
  }

  public int getInitialDelaySeconds() {
    return initialDelaySeconds;
  }

  public int getFailureThreshold() {
    return failureThreshold;
  }

  public int getSuccessThreshold() {
    return successThreshold;
  }

  public int getPeriodSeconds() {
    return periodSeconds;
  }

  public int getTimeoutSeconds() {
    return timeoutSeconds;
  }
}
