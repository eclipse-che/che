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
package org.eclipse.che.plugin.java.testing;

import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

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

  private boolean annotates(IAnnotationBinding[] annotations) {
    for (IAnnotationBinding annotation : annotations) {
      ITypeBinding annotationType = annotation.getAnnotationType();
      if (annotationType != null && (annotationType.getQualifiedName().equals(fName))) {
        return true;
      }
    }
    return false;
  }

  /**
   * Check if type annotated as test type.
   *
   * @param type type which should be checked
   * @return {@code true} if type annotated as test otherwise returns {@code false}
   */
  public boolean annotatesTypeOrSuperTypes(ITypeBinding type) {
    while (type != null) {
      if (annotates(type.getAnnotations())) {
        return true;
      }
      type = type.getSuperclass();
    }
    return false;
  }

  /**
   * Find method which annotated as test method.
   *
   * @param type type which contains methods
   * @return {@code true} if least one has test annotation otherwise returns {@code false}
   */
  public boolean annotatesAtLeastOneMethod(ITypeBinding type) {
    while (type != null) {
      IMethodBinding[] declaredMethods = type.getDeclaredMethods();
      for (int i = 0; i < declaredMethods.length; i++) {
        IMethodBinding curr = declaredMethods[i];
        if (annotates(curr.getAnnotations())) {
          return true;
        }
      }
      type = type.getSuperclass();
    }
    return false;
  }
}
