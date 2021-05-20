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
package org.eclipse.che.commons.lang;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.testng.Assert.assertEquals;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TopologicalSortTest {

  @Test(dataProvider = "sortingCases")
  public void shouldApplySorting(List<Var> list, List<Var> expectedSorting) throws Exception {
    // when
    sort(list);

    // then
    assertEquals(list, expectedSorting);
  }

  @DataProvider
  public static Object[][] sortingCases() {
    return new Object[][] {
      // keep order if no dependencies
      new Object[] {
        vars(var('b', "."), var('a', "."), var('c', ".")),
        vars(var('b', "."), var('a', "."), var('c', "."))
      },

      // sort dependents after dependencies
      new Object[] {
        vars(var('a', "b"), var('b', "."), var('c', ".")),
        vars(var('b', "."), var('a', "b"), var('c', "."))
      },

      // sort dependents after dependencies, multiple dependencies
      new Object[] {
        vars(var('a', "bc"), var('b', "."), var('c', ".")),
        vars(var('b', "."), var('c', "."), var('a', "bc"))
      },

      // sort dependents after dependencies, check dependee moved after dependencies
      new Object[] {
        vars(var('d', "."), var('a', "c"), var('b', "."), var('c', ".")),
        vars(var('d', "."), var('b', "."), var('c', "."), var('a', "c"))
      },

      // test the robustness against cycles

      // dependent directly on itself
      new Object[] {vars(var('a', "a")), vars(var('a', "a"))},

      // two mutually dependent nodes
      new Object[] {vars(var('a', "b"), var('b', "a")), vars(var('a', "b"), var('b', "a"))},

      // independent node mixed inside a cycle
      new Object[] {
        vars(var('b', "c"), var('d', "."), var('a', "b"), var('c', "a")),
        vars(var('d', "."), var('b', "c"), var('a', "b"), var('c', "a"))
      },

      // cycle (a-b-c) with one of the nodes also depending on another node (a-d),
      // mixed with a "chain" (f-e-b)
      new Object[] {
        vars(
            var('f', "e"),
            var('a', "bd"),
            var('b', "c"),
            var('e', "b"),
            var('c', "a"),
            var('d', ".")),
        vars(
            var('d', "."),
            var('a', "bd"),
            var('b', "c"),
            var('c', "a"),
            var('e', "b"),
            var('f', "e"))
      },

      // removes duplicates
      new Object[] {
        vars(var('a', "."), var('a', "."), var('b', ".")), vars(var('a', "."), var('b', "."))
      }
    };
  }

  private static Var var(char name, String value) {
    return new Var(name, value.chars().mapToObj(c -> (char) c).collect(toSet()));
  }

  private static List<Var> vars(Var... vars) {
    return Stream.of(vars).collect(toList());
  }

  private static void sort(List<Var> vars) {
    Function<Var, Character> idFunction = v -> v.name;
    Function<Var, Set<Character>> predecessors =
        v -> v.dependencies.stream().filter(Character::isAlphabetic).collect(toSet());

    TopologicalSort<Var, Character> sort = new TopologicalSort<>(idFunction, predecessors);
    List<Var> newVars = sort.sort(vars);
    vars.clear();
    vars.addAll(newVars);
  }

  private static final class Var {

    private final char name;
    private final Set<Character> dependencies;

    private Var(char name, Set<Character> dependencies) {
      this.name = name;
      this.dependencies = dependencies;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      Var var = (Var) o;
      return name == var.name && dependencies.equals(var.dependencies);
    }

    @Override
    public int hashCode() {
      return Objects.hash(name, dependencies);
    }

    @Override
    public String toString() {
      return "Var{" + "name='" + name + '\'' + ", deps='" + dependencies + '\'' + '}';
    }
  }
}
