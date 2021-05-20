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
package org.eclipse.che.workspace.infrastructure.kubernetes.namespace;

import io.fabric8.kubernetes.api.model.rbac.PolicyRuleBuilder;
import io.fabric8.kubernetes.api.model.rbac.Role;
import io.fabric8.kubernetes.api.model.rbac.RoleBinding;
import io.fabric8.kubernetes.api.model.rbac.RoleBindingBuilder;
import io.fabric8.kubernetes.api.model.rbac.RoleBuilder;
import io.fabric8.kubernetes.api.model.rbac.SubjectBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import java.util.List;
import java.util.Set;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesClientFactory;

/**
 * Holds logic for preparing workspace service account.
 *
 * <p>It checks that required service account, roles and role bindings exist and creates if needed.
 *
 * @author Sergii Leshchenko
 */
public class KubernetesWorkspaceServiceAccount
    extends AbstractWorkspaceServiceAccount<KubernetesClient, Role, RoleBinding> {

  public KubernetesWorkspaceServiceAccount(
      String workspaceId,
      String namespace,
      String serviceAccountName,
      Set<String> clusterRoleNames,
      KubernetesClientFactory clientFactory) {
    super(
        workspaceId,
        namespace,
        serviceAccountName,
        clusterRoleNames,
        clientFactory::create,
        c -> c.rbac().roles(),
        c -> c.rbac().roleBindings());
  }

  @Override
  protected Role buildRole(
      String name, List<String> resources, List<String> apiGroups, List<String> verbs) {
    return new RoleBuilder()
        .withNewMetadata()
        .withName(name)
        .endMetadata()
        .withRules(
            new PolicyRuleBuilder()
                .withResources(resources)
                .withApiGroups(apiGroups)
                .withVerbs(verbs)
                .build())
        .build();
  }

  @Override
  protected RoleBinding createRoleBinding(
      String roleName, String bindingName, boolean clusterRole) {
    return new RoleBindingBuilder()
        .withNewMetadata()
        .withName(bindingName)
        .withNamespace(namespace)
        .endMetadata()
        .withNewRoleRef()
        .withKind(clusterRole ? "ClusterRole" : "Role")
        .withName(roleName)
        .endRoleRef()
        .withSubjects(
            new SubjectBuilder()
                .withKind("ServiceAccount")
                .withName(serviceAccountName)
                .withNamespace(namespace)
                .build())
        .build();
  }
}
