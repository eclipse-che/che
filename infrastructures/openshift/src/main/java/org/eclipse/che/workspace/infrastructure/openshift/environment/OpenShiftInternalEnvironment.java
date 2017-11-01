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
package org.eclipse.che.workspace.infrastructure.openshift.environment;

import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.openshift.api.model.Route;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.Warning;
import org.eclipse.che.api.workspace.server.spi.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.spi.InternalRecipe;

/** @author Sergii Leshchenko */
public class OpenShiftInternalEnvironment extends InternalEnvironment {

  private final Map<String, Pod> pods;
  private final Map<String, Service> services;
  private final Map<String, Route> routes;
  private final Map<String, PersistentVolumeClaim> persistentVolumeClaims;

  public OpenShiftInternalEnvironment(
      Map<String, InternalMachineConfig> machines,
      InternalRecipe recipe,
      List<Warning> warnings,
      Map<String, Pod> pods,
      Map<String, Service> services,
      Map<String, PersistentVolumeClaim> pvcs,
      Map<String, Route> routes) {
    super(machines, recipe, warnings);
    this.pods = pods;
    this.services = services;
    this.persistentVolumeClaims = pvcs;
    this.routes = routes;
  }

  /** Returns pods that should be created when environment starts. */
  public Map<String, Pod> getPods() {
    return pods;
  }

  /** Returns services that should be created when environment starts. */
  public Map<String, Service> getServices() {
    return services;
  }

  /** Returns services that should be created when environment starts. */
  public Map<String, Route> getRoutes() {
    return routes;
  }

  /** Returns PVCs that should be created when environment starts. */
  public Map<String, PersistentVolumeClaim> getPersistentVolumeClaims() {
    return persistentVolumeClaims;
  }
}
