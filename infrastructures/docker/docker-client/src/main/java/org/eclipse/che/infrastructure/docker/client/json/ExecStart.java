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
package org.eclipse.che.infrastructure.docker.client.json;

/** @author andrew00x */
public class ExecStart {
  private boolean detach;
  private boolean tty;

  public boolean isDetach() {
    return detach;
  }

  public void setDetach(boolean detach) {
    this.detach = detach;
  }

  public boolean isTty() {
    return tty;
  }

  public void setTty(boolean tty) {
    this.tty = tty;
  }

  @Override
  public String toString() {
    return "ExecStart{" + "detach=" + detach + ", tty=" + tty + '}';
  }

  // -------------------

  public ExecStart withDetach(boolean detach) {
    this.detach = detach;
    return this;
  }

  public ExecStart withTty(boolean tty) {
    this.tty = tty;
    return this;
  }
}
