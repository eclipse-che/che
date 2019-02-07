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

import static java.lang.String.format;
import static java.util.Comparator.naturalOrder;

import com.google.common.annotations.VisibleForTesting;
import io.fabric8.kubernetes.api.model.EnvVar;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;

/**
 * If there are any references to other environment variables in the value of some environment
 * variable, the "dependent" environment variable needs to be defined "later" than the env var it
 * depends on for Kubernetes variable expansion to work.
 *
 * <p>This class makes sure that the environment variables are sorted.
 */
final class EnvironmentVariablesSorter implements Comparator<EnvVar> {

  private static final Pattern REFERENCE_PATTERN = Pattern.compile("\\$\\(\\w+\\)");

  private final Map<String, SortedSet<String>> referenceMap;
  private final Map<String, SortedSet<String>> recursiveReferenceMap;

  private EnvironmentVariablesSorter(Map<String, EnvVar> envVars) {
    this.referenceMap = new HashMap<>(envVars.size());
    for (Map.Entry<String, EnvVar> e : envVars.entrySet()) {
      referenceMap.put(e.getKey(), findReferences(e.getValue()));
    }

    recursiveReferenceMap = new HashMap<>(envVars.size());
  }

  /**
   * Sorts the provided env variables so that a variable depending on some other variable is always
   * later in the returned list than the env var it depends on.
   *
   * @param envVars the collection of environment variables to sort
   * @return a sorted list of the environment variables
   * @throws InfrastructureException if there is a cycle in the value dependencies and thus the
   *     expansion is impossible
   */
  static List<EnvVar> sort(Map<String, EnvVar> envVars) throws InfrastructureException {
    try {
      EnvironmentVariablesSorter sorter = new EnvironmentVariablesSorter(envVars);
      List<EnvVar> ret = new ArrayList<>(envVars.values());
      ret.sort(sorter);
      return ret;
    } catch (CycleException e) {
      throw new InfrastructureException(e.getMessage());
    }
  }

  @Override
  public int compare(EnvVar a, EnvVar b) throws CycleException {
    if (a == b) {
      return 0;
    }

    SortedSet<String> aRefs = referenceMap.get(a.getName());
    SortedSet<String> bRefs = referenceMap.get(b.getName());

    // quickly deal with the common case of no references in the value
    if (aRefs.isEmpty()) {
      // we need to have a total ordering, so if there is no difference in references, we need
      // to pick a different sorting criteria
      return bRefs.isEmpty() ? a.getName().compareTo(b.getName()) : -1;
    } else if (bRefs.isEmpty()) {
      // b has no references, a has, therefore a > b.
      return 1;
    }

    // ok, we have variable references in both a and b
    // we need to establish the transitive closure over the references to figure out whether there
    // is a cycle in the dependencies, or not
    SortedSet<String> aTransRefs = transitiveClosure(a.getName());
    SortedSet<String> bTransRefs = transitiveClosure(b.getName());

    // we don't need to check for cycle here, because that has been detected when establishing
    // the closure.
    if (aTransRefs.contains(b.getName())) {
      // a depends on b, hence a > b.
      return 1;
    } else if (bTransRefs.contains(a.getName())) {
      // b depends on a -> a < b.
      return -1;
    }

    // ok, the two env vars don't depend on each other, so we actually don't care too much about
    // the order. We need to be consistent and total, though...

    int sizeDiff = aTransRefs.size() - bTransRefs.size();
    if (sizeDiff != 0) {
      return sizeDiff;
    }

    // ah, the two env vars have the same number of references, so we need to take a "slow path"
    // in determining the total order.
    Iterator<String> aIt = aRefs.iterator();
    Iterator<String> bIt = bRefs.iterator();
    while (aIt.hasNext()) {
      if (!bIt.hasNext()) {
        // a has more references than b
        return 1;
      }

      String aRef = aIt.next();
      String bRef = bIt.next();

      int res = aRef.compareTo(bRef);
      if (res != 0) {
        return res;
      }
    }

    return 0;
  }

  private SortedSet<String> transitiveClosure(String variableName) {
    SortedSet<String> recRefs = recursiveReferenceMap.get(variableName);
    if (recRefs == null) {
      recRefs = new TreeSet<>(naturalOrder());
      fillRecursiveReferences(variableName, referenceMap, recRefs);
      recursiveReferenceMap.put(variableName, recRefs);
    }
    return recRefs;
  }

  @VisibleForTesting
  static SortedSet<String> findReferences(EnvVar var) throws CycleException {
    String val = var.getValue();

    Matcher matcher = REFERENCE_PATTERN.matcher(val);

    SortedSet<String> ret = new TreeSet<>(naturalOrder());

    while (matcher.find()) {
      int start = matcher.start();

      // the variable reference can be escaped using a double $, e.g. $$(VAR) is not a reference
      if (start > 0 && val.charAt(start - 1) == '$') {
        continue;
      }

      // extract the variable name out of the reference $(NAME) -> NAME
      String refName = matcher.group().substring(2, matcher.group().length() - 1);
      if (refName.equals(var.getName())) {
        throw new CycleException(refName);
      }
      ret.add(refName);
    }

    return ret;
  }

  private static void fillRecursiveReferences(
      String name, Map<String, SortedSet<String>> directReferences, SortedSet<String> result)
      throws CycleException {

    Set<String> nameRefs = directReferences.get(name);
    if (nameRefs == null || nameRefs.isEmpty()) {
      return;
    }

    if (result.contains(name)) {
      throw new CycleException(name);
    }

    for (String refName : nameRefs) {
      result.add(refName);
      fillRecursiveReferences(refName, directReferences, result);
    }
  }

  static final class CycleException extends RuntimeException {

    CycleException(String variableName) {
      super(format("Recursive definition of environment variable %s", variableName));
    }
  }
}
