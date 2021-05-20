/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.server.external;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class IngressPathTransformInverterTest {

  @Test(dataProvider = "transformationTestCases")
  public void testUndoPathTransformationWithBasicTransform(
      String format, String transformedPath, String originalPath) {
    IngressPathTransformInverter inverter = new IngressPathTransformInverter(format);
    assertEquals(inverter.undoPathTransformation(transformedPath), originalPath);
  }

  @DataProvider
  public static Object[][] transformationTestCases() {
    return new Object[][] {
      {"%s", "path", "path"},
      {"invalid", "path", "path"},
      {"%ssuffix", "pathsuffix", "path"},
      {"prefix%s", "prefixpath", "path"},
      {"prefix%ssuffix", "prefixpathsuffix", "path"},
      {"prefix%s", "non-matching", "non-matching"},
      {null, "some path", "some path"}
    };
  }
}
