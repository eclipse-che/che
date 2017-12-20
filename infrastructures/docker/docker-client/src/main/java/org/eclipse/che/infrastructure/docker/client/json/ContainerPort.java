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
package org.eclipse.che.infrastructure.docker.client.json;

import org.eclipse.che.commons.annotation.Nullable;

/**
 * Defines information about container port which was exposed or published.
 *
 * @author Alexander Andrienko
 * @author Alexander Garagatyi
 */
public class ContainerPort {
  private int privatePort;
  private int publicPort;
  private String type;

  public int getPrivatePort() {
    return privatePort;
  }

  public void setPrivatePort(int privatePort) {
    this.privatePort = privatePort;
  }

  /** When public port is {@code null} the port was not published but just exposed. */
  @Nullable
  public int getPublicPort() {
    return publicPort;
  }

  public void setPublicPort(int publicPort) {
    this.publicPort = publicPort;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return "ContainerPort{"
        + "privatePort="
        + privatePort
        + ", publicPort="
        + publicPort
        + ", type='"
        + type
        + '\''
        + '}';
  }
}
