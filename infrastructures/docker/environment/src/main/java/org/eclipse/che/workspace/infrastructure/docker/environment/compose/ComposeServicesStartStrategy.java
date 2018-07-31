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
package org.eclipse.che.workspace.infrastructure.docker.environment.compose;

import static java.lang.String.format;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.workspace.infrastructure.docker.environment.compose.model.ComposeService;

/**
 * Finds order of Che containers to start that respects dependencies between containers.
 *
 * @author Alexander Garagatyi
 * @author Alexander Andrienko
 */
public class ComposeServicesStartStrategy {
  /**
   * Resolves order of start for compose services in an environment.
   *
   * @throws ValidationException if order of services can not be calculated
   */
  public LinkedHashMap<String, ComposeService> order(Map<String, ComposeService> services)
      throws ValidationException {

    Map<String, Integer> weights = weightServices(services);
    List<String> orderedServicesNames = sortByWeight(weights);

    LinkedHashMap<String, ComposeService> orderedServices = new LinkedHashMap<>();
    for (String serviceName : orderedServicesNames) {
      orderedServices.put(serviceName, services.get(serviceName));
    }

    return orderedServices;
  }

  /**
   * Returns mapping of names of services to its weights in dependency graph.
   *
   * @throws ValidationException if weights of services can not be calculated
   */
  private Map<String, Integer> weightServices(Map<String, ComposeService> services)
      throws ValidationException {

    HashMap<String, Integer> weights = new HashMap<>();

    // create services dependency graph
    Map<String, Set<String>> dependencies = new HashMap<>(services.size());
    for (Map.Entry<String, ComposeService> containerEntry : services.entrySet()) {
      ComposeService service = containerEntry.getValue();

      Set<String> serviceDependencies =
          Sets.newHashSetWithExpectedSize(
              service.getDependsOn().size()
                  + service.getLinks().size()
                  + service.getVolumesFrom().size());

      for (String dependsOn : service.getDependsOn()) {
        checkDependency(
            dependsOn, containerEntry.getKey(), services, "A service can not depend on itself");
        serviceDependencies.add(dependsOn);
      }

      // links also counts as dependencies
      for (String link : service.getLinks()) {
        String dependency = getContainerFromLink(link);
        checkDependency(
            dependency, containerEntry.getKey(), services, "A service can not link to itself");
        serviceDependencies.add(dependency);
      }
      // volumesFrom also counts as dependencies
      for (String volumesFrom : service.getVolumesFrom()) {
        String dependency = getContainerFromVolumesFrom(volumesFrom);
        checkDependency(
            dependency,
            containerEntry.getKey(),
            services,
            "A service can not contain 'volumes_from' to itself");
        serviceDependencies.add(dependency);
      }
      dependencies.put(containerEntry.getKey(), serviceDependencies);
    }

    // Find weight of each service in graph.
    // Weight of service is calculated as sum of all weights of services it depends on.
    // Nodes with no dependencies gets weight 0
    while (!dependencies.isEmpty()) {
      int previousSize = dependencies.size();
      for (Iterator<Map.Entry<String, Set<String>>> it = dependencies.entrySet().iterator();
          it.hasNext(); ) {
        // process not yet processed services only
        Map.Entry<String, Set<String>> containerEntry = it.next();
        String container = containerEntry.getKey();
        Set<String> containerDependencies = containerEntry.getValue();

        if (containerDependencies.isEmpty()) {
          // no links - smallest weight 0
          weights.put(container, 0);
          it.remove();
        } else {
          // service has dependencies - check if it has not weighted dependencies
          if (weights.keySet().containsAll(containerDependencies)) {
            // all connections are weighted - lets evaluate current service
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
            "Launch order of services '"
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
        throw new ValidationException(format("Service link '%s' is invalid", link));
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
        throw new ValidationException(format("Service volumes_from '%s' is invalid", volumesFrom));
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
      Map<String, ComposeService> containers,
      String errorMessage)
      throws ValidationException {
    if (containerName.equals(dependency)) {
      throw new ValidationException(errorMessage + ": " + containerName);
    }
    if (!containers.containsKey(dependency)) {
      throw new ValidationException(
          format(
              "Dependency '%s' in service '%s' points to unknown service.",
              dependency, containerName));
    }
  }
}
