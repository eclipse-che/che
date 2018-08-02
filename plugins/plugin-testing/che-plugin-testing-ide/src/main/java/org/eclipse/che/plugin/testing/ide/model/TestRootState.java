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

/** Describes test root state. */
public class TestRootState extends TestState {
  public static final String ROOT = "Root";

  private boolean testReporterAttached;

  private String rootLocationUrl;
  private String presentation;
  private String comment;

  public TestRootState() {
    super(ROOT, true, null);
  }

  public boolean isTestReporterAttached() {
    return testReporterAttached;
  }

  public void setTestReporterAttached() {
    this.testReporterAttached = true;
  }

  public String getRootLocationUrl() {
    return rootLocationUrl;
  }

  public void setRootLocationUrl(String rootLocationUrl) {
    this.rootLocationUrl = rootLocationUrl;
  }

  public String getPresentation() {
    return presentation;
  }

  public void setPresentation(String presentation) {
    this.presentation = presentation;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }
}
