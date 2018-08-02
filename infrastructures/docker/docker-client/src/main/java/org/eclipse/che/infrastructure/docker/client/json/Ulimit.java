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
package org.eclipse.che.infrastructure.docker.client.json;

/** @author Alexander Garagatyi */
public class Ulimit {
  private String name;
  private int soft;
  private int hard;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getSoft() {
    return soft;
  }

  public void setSoft(int soft) {
    this.soft = soft;
  }

  public int getHard() {
    return hard;
  }

  public void setHard(int hard) {
    this.hard = hard;
  }

  @Override
  public String toString() {
    return "Ulimit{" + "name='" + name + '\'' + ", soft=" + soft + ", hard=" + hard + '}';
  }
}
