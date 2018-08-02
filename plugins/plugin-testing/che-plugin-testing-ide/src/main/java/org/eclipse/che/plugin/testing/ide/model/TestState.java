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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.plugin.testing.ide.model.info.AbstractTestStateInfo;
import org.eclipse.che.plugin.testing.ide.model.info.CompaundTestFailedInfo;
import org.eclipse.che.plugin.testing.ide.model.info.NotRunInfo;
import org.eclipse.che.plugin.testing.ide.model.info.SuiteFinishedInfo;
import org.eclipse.che.plugin.testing.ide.model.info.SuiteInProgressInfo;
import org.eclipse.che.plugin.testing.ide.model.info.TestComparisionFailedInfo;
import org.eclipse.che.plugin.testing.ide.model.info.TestErrorInfo;
import org.eclipse.che.plugin.testing.ide.model.info.TestFailedInfo;
import org.eclipse.che.plugin.testing.ide.model.info.TestIgnoredInfo;
import org.eclipse.che.plugin.testing.ide.model.info.TestInProgressInfo;
import org.eclipse.che.plugin.testing.ide.model.info.TestPassedInfo;
import org.eclipse.che.plugin.testing.ide.model.info.TestStateDescription;

/** Describes test/suite state. */
public class TestState implements Printable {

  private final String name;
  private final boolean isSuite;
  private final String locationURL;

  private List<TestState> children;
  private TestState parent;
  private List<Printable> printables = new ArrayList<>();
  private Printer printer;
  private Printer defaultPrinter;

  private AbstractTestStateInfo stateInfo = NotRunInfo.INSTANCE;
  private boolean treeBuildBeforeStart = false;
  private TestLocator testLocator;
  private boolean config;
  private String stackTrace;
  private int duraton;

  private String presentaton;

  public TestState(String testName, boolean isSuite, String locationURL) {
    this.name = testName;
    this.isSuite = isSuite;
    this.locationURL = locationURL;
  }

  public String getName() {
    return name;
  }

  public void setStarted() {
    stateInfo = isSuite ? TestInProgressInfo.INSTANCE : new SuiteInProgressInfo(this);
  }

  public String getLocationUrl() {
    return locationURL;
  }

  @NotNull
  public List<TestState> getChildren() {
    return children == null ? Collections.emptyList() : children;
  }

  public boolean isFinal() {
    return false;
  }

  public boolean isProblem() {
    return stateInfo.isProblem();
  }

  public boolean isSuite() {
    return isSuite;
  }

  public void setTreeBuildBeforeStart() {
    treeBuildBeforeStart = true;
  }

  public void setTestLocator(TestLocator testLocator) {
    this.testLocator = testLocator;
  }

  public void setPrinter(Printer printer) {
    this.printer = selectPrinter(printer);
    getChildren().forEach(c -> c.setPrinter(printer));
  }

  private Printer selectPrinter(Printer printer) {
    return ((defaultPrinter != null) && (printer != null)) ? defaultPrinter : printer;
  }

  public void setDefaultPrinter(@NotNull Printer defaultPrinter) {
    this.defaultPrinter = defaultPrinter;
  }

  public void addChild(TestState newState) {
    if (children == null) {
      children = new ArrayList<>();
    }

    children.add(newState);

    addLast(newState);

    newState.setParent(this);

    newState.setPrinter(printer);
    if (defaultPrinter != null && newState.defaultPrinter == null) {
      newState.setDefaultPrinter(defaultPrinter);
    }
  }

  protected void addLast(Printable printable) {
    printables.add(printable);
    callNewPrintable(printable);
  }

  private void callNewPrintable(Printable printable) {
    if (printer != null) {
      printer.onNewPrintable(printable);
    }
  }

  @Override
  public void print(Printer printer) {
    Printer selectedPrinter = selectPrinter(printer);
    for (Printable printable : printables) {
      printable.print(selectedPrinter);
    }

    stateInfo.print(selectedPrinter);
  }

  public void setFinished() {
    if (stateInfo.isFinal()) {
      return;
    }

    if (!isSuite()) {
      stateInfo = TestPassedInfo.INSTANCE;
    } else {
      stateInfo = calculateInfoOnFinish();
    }
    callNewPrintable(stateInfo);
  }

  public TestStateDescription getDescription() {
    return stateInfo.getDescription();
  }

  private AbstractTestStateInfo calculateInfoOnFinish() {
    AbstractTestStateInfo result;
    if (isLeaf()) {
      result = SuiteFinishedInfo.EMPTY_LEAF_SUITE;
    } else if (isEmptySuite()) {
      result = SuiteFinishedInfo.EMPTY_INFO;
    } else {
      if (isProblem()) {
        if (hasErrorTests()) {
          result = SuiteFinishedInfo.ERROR_SUITE;
        } else {
          if (hasFailedTest()) {
            result = SuiteFinishedInfo.FAILED_SUITE;
          } else {
            result = SuiteFinishedInfo.IGNORED_TEST_SUITE;
          }
        }
      } else {
        result = SuiteFinishedInfo.PASSED_SUITE;
      }
    }
    return result;
  }

  private boolean hasFailedTest() {
    for (TestState child : getChildren()) {
      if (child.getDescription() == TestStateDescription.FAILED) {
        return true;
      }
    }
    return false;
  }

  private boolean hasErrorTests() {
    for (TestState child : getChildren()) {
      if (child.getDescription() == TestStateDescription.ERROR) {
        return true;
      }
    }
    return false;
  }

  public boolean isEmptySuite() {

    if (!isSuite()) {
      return true;
    }
    for (TestState testState : getChildren()) {
      if (testState.isSuite()) {
        if (!testState.isEmptySuite()) {
          return false;
        }
      } else {
        return false;
      }
    }

    return true;
  }

  public boolean isLeaf() {
    return children == null || children.isEmpty();
  }

  public void addStdOut(String text, Printer.OutputType outputType) {
    addPrintableAfterLastTest(printer -> printer.print(text, outputType));
  }

  protected void addPrintableAfterLastTest(Printable printable) {
    if (treeBuildBeforeStart) {
      int index = 0;
      for (Printable prnt : printables) {
        if (prnt instanceof TestState && !((TestState) prnt).isFinal()) {
          break;
        }
        index++;
      }

      insert(printable, index);
    } else {
      addLast(printable);
    }
  }

  private void insert(Printable printable, int index) {
    if (index >= printables.size()) {
      printables.add(printable);
    } else {
      printables.add(index, printable);
    }

    callNewPrintable(printable);
  }

  public void addStdErr(String err) {
    addPrintableAfterLastTest(printer -> printer.print(err, Printer.OutputType.STDERR));
  }

  public boolean isConfig() {
    return config;
  }

  public void setConfig(boolean config) {
    this.config = config;
  }

  public void setTestFailed(String failureMessage, String stackTrace, boolean error) {
    checkAndSetStackTrace(stackTrace);
    TestFailedInfo failedInfo = new TestFailedInfo(failureMessage, stackTrace);
    if (stateInfo instanceof TestComparisionFailedInfo) {
      CompaundTestFailedInfo failedTests = new CompaundTestFailedInfo(failureMessage, stackTrace);
      failedTests.addFailedTest((TestFailedInfo) stateInfo);
      failedTests.addFailedTest(failedInfo);
      callNewPrintable(failedInfo);
      stateInfo = failedTests;
    } else if (stateInfo instanceof CompaundTestFailedInfo) {
      ((CompaundTestFailedInfo) stateInfo).addFailedTest(failedInfo);
      callNewPrintable(failedInfo);
    } else if (stateInfo instanceof TestFailedInfo) {
      ((TestFailedInfo) stateInfo).addError(failureMessage, stackTrace, printer);
    } else {
      if (error) {
        stateInfo = new TestErrorInfo(failureMessage, stackTrace);
      } else {
        stateInfo = failedInfo;
      }
      callNewPrintable(stateInfo);
    }
  }

  private void checkAndSetStackTrace(String stackTrace) {
    if (this.stackTrace == null) {
      this.stackTrace = stackTrace;
    }
  }

  public void setDuration(int duration) {
    if (!isSuite()) {
      this.duraton = duration;
    }
  }

  public void setTestIgnored(String ignoreComment, String stackTrace) {
    checkAndSetStackTrace(stackTrace);
    stateInfo = new TestIgnoredInfo(ignoreComment, stackTrace);
    callNewPrintable(stateInfo);
  }

  public TestState getParent() {
    return parent;
  }

  public void setParent(TestState parent) {
    this.parent = parent;
  }

  public String getPresentation() {
    if (presentaton == null) {
      presentaton = PresentationUtil.getPresentation(this);
    }
    return presentaton;
  }

  public boolean isPassed() {
    return stateInfo.getDescription() == TestStateDescription.SKIPPED
        || stateInfo.getDescription() == TestStateDescription.COMPLETED
        || stateInfo.getDescription() == TestStateDescription.PASSED;
  }
}
