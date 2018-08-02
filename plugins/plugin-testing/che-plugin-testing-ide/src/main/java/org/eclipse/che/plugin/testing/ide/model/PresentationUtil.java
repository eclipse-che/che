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

import org.eclipse.che.ide.util.StringUtils;

/** The utility class which makes readable presentation of the test. */
public class PresentationUtil {
  private static final String TEST_WITHOUT_NAME = "<No name>";

  public static String getPresentation(TestState testState) {
    TestState parent = testState.getParent();
    String name = testState.getName();
    String result = name;
    if (parent != null) {
      String parentName = parent.getName();
      if (parentName != null) {
        boolean startsWith = name.startsWith(parentName);
        if (!startsWith && parent instanceof TestRootState) {
          String presentation = parent.getPresentation();
          if (presentation != null) {
            parentName = presentation;
            startsWith = name.startsWith(parentName);

            if (!startsWith) {
              String comment = ((TestRootState) parent).getComment();
              if (comment != null) {
                parentName = asQualifiedName(comment, presentation);
                startsWith = name.startsWith(parentName);
              }
            }
          }
        }

        if (startsWith) {
          result = name.substring(parentName.length());
          result = StringUtils.trimStart(result, ".");
        }
      }
    }
    result = result.trim();

    if (result.isEmpty()) {
      return TEST_WITHOUT_NAME;
    }

    return result;
  }

  public static String asQualifiedName(String packageName, String typeName) {
    if (packageName == null || packageName.isEmpty()) {
      return typeName;
    }
    return packageName + "." + typeName;
  }
}
