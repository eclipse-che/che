/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
