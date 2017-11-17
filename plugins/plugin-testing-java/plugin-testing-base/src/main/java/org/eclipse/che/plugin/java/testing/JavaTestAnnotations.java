/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.java.testing;

/** Describes annotations for JUnit4x an TestNG. */
public class JavaTestAnnotations {
  public static final JavaTestAnnotations TESTNG_TEST =
      new JavaTestAnnotations("org.testng.annotations.Test");
  public static final JavaTestAnnotations JUNIT4X_RUN_WITH =
      new JavaTestAnnotations("org.junit.runner.RunWith");
  public static final JavaTestAnnotations JUNIT4X_TEST = new JavaTestAnnotations("org.junit.Test");

  private final String fName;

  private JavaTestAnnotations(String name) {
    fName = name;
  }

  /**
   * Returns name of the annotation.
   *
   * @return name of the annotation
   */
  public String getName() {
    return fName;
  }
}
