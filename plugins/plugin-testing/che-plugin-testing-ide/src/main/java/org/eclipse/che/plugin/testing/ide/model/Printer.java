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
package org.eclipse.che.plugin.testing.ide.model;

/** Describes object which can prints some information to some output panel. */
public interface Printer {

  String NEW_LINE = "\n";

  /**
   * Prints information to the output panel.
   *
   * @param text text message
   * @param type type of message
   */
  void print(String text, OutputType type);

  /** Sets new printable object. */
  void onNewPrintable(Printable printable);

  enum OutputType {
    SYSTEM,
    STDOUT,
    STDERR
  }
}
