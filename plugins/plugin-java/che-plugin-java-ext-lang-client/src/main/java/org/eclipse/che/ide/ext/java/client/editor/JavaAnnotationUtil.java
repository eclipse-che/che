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
package org.eclipse.che.ide.ext.java.client.editor;

import org.eclipse.che.ide.api.editor.text.annotation.Annotation;

public final class JavaAnnotationUtil {

  private JavaAnnotationUtil() {}

  public static boolean hasCorrections(final Annotation annotation) {
    if (annotation instanceof JavaAnnotation) {
      final JavaAnnotation javaAnnotation = (JavaAnnotation) annotation;
      final int problemId = javaAnnotation.getId();
      if (problemId != -1) {
        return QuickFixResolver.hasCorrections(problemId);
      }
    }
    return false;
  }

  public static boolean isQuickFixableType(final Annotation annotation) {
    return (annotation instanceof JavaAnnotation) && !annotation.isMarkedDeleted();
  }
}
