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
package org.eclipse.che.workspace.infrastructure.kubernetes.namespace;

import io.fabric8.kubernetes.api.model.rbac.KubernetesPolicyRuleBuilder;
import io.fabric8.kubernetes.api.model.rbac.KubernetesRole;
import io.fabric8.kubernetes.api.model.rbac.KubernetesRoleBinding;
import io.fabric8.kubernetes.api.model.rbac.KubernetesRoleBindingBuilder;
import io.fabric8.kubernetes.api.model.rbac.KubernetesRoleBuilder;
import io.fabric8.kubernetes.api.model.rbac.KubernetesSubjectBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesClientFactory;

/**
 * Holds logic for preparing workspace service account.
 *
 * <p>It checks that required service account, roles and role bindings exist and creates if needed.
 *
 * @author Sergii Leshchenko
 */
public class KubernetesWorkspaceServiceAccount {

  private final String namespace;
  private final String serviceAccountName;
  private final KubernetesClientFactory clientFactory;
  private final String workspaceId;

  public KubernetesWorkspaceServiceAccount(
      String workspaceId,
      String namespace,
      String serviceAccountName,
      KubernetesClientFactory clientFactory) {
    this.workspaceId = workspaceId;
    this.namespace = namespace;
    this.serviceAccountName = serviceAccountName;
    this.clientFactory = clientFactory;
  }

  /**
   * Make sure that workspace service account exists and has `view` and `exec` role bindings.
   *
   * <p>Note that `view` role is used from cluster scope and `exec` role is created in the current
   * namespace if does not exist.
   *
   * @throws InfrastructureException when any exception occurred
   */
  void prepare() throws InfrastructureException {
    KubernetesClient k8sClient = clientFactory.create(workspaceId);

    if (k8sClient.serviceAccounts().inNamespace(namespace).withName(serviceAccountName).get()
        == null) {
      createWorkspaceServiceAccount(k8sClient);
    }

    String execRoleName = "exec";
    if (k8sClient.rbac().kubernetesRoles().inNamespace(namespace).withName(execRoleName).get()
        == null) {
      createExecRole(k8sClient, execRoleName);
    }

    k8sClient
        .rbac()
        .kubernetesRoleBindings()
        .inNamespace(namespace)
        .createOrReplace(createExecRoleBinding());
    k8sClient
        .rbac()
        .kubernetesRoleBindings()
        .inNamespace(namespace)
        .createOrReplace(createViewRoleBinding());
  }

  private void createWorkspaceServiceAccount(KubernetesClient k8sClient) {
    k8sClient
        .serviceAccounts()
        .inNamespace(namespace)
        .createOrReplaceWithNew()
        .withAutomountServiceAccountToken(true)
        .withNewMetadata()
        .withName(serviceAccountName)
        .endMetadata()
        .done();
  }

  private void createExecRole(KubernetesClient k8sClient, String name) {
    KubernetesRole execRole =
        new KubernetesRoleBuilder()
            .withNewMetadata()
            .withName(name)
            .endMetadata()
            .withRules(
                new KubernetesPolicyRuleBuilder()
                    .withResources("pods/exec")
                    .withApiGroups("")
                    .withVerbs("create")
                    .build())
            .build();
    k8sClient.rbac().kubernetesRoles().inNamespace(namespace).create(execRole);
  }

  private KubernetesRoleBinding createViewRoleBinding() {
    return new KubernetesRoleBindingBuilder()
        .withNewMetadata()
        .withName(serviceAccountName + "-view")
        .withNamespace(namespace)
        .endMetadata()
        .withNewRoleRef()
        .withKind("ClusterRole")
        .withName("view")
        .endRoleRef()
        .withSubjects(
            new KubernetesSubjectBuilder()
                .withKind("ServiceAccount")
                .withName(serviceAccountName)
                .withNamespace(namespace)
                .build())
        .build();
  }

  private KubernetesRoleBinding createExecRoleBinding() {
    return new KubernetesRoleBindingBuilder()
        .withNewMetadata()
        .withName(serviceAccountName + "-exec")
        .withNamespace(namespace)
        .endMetadata()
        .withNewRoleRef()
        .withKind("Role")
        .withName("exec")
        .endRoleRef()
        .withSubjects(
            new KubernetesSubjectBuilder()
                .withKind("ServiceAccount")
                .withName(serviceAccountName)
                .withNamespace(namespace)
                .build())
        .build();
  }
}
