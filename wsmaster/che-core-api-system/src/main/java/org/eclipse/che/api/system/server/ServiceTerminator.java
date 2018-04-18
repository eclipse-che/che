/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.system.server;

import static java.lang.String.format;

import com.google.common.annotations.VisibleForTesting;

import java.util.*;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.system.shared.event.service.StoppingSystemServiceEvent;
import org.eclipse.che.api.system.shared.event.service.SuspendingSystemServiceEvent;
import org.eclipse.che.api.system.shared.event.service.SystemServiceStoppedEvent;
import org.eclipse.che.api.system.shared.event.service.SystemServiceSuspendedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Terminates system services.
 *
 * @author Yevhenii Voevodin
 */
class ServiceTerminator {

  private static final Logger LOG = LoggerFactory.getLogger(ServiceTerminator.class);

  private final EventService eventService;
  private final List<ServiceTermination> terminations;

  @Inject
  ServiceTerminator(EventService eventService, Set<ServiceTermination> terminations) {
    this.eventService = eventService;
    this.terminations = new ArrayList(terminations);
    Collections.sort(new ArrayList(terminations), new ServiceTerminationComparator(terminations));
  }

  public static class ServiceTerminationComparator implements Comparator<ServiceTermination> {
    public ServiceTerminationComparator(Set<ServiceTermination> terminations) {
      this.dependencies =
          terminations
              .stream()
              .collect(
                  Collectors.toMap(ServiceTermination::getServiceName, e -> e.getDependencies()));
    }

    private final Map<String, Set<String>> dependencies;

    @Override
    public int compare(ServiceTermination o1, ServiceTermination o2) {

      return checkTransitiveDependecy(o1.getServiceName(), o2.getServiceName(), new HashSet<>());
    }

    private int checkTransitiveDependecy(String o1, String o2, Set<String> loopList) {
      if (loopList.contains(o1)) {
        throw new RuntimeException("Loop detected " + loopList);
      }
      Set<String> directDependencies = dependencies.get(o1);
      if (directDependencies.isEmpty()) {
        return -1;
      } else {
        if (directDependencies.contains(o2)) {
          return 1;
        } else {
          loopList.add(o1);
          for (String dependency : directDependencies) {
            if (checkTransitiveDependecy(dependency, o2, loopList) > 0) {
              return 1;
            }
          }
          return -1;
        }
      }
    }
  }

  /**
   * Terminates system services.
   *
   * @throws InterruptedException when termination is interrupted
   */
  void terminateAll() throws InterruptedException {
    for (ServiceTermination termination : terminations) {
      LOG.info("Shutting down '{}' service", termination.getServiceName());
      doTerminate(termination);
    }
  }

  /**
   * Suspends system services.
   *
   * @throws InterruptedException when suspending is interrupted
   */
  void suspendAll() throws InterruptedException {
    for (ServiceTermination termination : terminations) {
      LOG.info("Suspending down '{}' service", termination.getServiceName());
      eventService.publish(new SuspendingSystemServiceEvent(termination.getServiceName()));
      try {
        termination.suspend();
      } catch (UnsupportedOperationException e) {
        LOG.info(
            "Suspending down '{}' service ins't supported, terminating it",
            termination.getServiceName());
        doTerminate(termination);
      } catch (InterruptedException x) {
        LOG.error(
            "Interrupted while waiting for '{}' service to suspend", termination.getServiceName());
        throw x;
      }
      LOG.info("Service '{}' is suspended", termination.getServiceName());
      eventService.publish(new SystemServiceSuspendedEvent(termination.getServiceName()));
    }
  }

  @VisibleForTesting
  void doTerminate(ServiceTermination termination) throws InterruptedException {
    eventService.publish(new StoppingSystemServiceEvent(termination.getServiceName()));
    try {
      termination.terminate();
    } catch (InterruptedException x) {
      LOG.error(
          "Interrupted while waiting for '{}' service to shutdown", termination.getServiceName());
      throw x;
    }
    LOG.info("Service '{}' is shut down", termination.getServiceName());
    eventService.publish(new SystemServiceStoppedEvent(termination.getServiceName()));
  }

  private LinkedHashSet<ServiceTermination> orderedTerminations(
      Set<ServiceTermination> terminations) {

    HashMap<String, ServiceTermination> unSorted =
        terminations
            .stream()
            .collect(
                HashMap<String, ServiceTermination>::new,
                (m, t) -> m.put(t.getServiceName(), t),
                (m, u) -> {});
    LinkedHashMap<String, ServiceTermination> sorted = new LinkedHashMap<>();

    for (ServiceTermination termination : terminations) {
      doSort(termination, sorted, unSorted, new HashSet<>());
    }
    return new LinkedHashSet<>(sorted.values());
  }

  private void doSort(
      ServiceTermination termination,
      LinkedHashMap<String, ServiceTermination> sorted,
      HashMap<String, ServiceTermination> unSorted,
      Set<String> pending) {
    if (sorted.keySet().contains(termination.getServiceName())) {
      return;
    }
    pending.add(termination.getServiceName());

    Set<String> dependencies = termination.getDependencies();
    for (String dependency : dependencies) {
      if (pending.contains(dependency)) {
        throw new RuntimeException(
            format(
                "Circular dependency found between terminations '%s' and '%s'",
                termination.getServiceName(), dependency));
      }

      doSort(unSorted.get(dependency), sorted, unSorted, pending);
    }
    sorted.put(termination.getServiceName(), termination);
    pending.remove(termination.getServiceName());
  }
}
