/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.environment.server;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;

import org.eclipse.che.api.environment.server.model.CheServiceImpl;
import org.eclipse.che.api.environment.server.model.CheServicesEnvironmentImpl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;

/**
 * Finds order of Che services to start that respects dependencies between services.
 *
 * @author Alexander Garagatyi
 * @author Alexander Andrienko
 */
public class DefaultServicesStartStrategy {
    /**
     * Resolves order of start for machines in an environment.
     *
     * @throws IllegalArgumentException
     *         if order of machines can not be calculated
     */
    public List<String> order(CheServicesEnvironmentImpl composeEnvironment) throws IllegalArgumentException {

        Map<String, Integer> weights = weightMachines(composeEnvironment.getServices());

        return sortByWeight(weights);
    }

    /**
     * Returns mapping of names of machines to its weights in dependency graph.
     *
     * @throws IllegalArgumentException
     *         if weights of machines can not be calculated
     */
    private Map<String, Integer> weightMachines(Map<String, CheServiceImpl> services)
            throws IllegalArgumentException {

        HashMap<String, Integer> weights = new HashMap<>();

        // create machines dependency graph
        Map<String, Set<String>> dependencies = new HashMap<>(services.size());
        for (Map.Entry<String, CheServiceImpl> serviceEntry : services.entrySet()) {
            CheServiceImpl service = serviceEntry.getValue();

            Set<String> machineDependencies = Sets.newHashSetWithExpectedSize(service.getDependsOn().size() +
                                                                              service.getLinks().size() +
                                                                              service.getVolumesFrom().size());

            for (String dependsOn : service.getDependsOn()) {
                checkDependency(dependsOn, serviceEntry.getKey(), services, "A machine can not depend on itself");
                machineDependencies.add(dependsOn);
            }

            // links also counts as dependencies
            for (String link : service.getLinks()) {
                String dependency = getServiceFromLink(link);
                checkDependency(dependency, serviceEntry.getKey(), services, "A machine can not link to itself");
                machineDependencies.add(dependency);
            }
            // volumesFrom also counts as dependencies
            for (String volumesFrom : service.getVolumesFrom()) {
                String dependency = getServiceFromVolumesFrom(volumesFrom);
                checkDependency(dependency, serviceEntry.getKey(), services, "A machine can not contain 'volumes_from' to itself");
                machineDependencies.add(dependency);
            }
            dependencies.put(serviceEntry.getKey(), machineDependencies);
        }

        // Find weight of each machine in graph.
        // Weight of machine is calculated as sum of all weights of machines it depends on.
        // Nodes with no dependencies gets weight 0
        while (!dependencies.isEmpty()) {
            int previousSize = dependencies.size();
            for (Iterator<Map.Entry<String, Set<String>>> it = dependencies.entrySet().iterator(); it.hasNext();) {
                // process not yet processed machines only
                Map.Entry<String, Set<String>> serviceEntry = it.next();
                String service = serviceEntry.getKey();
                Set<String> serviceDependencies = serviceEntry.getValue();

                if (serviceDependencies.isEmpty()) {
                    // no links - smallest weight 0
                    weights.put(service, 0);
                    it.remove();
                } else {
                    // machine has dependencies - check if it has not weighted dependencies
                    if (weights.keySet().containsAll(serviceDependencies)) {
                        // all connections are weighted - lets evaluate current machine
                        Optional<String> maxWeight = serviceDependencies.stream()
                                                                        .max((o1, o2) -> weights.get(o1).compareTo(weights.get(o2)));
                        // optional can't be empty because size of the list is checked above
                        //noinspection OptionalGetWithoutIsPresent
                        weights.put(service, weights.get(maxWeight.get()) + 1);
                        it.remove();
                    }
                }
            }
            if (dependencies.size() == previousSize) {
                throw new IllegalArgumentException("Launch order of machines '" +
                                                   Joiner.on(", ").join(dependencies.keySet()) +
                                                   "' can't be evaluated. Circular dependency.");
            }
        }

        return weights;
    }

    /**
     * Parses link content into depends_on field representation - removes column and further chars
     */
    private String getServiceFromLink(String link) throws IllegalArgumentException {
        String service = link;
        if (link != null) {
            String[] split = service.split(":");
            if (split.length > 2) {
                throw new IllegalArgumentException(format("Service link '%s' is invalid", link));
            }
            service = split[0];
        }
        return service;
    }

    /**
     * Parses volumesFrom content into depends_on field representation - removes column and further chars
     */
    private String getServiceFromVolumesFrom(String volumesFrom) throws IllegalArgumentException {
        String service = volumesFrom;
        if (volumesFrom != null) {
            String[] split = service.split(":");
            if (split.length > 2) {
                throw new IllegalArgumentException(format("Service volumes_from '%s' is invalid", volumesFrom));
            }
            service = split[0];
        }
        return service;
    }

    private List<String> sortByWeight(Map<String, Integer> weights) {
        return weights.entrySet()
                      .stream()
                      .sorted((o1, o2) -> o1.getValue().compareTo(o2.getValue()))
                      .map(Map.Entry::getKey)
                      .collect(Collectors.toList());
    }

    private void checkDependency(String dependency, String serviceName, Map<String, CheServiceImpl> services, String errorMessage) {
        if (serviceName.equals(dependency)) {
            throw new IllegalArgumentException(errorMessage + ": " + serviceName);
        }
        if (!services.containsKey(dependency)) {
            throw new IllegalArgumentException(
                    format("Dependency '%s' in machine '%s' points to unknown machine.",
                           dependency, serviceName));
        }
    }
}
