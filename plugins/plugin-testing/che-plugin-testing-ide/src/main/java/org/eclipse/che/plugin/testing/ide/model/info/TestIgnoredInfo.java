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

/** Describes information about test which is ignored. */
public class TestIgnoredInfo extends AbstractTestStateInfo {

  private final String text;
  private final String stackTrace;

  public TestIgnoredInfo(String text, String stackTrace) {
    this.text = text;
    this.stackTrace = stackTrace;
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
    return false;
  }

  @Override
  public void print(Printer printer) {
    super.print(printer);

    if (text != null) {
      printer.print(text, Printer.OutputType.STDERR);
    }

    if (!StringUtils.isNullOrWhitespace(stackTrace)) {
      printer.print("\n", Printer.OutputType.STDERR);
      printer.print(stackTrace, Printer.OutputType.STDERR);
    }
  }

  @Override
  public TestStateDescription getDescription() {
    return TestStateDescription.IGNORED;
  }
}
