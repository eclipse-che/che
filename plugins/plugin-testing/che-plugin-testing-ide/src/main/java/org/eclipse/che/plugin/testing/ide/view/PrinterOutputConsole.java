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
package org.eclipse.che.plugin.testing.ide.view;

import javax.inject.Inject;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.console.OutputConsoleViewImpl;
import org.eclipse.che.ide.machine.MachineResources;
import org.eclipse.che.plugin.testing.ide.model.Printable;
import org.eclipse.che.plugin.testing.ide.model.Printer;
import org.eclipse.che.plugin.testing.ide.model.TestState;

/** Represents an output console for test results. */
public class PrinterOutputConsole extends OutputConsoleViewImpl implements Printer {
  private TestState currentTest;

  @Inject
  public PrinterOutputConsole(MachineResources resources, CoreLocalizationConstant localization) {
    super(resources, localization);

    reRunProcessButton.removeFromParent();
    stopProcessButton.removeFromParent();
    clearOutputsButton.removeFromParent();
    downloadOutputsButton.removeFromParent();

    consolePanel.remove(commandPanel);
    consolePanel.remove(previewPanel);
  }

  @Override
  public void print(String text, OutputType type) {
    if (type == OutputType.STDERR) {
      print(text, false, "#FF4343");
    } else {
      print(text, false);
    }
    enableAutoScroll(true);
  }

  @Override
  public void onNewPrintable(Printable printable) {
    printable.print(this);
  }

  public void testSelected(TestState testState) {
    if (currentTest == testState) {
      return;
    }
    if (currentTest != null) {
      currentTest.setPrinter(null);
    }
    if (testState == null) {
      clearConsole();
      return;
    }
    currentTest = testState;
    clearConsole();
    testState.setPrinter(this);
    testState.print(this);
  }
}
