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

package org.eclipse.che.ide.console.colorizer;

/**
 * Output console colorizer uses to change color for some output fragments.
 *
 * @author Alexander Andriienko
 */
public interface OutputConsoleColorizer {
  /**
   * Find "special text fragment" by regexp pattern in the output content {@code outPutText} and
   * colorize them by applying ansi color control sequences. Returns original text in case if
   * "special text fragments" was not found.
   *
   * @param outPutText text to colorize
   */
  String colorize(String outPutText);
}
