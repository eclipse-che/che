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
package org.eclipse.che.ide.ext.git.client.outputconsole;

import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.outputconsole.OutputConsole;

/**
 * Describes requirements for the console for displaying git output.
 *
 * @author Roman Nikitenko
 */
public interface GitOutputConsole extends OutputConsole {
  /**
   * Print text in console.
   *
   * @param text text that need to be shown
   */
  void print(@NotNull String text);

  /**
   * Print colored text in console.
   *
   * @param text text that need to be shown
   * @param color color of printed text
   */
  void print(@NotNull String text, @NotNull String color);

  /**
   * Print error in console.
   *
   * @param text text that need to be shown as error
   */
  void printError(@NotNull String text);

  /** Clear console. Remove all messages. */
  void clear();
}
