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
package org.eclipse.che.infrastructure.docker.client.json;

/** @author Alexander Garagatyi */
public class RestartPolicy {
  private String name;
  private int maximumRetryCount;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public RestartPolicy withName(String name) {
    this.name = name;
    return this;
  }

  public int getMaximumRetryCount() {
    return maximumRetryCount;
  }

  public void setMaximumRetryCount(int maximumRetryCount) {
    this.maximumRetryCount = maximumRetryCount;
  }

  public RestartPolicy withMaximumRetryCount(int maximumRetryCount) {
    this.maximumRetryCount = maximumRetryCount;
    return this;
  }

  @Override
  public String toString() {
    return "RestartPolicy{"
        + "name='"
        + name
        + '\''
        + ", maximumRetryCount="
        + maximumRetryCount
        + '}';
  }
}
