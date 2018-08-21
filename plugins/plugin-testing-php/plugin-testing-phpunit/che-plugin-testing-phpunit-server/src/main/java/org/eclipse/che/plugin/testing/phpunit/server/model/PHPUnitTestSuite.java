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

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.eclipse.che.plugin.testing.phpunit.server.PHPUnitMessageParser;

/**
 * Test suite model element implementation.
 *
 * @author Bartlomiej Laczkowski
 */
public class PHPUnitTestSuite extends AbstractPHPUnitTestResult {

  protected class StatusCount {
    public int[] counts = {0, 0, 0, 0, 0, 0, 0}; // STATUS_STARTED,
    // STATUS_PASS,
    // STATUS_SKIP,
    // STATUS_INCOMPLETE,
    // STATUS_FAIL,
    // STATUS_ERROR
  }

  protected final StatusCount statusCount = new StatusCount();
  private Set<AbstractPHPUnitTestResult> children = null;
  private int runCount = 0;
  private int totalCount;

  public PHPUnitTestSuite(final Map<?, ?> test, final PHPUnitTestSuite parent) {
    super(test, parent);
    if (test == null) totalCount = 0;
    else totalCount = Integer.parseInt((String) test.get(PHPUnitMessageParser.PROPERTY_COUNT));
  }

  @Override
  public Set<AbstractPHPUnitTestResult> getChildren() {
    return children;
  }

  @Override
  public int getRunCount() {
    return runCount;
  }

  @Override
  public int getStatus() {
    return status;
  }

  @Override
  public void setStatus(final int status) {
    statusCount.counts[status]++;
    this.status = Math.max(this.status, status);
    if (parent != null) ((PHPUnitTestSuite) parent).setStatus(status);
  }

  /**
   * Adds child element to this test suite.
   *
   * @param test
   * @param finished
   */
  public void addChild(final AbstractPHPUnitTestResult test, boolean finished) {
    if (children == null) {
      children = new LinkedHashSet<>();
    }
    children.add(test);
    if (test instanceof PHPUnitTestCase && finished) {
      addRunCount(1);
    }
    time += test.getTime();
    setStatus(test.getStatus());
  }

  /**
   * Returns status count.
   *
   * @param status
   * @return status count for provided status
   */
  public int getStatusCount(final int status) {
    return statusCount.counts[status];
  }

  /**
   * Returns total tests count.
   *
   * @return total tests count
   */
  public int getTotalCount() {
    return totalCount;
  }

  /**
   * Sets test suite element parent.
   *
   * @param group
   */
  public void setParent(final PHPUnitTestSuite group) {
    parent = group;
  }

  private void addRunCount(final int count) {
    runCount += count;
    if (parent != null) ((PHPUnitTestSuite) parent).addRunCount(count);
  }
}
