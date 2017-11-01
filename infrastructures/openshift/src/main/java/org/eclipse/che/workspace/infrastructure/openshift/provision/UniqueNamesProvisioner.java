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
package org.eclipse.che.workspace.infrastructure.openshift.provision;

import static org.eclipse.che.workspace.infrastructure.openshift.project.OpenShiftObjectUtil.putLabel;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.openshift.api.model.Route;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.openshift.Constants;
import org.eclipse.che.workspace.infrastructure.openshift.Names;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftInternalEnvironment;

/**
 * Makes names of OpenShift pods and routes unique whole namespace by {@link Names}.
 *
 * <p>Original names will be stored in {@link Constants#CHE_ORIGINAL_NAME_LABEL} label of renamed
 * object.
 *
 * @author Anton Korneta
 * @see Names#uniquePodName(String, String)
 * @see Names#uniqueRouteName()
 */
@Singleton
public class UniqueNamesProvisioner implements ConfigurationProvisioner {

  @Override
  public void provision(OpenShiftInternalEnvironment osEnv, RuntimeIdentity identity)
      throws InfrastructureException {
    final String workspaceId = identity.getWorkspaceId();
    final Set<Pod> pods = new HashSet<>(osEnv.getPods().values());
    osEnv.getPods().clear();
    for (Pod pod : pods) {
      final ObjectMeta podMeta = pod.getMetadata();
      putLabel(pod, Constants.CHE_ORIGINAL_NAME_LABEL, podMeta.getName());
      final String podName = Names.uniquePodName(podMeta.getName(), workspaceId);
      podMeta.setName(podName);
      osEnv.getPods().put(podName, pod);
    }
    final Set<Route> routes = new HashSet<>(osEnv.getRoutes().values());
    osEnv.getRoutes().clear();
    for (Route route : routes) {
      final ObjectMeta routeMeta = route.getMetadata();
      putLabel(route, Constants.CHE_ORIGINAL_NAME_LABEL, routeMeta.getName());
      final String routeName = Names.uniqueRouteName();
      routeMeta.setName(routeName);
      osEnv.getRoutes().put(routeName, route);
    }
  }
}
