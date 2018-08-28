/*
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.testing.phpunit.server.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.che.plugin.testing.phpunit.server.PHPUnitMessageParser;

/**
 * Test case model element implementation.
 *
 * @author Bartlomiej Laczkowski
 */
public class PHPUnitTestCase extends AbstractPHPUnitTestResult {

  protected PHPUnitTestException exception = null;
  protected List<PHPUnitTestWarning> warnings = null;

  public PHPUnitTestCase(final Map<?, ?> test, final PHPUnitTestSuite parent) {
    super(test, parent);
  }

  public PHPUnitTestCase(
      final Map<?, ?> test, final PHPUnitTestSuite parent, final String sStatus) {
    this(test, parent);
    updateStatus(sStatus);
  }

  @Override
  public Set<AbstractPHPUnitTestResult> getChildren() {
    return Collections.emptySet();
  }

  /**
   * Updates status of this test case.
   *
   * @param sStatus
   */
  public void updateStatus(String sStatus) {
    if (sStatus.equals(PHPUnitMessageParser.STATUS_PASS)) status = STATUS_PASS;
    else if (sStatus.equals(PHPUnitMessageParser.STATUS_SKIP)) status = STATUS_SKIP;
    else if (sStatus.equals(PHPUnitMessageParser.STATUS_INCOMPLETE)) status = STATUS_INCOMPLETE;
    else if (sStatus.equals(PHPUnitMessageParser.STATUS_WARNING)) status = STATUS_WARNING;
    else if (sStatus.equals(PHPUnitMessageParser.STATUS_FAIL)) status = STATUS_FAIL;
    else if (sStatus.equals(PHPUnitMessageParser.STATUS_ERROR)) status = STATUS_ERROR;
    else if (sStatus.equals(PHPUnitMessageParser.TAG_START)) status = STATUS_STARTED;
  }

  /**
   * returns related exception if there is any.
   *
   * @return related exception if there is any
   */
  public PHPUnitTestException getException() {
    return exception;
  }

  /**
   * Returns related warnings if there are any.
   *
   * @return related warnings if there are any
   */
  public List<PHPUnitTestWarning> getWarnings() {
    return warnings;
  }

  /**
   * Sets test case related exception.
   *
   * @param exception
   */
  public void setException(final PHPUnitTestException exception) {
    this.exception = exception;
  }

  /**
   * Sets test case related warnings.
   *
   * @param warnings
   */
  public void setWarnings(final List<PHPUnitTestWarning> warnings) {
    this.warnings = warnings;
  }
}
