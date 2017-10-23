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
package org.eclipse.che.workspace.infrastructure.openshift.provision.labels;

import static org.eclipse.che.workspace.infrastructure.openshift.Constants.CHE_POD_NAME_LABEL;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalEnvironment;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
import org.eclipse.che.workspace.infrastructure.openshift.provision.ConfigurationProvisioner;

/** @author Alexander Garagatyi */
public class PodNameLabelProvisioner implements ConfigurationProvisioner {

  @Override
  public void provision(
      InternalEnvironment environment, OpenShiftEnvironment osEnv, RuntimeIdentity identity)
      throws InfrastructureException {

    for (Pod podConfig : osEnv.getPods().values()) {
      final String podName = podConfig.getMetadata().getName();
      getLabels(podConfig).put(CHE_POD_NAME_LABEL, podName);
    }
  }

  private Map<String, String> getLabels(Pod pod) {
    ObjectMeta metadata = pod.getMetadata();
    if (metadata == null) {
      metadata = new ObjectMeta();
      pod.setMetadata(metadata);
    }

    Map<String, String> labels = metadata.getLabels();
    if (labels == null) {
      labels = new HashMap<>();
      metadata.setLabels(labels);
    }
    return labels;
  }
}
