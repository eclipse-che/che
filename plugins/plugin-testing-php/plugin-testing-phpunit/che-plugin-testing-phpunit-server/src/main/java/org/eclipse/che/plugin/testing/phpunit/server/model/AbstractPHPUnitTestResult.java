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

import java.util.Map;
import java.util.Set;
import org.eclipse.che.plugin.testing.phpunit.server.PHPUnitMessageParser;

/**
 * Abstract implementation for PHP unit test result elements.
 *
 * @author Bartlomiej Laczkowski
 */
public abstract class AbstractPHPUnitTestResult extends AbstractPHPUnitElement {

  public static final int STATUS_STARTED = 0;
  public static final int STATUS_PASS = 1;
  public static final int STATUS_SKIP = 2;
  public static final int STATUS_INCOMPLETE = 3;
  public static final int STATUS_WARNING = 4;
  public static final int STATUS_FAIL = 5;
  public static final int STATUS_ERROR = 6;

  protected String name = "";
  protected int status = 0;
  protected double time = 0;

  public AbstractPHPUnitTestResult(final Map<?, ?> test, final PHPUnitTestSuite parent) {
    super(test, parent);
    if (test != null) name = (String) test.get(PHPUnitMessageParser.PROPERTY_NAME);
  }

  /**
   * Implementors should return this element children.
   *
   * @return this element children
   */
  public abstract Set<AbstractPHPUnitTestResult> getChildren();

  /**
   * Returns element name.
   *
   * @return element name.
   */
  public String getName() {
    return name;
  }

  /**
   * Returns number of run count.
   *
   * @return number of run count
   */
  public int getRunCount() {
    return 1;
  }

  /**
   * Returns element status.
   *
   * @return element status.
   */
  public int getStatus() {
    return status;
  }

  /**
   * Sets element status.
   *
   * @param status
   */
  public void setStatus(final int status) {
    this.status = status;
  }

  /**
   * Returns execution time.
   *
   * @return execution time
   */
  public double getTime() {
    return time;
  }

  /**
   * Sets the execution time.
   *
   * @param time
   */
  public void setTime(double time) {
    this.time = time;
  }
}
