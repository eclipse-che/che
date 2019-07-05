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

/**
 * Tests root element.
 *
 * @author Bartlomiej Laczkowski
 */
public class PHPUnitTestRoot extends PHPUnitTestSuite {

  public PHPUnitTestRoot() {
    super(null, null);
  }

  @Override
  public String getName() {
    return "Test Results";
  }

  @Override
  public void setStatus(final int status) {
    statusCount.counts[status]++;
    this.status = Math.max(this.status, status);
    if (parent != null) ((PHPUnitTestSuite) parent).setStatus(status);
  }
}
