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
package org.eclipse.che.workspace.infrastructure.kubernetes.provision;

import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesObjectUtil.putLabel;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.annotation.Traced;
import org.eclipse.che.commons.tracing.TracingTags;
import org.eclipse.che.workspace.infrastructure.kubernetes.Constants;
import org.eclipse.che.workspace.infrastructure.kubernetes.Names;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;

/**
 * Makes names of Kubernetes pods and ingresses unique whole namespace by {@link Names}.
 *
 * <p>Original names will be stored in {@link Constants#CHE_ORIGINAL_NAME_LABEL} label of renamed
 * object.
 *
 * @author Anton Korneta
 * @see Names#uniquePodName(String, String)
 * @see Names#generateName(String)
 */
@Singleton
public class UniqueNamesProvisioner<T extends KubernetesEnvironment>
    implements ConfigurationProvisioner<T> {

  @Override
  @Traced
  public void provision(T k8sEnv, RuntimeIdentity identity) throws InfrastructureException {
    final String workspaceId = identity.getWorkspaceId();

    TracingTags.WORKSPACE_ID.set(workspaceId);

    final Set<Pod> pods = new HashSet<>(k8sEnv.getPods().values());
    k8sEnv.getPods().clear();
    for (Pod pod : pods) {
      final ObjectMeta podMeta = pod.getMetadata();
      putLabel(pod, Constants.CHE_ORIGINAL_NAME_LABEL, podMeta.getName());
      final String podName = Names.uniquePodName(podMeta.getName(), workspaceId);
      podMeta.setName(podName);
      k8sEnv.getPods().put(podName, pod);
    }
    final Set<Ingress> ingresses = new HashSet<>(k8sEnv.getIngresses().values());
    k8sEnv.getIngresses().clear();
    for (Ingress ingress : ingresses) {
      final ObjectMeta ingressMeta = ingress.getMetadata();
      putLabel(ingress, Constants.CHE_ORIGINAL_NAME_LABEL, ingressMeta.getName());
      final String ingressName = Names.generateName("ingress");
      ingressMeta.setName(ingressName);
      k8sEnv.getIngresses().put(ingressName, ingress);
    }
  }
}
