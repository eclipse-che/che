/**
 * ***************************************************************************** Copyright (c) 2016
 * Rogue Wave Software, Inc. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Rogue Wave Software, Inc. - initial API and implementation
 * *****************************************************************************
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
