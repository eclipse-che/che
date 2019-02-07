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
package org.eclipse.che.workspace.infrastructure.kubernetes.provision.env;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.function.Function.identity;
import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.env.EnvironmentVariablesSorter.findReferences;
import static org.testng.Assert.assertEquals;

import io.fabric8.kubernetes.api.model.EnvVar;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.testng.annotations.Test;

public class EnvironmentVariableSorterTest {

  @Test
  public void shouldDetectReferences() {
    // creating a separate test case for each of these is just too much for me :)
    // breaking given-when-then flow of our tests on purpose here, because the below would result
    // in 24 test cases I refuse to write out manually

    testSingleValue("value", emptySet(), "no refs");
    testSingleValue("$(NO_REF", emptySet(), "unclosed ref");
    testSingleValue("$$(NO_REF)", emptySet(), "escaped ref");
    testSingleValue("$NO_REF)", emptySet(), "invalid start ref");
    testSingleValue("$(NO REF)", emptySet(), "invalid name ref");
    testSingleValue("$(REF)", singleton("REF"), "valid ref");
  }

  @Test
  public void shouldSortByNameIfNoDependencies() throws Exception {
    // given
    Map<String, EnvVar> vars = vars(var("b", "b"), var("a", "a"), var("c", "c"));

    // when
    List<EnvVar> sorted = EnvironmentVariablesSorter.sort(vars);

    // then
    assertEquals(sorted, Arrays.asList(var("a", "a"), var("b", "b"), var("c", "c")));
  }

  @Test
  public void shouldSortDependenciesBeforeDependents() throws Exception {
    // given
    Map<String, EnvVar> vars = vars(var("a", "$(b)"), var("b", "b"), var("c", "c"));

    // when
    List<EnvVar> sorted = EnvironmentVariablesSorter.sort(vars);

    // then
    assertEquals(sorted, Arrays.asList(var("b", "b"), var("c", "c"), var("a", "$(b)")));
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void shouldDetectSelfReference() throws Exception {
    // given
    Map<String, EnvVar> vars = vars(var("a", "$(a)"));

    // when
    EnvironmentVariablesSorter.sort(vars);

    // then the exception is expected
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void shouldDetectDirectCycles() throws Exception {
    // given
    Map<String, EnvVar> vars = vars(var("a", "$(b)"), var("b", "$(a)"));

    // when
    EnvironmentVariablesSorter.sort(vars);

    // then the exception is expected
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void shouldDetectIndirectCycles() throws Exception {
    // given
    Map<String, EnvVar> vars = vars(var("a", "$(b)"), var("b", "$(c)"), var("c", "$(a)"));

    // when
    EnvironmentVariablesSorter.sort(vars);

    // then the exception is expected
  }

  @Test
  public void shouldDetectMultipleReferences() throws Exception {
    // given
    EnvVar envVar = var("a", "$(b) $(c) $$(d)");

    // when
    SortedSet<String> refs = EnvironmentVariablesSorter.findReferences(envVar);

    // then
    assertEquals(refs, new HashSet<>(Arrays.asList("b", "c")));
  }

  private static EnvVar var(String name, String value) {
    return new EnvVar(name, value, null);
  }

  private static Map<String, EnvVar> vars(EnvVar... vars) {
    return Stream.of(vars).collect(Collectors.toMap(EnvVar::getName, identity()));
  }

  private static void testSingleValue(String value, Set<String> expected, String caseName) {
    assertEquals(findReferences(var("name", value)), expected, caseName + ": sole");
    assertEquals(findReferences(var("name", value + "v")), expected, caseName + ": start");
    assertEquals(findReferences(var("name", "v" + value + "v")), expected, caseName + ": middle");
    assertEquals(findReferences(var("name", value + "v")), expected, caseName + ": end");
  }
}
