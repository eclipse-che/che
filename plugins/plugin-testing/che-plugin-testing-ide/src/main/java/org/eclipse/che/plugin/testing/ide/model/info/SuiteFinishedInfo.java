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

import org.eclipse.che.plugin.testing.ide.model.Printer;

/** Describes information about suit which is finished. */
public class SuiteFinishedInfo extends AbstractTestStateInfo {

  public static final SuiteFinishedInfo INSTANCE = new SuiteFinishedInfo();

  public static final SuiteFinishedInfo PASSED_SUITE =
      new SuiteFinishedInfo() {
        @Override
        public TestStateDescription getDescription() {
          return TestStateDescription.PASSED;
        }
      };

  public static final SuiteFinishedInfo EMPTY_INFO = new EmptySuite();

  public static final SuiteFinishedInfo FAILED_SUITE =
      new SuiteFinishedInfo() {
        @Override
        public boolean isProblem() {
          return true;
        }

        @Override
        public TestStateDescription getDescription() {
          return TestStateDescription.FAILED;
        }
      };

  public static final SuiteFinishedInfo IGNORED_TEST_SUITE =
      new SuiteFinishedInfo() {
        @Override
        public boolean isProblem() {
          return true;
        }

        @Override
        public TestStateDescription getDescription() {
          return TestStateDescription.IGNORED;
        }
      };

  public static final SuiteFinishedInfo ERROR_SUITE =
      new SuiteFinishedInfo() {
        @Override
        public boolean isProblem() {
          return true;
        }

        @Override
        public TestStateDescription getDescription() {
          return TestStateDescription.ERROR;
        }
      };

  public static final SuiteFinishedInfo EMPTY_LEAF_SUITE =
      new EmptySuite() {
        @Override
        public void print(Printer printer) {
          super.print(printer);

          printer.print("Test suite is empty.", Printer.OutputType.STDOUT);
        }
      };

  public static final SuiteFinishedInfo TESTS_REPORTER_NOT_ATTACHED =
      new SuiteFinishedInfo() {
        @Override
        public boolean isProblem() {
          return false;
        }
      };

  private SuiteFinishedInfo() {}

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
    return false;
  }

  @Override
  public boolean wasLaunched() {
    return true;
  }

  @Override
  public boolean wasTerminated() {
    return false;
  }

  private static class EmptySuite extends SuiteFinishedInfo {

    @Override
    public boolean isProblem() {
      return false;
    }

    @Override
    public TestStateDescription getDescription() {
      return TestStateDescription.COMPLETED;
    }
  }

  @Override
  public TestStateDescription getDescription() {
    return TestStateDescription.COMPLETED;
  }
}
