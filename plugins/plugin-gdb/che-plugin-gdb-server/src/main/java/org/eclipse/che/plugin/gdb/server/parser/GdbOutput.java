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
package org.eclipse.che.plugin.gdb.server.parser;

/**
 * Wrapper for GDB output.
 *
 * @author Anatoliy Bazko
 */
public class GdbOutput {
  private final String output;
  private final boolean terminated;

  private GdbOutput(String output, boolean terminated) {
    this.output = output;
    this.terminated = terminated;
  }

  public static GdbOutput of(String output) {
    return new GdbOutput(output, false);
  }

  public static GdbOutput of(String output, boolean terminated) {
    return new GdbOutput(output, terminated);
  }

  public String getOutput() {
    return output;
  }

  public boolean isTerminated() {
    return terminated;
  }
}
