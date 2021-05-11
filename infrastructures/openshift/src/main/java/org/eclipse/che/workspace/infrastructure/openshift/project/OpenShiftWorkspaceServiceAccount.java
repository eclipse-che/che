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
package org.eclipse.che.workspace.infrastructure.openshift.project;

import io.fabric8.kubernetes.api.model.ObjectReferenceBuilder;
import io.fabric8.openshift.api.model.PolicyRuleBuilder;
import io.fabric8.openshift.api.model.Role;
import io.fabric8.openshift.api.model.RoleBinding;
import io.fabric8.openshift.api.model.RoleBindingBuilder;
import io.fabric8.openshift.api.model.RoleBindingFluent;
import io.fabric8.openshift.api.model.RoleBuilder;
import io.fabric8.openshift.client.OpenShiftClient;
import java.util.List;
import java.util.Set;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.AbstractWorkspaceServiceAccount;
import org.eclipse.che.workspace.infrastructure.openshift.OpenShiftClientFactory;

/**
 * Holds logic for preparing workspace service account.
 *
 * <p>It checks that required service account, roles and role bindings exist and creates if needed.
 *
 * @author Sergii Leshchenko
 * @see
 *     org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesWorkspaceServiceAccount
 */
class OpenShiftWorkspaceServiceAccount
    extends AbstractWorkspaceServiceAccount<OpenShiftClient, Role, RoleBinding> {

  OpenShiftWorkspaceServiceAccount(
      String workspaceId,
      String projectName,
      String serviceAccountName,
      Set<String> clusterRoleNames,
      OpenShiftClientFactory clientFactory) {

    super(
        workspaceId,
        projectName,
        serviceAccountName,
        clusterRoleNames,
        clientFactory::createOC,
        OpenShiftClient::roles,
        OpenShiftClient::roleBindings);
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
    RoleBindingFluent.RoleRefNested<RoleBindingBuilder> bld =
        new RoleBindingBuilder()
            .withNewMetadata()
            .withName(bindingName)
            .withNamespace(namespace)
            .endMetadata()
            .withNewRoleRef()
            .withName(roleName);
    if (!clusterRole) {
      bld.withNamespace(namespace);
    }

    return bld.endRoleRef()
        .withSubjects(
            new ObjectReferenceBuilder()
                .withKind("ServiceAccount")
                .withName(serviceAccountName)
                .build())
        .build();
  }
}
