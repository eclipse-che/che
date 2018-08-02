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
package org.eclipse.che.ide.ext.java.client;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Tests for {@link JavaUtils} */
public class JavaUtilsTest {
  @Test
  public void shouldValidatePackageNameWithCorrectContent() throws Exception {
    assertTrue(JavaUtils.isValidPackageName("Package_name"));
  }

  @Test
  public void shouldInvalidatePackageNameWithWhiteSpaces() throws Exception {
    assertFalse(JavaUtils.isValidPackageName("Package name"));
  }

  @Test
  public void shouldInvalidatePackageNameWithMinuses() throws Exception {
    assertFalse(JavaUtils.isValidPackageName("Package-name"));
  }

  @Test
  public void shouldInvalidatePackageNameStartingWithNumbers() throws Exception {
    assertFalse(JavaUtils.isValidPackageName("1Package_name"));
  }

  @Test
  public void shouldInvalidatePackageNameStartingWithSpaces() throws Exception {
    assertFalse(JavaUtils.isValidPackageName(" Package_name"));
  }

  @Test
  public void shouldValidateClassNameWithCorrectContent() throws Exception {
    assertTrue(JavaUtils.isValidCompilationUnitName("Class_name"));
  }

  @Test
  public void shouldInvalidateClassNameWithSpecialSymbols() throws Exception {
    assertFalse(JavaUtils.isValidCompilationUnitName("&%$#"));
  }

  @Test
  public void shouldInvalidateClassNameWithMinuses() throws Exception {
    assertFalse(JavaUtils.isValidCompilationUnitName("Class-name"));
  }

  @Test
  public void shouldInvalidateClassNameStartingWithNumbers() throws Exception {
    assertFalse(JavaUtils.isValidCompilationUnitName("1Class_name"));
  }

  @Test
  public void shouldInvalidateClassNameStartingWithSpaces() throws Exception {
    assertFalse(JavaUtils.isValidCompilationUnitName(" Class_name"));
  }
}
