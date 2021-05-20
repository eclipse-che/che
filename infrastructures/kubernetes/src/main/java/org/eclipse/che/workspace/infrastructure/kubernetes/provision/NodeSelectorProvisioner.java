/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.provision;

import static java.util.Collections.emptyMap;

import com.google.common.base.Splitter;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;

/** Provisions node selector into workspace pod spec. */
public class NodeSelectorProvisioner implements ConfigurationProvisioner {

  private final Map<String, String> nodeSelectorAttributes;

  @Inject
  public NodeSelectorProvisioner(
      @Nullable @Named("che.workspace.pod.node_selector") String nodeSelectorProperty) {
    this.nodeSelectorAttributes =
        nodeSelectorProperty != null
            ? Splitter.on(",").withKeyValueSeparator("=").split(nodeSelectorProperty)
            : emptyMap();
  }

  @Override
  public void provision(KubernetesEnvironment k8sEnv, RuntimeIdentity identity)
      throws InfrastructureException {
    if (!nodeSelectorAttributes.isEmpty()) {
      k8sEnv
          .getPodsData()
          .values()
          .forEach(d -> d.getSpec().setNodeSelector(nodeSelectorAttributes));
    }
  }
}
