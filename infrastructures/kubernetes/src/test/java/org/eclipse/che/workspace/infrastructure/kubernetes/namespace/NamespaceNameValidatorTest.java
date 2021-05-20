/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.namespace;

import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.NamespaceNameValidator.ValidationResult.INVALID;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.NamespaceNameValidator.ValidationResult.NULL_OR_EMPTY;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.NamespaceNameValidator.ValidationResult.TOO_LONG;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.NamespaceNameValidator.validateInternal;
import static org.testng.Assert.assertEquals;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class NamespaceNameValidatorTest {

  @Test
  public void testFailsOnNull() {
    assertEquals(validateInternal(null), NULL_OR_EMPTY);
  }

  @Test
  public void testFailsOnEmpty() {
    assertEquals(validateInternal(""), NULL_OR_EMPTY);
  }

  @Test
  public void testFailsOnTooLong() {
    assertEquals(
        validateInternal("0123456789012345678901234567890123456789012345678901234567890123"),
        TOO_LONG);
  }

  @Test(dataProvider = "invalidDnsNames")
  public void testFailsOnInvalidChars(String invalidName) {
    assertEquals(validateInternal(invalidName), INVALID);
  }

  @DataProvider
  public static Object[][] invalidDnsNames() {
    return new Object[][] {
      new Object[] {" "},
      new Object[] {"-a"},
      new Object[] {"a-"},
      new Object[] {"A"},
      new Object[] {"<"},
      new Object[] {"@"},
      new Object[] {"*"},
      new Object[] {";"},
    };
  }
}
