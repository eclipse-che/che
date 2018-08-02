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
package org.eclipse.che.plugin.testing.ide.model.info;

import org.eclipse.che.ide.util.StringUtils;
import org.eclipse.che.plugin.testing.ide.model.Printer;

/** Describes information about failed test. */
public class TestComparisionFailedInfo extends TestFailedInfo {

  private final String errorMessage;
  private final String stackTrace;

  public TestComparisionFailedInfo(String message, String stackTrace) {
    super(message, stackTrace);
    this.errorMessage = StringUtils.isNullOrEmpty(message) ? "" : message;
    this.stackTrace = StringUtils.isNullOrEmpty(stackTrace) ? "" : stackTrace;
  }

  @Override
  public void print(Printer printer) {
    printer.print(Printer.NEW_LINE, Printer.OutputType.STDERR);
    printer.print(errorMessage, Printer.OutputType.STDERR);

    printer.print(Printer.NEW_LINE, Printer.OutputType.STDERR);
    printer.print(stackTrace, Printer.OutputType.STDERR);
    printer.print(Printer.NEW_LINE, Printer.OutputType.STDERR);
  }
}
