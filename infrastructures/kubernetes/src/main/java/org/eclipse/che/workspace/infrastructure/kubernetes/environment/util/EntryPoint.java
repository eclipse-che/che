/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.environment.util;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;

/** Represents an entry-point definition parsed from a string using the {@link EntryPointParser}. */
public final class EntryPoint {

  private final List<String> command;
  private final List<String> arguments;

  public EntryPoint(List<String> command, List<String> arguments) {
    this.command = ImmutableList.copyOf(command);
    this.arguments = ImmutableList.copyOf(arguments);
  }

  /** @return unmodifiable list representing the command of the entrypoint */
  public List<String> getCommand() {
    return command;
  }

  /** @return unmodifiable list representing the arguments of the entrypoint */
  public List<String> getArguments() {
    return arguments;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EntryPoint that = (EntryPoint) o;
    return Objects.equals(command, that.command) && Objects.equals(arguments, that.arguments);
  }

  @Override
  public int hashCode() {
    return Objects.hash(command, arguments);
  }

  @Override
  public String toString() {
    return "EntryPoint{" + "command=" + command + ", arguments=" + arguments + '}';
  }
}
