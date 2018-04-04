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
