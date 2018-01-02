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

/**
 * View representation of output console.
 *
 * @author Vitaliy Guliy
 * @author Alexander Andriienko
 */
package org.eclipse.che.ide.console;

/**
 * Output console colorizer finds some text fragments by regex pattern from the console output
 * content and applies color control sequences to change text fragments color.
 */
public interface OutputConsoleColorizer {
  /**
   * Find some text fragment in the output content and colorize them by applying ansi color control
   * sequences.
   *
   * @param outPutText text to colorize
   */
  String colorize(String outPutText);
}
