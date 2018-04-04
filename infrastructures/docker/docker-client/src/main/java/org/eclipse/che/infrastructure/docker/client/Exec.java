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
package org.eclipse.che.infrastructure.docker.client;

import java.util.Arrays;
import java.util.Objects;

/** @author andrew00x */
public class Exec {
  private final String[] command;
  private final String id;

  public Exec(String[] command, String id) {
    this.command = command;
    this.id = id;
  }

  public String[] getCommand() {
    return command;
  }

  public String getId() {
    return id;
  }

  @Override
  public String toString() {
    return "Exec{" + "command=" + Arrays.toString(command) + ", id='" + id + '\'' + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Exec exec = (Exec) o;
    return Arrays.equals(command, exec.command) && Objects.equals(id, exec.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(Arrays.hashCode(command), id);
  }
}
