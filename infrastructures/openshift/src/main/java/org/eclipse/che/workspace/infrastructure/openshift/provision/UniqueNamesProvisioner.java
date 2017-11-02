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
import org.eclipse.che.api.workspace.server.spi.InternalEnvironment;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.workspace.infrastructure.openshift.Constants;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;

/**
 * Changes names of OpenShift pods by adding the workspace identifier to the prefix also generates
 * OpenShift routes names with prefix 'route' see {@link NameGenerator#generate(String, int)}.
 *
 * <p>Original names will be stored in {@link Constants#CHE_ORIGINAL_NAME_LABEL} label of renamed
 * object.
 *
 * @author Anton Korneta
 */
@Singleton
public class UniqueNamesProvisioner implements ConfigurationProvisioner {

  public static final String ROUTE_PREFIX = "route";
  public static final int ROUTE_PREFIX_SIZE = 8;
  public static final char SEPARATOR = '.';

  @Override
  public void provision(
      InternalEnvironment environment, OpenShiftEnvironment osEnv, RuntimeIdentity identity)
      throws InfrastructureException {
    final String workspaceId = identity.getWorkspaceId();
    final Set<Pod> pods = new HashSet<>(osEnv.getPods().values());
    osEnv.getPods().clear();
    for (Pod pod : pods) {
      final ObjectMeta podMeta = pod.getMetadata();
      putLabel(pod, Constants.CHE_ORIGINAL_NAME_LABEL, podMeta.getName());
      final String podName = workspaceId + SEPARATOR + podMeta.getName();
      podMeta.setName(podName);
      osEnv.getPods().put(podName, pod);
    }
    final Set<Route> routes = new HashSet<>(osEnv.getRoutes().values());
    osEnv.getRoutes().clear();
    for (Route route : routes) {
      final ObjectMeta routeMeta = route.getMetadata();
      putLabel(route, Constants.CHE_ORIGINAL_NAME_LABEL, routeMeta.getName());
      final String routeName = NameGenerator.generate(ROUTE_PREFIX, ROUTE_PREFIX_SIZE);
      routeMeta.setName(routeName);
      osEnv.getRoutes().put(routeName, route);
    }
  }
}
