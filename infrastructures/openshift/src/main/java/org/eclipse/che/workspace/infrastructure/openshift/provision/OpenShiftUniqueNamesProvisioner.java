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
package org.eclipse.che.workspace.infrastructure.openshift.provision;

import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesObjectUtil.putLabel;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.openshift.api.model.Route;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.Constants;
import org.eclipse.che.workspace.infrastructure.kubernetes.Names;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.UniqueNamesProvisioner;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;

/** @author Sergii Leshchenko */
public class OpenShiftUniqueNamesProvisioner extends UniqueNamesProvisioner<OpenShiftEnvironment> {

  @Override
  public void provision(OpenShiftEnvironment osEnv, RuntimeIdentity identity)
      throws InfrastructureException {
    super.provision(osEnv, identity);

    final Set<Route> routes = new HashSet<>(osEnv.getRoutes().values());
    osEnv.getRoutes().clear();
    for (Route route : routes) {
      final ObjectMeta ingressMeta = route.getMetadata();
      putLabel(route, Constants.CHE_ORIGINAL_NAME_LABEL, ingressMeta.getName());
      final String routeName = Names.generateName("route");
      ingressMeta.setName(routeName);
      osEnv.getRoutes().put(routeName, route);
    }
  }
}
