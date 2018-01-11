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
