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
      final ObjectMeta routeMeta = route.getMetadata();
      putLabel(route, Constants.CHE_ORIGINAL_NAME_LABEL, routeMeta.getName());
      final String routeName = Names.generateName("route");
      routeMeta.setName(routeName);
      osEnv.getRoutes().put(routeName, route);
    }
  }
}
