/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.docker.container;

import static java.lang.String.format;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;

/**
 * Finds order of Che containers to start that respects dependencies between containers.
 *
 * @author Alexander Garagatyi
 * @author Alexander Andrienko
 */
public class ContainersStartStrategy {
  /**
   * Resolves order of start for machines in an environment.
   *
   * @throws ValidationException if order of machines can not be calculated
   */
  public List<String> order(DockerEnvironment environment) throws ValidationException {

    Map<String, Integer> weights = weightMachines(environment.getContainers());

    return sortByWeight(weights);
  }

  /**
   * Returns mapping of names of machines to its weights in dependency graph.
   *
   * @throws ValidationException if weights of machines can not be calculated
   */
  private Map<String, Integer> weightMachines(Map<String, DockerContainerConfig> containers)
      throws ValidationException {

    HashMap<String, Integer> weights = new HashMap<>();

    // create machines dependency graph
    Map<String, Set<String>> dependencies = new HashMap<>(containers.size());
    for (Map.Entry<String, DockerContainerConfig> containerEntry : containers.entrySet()) {
      DockerContainerConfig container = containerEntry.getValue();

      Set<String> machineDependencies =
          Sets.newHashSetWithExpectedSize(
              container.getDependsOn().size()
                  + container.getLinks().size()
                  + container.getVolumesFrom().size());

      for (String dependsOn : container.getDependsOn()) {
        checkDependency(
            dependsOn, containerEntry.getKey(), containers, "A machine can not depend on itself");
        machineDependencies.add(dependsOn);
      }

      // links also counts as dependencies
      for (String link : container.getLinks()) {
        String dependency = getContainerFromLink(link);
        checkDependency(
            dependency, containerEntry.getKey(), containers, "A machine can not link to itself");
        machineDependencies.add(dependency);
      }
      // volumesFrom also counts as dependencies
      for (String volumesFrom : container.getVolumesFrom()) {
        String dependency = getContainerFromVolumesFrom(volumesFrom);
        checkDependency(
            dependency,
            containerEntry.getKey(),
            containers,
            "A machine can not contain 'volumes_from' to itself");
        machineDependencies.add(dependency);
      }
      dependencies.put(containerEntry.getKey(), machineDependencies);
    }

    // Find weight of each machine in graph.
    // Weight of machine is calculated as sum of all weights of machines it depends on.
    // Nodes with no dependencies gets weight 0
    while (!dependencies.isEmpty()) {
      int previousSize = dependencies.size();
      for (Iterator<Map.Entry<String, Set<String>>> it = dependencies.entrySet().iterator();
          it.hasNext(); ) {
        // process not yet processed machines only
        Map.Entry<String, Set<String>> containerEntry = it.next();
        String container = containerEntry.getKey();
        Set<String> containerDependencies = containerEntry.getValue();

        if (containerDependencies.isEmpty()) {
          // no links - smallest weight 0
          weights.put(container, 0);
          it.remove();
        } else {
          // machine has dependencies - check if it has not weighted dependencies
          if (weights.keySet().containsAll(containerDependencies)) {
            // all connections are weighted - lets evaluate current machine
            Optional<String> maxWeight =
                containerDependencies.stream().max(Comparator.comparing(weights::get));
            // optional can't be empty because size of the list is checked above
            // noinspection OptionalGetWithoutIsPresent
            weights.put(container, weights.get(maxWeight.get()) + 1);
            it.remove();
          }
        }
      }
      if (dependencies.size() == previousSize) {
        throw new ValidationException(
            "Launch order of machines '"
                + Joiner.on(", ").join(dependencies.keySet())
                + "' can't be evaluated. Circular dependency.");
      }
    }

    return weights;
  }

  /** Parses link content into depends_on field representation - removes column and further chars */
  private String getContainerFromLink(String link) throws ValidationException {
    String container = link;
    if (link != null) {
      String[] split = container.split(":");
      if (split.length > 2) {
        throw new ValidationException(format("Container link '%s' is invalid", link));
      }
      container = split[0];
    }
    return container;
  }

  /**
   * Parses volumesFrom content into depends_on field representation - removes column and further
   * chars
   */
  private String getContainerFromVolumesFrom(String volumesFrom) throws ValidationException {
    String container = volumesFrom;
    if (volumesFrom != null) {
      String[] split = container.split(":");
      if (split.length > 2) {
        throw new ValidationException(
            format("Container volumes_from '%s' is invalid", volumesFrom));
      }
      container = split[0];
    }
    return container;
  }

  private List<String> sortByWeight(Map<String, Integer> weights) {
    return weights
        .entrySet()
        .stream()
        .sorted(Comparator.comparing(Map.Entry::getValue))
        .map(Map.Entry::getKey)
        .collect(Collectors.toList());
  }

  private void checkDependency(
      String dependency,
      String containerName,
      Map<String, DockerContainerConfig> containers,
      String errorMessage)
      throws ValidationException {
    if (containerName.equals(dependency)) {
      throw new ValidationException(errorMessage + ": " + containerName);
    }
    if (!containers.containsKey(dependency)) {
      throw new ValidationException(
          format(
              "Dependency '%s' in machine '%s' points to unknown machine.",
              dependency, containerName));
    }
  }
}
