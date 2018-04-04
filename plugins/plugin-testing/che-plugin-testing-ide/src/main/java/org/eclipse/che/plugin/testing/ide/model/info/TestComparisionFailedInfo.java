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
