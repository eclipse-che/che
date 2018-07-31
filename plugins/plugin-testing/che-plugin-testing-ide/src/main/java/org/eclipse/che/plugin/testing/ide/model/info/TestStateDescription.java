/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.testing.ide.model.info;

/** Describes state of test. */
public enum TestStateDescription {
  SKIPPED("Skiped"),
  COMPLETED("Completed"),
  NOT_RUN("Not run"),
  RUNNING("Running..."),
  TERMINATED("Terminated"),
  IGNORED("Ignored"),
  FAILED("Failed"),
  ERROR("Error"),
  PASSED("Passed");

  private final String title;

  TestStateDescription(String title) {
    this.title = title;
  }

  public String getTitle() {
    return title;
  }
}
