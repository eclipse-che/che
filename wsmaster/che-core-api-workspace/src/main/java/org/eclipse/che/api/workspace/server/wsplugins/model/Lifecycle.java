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
package org.eclipse.che.api.workspace.server.wsplugins.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * Lifecycle describes actions that the management system should take in response to container
 * lifecycle events.
 *
 * <p>For the PostStart and PreStop lifecycle handlers, management of the container blocks until the
 * action is complete, unless the container process fails, in which case the handler is aborted.
 *
 * <p>PostStart is called immediately after a container is created. If the handler fails, the
 * container is terminated and restarted according to its restart policy. Other management of the
 * container blocks until the hook completes. More info:
 * https://kubernetes.io/docs/concepts/containers/container-lifecycle-hooks/#container-hooks
 *
 * <p>PreStop is called immediately before a container is terminated due to an API request or
 * management event such as liveness/startup probe failure, preemption, resource contention, etc.
 * The handler is not called if the container crashes or exits. The reason for termination is passed
 * to the handler. The Pod's termination grace period countdown begins before the PreStop hooked is
 * executed. Regardless of the outcome of the handler, the container will eventually terminate
 * within the Pod's termination grace period. Other management of the container blocks until the
 * hook completes or until the termination grace period is reached. More info:
 * https://kubernetes.io/docs/concepts/containers/container-lifecycle-hooks/#container-hooks
 */
public class Lifecycle {

  @JsonProperty("preStop")
  private Handler preStop = new Handler();

  @JsonProperty("postStart")
  private Handler postStart = new Handler();

  public Lifecycle preStop(Handler preStop) {
    this.preStop = preStop;
    return this;
  }

  public Handler getPreStop() {
    return preStop;
  }

  public void setPreStop(Handler preStop) {
    this.preStop = preStop;
  }

  public Lifecycle postStart(Handler postStart) {
    this.postStart = postStart;
    return this;
  }

  public Handler getPostStart() {
    return postStart;
  }

  public void setPostStart(Handler postStart) {
    this.postStart = postStart;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Lifecycle that = (Lifecycle) o;
    return Objects.equals(preStop, that.preStop) && Objects.equals(postStart, that.postStart);
  }

  @Override
  public int hashCode() {
    return Objects.hash(postStart, preStop);
  }

  @Override
  public String toString() {
    return "Lifecycle{" + "postStart=" + postStart + ", preStop=" + preStop + '}';
  }
}
