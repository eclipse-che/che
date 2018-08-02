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

import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.util.StringUtils;
import org.eclipse.che.plugin.testing.ide.model.Printer;

/** Describes information about test which is failed. */
public class TestFailedInfo extends AbstractTestStateInfo {

  private final List<String> errorOutput = new ArrayList<>();

  public TestFailedInfo(String message, String stackTrace) {
    errorOutput.add(createErrMessage(message, stackTrace));
  }

  @Nullable
  public static String createErrMessage(String message, String stackTrace) {
    StringBuilder builder = new StringBuilder();

    if (!StringUtils.isNullOrWhitespace(message) && !"null".equals(message)) {
      builder.append(message).append('\n');
    }

    if (!StringUtils.isNullOrWhitespace(stackTrace)) {
      builder.append(stackTrace).append('\n');
    }

    return builder.toString();
  }

  public void addError(String message, String stackTrace, Printer printer) {
    String errMessage = createErrMessage(message, stackTrace);
    if (errMessage.isEmpty()) {
      errorOutput.add(errMessage);
      if (printer != null) {
        printErrors(printer);
      }
    }
  }

  @Override
  public boolean isFinal() {
    return true;
  }

  @Override
  public boolean isInProgress() {
    return false;
  }

  @Override
  public boolean isProblem() {
    return true;
  }

  @Override
  public boolean wasLaunched() {
    return true;
  }

  @Override
  public boolean wasTerminated() {
    return true;
  }

  @Override
  public TestStateDescription getDescription() {
    return TestStateDescription.FAILED;
  }

  @Override
  public void print(Printer printer) {
    super.print(printer);
    printErrors(printer);
  }

  private void printErrors(Printer printer) {
    for (String error : errorOutput) {
      if (error != null) {
        printer.print(Printer.NEW_LINE, Printer.OutputType.STDERR);
        printer.print(error, Printer.OutputType.STDERR);
      }
    }
  }
}
