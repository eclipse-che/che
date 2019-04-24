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
package org.eclipse.che.workspace.infrastructure.kubernetes.util;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.eclipse.che.workspace.infrastructure.kubernetes.util.EnvVars.extractReferencedVariables;
import static org.testng.Assert.assertEquals;

import io.fabric8.kubernetes.api.model.EnvVar;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class EnvVarsTest {

  @Test(dataProvider = "detectReferencesTestValues")
  public void shouldDetectReferences(String value, Set<EnvVar> expected, String caseName) {
    assertEquals(
        extractReferencedVariables(var("name", value)), expected, caseName + ": just value");
    assertEquals(
        extractReferencedVariables(var("name", "v" + value)),
        expected,
        caseName + ": value with prefix");
    assertEquals(
        extractReferencedVariables(var("name", "v" + value + "v")),
        expected,
        caseName + ": value with prefix and postfix");
    assertEquals(
        extractReferencedVariables(var("name", value + "v")),
        expected,
        caseName + ": value with postfix");
  }

  @DataProvider
  public static Object[][] detectReferencesTestValues() {
    return new Object[][] {
      new Object[] {"value", emptySet(), "no refs"},
      new Object[] {"$(NO_REF", emptySet(), "unclosed ref"},
      new Object[] {"$$(NO_REF)", emptySet(), "escaped ref"},
      new Object[] {"$NO_REF)", emptySet(), "invalid start ref"},
      new Object[] {"$(NO REF)", emptySet(), "invalid name ref"},
      new Object[] {"$(REF)", singleton("REF"), "valid ref"}
    };
  }

  @Test
  public void shouldDetectMultipleReferences() throws Exception {
    // given
    EnvVar envVar = var("a", "$(b) $(c) $$(d)");

    // when
    Set<String> refs = extractReferencedVariables(envVar);

    // then
    assertEquals(refs, new HashSet<>(Arrays.asList("b", "c")));
  }

  private static EnvVar var(String name, String value) {
    return new EnvVar(name, value, null);
  }
}
